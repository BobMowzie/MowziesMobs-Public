package com.bobmowzie.mowziesmobs.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HumanoidArmorLayer.class)
public interface HumanoidArmorLayerAccess {
    @Invoker("renderTrim")
    void mowziesmobs$renderTrim(Holder<ArmorMaterial> material, PoseStack poseStack, MultiBufferSource buffer, int packedLight, ArmorTrim trim, Model model, boolean innerModel);

    @Invoker("renderGlint")
    void mowziesmobs$renderGlint(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Model model);
}
