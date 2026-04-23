package com.bobmowzie.mowziesmobs.datagen;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.block.BlockHandler;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class MMRecipes extends RecipeProvider {
    public MMRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output, @NotNull HolderLookup.Provider lookup) {
        // Shaped
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemHandler.SPEAR.value())
                .pattern(" F ")
                .pattern("ES ")
                .pattern(" S ")
                .define('F', Items.FLINT)
                .define('S', Tags.Items.RODS_WOODEN)
                .define('E', Tags.Items.FEATHERS)
                .unlockedBy(getHasName(Items.FLINT), has(Items.FLINT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemHandler.BLOWGUN.value())
                .pattern(" B ")
                .pattern("SBS")
                .pattern(" B ")
                .define('B', Items.BAMBOO)
                .define('S', Tags.Items.STRINGS)
                .unlockedBy(getHasName(Items.BAMBOO), has(Items.BAMBOO))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemHandler.DART.value(), 8)
                .pattern("F")
                .pattern("S")
                .pattern("E")
                .define('F', ItemHandler.NAGA_FANG.value())
                .define('S', Tags.Items.RODS_WOODEN)
                .define('E', Tags.Items.FEATHERS)
                .unlockedBy(getHasName(ItemHandler.NAGA_FANG.value()), has(ItemHandler.NAGA_FANG.value()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ItemHandler.NAGA_FANG_DAGGER.value())
                .pattern("F")
                .pattern("I")
                .pattern("S")
                .define('F', ItemHandler.NAGA_FANG.value())
                .define('I', Tags.Items.INGOTS_IRON)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy(getHasName(ItemHandler.NAGA_FANG.value()), has(ItemHandler.NAGA_FANG.value()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlockHandler.PAINTED_ACACIA.value())
                .pattern("S")
                .pattern("S")
                .define('S', BlockHandler.PAINTED_ACACIA_SLAB.value())
                .unlockedBy(getHasName(BlockHandler.PAINTED_ACACIA_SLAB.value()), has(BlockHandler.PAINTED_ACACIA_SLAB.value()))
                .save(output, MMCommon.resource("painted_acacia_block_from_slab"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlockHandler.PAINTED_ACACIA_SLAB.value())
                .pattern("###")
                .define('#', BlockHandler.PAINTED_ACACIA.value())
                .unlockedBy(getHasName(BlockHandler.PAINTED_ACACIA.value()), has(BlockHandler.PAINTED_ACACIA.value()))
                .save(output);

        // Shapeless
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, Items.JUNGLE_PLANKS, 4)
                .requires(BlockHandler.CLAWED_LOG.value())
                .unlockedBy(getHasName(BlockHandler.CLAWED_LOG.value()), has(BlockHandler.CLAWED_LOG.value()))
                .save(output, MMCommon.resource("jungle_planks_from_clawed_log"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, BlockHandler.PAINTED_ACACIA.value(), 4)
                .requires(Items.ACACIA_PLANKS, 4)
                .requires(Items.YELLOW_DYE)
                .requires(Items.WHITE_DYE)
                .requires(Items.CYAN_DYE)
                .unlockedBy("has_painted_acacia_materials", has(
                        BlockHandler.PAINTED_ACACIA.value(),
                        Items.YELLOW_DYE,
                        Items.WHITE_DYE,
                        Items.CYAN_DYE)
                )
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, BlockHandler.THATCH.value())
                .requires(Ingredient.of(Items.SHORT_GRASS, Items.TALL_GRASS), 9)
                .unlockedBy("has_thatch_material", has(Items.SHORT_GRASS, Items.TALL_GRASS))
                .save(output);

        // Smelting
        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(BlockHandler.CLAWED_LOG.value()), RecipeCategory.MISC, Items.CHARCOAL, 0.15f, 200)
                .unlockedBy(getHasName(BlockHandler.CLAWED_LOG.value()), has(BlockHandler.CLAWED_LOG.value()))
                .save(output, MMCommon.resource("charcoal"));

        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ItemHandler.CAPTURED_GROTTOL.value()), RecipeCategory.MISC, Items.DIAMOND, 1, 200)
                .unlockedBy(getHasName(ItemHandler.CAPTURED_GROTTOL.value()), has(ItemHandler.CAPTURED_GROTTOL.value()))
                .save(output, MMCommon.resource("grottol_smelt"));

        // Blasting
        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ItemHandler.CAPTURED_GROTTOL.value()), RecipeCategory.MISC, Items.DIAMOND, 1, 200)
                .unlockedBy(getHasName(ItemHandler.CAPTURED_GROTTOL.value()), has(ItemHandler.CAPTURED_GROTTOL.value()))
                .save(output, MMCommon.resource("grottol_blast"));
    }

    protected static @NotNull Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike... items) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(items));
    }
}
