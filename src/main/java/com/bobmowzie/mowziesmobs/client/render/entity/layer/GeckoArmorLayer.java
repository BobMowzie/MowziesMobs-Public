package com.bobmowzie.mowziesmobs.client.render.entity.layer;

import com.bobmowzie.mowziesmobs.client.model.entity.ModelBipedAnimated;
import com.bobmowzie.mowziesmobs.client.render.entity.MowzieGeoArmorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

public class GeckoArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, M, A> {
    public GeckoArmorLayer(RenderLayerParent<T, M> layerParent, A innerModel, A outerModel, ModelManager modelManager) {
        super(layerParent, innerModel, outerModel, modelManager);
    }

    @Override
    protected void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, T entity, EquipmentSlot equipmentSlot, int p_117123_, A baseModel) {
        ItemStack itemstack = entity.getItemBySlot(equipmentSlot);
        if (itemstack.getItem() instanceof ArmorItem) {
            ArmorItem armoritem = (ArmorItem)itemstack.getItem();
            if (armoritem.getType().getSlot() == equipmentSlot) {
                net.minecraft.client.model.Model model = getArmorModelHook(entity, itemstack, equipmentSlot, baseModel);
                if (model instanceof HumanoidModel<?>) {
                    HumanoidModel<T> humanoidModel = (HumanoidModel<T>) model;
                    this.getParentModel().copyPropertiesTo(baseModel);
                    this.getParentModel().copyPropertiesTo(humanoidModel);
                    this.setPartVisibility(baseModel, equipmentSlot);
                    this.setPartVisibility((A) humanoidModel, equipmentSlot);
                    boolean flag = this.usesInnerModel(equipmentSlot);
                    boolean flag1 = itemstack.hasFoil();
                    ModelBipedAnimated.setUseMatrixMode(humanoidModel, true);
                    if (armoritem instanceof net.minecraft.world.item.DyeableLeatherItem) {
                        int i = ((net.minecraft.world.item.DyeableLeatherItem) armoritem).getColor(itemstack);
                        float f = (float) (i >> 16 & 255) / 255.0F;
                        float f1 = (float) (i >> 8 & 255) / 255.0F;
                        float f2 = (float) (i & 255) / 255.0F;
                        this.renderModel(poseStack, bufferSource, p_117123_, model, f, f1, f2, this.getArmorResource(entity, itemstack, equipmentSlot, null));
                        this.renderModel(poseStack, bufferSource, p_117123_, model, 1.0F, 1.0F, 1.0F, this.getArmorResource(entity, itemstack, equipmentSlot, "overlay"));
                    } else {
                        this.renderModel(poseStack, bufferSource, p_117123_, model, 1.0F, 1.0F, 1.0F, this.getArmorResource(entity, itemstack, equipmentSlot, null));
                    }

                    ArmorTrim.getTrim(entity.level().registryAccess(), itemstack).ifPresent((p_289638_) -> {
                        ModelBipedAnimated.setUseMatrixMode(humanoidModel, true);
                        this.renderTrim(armoritem.getMaterial(), poseStack, bufferSource, p_117123_, p_289638_, model, flag);
                    });
                    if (itemstack.hasFoil()) {
                        ModelBipedAnimated.setUseMatrixMode(humanoidModel, true);
                        this.renderGlint(poseStack, bufferSource, p_117123_, model);
                    }
                }
            }
        }
    }

    private void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, net.minecraft.client.model.Model armorModel, float red, float green, float blue, ResourceLocation armorResource) {
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(armorResource));
        if (armorModel instanceof MowzieGeoArmorRenderer<?> mowzieGeoArmorRenderer) {
            mowzieGeoArmorRenderer.usingCustomPlayerAnimations = true;
        }

        armorModel.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
    }

    private void renderTrim(ArmorMaterial p_289690_, PoseStack p_289687_, MultiBufferSource p_289643_, int p_289683_, ArmorTrim p_289692_, net.minecraft.client.model.Model p_289663_, boolean p_289651_) {
        TextureAtlasSprite textureatlassprite = this.armorTrimAtlas.getSprite(p_289651_ ? p_289692_.innerTexture(p_289690_) : p_289692_.outerTexture(p_289690_));
        VertexConsumer vertexconsumer = textureatlassprite.wrap(p_289643_.getBuffer(Sheets.armorTrimsSheet()));
        p_289663_.renderToBuffer(p_289687_, vertexconsumer, p_289683_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderGlint(PoseStack p_289673_, MultiBufferSource p_289654_, int p_289649_, net.minecraft.client.model.Model p_289659_) {
        p_289659_.renderToBuffer(p_289673_, p_289654_.getBuffer(RenderType.armorEntityGlint()), p_289649_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
