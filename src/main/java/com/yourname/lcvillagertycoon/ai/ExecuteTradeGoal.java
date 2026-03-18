package com.yourname.lcvillagertycoon.ai;

import com.yourname.lcvillagertycoon.config.TycoonConfig;
import com.yourname.lcvillagertycoon.entity.ShopperVillagerEntity;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraftforge.common.util.FakePlayerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ExecuteTradeGoal extends Goal {

    private final ShopperVillagerEntity shopper;
    private ItemTraderData targetTrader;
    private int browseWaitTicks;
    private boolean completed;

    public ExecuteTradeGoal(ShopperVillagerEntity shopper) {
        this.shopper = shopper;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (shopper.level().isClientSide) return false;
        
        targetTrader = ShopperGoalSharedState.getTargetTrader(shopper);
        if (targetTrader == null) return false;

        BlockPos targetPos = targetTrader.getPos();
        if (targetPos == null) {
            ShopperGoalSharedState.setTargetTrader(shopper, null);
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !completed && targetTrader != null && targetTrader.getPos() != null;
    }

    @Override
    public void start() {
        completed = false;
        browseWaitTicks = TycoonConfig.SERVER.browsingPauseTicks.get();
        BlockPos p = targetTrader.getPos();
        this.shopper.getNavigation().moveTo(p.getX(), p.getY(), p.getZ(), 0.6D);
    }

    @Override
    public void tick() {
        if (targetTrader == null || targetTrader.getPos() == null) {
            completed = true;
            return;
        }
        
        BlockPos p = targetTrader.getPos();
        this.shopper.getLookControl().setLookAt(p.getX(), p.getY(), p.getZ());
        
        if (this.shopper.distanceToSqr(p.getX(), p.getY(), p.getZ()) <= 4.0D) {
            this.shopper.getNavigation().stop();
            if (browseWaitTicks > 0) {
                browseWaitTicks--;
            } else {
                executeRandomAffordableTrade();
                completed = true;
            }
        }
    }

    private void executeRandomAffordableTrade() {
        int budget = shopper.getCoinBudget();
        if (budget <= 0) return;

        List<Integer> validTrades = new ArrayList<>();
        boolean checkStock = TycoonConfig.SERVER.requireStockedTrader.get();
        var fakePlayer = FakePlayerFactory.getMinecraft((ServerLevel) shopper.level());
        TradeContext stockContext = checkStock ? TradeContext.create(targetTrader, fakePlayer).build() : null;

        for (int i = 0; i < targetTrader.getTradeCount(); i++) {
            var trade = targetTrader.getTrade(i);
            if (trade == null) continue;
            
            if (checkStock && trade.outOfStock(stockContext)) continue;
            
            long cost = trade.getCost().getCoreValue();
            if (cost > 0 && cost <= budget) {
                validTrades.add(i);
            }
        }

        if (validTrades.isEmpty()) return;

        int tradeIndex = validTrades.get(this.shopper.getRandom().nextInt(validTrades.size()));

        NPCMoneyHolder moneyHolder = new NPCMoneyHolder(shopper);

        TradeContext context = TradeContext.create(targetTrader, fakePlayer)
                .withMoneyHolder(moneyHolder)
                .build();

        TradeResult result = targetTrader.TryExecuteTrade(context, tradeIndex);
        
        if (result.isSuccess()) {
            shopper.incrementTradesDoneToday();
            // Start the short post-purchase despawn countdown
            shopper.startPostPurchaseDespawn();
        }

        ShopperGoalSharedState.setTargetTrader(shopper, null);
    }

    @Override
    public void stop() {
        targetTrader = null;
        browseWaitTicks = 0;
        if (!completed) ShopperGoalSharedState.setTargetTrader(shopper, null);
    }

    /**
     * Virtual wallet wrapper bridging the NPC's coin balance to LightmansCurrency
     */
    private static class NPCMoneyHolder implements IMoneyHolder {
        private final ShopperVillagerEntity npc;

        public NPCMoneyHolder(ShopperVillagerEntity npc) {
            this.npc = npc;
        }

        @Nonnull
        @Override
        public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulate) {
            long inserted = insertAmount.getCoreValue();
            if (!simulate) {
                npc.setCoinBudget((int)(npc.getCoinBudget() + inserted));
            }
            return CoinValue.fromNumber("lightmanscurrency:main", 0);
        }

        @Nonnull
        @Override
        public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulate) {
            long required = extractAmount.getCoreValue();
            long available = npc.getCoinBudget();
            long extracted = Math.min(required, available);
            
            if (!simulate) {
                npc.deductCoins((int)extracted);
            }
            return CoinValue.fromNumber("lightmanscurrency:main", extracted);
        }

        @Override
        public boolean isMoneyTypeValid(MoneyValue value) {
            return value instanceof CoinValue;
        }

        @Nonnull
        @Override
        public MoneyView getStoredMoney() {
            return MoneyView.singleton(CoinValue.fromNumber("lightmanscurrency:main", npc.getCoinBudget()));
        }

        @Override
        public Component getTooltipTitle() {
            return Component.literal("NPC Wallet");
        }
    }
}
