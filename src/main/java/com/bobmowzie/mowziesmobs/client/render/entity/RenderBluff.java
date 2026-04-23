package com.bobmowzie.mowziesmobs.client.render.entity;

import com.bobmowzie.mowziesmobs.client.model.entity.ModelBluff;
import com.bobmowzie.mowziesmobs.server.entity.bluff.EntityBluff;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBluff extends MowzieGeoEntityRenderer<EntityBluff> {
    public RenderBluff(EntityRendererProvider.Context mgr) {
        super(mgr, new ModelBluff());
        shadowRadius = 0.5F;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBluff entity) {
        return this.getMowzieGeoModel().getTextureResource(entity);
    }
}
