package com.yourname.lcvillagertycoon.network;

import com.yourname.lcvillagertycoon.LCVillagerTycoonMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class TycoonNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LCVillagerTycoonMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        CHANNEL.registerMessage(id++, TycoonStatsPacket.class,
                TycoonStatsPacket::encode,
                TycoonStatsPacket::decode,
                TycoonStatsPacket::handle);

        CHANNEL.registerMessage(id++, TycoonReputationPacket.class,
                TycoonReputationPacket::encode,
                TycoonReputationPacket::decode,
                TycoonReputationPacket::handle);
    }
}
