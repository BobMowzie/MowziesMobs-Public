package com.bobmowzie.mowziesmobs.datagen;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.block.BlockHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MMBlockTags extends BlockTagsProvider {
    public static final TagKey<Block> GEOMANCY_USEABLE = key("geomancy_useable");
    public static final TagKey<Block> CAN_GROTTOL_DIG = key("can_grottol_dig");
    public static final TagKey<Block> GEOMANCY_TUNNELABLE = key("geomancy_tunnelable");

    public MMBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MMCommon.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        addToVanillaTags();
        addToCommonTags();

        // These blocks are considered bricks
        TagKey<Block> bricks = key("bricks");
        tagBricks(bricks);

        // These blocks are considered concrete
        TagKey<Block> concrete = key("concrete");
        tagConcrete(concrete);

        // These blocks are considered concrete
        TagKey<Block> concretePowder = key("concrete_powder");
        tagConcretePowder(concretePowder);

        // These blocks can be used with Geomancy abilities
        // Blocks that copy their properties from these tagged blocks will also be useable
        tag(GEOMANCY_USEABLE).addTag(bricks).addTag(concrete).addTag(concretePowder);
        tagGeomancyUsable();

        // These blocks allow grottols to dig and disappear
        // Blocks that copy their properties from these tagged blocks will also be allowed
        tag(CAN_GROTTOL_DIG).addTag(GEOMANCY_USEABLE);

        // These blocks can be used with Geomancy abilities. Blocks that copy their properties from these tagged blocks will also be useable.
        tag(GEOMANCY_TUNNELABLE)
                .addTag(GEOMANCY_USEABLE)
                .add(Blocks.SNOW)
                .add(Blocks.SNOW_BLOCK)
                .add(Blocks.PACKED_ICE);
    }

    private void addToVanillaTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BlockHandler.GONG.value())
                .add(BlockHandler.GONG_PART.value());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(BlockHandler.RAKED_SAND.value())
                .add(BlockHandler.RED_RAKED_SAND.value());

        tag(BlockTags.AZALEA_GROWS_ON)
                .add(BlockHandler.RAKED_SAND.value())
                .add(BlockHandler.RED_RAKED_SAND.value());

        tag(BlockTags.AZALEA_GROWS_ON)
                .add(BlockHandler.RAKED_SAND.value())
                .add(BlockHandler.RED_RAKED_SAND.value());

        tag(BlockTags.BAMBOO_PLANTABLE_ON)
                .add(BlockHandler.RAKED_SAND.value())
                .add(BlockHandler.RED_RAKED_SAND.value());

        tag(BlockTags.LUSH_GROUND_REPLACEABLE)
                .add(BlockHandler.RAKED_SAND.value())
                .add(BlockHandler.RED_RAKED_SAND.value());

        tag(BlockTags.PLANKS).add(BlockHandler.PAINTED_ACACIA.value());
        tag(BlockTags.JUNGLE_LOGS).add(BlockHandler.CLAWED_LOG.value());
        tag(BlockTags.WOODEN_SLABS).add(BlockHandler.PAINTED_ACACIA_SLAB.value());
    }

    private void addToCommonTags() {
        tag(Tags.Blocks.SANDS_COLORLESS).add(BlockHandler.RAKED_SAND.value());
        tag(Tags.Blocks.SANDS_RED).add(BlockHandler.RED_RAKED_SAND.value());
    }

    private void tagGeomancyUsable() {
        tag(GEOMANCY_USEABLE)
                .addTag(BlockTags.DIRT)
                .addTag(BlockTags.TERRACOTTA)
                .addTag(BlockTags.NYLIUM)
                .addTag(Tags.Blocks.GLAZED_TERRACOTTAS)
                .addTag(Tags.Blocks.COBBLESTONES)
                .addTag(Tags.Blocks.END_STONES)
                .addTag(Tags.Blocks.GRAVELS)
                .addTag(Tags.Blocks.NETHERRACKS)
                .addTag(Tags.Blocks.OBSIDIANS)
                .addTag(Tags.Blocks.ORES)
                .addTag(Tags.Blocks.SANDS)
                .addTag(Tags.Blocks.SANDSTONE_BLOCKS)
                .addTag(Tags.Blocks.SANDSTONE_SLABS)
                .addTag(Tags.Blocks.STONES)
                .add(Blocks.BLACKSTONE)
                .add(Blocks.BASALT)
                .add(Blocks.SMOOTH_BASALT)
                .add(Blocks.SOUL_SAND)
                .add(Blocks.SOUL_SOIL)
                .add(Blocks.SMOOTH_STONE)
                .add(Blocks.QUARTZ_BLOCK)
                .add(Blocks.CHISELED_QUARTZ_BLOCK)
                .add(Blocks.QUARTZ_PILLAR)
                .add(Blocks.SMOOTH_QUARTZ)
                .add(Blocks.PURPUR_BLOCK)
                .add(Blocks.PURPUR_PILLAR)
                .add(Blocks.PRISMARINE)
                .add(Blocks.DARK_PRISMARINE)
                .add(Blocks.SUSPICIOUS_SAND)
                .add(Blocks.SUSPICIOUS_GRAVEL)
                .add(Blocks.MAGMA_BLOCK)
                .add(Blocks.DRIPSTONE_BLOCK)
                .add(Blocks.CLAY)
                .add(Blocks.DEEPSLATE_TILES)
                .add(Blocks.POLISHED_BASALT)
                .add(Blocks.DIRT_PATH)
                .add(Blocks.FARMLAND)
                .add(Blocks.CHISELED_POLISHED_BLACKSTONE)
                .add(Blocks.CRACKED_DEEPSLATE_BRICKS)
                .add(Blocks.STONE_SLAB)
                .add(Blocks.COBBLESTONE_SLAB)
                .add(Blocks.SMOOTH_STONE_SLAB)
                .add(Blocks.STONE_BRICK_SLAB)
                .add(Blocks.DEEPSLATE_TILE_SLAB)
                .add(Blocks.CUT_RED_SANDSTONE_SLAB)
                .add(Blocks.BRICK_SLAB)
                .add(Blocks.PRISMARINE_SLAB)
                .add(Blocks.PRISMARINE_BRICK_SLAB)
                .add(Blocks.DARK_PRISMARINE_SLAB)
                .add(Blocks.MUD_BRICK_SLAB)
                .add(Blocks.SANDSTONE_SLAB)
                .add(Blocks.NETHER_BRICK_SLAB)
                .add(Blocks.QUARTZ_SLAB)
                .add(Blocks.POLISHED_BLACKSTONE_SLAB)
                .add(Blocks.PURPUR_SLAB)
                .add(Blocks.NETHER_BRICK_FENCE)
                .add(Blocks.POLISHED_BLACKSTONE_STAIRS)
                .add(Blocks.POLISHED_BLACKSTONE_WALL);
    }

    private void tagConcretePowder(TagKey<Block> key) {
        tag(key)
                .add(Blocks.WHITE_CONCRETE_POWDER)
                .add(Blocks.LIGHT_GRAY_CONCRETE_POWDER)
                .add(Blocks.GRAY_CONCRETE_POWDER)
                .add(Blocks.BLACK_CONCRETE_POWDER)
                .add(Blocks.BROWN_CONCRETE_POWDER)
                .add(Blocks.RED_CONCRETE_POWDER)
                .add(Blocks.ORANGE_CONCRETE_POWDER)
                .add(Blocks.YELLOW_CONCRETE_POWDER)
                .add(Blocks.LIME_CONCRETE_POWDER)
                .add(Blocks.GREEN_CONCRETE_POWDER)
                .add(Blocks.CYAN_CONCRETE_POWDER)
                .add(Blocks.LIGHT_BLUE_CONCRETE_POWDER)
                .add(Blocks.PURPLE_CONCRETE_POWDER)
                .add(Blocks.MAGENTA_CONCRETE_POWDER)
                .add(Blocks.PINK_CONCRETE_POWDER);
    }

    private void tagConcrete(TagKey<Block> key) {
        tag(key).addTag(Tags.Blocks.CONCRETES);
    }

    private void tagBricks(TagKey<Block> key) {
        tag(key)
                .addTag(BlockTags.STONE_BRICKS)
                .add(Blocks.BRICKS)
                .add(Blocks.DEEPSLATE_BRICKS)
                .add(Blocks.CRACKED_DEEPSLATE_BRICKS)
                .add(Blocks.MUD_BRICKS)
                .add(Blocks.PRISMARINE_BRICKS)
                .add(Blocks.NETHER_BRICKS)
                .add(Blocks.CRACKED_NETHER_BRICKS)
                .add(Blocks.CHISELED_NETHER_BRICKS)
                .add(Blocks.RED_NETHER_BRICKS)
                .add(Blocks.POLISHED_BLACKSTONE_BRICKS)
                .add(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
                .add(Blocks.END_STONE_BRICKS)
                .add(Blocks.QUARTZ_BRICKS)
                .add(Blocks.INFESTED_STONE_BRICKS)
                .add(Blocks.INFESTED_MOSSY_STONE_BRICKS)
                .add(Blocks.INFESTED_CRACKED_STONE_BRICKS)
                .add(Blocks.INFESTED_CHISELED_STONE_BRICKS);
    }

    private static TagKey<Block> key(String path) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, path));
    }
}
