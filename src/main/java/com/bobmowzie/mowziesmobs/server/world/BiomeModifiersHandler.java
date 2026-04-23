package com.bobmowzie.mowziesmobs.server.world;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class BiomeModifiersHandler
{
	public static final DeferredRegister<MapCodec<? extends BiomeModifier>> REG = DeferredRegister.create(NeoForgeRegistries.BIOME_MODIFIER_SERIALIZERS, MMCommon.MODID);
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<? extends BiomeModifier>> MOWZIE_MOB_SPAWNS = REG.register("mowzie_mob_spawns", MobSpawnBiomeModifier::makeCodec);
}