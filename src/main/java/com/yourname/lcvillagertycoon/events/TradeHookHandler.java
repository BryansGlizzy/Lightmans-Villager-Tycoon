package com.yourname.lcvillagertycoon.events;

import com.yourname.lcvillagertycoon.network.TycoonNetwork;
import com.yourname.lcvillagertycoon.network.TycoonReputationPacket;
import com.yourname.lcvillagertycoon.network.TycoonStatsPacket;
import com.yourname.lcvillagertycoon.tycoon.TycoonStats;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class TradeHookHandler {

    @SubscribeEvent
    public void onPostTrade(TradeEvent.PostTradeEvent event) {
        TraderData trader = event.getTrader();
        if (trader == null) return;

        // Only process trades made by our shopper NPC (dispatched via FakePlayer)
        if (!(event.getContext().getPlayer() instanceof FakePlayer)) return;

        long coinsSpent = event.getTrade().getCost(event.getContext()).getCoreValue();
        if (coinsSpent <= 0) return;

        // Resolve trader owner UUID
        UUID owner = null;
        if (trader.getOwner().getValidOwner() != null) {
            var playerRef = trader.getOwner().getValidOwner().asPlayerReference();
            if (playerRef != null) owner = playerRef.id;
        }

        ServerLevel serverLevel = event.getContext().getPlayer().getServer().getLevel(trader.getLevel());
        if (owner == null || serverLevel == null) return;

        // Record purchase in world data
        TycoonStats.get(serverLevel).recordPurchase(owner, coinsSpent);

        // Push packets to owner if they're online
        ServerPlayer ownerPlayer = serverLevel.getServer().getPlayerList().getPlayer(owner);
        if (ownerPlayer != null) {
            // Income HUD toast notification
            TycoonNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> ownerPlayer),
                    new TycoonStatsPacket((int) coinsSpent, "an item", 1)
            );

            // Reputation panel live sync
            long totalEarned = TycoonStats.get(serverLevel).getStats(owner).coinsEarned;
            TycoonNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> ownerPlayer),
                    new TycoonReputationPacket(ownerPlayer.getName().getString(), totalEarned)
            );
        }
    }
}
