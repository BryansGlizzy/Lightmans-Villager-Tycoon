package com.yourname.lcvillagertycoon.registry;

import com.yourname.lcvillagertycoon.entity.ShopperVillagerEntity;
import com.yourname.lcvillagertycoon.registry.ModEntities;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModAttributes {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        AttributeSupplier.Builder builder = ShopperVillagerEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
        event.put(ModEntities.SHOPPER_VILLAGER.get(), builder.build());
    }

    private ModAttributes() {}
}
