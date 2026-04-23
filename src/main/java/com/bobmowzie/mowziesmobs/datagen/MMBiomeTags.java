package com.bobmowzie.mowziesmobs.datagen;

import com.bobmowzie.mowziesmobs.MMCommon;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MMBiomeTags extends BiomeTagsProvider {
    public static final TagKey<Biome> HAS_MOWZIE_STRUCTURE = key("has_structure/has_mowzie_structure");
    public static final TagKey<Biome> IS_MAGICAL = key("is_magical");

    public MMBiomeTags(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, MMCommon.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        // Any biome you want Mowzie's Mobs structures to spawn in must appear here
        // By default, this includes all overworld biomes and tags
        // Modded biomes may not by included by default
        // During placement, biomes are restricted by the dictionary tags from the Mowzie's Mobs config
        tag(HAS_MOWZIE_STRUCTURE).addTag(Tags.Biomes.IS_OVERWORLD);

        tag(IS_MAGICAL).add(Biomes.DARK_FOREST);
    }

    private static TagKey<Biome> key(String path) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, path));
    }
}
