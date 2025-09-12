package com.bobmowzie.mowziesmobs.server.damage;

import com.bobmowzie.mowziesmobs.MowziesMobs;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class DamageTypes {
    public static final ResourceKey<DamageType> HELIOMANCY = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(MowziesMobs.MODID, "heliomancy")
    );

    public static final ResourceKey<DamageType> VENOM = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(MowziesMobs.MODID, "toxicomancy")
    );

    public static final ResourceKey<DamageType> ICE_BREATH = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(MowziesMobs.MODID, "ice_breath")
    );

    public static final ResourceKey<DamageType> GEOMANCY = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(MowziesMobs.MODID, "geomancy")
    );
}
