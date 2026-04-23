package com.bobmowzie.mowziesmobs.server.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class EffectFragility extends MobEffect {
    protected EffectFragility() {
        super(MobEffectCategory.HARMFUL, 0xa5f046);
    }
}
