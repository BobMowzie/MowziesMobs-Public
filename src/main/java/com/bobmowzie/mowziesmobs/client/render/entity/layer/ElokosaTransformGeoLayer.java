package com.bobmowzie.mowziesmobs.client.render.entity.layer;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieGeoModel;
import com.bobmowzie.mowziesmobs.server.entity.elokosa.EntityElokosa;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ElokosaTransformGeoLayer extends GeoRenderLayer<EntityElokosa> {
    public ElokosaTransformGeoLayer(GeoRenderer<EntityElokosa> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    protected ResourceLocation getTextureResource(EntityElokosa animatable) {
        return new ResourceLocation(MowziesMobs.MODID, "textures/entity/elokosa_transforming.png");
    }

    @Override
    public void render(PoseStack poseStack, EntityElokosa animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderType renderTypeTranslucent = RenderType.entityTranslucent(getTextureResource(animatable));
        float opacity = getOpacity(animatable, (MowzieGeoModel<EntityElokosa>) renderer.getGeoModel());
        getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, renderTypeTranslucent,
                bufferSource.getBuffer(renderTypeTranslucent), partialTick, packedLight, OverlayTexture.NO_OVERLAY,
                1F, 1F, 1F, opacity);
    }

    private float getOpacity(EntityElokosa entity, MowzieGeoModel<EntityElokosa> model) {
        float textureBlend = 0;
        if (model.isInitialized()) {
            textureBlend = -model.getControllerValue("transformTextureController");
        }
        return textureBlend;
    }
}
