package com.bobmowzie.mowziesmobs.client.render.entity.layer;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieGeoBone;
import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieGeoModel;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthana;
import com.ilexiconn.llibrary.client.util.ClientUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class UmvuthanaSunLayer extends GeoRenderLayer<EntityUmvuthana> {
    protected final EntityRenderDispatcher entityRenderDispatcher;

    public UmvuthanaSunLayer(GeoRenderer<EntityUmvuthana> entityRendererIn, EntityRendererProvider.Context context) {
        super(entityRendererIn);
        entityRenderDispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    public void render(PoseStack poseStack, EntityUmvuthana animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        MowzieGeoModel<EntityUmvuthana> model = (MowzieGeoModel<EntityUmvuthana>) getGeoModel();
        MowzieGeoBone head = model.getMowzieBone("head");
        if (!head.isHidden()) {
            poseStack.pushPose();
            poseStack.setIdentity();
            poseStack.mulPose(head.getPose());
            renderSun(animatable, bufferSource, poseStack, packedLight, partialTick);
            bufferSource.getBuffer(renderType);
            poseStack.popPose();
        }
    }

    private void renderSun(EntityUmvuthana animatable, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, float partialTick) {
        PoseStack.Pose matrixstack$entry = poseStack.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Vector4f vecTranslation = new Vector4f(0, 0, 0, 1);
        vecTranslation.mul(matrix4f);
        PoseStack newPoseStack = new PoseStack();
        newPoseStack.translate(vecTranslation.x(), vecTranslation.y(), vecTranslation.z());
        Vector4f vecScale = new Vector4f(1, 0, 0, 1);
        vecScale.mul(matrix4f);
        float scale = (float) new Vec3(vecScale.x() - vecTranslation.x(), vecScale.y() - vecTranslation.y(), vecScale.z() - vecTranslation.z()).length();
        newPoseStack.scale(scale, scale, scale);
        newPoseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        VertexConsumer ivertexbuilder = bufferSource.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "textures/particle/sun_no_glow.png"),true));
        PoseStack.Pose matrixstack$entry2 = newPoseStack.last();
        Matrix4f matrix4f2 = matrixstack$entry2.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();
        drawSun(matrix4f2, matrix3f, ivertexbuilder, packedLight, animatable.tickCount + partialTick);
    }

    private void drawSun(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer builder, int packedLightIn, float time) {
        float sunRadius = 1.2f + (float) Math.sin(time * 4) * 0.085f;
        this.drawVertex(matrix4f, matrix3f, builder, -sunRadius, -sunRadius, 0, 0, 0, 1, packedLightIn);
        this.drawVertex(matrix4f, matrix3f, builder, -sunRadius, sunRadius, 0, 0, 1, 1, packedLightIn);
        this.drawVertex(matrix4f, matrix3f, builder, sunRadius, sunRadius, 0, 1, 1, 1, packedLightIn);
        this.drawVertex(matrix4f, matrix3f, builder, sunRadius, -sunRadius, 0, 1, 0, 1, packedLightIn);
    }

    public void drawVertex(Matrix4f matrix, Matrix3f normals, VertexConsumer vertexBuilder, float offsetX, float offsetY, float offsetZ, float textureX, float textureY, float alpha, int packedLightIn) {
        VertexConsumer consumer = vertexBuilder.addVertex(matrix, offsetX, offsetY, offsetZ).setColor(1f, 1f, 1f, 1.0f).setUv(textureX, textureY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880);
        ClientUtils.transformNormals(consumer, normals, 1, 1, 1);
    }

}
