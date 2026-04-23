package com.bobmowzie.mowziesmobs.client.render.entity.layer;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GeckoSunblockLayer<T extends LivingEntity & GeoEntity> extends GeoRenderLayer<T> {
    private static final ResourceLocation SUNBLOCK_ARMOR = ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "textures/entity/sunblock_glow.png");

    public GeckoSunblockLayer(GeoRenderer<T> entityRendererIn, EntityRendererProvider.Context context) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (DataHandler.getData(animatable, DataHandler.LIVING_DATA).getHasSunblock()) {
            float f = (float) animatable.tickCount + partialTick;
            RenderType renderTypeSwirl = RenderType.energySwirl(this.getTextureLocation(), this.xOffset(f), f * 0.01F);

            getRenderer().reRender(getDefaultBakedModel(animatable),
                    poseStack, bufferSource, animatable, renderTypeSwirl, bufferSource.getBuffer(renderTypeSwirl), partialTick,
                    packedLight, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(1, 1, 1, 0.1f));
        }
    }

    protected float xOffset(float p_225634_1_) {
        return p_225634_1_ * 0.02F;
    }

    protected ResourceLocation getTextureLocation() {
        return SUNBLOCK_ARMOR;
    }
}
