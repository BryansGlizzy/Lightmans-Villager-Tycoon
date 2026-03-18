package com.yourname.lcvillagertycoon.ai;

import com.yourname.lcvillagertycoon.config.TycoonConfig;
import com.yourname.lcvillagertycoon.entity.ShopperVillagerEntity;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * FindNearbyTraderGoal — scans block entities within the configured radius
 * for LightmansCurrency ItemTraderData instances.
 *
 * When a suitable trader is found it stores a reference so that
 * ExecuteTradeGoal can immediately pick it up.
 */
public class FindNearbyTraderGoal extends Goal {

    private final ShopperVillagerEntity shopper;
    private int cooldownTicks = 0;

    public FindNearbyTraderGoal(ShopperVillagerEntity shopper) {
        this.shopper = shopper;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // ─── Eligibility ──────────────────────────────────────────────────────────

    @Override
    public boolean canUse() {
        if (shopper.level().isClientSide) return false;
        if (cooldownTicks-- > 0) return false;
        if (shopper.getCoinBudget() <= 0) return false;
        if (ShopperGoalSharedState.getTargetTrader(shopper) != null) return false; // already have a target

        TycoonConfig.Server cfg = TycoonConfig.SERVER;
        int maxToday = cfg.maxTradesPerVisit.get();
        if (shopper.getTradesDoneToday() >= maxToday) return false;

        return findTrader();
    }

    @Override
    public boolean canContinueToUse() { return false; } // one-shot scan

    @Override
    public void start() {
        // Reset cooldown to the configured decision interval
        cooldownTicks = TycoonConfig.SERVER.shopDecisionIntervalTicks.get();
    }

    // ─── Core scan logic ──────────────────────────────────────────────────────

    private boolean findTrader() {
        int radius = TycoonConfig.SERVER.traderSearchRadiusBlocks.get();
        BlockPos center = shopper.blockPosition();
        int rangeSq = radius * radius;

        // Ask LightmansCurrency's TraderAPI for all traders in the level.
        List<TraderData> allTraders = io.github.lightman314.lightmanscurrency.api.traders.TraderAPI.getApi().GetAllTraders(false);
        if (allTraders == null) return false;

        List<TraderData> candidates = allTraders.stream()
                .filter(t -> t instanceof ItemTraderData)
                .filter(t -> t.getPos() != null && t.getPos().distSqr(center) <= rangeSq)
                .filter(t -> hasAffordableTrade((ItemTraderData) t))
                .sorted(Comparator.comparingDouble(t ->
                        t.getPos().distSqr(center)))
                .toList();

        if (candidates.isEmpty()) return false;

        // Pick the closest one
        TraderData chosen = candidates.get(0);
        ShopperGoalSharedState.setTargetTrader(shopper, (ItemTraderData) chosen);
        return true;
    }

    private boolean hasAffordableTrade(ItemTraderData trader) {
        TycoonConfig.Server cfg = TycoonConfig.SERVER;
        int budget = shopper.getCoinBudget();
        int minStock = cfg.requireStockedTrader.get() ? cfg.minStockRequired.get() : 0;

        for (int i = 0; i < trader.getTradeCount(); i++) {
            var trade = trader.getTrade(i);
            if (trade == null) continue;
            // Check stock if required
            if (minStock > 0) {
                // Fix: provide a valid TradeContext instead of null to avoid NPE in Lightman's Currency
                TradeContext context = TradeContext.create(trader, FakePlayerFactory.getMinecraft((ServerLevel) shopper.level())).build();
                if (trade.outOfStock(context)) continue;
            }
            // Check price vs budget (cost in copper-coin-equivalent)
            // Simple check if NPC can theoretically afford it
            long cost = trade.getCost().getCoreValue();
            if (cost > 0 && cost <= budget) return true;
        }
        return false;
    }
}
