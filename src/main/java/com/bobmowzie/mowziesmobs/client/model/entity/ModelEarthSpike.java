package com.bobmowzie.mowziesmobs.client.model.entity;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieGeoModel;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityEarthSpike;
import net.minecraft.resources.ResourceLocation;

public class ModelEarthSpike extends MowzieGeoModel<EntityEarthSpike> {
    private static final ResourceLocation MODEL = MMCommon.resource("geo/earth_spike.geo.json");
    private static final ResourceLocation TEXTURE = MMCommon.resource("textures/entity/umvuthi.png");
    private static final ResourceLocation ANIMATION = MMCommon.resource("animations/earth_spike.animation.json");

    public ModelEarthSpike() {
        super();
    }

    @Override
    public ResourceLocation getModelResource(EntityEarthSpike object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(EntityEarthSpike object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(EntityEarthSpike object) {
        return ANIMATION;
    }
}