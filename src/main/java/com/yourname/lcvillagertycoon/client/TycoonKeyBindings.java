package com.yourname.lcvillagertycoon.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class TycoonKeyBindings {

    public static final KeyMapping OPEN_REPUTATION_PANEL = new KeyMapping(
            "key.lcvillagertycoon.open_reputation_panel",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SEMICOLON,
            "key.categories.lcvillagertycoon"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_REPUTATION_PANEL);
    }
}
