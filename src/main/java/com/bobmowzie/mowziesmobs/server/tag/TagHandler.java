package com.bobmowzie.mowziesmobs.server.tag;

import com.bobmowzie.mowziesmobs.MMCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class TagHandler {
    public static final TagKey<Item> CAN_HIT_GROTTOL = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "can_hit_grottol"));
    public static final TagKey<Item> HAND_WEAPONS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "hand_weapons"));

    public static final TagKey<Block> CAN_GROTTOL_DIG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "can_grottol_dig"));
    public static final TagKey<Block> GEOMANCY_USEABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "geomancy_useable"));
    public static final TagKey<Block> GEOMANCY_TUNNELABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "geomancy_tunnelable"));

    public static final TagKey<EntityType<?>> UMVUTHANA = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "umvuthana"));
    public static final TagKey<EntityType<?>> UMVUTHANA_UMVUTHI_ALIGNED = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "umvuthana_umvuthi_aligned"));

    public static final TagKey<Biome> HAS_MOWZIE_STRUCTURE = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "has_structure/has_mowzie_structure"));
    public static final TagKey<Biome> IS_MAGICAL = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "is_magical"));
}
