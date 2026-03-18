package com.yourname.lcvillagertycoon.entity;

import com.yourname.lcvillagertycoon.ai.ExecuteTradeGoal;
import com.yourname.lcvillagertycoon.ai.FindNearbyTraderGoal;
import com.yourname.lcvillagertycoon.ai.WanderHomeGoal;
import com.yourname.lcvillagertycoon.config.TycoonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * ShopperVillagerEntity — a custom villager NPC that wanders the world
 * and purchases items from LightmansCurrency player-owned traders.
 *
 * Key NBT data saved per entity:
 *   - CoinBudget       (int)  — current copper-coin-equivalent wallet
 *   - OriginalBudget   (int)  — budget at spawn, used for daily refresh
 *   - HomeX/Y/Z        (int)  — spawn position for WanderHomeGoal
 *   - IdleTicks        (int)  — ticks without finding a trader
 *   - TradesDoneToday  (int)  — trades executed this in-game day
 */
public class ShopperVillagerEntity extends AbstractVillager {

    // ── Synced data (visible server + client) ──────────────────────────────
    private static final EntityDataAccessor<Integer> DATA_COIN_BUDGET =
            SynchedEntityData.defineId(ShopperVillagerEntity.class, EntityDataSerializers.INT);

    // ── Server-only state ──────────────────────────────────────────────────
    private int originalBudget;
    private BlockPos homePos;
    private int idleTicks = 0;
    private int tradesDoneToday = 0;
    private int dayRefreshTimer = 0;
    /** When >= 0, counts down each tick. At 0, the NPC despawns. -1 = not active. */
    private int postPurchaseTimer = -1;
    private static final int DAY_TICKS = 24000;

    public ShopperVillagerEntity(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
        this.setNoAi(false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data sync
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_COIN_BUDGET, 100);
    }

    /** Returns the NPC's current copper-coin-equivalent wallet. */
    public int getCoinBudget() {
        return entityData.get(DATA_COIN_BUDGET);
    }

    public void setCoinBudget(int amount) {
        int newAmount = Math.max(0, amount);
        entityData.set(DATA_COIN_BUDGET, newAmount);
        this.setCustomName(net.minecraft.network.chat.Component.literal("§e🪙 " + newAmount));
        this.setCustomNameVisible(false); // Only visible when hovered over crosshair
    }

    public void deductCoins(int amount) {
        setCoinBudget(getCoinBudget() - amount);
    }

    public boolean canAfford(int copperCost) {
        return getCoinBudget() >= copperCost;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Spawn initialisation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called immediately after spawning to randomise the wallet within config bounds.
     * Also records home position.
     */
    public void initShopperBudget(BlockPos spawnPos) {
        TycoonConfig.Server cfg = TycoonConfig.SERVER;
        initShopperBudget(spawnPos, cfg.reputationStar0BudgetMin.get(), cfg.reputationStar0BudgetMax.get());
    }

    /** Overload used by ShopperManager which supplies a reputation-scaled min/max. */
    public void initShopperBudget(BlockPos spawnPos, int maxBudget) {
        TycoonConfig.Server cfg = TycoonConfig.SERVER;
        initShopperBudget(spawnPos, cfg.reputationStar0BudgetMin.get(), maxBudget);
    }

    /** Primary initialiser — randomises wallet between [min, max] and sets home position. */
    public void initShopperBudget(BlockPos spawnPos, int minBudget, int maxBudget) {
        if (minBudget > maxBudget) minBudget = maxBudget;
        int budget = minBudget + this.random.nextInt(Math.max(1, maxBudget - minBudget + 1));
        this.originalBudget = budget;
        this.setCoinBudget(budget);
        this.homePos = spawnPos;
    }

    @Nullable
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(@Nonnull net.minecraft.world.level.ServerLevelAccessor level, @Nonnull net.minecraft.world.DifficultyInstance difficulty, @Nonnull net.minecraft.world.entity.MobSpawnType reason, @Nullable net.minecraft.world.entity.SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        net.minecraft.world.entity.SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        
        // Handle vanilla spawning (like /summon, spawn egg, natural gen) by
        // guaranteeing they receive a budget if they don't already have one.
        if (getCoinBudget() <= 0 && this.originalBudget <= 0) {
            initShopperBudget(this.blockPosition());
        }
        
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tick
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            tickServerSide();
        }
    }

    private void tickServerSide() {
        // ── Daily budget refresh ──────────────────────────────────────────
        TycoonConfig.Server cfg = TycoonConfig.SERVER;
        if (cfg.refreshBudgetDaily.get()) {
            dayRefreshTimer++;
            if (dayRefreshTimer >= DAY_TICKS) {
                dayRefreshTimer = 0;
                tradesDoneToday = 0;
                int refresh = cfg.dailyBudgetRefreshAmount.get();
                if (refresh < 0) {
                    // -1 → fully restore
                    setCoinBudget(originalBudget);
                } else {
                    setCoinBudget(getCoinBudget() + refresh);
                }
            }
        }

        // ── Post-purchase despawn countdown ─────────────────────────────────
        if (postPurchaseTimer >= 0) {
            if (postPurchaseTimer == 0) {
                this.discard();
                return;
            }
            postPurchaseTimer--;
        }

        // ── Idle despawn ──────────────────────────────────────────────────
        int despawnLimit = cfg.idleDespawnTicks.get();
        if (despawnLimit >= 0) {
            if (isIdle()) {
                idleTicks++;
                if (idleTicks >= despawnLimit) {
                    this.discard();
                }
            } else {
                idleTicks = 0;
            }
        }
    }

    /**
     * Starts the post-purchase despawn countdown.
     * Called by ExecuteTradeGoal after a successful trade.
     */
    public void startPostPurchaseDespawn() {
        int delay = TycoonConfig.SERVER.postPurchaseDespawnTicks.get();
        if (delay >= 0) {
            this.postPurchaseTimer = delay;
        }
    }

    /** Returns true when the entity has no active path and is not currently executing a trade. */
    private boolean isIdle() {
        return (this.getNavigation().isDone() || !this.getNavigation().isInProgress())
                && !this.goalSelector.getRunningGoals()
                        .anyMatch(g -> g.getGoal() instanceof ExecuteTradeGoal
                                    || g.getGoal() instanceof FindNearbyTraderGoal);
    }

    public int getTradesDoneToday() { return tradesDoneToday; }
    public void incrementTradesDoneToday() { tradesDoneToday++; }

    @Nullable
    public BlockPos getHomePos() { return homePos; }

    // ─────────────────────────────────────────────────────────────────────────
    // AI Goal registration
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void registerGoals() {
        // Priority 1 — flee panic
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 0.8D));
        // Priority 3 — find trader and go buy things
        this.goalSelector.addGoal(3, new FindNearbyTraderGoal(this));
        this.goalSelector.addGoal(4, new ExecuteTradeGoal(this));
        // Priority 5 — wander back home, then random strolling
        this.goalSelector.addGoal(5, new WanderHomeGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void updateTrades() { /* handled by ExecuteTradeGoal */ }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) { /* no-op */ }

    @Override
    @Nonnull
    public MerchantOffers getOffers() { return new MerchantOffers(); }

    @Override
    public void notifyTrade(MerchantOffer offer) { /* no-op */ }

    @Override
    public void notifyTradeUpdated(net.minecraft.world.item.ItemStack stack) { /* no-op */ }

    @Override
    public boolean isClientSide() { return this.level().isClientSide; }

    @Nullable
    @Override
    public net.minecraft.world.entity.AgeableMob getBreedOffspring(@Nonnull ServerLevel level, net.minecraft.world.entity.AgeableMob other) {
        return null; // Shoppers don't breed
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return AbstractVillager.createMobAttributes()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.5D)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE, 48.0D);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NBT persistence
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("CoinBudget", getCoinBudget());
        tag.putInt("OriginalBudget", originalBudget);
        tag.putInt("IdleTicks", idleTicks);
        tag.putInt("TradesDoneToday", tradesDoneToday);
        tag.putInt("DayRefreshTimer", dayRefreshTimer);
        tag.putInt("PostPurchaseTimer", postPurchaseTimer);
        if (homePos != null) {
            tag.putInt("HomeX", homePos.getX());
            tag.putInt("HomeY", homePos.getY());
            tag.putInt("HomeZ", homePos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setCoinBudget(tag.getInt("CoinBudget"));
        originalBudget = tag.getInt("OriginalBudget");
        idleTicks = tag.getInt("IdleTicks");
        tradesDoneToday = tag.getInt("TradesDoneToday");
        dayRefreshTimer = tag.getInt("DayRefreshTimer");
        postPurchaseTimer = tag.contains("PostPurchaseTimer") ? tag.getInt("PostPurchaseTimer") : -1;
        if (tag.contains("HomeX")) {
            homePos = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
        }
    }
}
