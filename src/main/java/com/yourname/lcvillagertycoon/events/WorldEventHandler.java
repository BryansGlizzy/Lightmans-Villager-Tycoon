package com.yourname.lcvillagertycoon.events;

import com.yourname.lcvillagertycoon.network.TycoonNetwork;
import com.yourname.lcvillagertycoon.network.TycoonReputationPacket;
import com.yourname.lcvillagertycoon.tycoon.ShopperManager;
import com.yourname.lcvillagertycoon.tycoon.TycoonStats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class WorldEventHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            ShopperManager.get().tick((ServerLevel) event.level);
        }
    }

    /**
     * Sends the player's current reputation data as soon as they join so the
     * panel shows accurate info even if no purchases have happened yet.
     */
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        ServerLevel overworld = serverPlayer.getServer().overworld();
        long earned = TycoonStats.get(overworld).getStats(serverPlayer.getUUID()).coinsEarned;
        TycoonNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> serverPlayer),
                new TycoonReputationPacket(serverPlayer.getName().getString(), earned)
        );
    }
}
