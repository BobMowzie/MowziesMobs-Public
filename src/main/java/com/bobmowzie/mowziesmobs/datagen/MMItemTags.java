package com.bobmowzie.mowziesmobs.datagen;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.block.BlockHandler;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MMItemTags extends ItemTagsProvider {
    public static final TagKey<Item> CAN_HIT_GROTTOL = key("can_hit_grottol");
    public static final TagKey<Item> HAND_WEAPONS = key("hand_weapons");

    public MMItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, MMCommon.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        addToVanillaTags();
        addToCommonTags();

        tag(CAN_HIT_GROTTOL)
                .addOptional(ResourceLocation.fromNamespaceAndPath("cagedmobs", "dnasamplerdiamond"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("cagedmobs", "dnasamplernetherite"));

        tag(HAND_WEAPONS).add(ItemHandler.EARTHREND_GAUNTLET.value());
    }

    private void addToVanillaTags() {
        tag(ItemTags.DURABILITY_ENCHANTABLE)
                .add(ItemHandler.SPEAR.value())
                .add(ItemHandler.NAGA_FANG_DAGGER.value());

        tag(ItemTags.SHARP_WEAPON_ENCHANTABLE)
                .add(ItemHandler.SPEAR.value())
                .add(ItemHandler.NAGA_FANG_DAGGER.value());

        tag(ItemTags.SWORD_ENCHANTABLE)
                .add(ItemHandler.SPEAR.value())
                .add(ItemHandler.NAGA_FANG_DAGGER.value());

        tag(ItemTags.FIRE_ASPECT_ENCHANTABLE)
                .add(ItemHandler.SPEAR.value())
                .add(ItemHandler.NAGA_FANG_DAGGER.value())
                .add(ItemHandler.WROUGHT_AXE.value());

        tag(ItemTags.AXES)
                .add(ItemHandler.WROUGHT_AXE.value());

        tag(ItemTags.BOW_ENCHANTABLE)
                .add(ItemHandler.BLOWGUN.value());

        tag(ItemTags.BOW_ENCHANTABLE)
                .add(ItemHandler.BLOWGUN.value());

        tag(ItemTags.HEAD_ARMOR)
                .add(ItemHandler.GEOMANCER_BEADS.value())
                .add(ItemHandler.WROUGHT_HELMET.value())
                .add(ItemHandler.SOL_VISAGE.value())
                .add(ItemHandler.UMVUTHANA_MASK_FURY.value())
                .add(ItemHandler.UMVUTHANA_MASK_FEAR.value())
                .add(ItemHandler.UMVUTHANA_MASK_RAGE.value())
                .add(ItemHandler.UMVUTHANA_MASK_BLISS.value())
                .add(ItemHandler.UMVUTHANA_MASK_MISERY.value())
                .add(ItemHandler.UMVUTHANA_MASK_FAITH.value());

        tag(ItemTags.CHEST_ARMOR).add(ItemHandler.GEOMANCER_ROBE.value());
        tag(ItemTags.LEG_ARMOR).add(ItemHandler.GEOMANCER_BELT.value());
        tag(ItemTags.FOOT_ARMOR).add(ItemHandler.GEOMANCER_SANDALS.value());
        tag(ItemTags.PIGLIN_LOVED).add(ItemHandler.SOL_VISAGE.value());

        tag(ItemTags.PLANKS).add(BlockHandler.PAINTED_ACACIA.value().asItem());
        tag(ItemTags.WOODEN_SLABS).add(BlockHandler.PAINTED_ACACIA_SLAB.value().asItem());
    }

    private void addToCommonTags() {
        tag(Tags.Items.TOOLS_SPEAR).add(ItemHandler.SPEAR.value());
        tag(Tags.Items.MUSIC_DISCS).add(ItemHandler.PETIOLE_MUSIC_DISC.value());
        tag(Tags.Items.SEEDS).add(ItemHandler.FOLIAATH_SEED.value());
        tag(Tags.Items.SLIME_BALLS).add(ItemHandler.GLOWING_JELLY.value());
    }

    private static TagKey<Item> key(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, path));
    }
}
