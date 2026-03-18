package com.yourname.lcvillagertycoon.tycoon;

import java.util.Random;

import com.yourname.lcvillagertycoon.config.TycoonConfig;
import com.yourname.lcvillagertycoon.entity.ShopperVillagerEntity;
import com.yourname.lcvillagertycoon.registry.ModEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;

/**
 * Handles periodic spawning of ShopperVillagers around LC traders.
 *
 * Spawning uses a per-tick probability model so that NPCs trickle in
 * throughout the day rather than appearing in a burst.
 *
 * Given N configured spawn attempts per day and a day length of 24,000 ticks,
 * each tick has a (N / 24000) chance of triggering a single spawn attempt
 * for each trader. This produces approximately N spawns per day per trader,
 * spread evenly across the full day/night cycle.
 */
public class ShopperManager {

    private static final ShopperManager INSTANCE = new ShopperManager();
    private final Random random = new Random();
    private static final int DAY_TICKS = 24000;

    public static ShopperManager get() {
        return INSTANCE;
    }

    /**
     * Called every server tick per dimension.
     * Each tick, we roll for each trader to see if a shopper should spawn.
     */
    public void tick(ServerLevel level) {
        // Only run in the Overworld to avoid cross-dimension duplication
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;

        TycoonConfig.Server cfg = TycoonConfig.SERVER;
        int globalCap = cfg.maxShoppersGlobal.get();

        // Count current global shoppers
        int globalCount = 0;
        for (var entity : level.getAllEntities()) {
            if (entity instanceof ShopperVillagerEntity) globalCount++;
        }
        if (globalCount >= globalCap) return;

        int attemptsPerDay = cfg.shopperSpawnAttemptsPerDay.get();
        int chancePercent = cfg.shopperSpawnChancePercent.get();
        int chunkCap = cfg.maxShoppersPerChunk.get();

        // Per-tick probability: each tick has a (attemptsPerDay / DAY_TICKS) chance
        // of being a "spawn tick". We use a random threshold against 1,000,000
        // for fine-grained fractional chances.
        double spawnChancePerTick = (double) attemptsPerDay / DAY_TICKS;
        if (random.nextDouble() >= spawnChancePerTick) return;

        // This tick is a spawn tick — pick a random trader to spawn near
        var allTraders = TraderAPI.getApi().GetAllTraders(false);
        if (allTraders == null || allTraders.isEmpty()) return;

        // Filter to traders in this dimension
        var dimTraders = allTraders.stream()
                .filter(t -> t instanceof ItemTraderData && t.getPos() != null
                        && t.getLevel().equals(level.dimension()))
                .toList();
        if (dimTraders.isEmpty()) return;

        // Pick one random trader
        var trader = dimTraders.get(random.nextInt(dimTraders.size()));

        // Apply per-attempt chance
        if (random.nextInt(100) >= chancePercent) return;

        BlockPos traderPos = trader.getPos();
        BlockPos spawnPos = findSpawnPosition(level, traderPos);
        if (spawnPos == null) return;

        // Check local (chunk) cap
        int localCount = level.getEntitiesOfClass(
                ShopperVillagerEntity.class,
                new net.minecraft.world.phys.AABB(spawnPos).inflate(32.0D)
        ).size();
        if (localCount >= chunkCap) return;

        // Determine budget range via Reputation tier
        int budgetMin = cfg.reputationStar0BudgetMin.get();
        int budgetMax = cfg.reputationStar0BudgetMax.get();
        var ownerRef = trader.getOwner().getValidOwner();
        if (ownerRef != null) {
            var playerRef = ownerRef.asPlayerReference();
            if (playerRef != null) {
                java.util.UUID ownerId = playerRef.id;
                long coinsEarned = TycoonStats.get(level).getStats(ownerId).coinsEarned;
                int repLevel = TycoonStats.getReputationLevel(coinsEarned);
                budgetMin = switch (repLevel) {
                    case 5 -> cfg.reputationStar5BudgetMin.get();
                    case 4 -> cfg.reputationStar4BudgetMin.get();
                    case 3 -> cfg.reputationStar3BudgetMin.get();
                    case 2 -> cfg.reputationStar2BudgetMin.get();
                    case 1 -> cfg.reputationStar1BudgetMin.get();
                    default -> cfg.reputationStar0BudgetMin.get();
                };
                budgetMax = switch (repLevel) {
                    case 5 -> cfg.reputationStar5BudgetMax.get();
                    case 4 -> cfg.reputationStar4BudgetMax.get();
                    case 3 -> cfg.reputationStar3BudgetMax.get();
                    case 2 -> cfg.reputationStar2BudgetMax.get();
                    case 1 -> cfg.reputationStar1BudgetMax.get();
                    default -> cfg.reputationStar0BudgetMax.get();
                };
            }
        }
        if (budgetMin > budgetMax) budgetMin = budgetMax;

        ShopperVillagerEntity npc = ModEntities.SHOPPER_VILLAGER.get().create(level);
        if (npc != null) {
            npc.moveTo(spawnPos, 0.0F, 0.0F);
            npc.initShopperBudget(spawnPos, budgetMin, budgetMax);
            level.addFreshEntity(npc);
        }
    }

    /**
     * Force-spawns a ShopperVillager at the player's current location (or nearby safe spot).
     * Bypasses all caps and chance checks. Allows an optional budget override.
     */
    public boolean forceSpawn(Player player, int overrideBudget) {
        if (!(player.level() instanceof ServerLevel level)) return false;

        BlockPos spawnPos = findSpawnPosition(level, player.blockPosition());
        if (spawnPos == null) spawnPos = player.blockPosition();

        ShopperVillagerEntity npc = ModEntities.SHOPPER_VILLAGER.get().create(level);
        if (npc != null) {
            npc.moveTo(spawnPos, 0.0F, 0.0F);
            if (overrideBudget > 0) {
                npc.setCoinBudget(overrideBudget);
            } else {
                npc.initShopperBudget(spawnPos);
            }
            level.addFreshEntity(npc);
            return true;
        }
        return false;
    }

    private BlockPos findSpawnPosition(ServerLevel level, BlockPos traderPos) {
        int rMin = 10;
        int rMax = 20;
        int dx = random.nextInt(rMax - rMin + 1) + rMin;
        int dz = random.nextInt(rMax - rMin + 1) + rMin;

        if (random.nextBoolean()) dx = -dx;
        if (random.nextBoolean()) dz = -dz;

        BlockPos target = traderPos.offset(dx, 0, dz);

        // 1) Iterate vertically near trader's matching floor (underground/indoors)
        for (int yOffset = -5; yOffset <= 5; yOffset++) {
            BlockPos testPos = target.above(yOffset);
            if (level.getBlockState(testPos.below()).isSolidRender(level, testPos.below())
                && level.isEmptyBlock(testPos)
                && level.isEmptyBlock(testPos.above())) {
                return testPos;
            }
        }

        // 2) Fallback to surface terrain
        target = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target);

        if (target.getY() < level.getMinBuildHeight() || target.getY() >= level.getMaxBuildHeight()) {
            return null;
        }

        if (level.getBlockState(target.below()).isSolidRender(level, target.below()) && level.isEmptyBlock(target) && level.isEmptyBlock(target.above())) {
            return target;
        }

        return null;
    }
}
