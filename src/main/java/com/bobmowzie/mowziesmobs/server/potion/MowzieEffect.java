package com.bobmowzie.mowziesmobs.server.potion;

import com.bobmowzie.mowziesmobs.MMCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class MowzieEffect extends MobEffect {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "textures/gui/container/potions.png");

    public MowzieEffect(MobEffectCategory type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int id, int amplifier) {
        return true;
    }
}
