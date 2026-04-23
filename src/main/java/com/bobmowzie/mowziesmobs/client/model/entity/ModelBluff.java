package com.bobmowzie.mowziesmobs.client.model.entity;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieGeoBone;
import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieGeoModel;
import com.bobmowzie.mowziesmobs.server.entity.bluff.EntityBluff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.data.EntityModelData;

public class ModelBluff extends MowzieGeoModel<EntityBluff> {
    public ModelBluff() {
        super();
    }

    @Override
    public ResourceLocation getModelResource(EntityBluff object) {
        return new ResourceLocation(MowziesMobs.MODID, "geo/bluff.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EntityBluff object) {
        return new ResourceLocation(MowziesMobs.MODID, "textures/entity/bluff.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EntityBluff object) {
        return new ResourceLocation(MowziesMobs.MODID, "animations/bluff.animation.json");
    }


    @Override
    public void setCustomAnimations(EntityBluff entity, long instanceId, AnimationState<EntityBluff> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);
        float frame = entity.frame + animationState.getPartialTick();
        float ticks = entity.tickCount;

        MowzieGeoBone rotation1 = getMowzieBone("rotation1");
        MowzieGeoBone rotation2 = getMowzieBone("rotation2");
        MowzieGeoBone rotation3 = getMowzieBone("rotation3");
        MowzieGeoBone core = getMowzieBone("core");

        if (entity.isAlive()) {
            rotation1.addRotY((frame % 360 / 4f));
            rotation2.addRotY((frame % 360 / 4f));
            rotation3.addRotY((frame % 360 / 4f));
            core.addRotY((frame % 360 / -4f));
            core.addPosY((float) (Math.sin(frame / 5f) *0.8f));
            core.addRotX((float) (Math.sin(frame / 9f) *1f));
        }

        MowzieGeoBone head = getMowzieBone("head");
        MowzieGeoBone root = getMowzieBone("root");

        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float headYaw = Mth.wrapDegrees(entityData.netHeadYaw());
        float headPitch = Mth.wrapDegrees(entityData.headPitch());
        head.addRotX(headPitch * (float) Math.PI / 180F);
        root.addRotY(headYaw * (float) Math.PI / 180F);
    }
}