package com.bobmowzie.mowziesmobs.client.render.entity.layer;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;

public class SunblockLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T,M> {
    private static final ResourceLocation SUNBLOCK_ARMOR = ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "textures/entity/sunblock_glow.png");

    public SunblockLayer(RenderLayerParent<T, M> entityRendererIn) {
        super(entityRendererIn);
    }

    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (DataHandler.getData(entitylivingbaseIn, DataHandler.LIVING_DATA).getHasSunblock()) {
            float f = (float) entitylivingbaseIn.tickCount + partialTicks;
            EntityModel<T> entitymodel = this.getParentModel();
            entitymodel.prepareMobModel(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
            this.getParentModel().copyPropertiesTo(entitymodel);
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.energySwirl(this.getTextureLocation(), this.xOffset(f), f * 0.01F));
            entitymodel.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            entitymodel.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(1, 1, 1, 0.1f));
        }
    }

    protected float xOffset(float p_225634_1_) {
        return p_225634_1_ * 0.02F;
    }

    protected ResourceLocation getTextureLocation() {
        return SUNBLOCK_ARMOR;
    }
}
