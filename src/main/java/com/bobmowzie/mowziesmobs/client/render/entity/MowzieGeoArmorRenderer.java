package com.bobmowzie.mowziesmobs.client.render.entity;

import com.bobmowzie.mowziesmobs.client.model.tools.MathUtils;
import com.bobmowzie.mowziesmobs.client.model.tools.ModelPartMatrix;
import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieGeoBone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ArmorItem;
import org.joml.Matrix4f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class MowzieGeoArmorRenderer<T extends ArmorItem & GeoItem> extends GeoArmorRenderer<T> {
    public boolean usingCustomPlayerAnimations = false;

    public MowzieGeoArmorRenderer(GeoModel<T> modelProvider) {
        super(modelProvider);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (currentEntity != null) {
            super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
            usingCustomPlayerAnimations = false;
        }
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferIn, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();
        if (usingCustomPlayerAnimations && bone instanceof MowzieGeoBone && ((MowzieGeoBone) bone).isForceMatrixTransform()) {
            PoseStack.Pose last = poseStack.last();
            last.pose().identity();
            last.normal().identity();
            Matrix4f matrix4f = bone.getWorldSpaceMatrix();
            last.pose().mul(matrix4f);
            last.normal().mul(bone.getWorldSpaceNormal());
            poseStack.mulPose(MathUtils.quatFromRotationXYZ(0, 0, 180, true));
            poseStack.translate(0, -1.5, 0);
        }
        else {
            RenderUtil.prepMatrixForBone(poseStack, bone);
        }
        renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, color);
        renderChildBones(poseStack, animatable, bone, renderType, bufferIn, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
        poseStack.popPose();
//        super.renderRecursively(poseStack, animatable, bone, renderType, bufferIn, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
    }

    public void copyFrom(ModelPart modelPart, GeoBone geoBone, float offsetX, float offsetY, float offsetZ) {
        if (usingCustomPlayerAnimations && modelPart instanceof ModelPartMatrix other && geoBone instanceof MowzieGeoBone thisBone) {
            thisBone.setWorldSpaceNormal(other.getWorldNormal());
            Matrix4f newMatrix = new Matrix4f();
            newMatrix.translate(offsetX / 16f, -offsetY / 16f, offsetZ / 16f);
            Matrix4f worldXform = new Matrix4f(other.getWorldXform());
            Matrix4f offsetMatrix = worldXform.mul(newMatrix);
            thisBone.setWorldSpaceMatrix(offsetMatrix);
            thisBone.setForceMatrixTransform(true);
        }
        else {
            RenderUtil.matchModelPartRot(modelPart, geoBone);
            geoBone.updatePosition(modelPart.x + offsetX, -modelPart.y + offsetY, modelPart.z + offsetZ);
        }
    }

    @Override
    protected void applyBaseTransformations(HumanoidModel<?> baseModel) {
        if (getHeadBone(model) != null) {
            ModelPart headPart = baseModel.head;
            copyFrom(headPart, getHeadBone(model), 0, 0, 0);
        }

        if (getBodyBone(model) != null) {
            ModelPart bodyPart = baseModel.body;
            copyFrom(bodyPart, getBodyBone(model), 0, 0, 0);
        }

        if (getRightArmBone(model) != null) {
            ModelPart rightArmPart = baseModel.rightArm;
            copyFrom(rightArmPart, getRightArmBone(model), 5, 2, 0);
        }

        if (getLeftArmBone(model) != null) {
            ModelPart leftArmPart = baseModel.leftArm;
            copyFrom(leftArmPart, getLeftArmBone(model),  -5, 2, 0);
        }

        if (getRightLegBone(model) != null) {
            ModelPart rightLegPart = baseModel.rightLeg;
            copyFrom(rightLegPart, getRightLegBone(model), 2, 12, 0);

            if (getRightBootBone(model) != null) {
                copyFrom(rightLegPart, getRightBootBone(model), 2, 12, 0);
            }
        }

        if (getLeftLegBone(model) != null) {
            ModelPart leftLegPart = baseModel.leftLeg;
            copyFrom(leftLegPart, getLeftLegBone(model), -2, 12, 0);

            if (getLeftBootBone(model) != null) {
                copyFrom(leftLegPart, getLeftBootBone(model), -2, 12, 0);
            }
        }
    }
}
