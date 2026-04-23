package com.bobmowzie.mowziesmobs.client.render.entity;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.client.model.entity.ModelFoliaathBaby;
import com.bobmowzie.mowziesmobs.server.entity.foliaath.EntityBabyFoliaath;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderFoliaathBaby extends MobRenderer<EntityBabyFoliaath, ModelFoliaathBaby<EntityBabyFoliaath>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "textures/entity/foliaath_baby.png");

    public RenderFoliaathBaby(EntityRendererProvider.Context mgr) {
        super(mgr, new ModelFoliaathBaby<>(), 0);
    }

    @Override
    protected float getFlipDegrees(EntityBabyFoliaath entity) {
        return 0;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBabyFoliaath entity) {
        return RenderFoliaathBaby.TEXTURE;
    }
}