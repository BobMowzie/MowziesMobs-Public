package com.bobmowzie.mowziesmobs.client.model.item;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.item.ItemSculptorStaff;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ModelSculptorStaff extends GeoModel<ItemSculptorStaff> {

    @Override
    public ResourceLocation getModelResource(ItemSculptorStaff object) {
        return ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "geo/sculptor_staff.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ItemSculptorStaff object) {
        return ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "textures/item/sculptor_staff.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ItemSculptorStaff animatable) {
        return ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "animations/sculptor.animation.json");
    }
}