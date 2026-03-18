package com.yourname.lcvillagertycoon.ai;

import com.yourname.lcvillagertycoon.entity.ShopperVillagerEntity;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Shared state for shopper goals to avoid direct coupling.
 * Enables FindNearbyTraderGoal to pass a trader target to ExecuteTradeGoal.
 */
public class ShopperGoalSharedState {
    
    // Use WeakHashMap so we don't leak entity memory if they are discarded
    private static final Map<ShopperVillagerEntity, ItemTraderData> targetMap = new WeakHashMap<>();

    @Nullable
    public static ItemTraderData getTargetTrader(ShopperVillagerEntity shopper) {
        return targetMap.get(shopper);
    }

    public static void setTargetTrader(ShopperVillagerEntity shopper, @Nullable ItemTraderData trader) {
        if (trader == null) {
            targetMap.remove(shopper);
        } else {
            targetMap.put(shopper, trader);
        }
    }
}
