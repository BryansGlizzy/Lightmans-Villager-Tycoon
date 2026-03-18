package com.yourname.lcvillagertycoon.network;

import com.yourname.lcvillagertycoon.client.TycoonHudOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TycoonStatsPacket {

    private final int coinsEarned;
    private final String itemName;
    private final int quantity;

    public TycoonStatsPacket(int coinsEarned, String itemName, int quantity) {
        this.coinsEarned = coinsEarned;
        this.itemName = itemName;
        this.quantity = quantity;
    }

    public static void encode(TycoonStatsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.coinsEarned);
        buf.writeUtf(msg.itemName);
        buf.writeInt(msg.quantity);
    }

    public static TycoonStatsPacket decode(FriendlyByteBuf buf) {
        return new TycoonStatsPacket(
                buf.readInt(),
                buf.readUtf(256),
                buf.readInt()
        );
    }

    public static void handle(TycoonStatsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Handle on client thread
            TycoonHudOverlay.addNotification(msg.coinsEarned, msg.itemName, msg.quantity);
        });
        ctx.get().setPacketHandled(true);
    }
}
