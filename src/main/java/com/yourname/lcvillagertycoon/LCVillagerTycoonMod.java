package com.yourname.lcvillagertycoon;

import com.yourname.lcvillagertycoon.client.TycoonHudOverlay;
import com.yourname.lcvillagertycoon.config.TycoonConfig;
import com.yourname.lcvillagertycoon.events.TradeHookHandler;
import com.yourname.lcvillagertycoon.events.WorldEventHandler;
import com.yourname.lcvillagertycoon.network.TycoonNetwork;
import com.yourname.lcvillagertycoon.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LCVillagerTycoonMod.MOD_ID)
public class LCVillagerTycoonMod {

    public static final String MOD_ID = "lcvillagertycoon";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public LCVillagerTycoonMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register mod-level events
        ModEntities.ENTITY_TYPES.register(modBus);

        modBus.addListener(this::commonSetup);

        // Register server/world events on the main Forge event bus
        MinecraftForge.EVENT_BUS.register(new WorldEventHandler());
        MinecraftForge.EVENT_BUS.register(new TradeHookHandler());

        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TycoonConfig.SERVER_SPEC, "lcvillagertycoon-common.toml");

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(this::clientSetup);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(TycoonNetwork::register);
        LOGGER.info("LCVillagerTycoon common setup complete.");
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.register(new TycoonHudOverlay());
            LOGGER.info("LCVillagerTycoon client setup complete.");
        });
    }
}
