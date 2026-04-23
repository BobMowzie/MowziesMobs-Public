package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.config.ConfigurableArmorMaterial;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class MaterialHandler { // FIXME 1.21 :: unsure if or where the layer resources are needed
    public static final DeferredRegister<ArmorMaterial> MM_ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, MMCommon.MODID);

    // Toughness and defense gets set as the base value, the configurable multipliers are applied through the 'ArmorMaterialMixin'
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SOL_VISAGE_MATERIAL = MM_ARMOR_MATERIALS.register("sol_visage", () -> {
        ArmorMaterial material = new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.HELMET, ArmorMaterials.GOLD.value().getDefense(ArmorItem.Type.HELMET));
        }), ArmorMaterials.GOLD.value().enchantmentValue(),
                ArmorMaterials.GOLD.value().equipSound(),
                ArmorMaterials.GOLD.value().repairIngredient(),
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "sol_visage"))),
                ArmorMaterials.GOLD.value().toughness(),
                ArmorMaterials.GOLD.value().knockbackResistance()
        );

        // Need to trick the compiler
        ConfigurableArmorMaterial configurable = (ConfigurableArmorMaterial) (Object) material;
        configurable.mowziesmobs$setConfig(ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SOL_VISAGE.armorConfig);

        return material;
    });

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> UMVUTHANA_MASK_MATERIAL = MM_ARMOR_MATERIALS.register("umvuthana_mask", () -> {
        ArmorMaterial material = new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.HELMET, ArmorMaterials.LEATHER.value().getDefense(ArmorItem.Type.HELMET));
        }), ArmorMaterials.LEATHER.value().enchantmentValue(),
                ArmorMaterials.LEATHER.value().equipSound(),
                () -> Ingredient.of(Items.AIR),
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "umvuthana_mask"))),
                ArmorMaterials.LEATHER.value().toughness(),
                ArmorMaterials.LEATHER.value().knockbackResistance()
        );

        ConfigurableArmorMaterial configurable = (ConfigurableArmorMaterial) (Object) material;
        configurable.mowziesmobs$setConfig(ConfigHandler.COMMON.TOOLS_AND_ABILITIES.UMVUTHANA_MASK.armorConfig);

        return material;
    });

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARMOR_WROUGHT_HELM = MM_ARMOR_MATERIALS.register("wrought_helm", () -> {
        ArmorMaterial material = new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.HELMET, ArmorMaterials.IRON.value().getDefense(ArmorItem.Type.HELMET));
        }), ArmorMaterials.IRON.value().enchantmentValue(),
                ArmorMaterials.IRON.value().equipSound(),
                ArmorMaterials.IRON.value().repairIngredient(),
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "wrought_helm"))),
                ArmorMaterials.IRON.value().toughness(),
                0.1f
        );

        ConfigurableArmorMaterial configurable = (ConfigurableArmorMaterial) (Object) material;
        configurable.mowziesmobs$setConfig(ConfigHandler.COMMON.TOOLS_AND_ABILITIES.WROUGHT_HELM.armorConfig);

        return material;
    });

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GEOMANCER_ARMOR_MATERIAL = MM_ARMOR_MATERIALS.register("geomancer_armor", () -> {
        ArmorMaterial material = new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 2);
            map.put(ArmorItem.Type.LEGGINGS, 6);
            map.put(ArmorItem.Type.CHESTPLATE, 7);
            map.put(ArmorItem.Type.HELMET, 2);
        }), ArmorMaterials.IRON.value().enchantmentValue(),
                ArmorMaterials.IRON.value().equipSound(),
                () -> Ingredient.of(ItemHandler.BLUFF_ROD.value()),
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "geomancer_armor"))),
                ArmorMaterials.IRON.value().toughness(),
                0
        );

        ConfigurableArmorMaterial configurable = (ConfigurableArmorMaterial) (Object) material;
        configurable.mowziesmobs$setConfig(ConfigHandler.COMMON.TOOLS_AND_ABILITIES.GEOMANCER_ARMOR.armorConfig);

        return material;
    });
}
