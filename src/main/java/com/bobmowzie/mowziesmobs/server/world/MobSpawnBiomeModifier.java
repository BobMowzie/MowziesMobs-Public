package com.bobmowzie.mowziesmobs.server.world;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.world.feature.structure.StructureTypeHandler;
import com.bobmowzie.mowziesmobs.server.world.spawn.SpawnHandler;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MobSpawnBiomeModifier implements BiomeModifier {
    private static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<? extends BiomeModifier>> SERIALIZER = DeferredHolder.create(NeoForgeRegistries.BIOME_MODIFIER_SERIALIZERS.key(), ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "mowzie_mob_spawns"));

    @Override
    public void modify(Holder<Biome> biome, BiomeModifier.Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD) {
            SpawnHandler.addBiomeSpawns(biome, builder);
            StructureTypeHandler.addBiomeSpawns(biome);
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return SERIALIZER.get();
    }

    public static MapCodec<MobSpawnBiomeModifier> makeCodec() {
        return MapCodec.unit(MobSpawnBiomeModifier::new);
    }
}