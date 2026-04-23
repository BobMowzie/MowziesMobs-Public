package com.bobmowzie.mowziesmobs.client.render.entity.layer;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.entity.elokosa.EntityElokosa;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ElokosaHandSymbolGeoLayer extends GeoRenderLayer<EntityElokosa> {
    public ElokosaHandSymbolGeoLayer(GeoRenderer<EntityElokosa> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    protected ResourceLocation getTextureResource(EntityElokosa animatable) {
        return switch (animatable.level().getMoonPhase()) {
            case 0 -> MMCommon.resource("textures/entity/elokosa_paw_full.png");
            case 1, 7 -> MMCommon.resource("textures/entity/elokosa_paw_gibbous.png");
            case 2, 6 -> MMCommon.resource("textures/entity/elokosa_paw_half.png");
            default -> MMCommon.resource("textures/entity/elokosa_paw_crescent.png");
        };
    }

    @Override
    public void render(PoseStack poseStack, EntityElokosa animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderType renderTypeTranslucent = RenderType.entityTranslucent(getTextureResource(animatable));
        getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, renderTypeTranslucent,
                bufferSource.getBuffer(renderTypeTranslucent), partialTick, packedLight, packedOverlay,
                FastColor.ARGB32.colorFromFloat(1, 1, 1, 1f));
    }
}
