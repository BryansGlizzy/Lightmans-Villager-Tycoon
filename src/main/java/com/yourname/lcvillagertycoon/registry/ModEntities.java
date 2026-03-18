package com.yourname.lcvillagertycoon.registry;

import com.yourname.lcvillagertycoon.LCVillagerTycoonMod;
import com.yourname.lcvillagertycoon.entity.ShopperVillagerEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LCVillagerTycoonMod.MOD_ID);

    public static final RegistryObject<EntityType<ShopperVillagerEntity>> SHOPPER_VILLAGER =
            ENTITY_TYPES.register("shopper_villager", () ->
                    EntityType.Builder.<ShopperVillagerEntity>of(ShopperVillagerEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.95f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(new ResourceLocation(LCVillagerTycoonMod.MOD_ID, "shopper_villager").toString())
            );

    private ModEntities() {}
}
