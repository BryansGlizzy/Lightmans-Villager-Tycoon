package com.yourname.lcvillagertycoon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.yourname.lcvillagertycoon.LCVillagerTycoonMod;
import com.yourname.lcvillagertycoon.tycoon.ShopperManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LCVillagerTycoonMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TycoonCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("tycoon")
            .requires(source -> source.hasPermission(2)) // Require OP level 2
            .then(Commands.literal("spawn")
                .executes(context -> spawnShopper(context.getSource(), -1))
                .then(Commands.argument("budget", IntegerArgumentType.integer(1))
                    .executes(context -> spawnShopper(context.getSource(), IntegerArgumentType.getInteger(context, "budget")))
                )
            )
        );
    }

    private static int spawnShopper(CommandSourceStack source, int budget) {
        if (source.getEntity() instanceof ServerPlayer player) {
            boolean success = ShopperManager.get().forceSpawn(player, budget);
            if (success) {
                source.sendSuccess(() -> Component.literal("§a[Tycoon]§f Forced a Shopper Villager spawn nearby!"), true);
                return 1;
            }
            source.sendFailure(Component.literal("§c[Tycoon]§f Failed to spawn Shopper Villager."));
            return 0;
        }
        source.sendFailure(Component.literal("§c[Tycoon]§f Command must be run by a player."));
        return 0;
    }

    private TycoonCommands() {}
}
