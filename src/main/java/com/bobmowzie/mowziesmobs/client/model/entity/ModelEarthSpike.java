package com.bobmowzie.mowziesmobs.client.model.entity;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieGeoModel;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityEarthSpike;
import net.minecraft.resources.ResourceLocation;

public class ModelEarthSpike extends MowzieGeoModel<EntityEarthSpike> {
    public ModelEarthSpike() {
        super();
    }

    @Override
    public ResourceLocation getModelResource(EntityEarthSpike object) {
        return new ResourceLocation(MowziesMobs.MODID, "geo/earth_spike.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EntityEarthSpike object) {
        return new ResourceLocation(MowziesMobs.MODID, "textures/entity/umvuthi.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EntityEarthSpike object) {
        return new ResourceLocation(MowziesMobs.MODID, "animations/earth_spike.animation.json");
    }
}