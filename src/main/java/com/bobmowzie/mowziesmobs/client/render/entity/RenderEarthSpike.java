package com.bobmowzie.mowziesmobs.client.render.entity;

import com.bobmowzie.mowziesmobs.client.model.entity.ModelEarthSpike;
import com.bobmowzie.mowziesmobs.client.render.entity.layer.GeckoBlockLayer;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityEarthSpike;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.cache.object.GeoCube;

@OnlyIn(Dist.CLIENT)
public class RenderEarthSpike extends RenderGeomancyBase<EntityEarthSpike> {
    private static final ResourceLocation TEXTURE_DIRT = new ResourceLocation("textures/block/dirt.png");

    public RenderEarthSpike(EntityRendererProvider.Context mgr) {
        super(mgr, new ModelEarthSpike());
        this.addRenderLayer(new GeckoBlockLayer<>(this,
                (bone, animatable) -> {
                    if (bone.getName().contains("block")) {
                        return animatable.getBlock();
                    }
                    return null;
                }));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityEarthSpike entity) {
        return TEXTURE_DIRT;
    }

    @Override
    public void render(EntityEarthSpike entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    protected void applyRotations(EntityEarthSpike animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        rotationYaw = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick);
    }

    @Override
    public void renderCube(PoseStack poseStack, GeoCube cube, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

    }
}
