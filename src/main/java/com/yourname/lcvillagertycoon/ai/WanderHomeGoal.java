package com.yourname.lcvillagertycoon.ai;

import com.yourname.lcvillagertycoon.config.TycoonConfig;
import com.yourname.lcvillagertycoon.entity.ShopperVillagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * WanderHomeGoal — the goal that drives the NPC back to its spawn
 * position when it has finished shopping or run out of money.
 */
public class WanderHomeGoal extends Goal {

    private final ShopperVillagerEntity shopper;
    private BlockPos targetPos;

    public WanderHomeGoal(ShopperVillagerEntity shopper) {
        this.shopper = shopper;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (shopper.level().isClientSide) return false;
        
        BlockPos home = shopper.getHomePos();
        if (home == null) return false;

        boolean isBroke = shopper.getCoinBudget() <= 0;
        int maxTrades = TycoonConfig.SERVER.maxTradesPerVisit.get();
        boolean hasFinishedShopping = shopper.getTradesDoneToday() >= maxTrades;
        boolean isFarFromHome = shopper.distanceToSqr(home.getX(), home.getY(), home.getZ()) > 64.0; // > 8 blocks

        if ((isBroke || hasFinishedShopping || isFarFromHome) && ShopperGoalSharedState.getTargetTrader(shopper) == null) {
            targetPos = home;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.shopper.getNavigation().isDone() 
            && targetPos != null 
            && shopper.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) > 4.0;
    }

    @Override
    public void start() {
        this.shopper.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 0.6D);
    }

    @Override
    public void stop() {
        this.targetPos = null;
        this.shopper.getNavigation().stop();
    }
}
