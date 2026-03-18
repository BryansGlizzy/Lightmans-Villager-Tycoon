package com.yourname.lcvillagertycoon.network;

import com.yourname.lcvillagertycoon.client.ReputationPanelState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → Client packet carrying the player's current reputation data.
 * Sent on login and after every NPC purchase recorded by TradeHookHandler.
 */
public class TycoonReputationPacket {

    private final String playerName;
    private final long coinsEarned;

    public TycoonReputationPacket(String playerName, long coinsEarned) {
        this.playerName = playerName;
        this.coinsEarned = coinsEarned;
    }

    public static void encode(TycoonReputationPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.playerName, 64);
        buf.writeLong(msg.coinsEarned);
    }

    public static TycoonReputationPacket decode(FriendlyByteBuf buf) {
        return new TycoonReputationPacket(buf.readUtf(64), buf.readLong());
    }

    public static void handle(TycoonReputationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ReputationPanelState.playerName = msg.playerName;
            ReputationPanelState.coinsEarned = msg.coinsEarned;
        });
        ctx.get().setPacketHandled(true);
    }
}
