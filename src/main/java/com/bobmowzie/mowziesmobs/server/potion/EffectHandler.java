package com.bobmowzie.mowziesmobs.server.potion;

import com.bobmowzie.mowziesmobs.MMCommon;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EffectHandler {
	public static final DeferredRegister<MobEffect> REG = DeferredRegister.create(Registries.MOB_EFFECT, MMCommon.MODID);
	
    public static final DeferredHolder<MobEffect, EffectSunsBlessing> SUNS_BLESSING = REG.register("suns_blessing", EffectSunsBlessing::new);
    public static final DeferredHolder<MobEffect, EffectGeomancy> GEOMANCY = REG.register("geomancy", EffectGeomancy::new);
    public static final DeferredHolder<MobEffect, EffectFrozen> FROZEN = REG.register("frozen", EffectFrozen::new);
    public static final DeferredHolder<MobEffect, EffectPoisonResist> POISON_RESIST = REG.register("poison_resist", EffectPoisonResist::new);
    public static final DeferredHolder<MobEffect, EffectSunblock> SUNBLOCK = REG.register("sunblock", EffectSunblock::new);
    public static final DeferredHolder<MobEffect, EffectMoonsCurse> MOONS_CURSE = REG.register("moons_curse", EffectMoonsCurse::new);
    public static final DeferredHolder<MobEffect, EffectFragility> FRAGILITY = REG.register("fragility", EffectFragility::new);
    public static final DeferredHolder<MobEffect, EffectEclipsed> ECLIPSED = REG.register("eclipsed", EffectEclipsed::new);

    public static void addOrCombineEffect(LivingEntity entity, Holder<MobEffect> effect, int duration, int amplifier, boolean ambient, boolean showParticles) {
        if (effect == null) return;
        MobEffectInstance effectInst = entity.getEffect(effect);
        MobEffectInstance newEffect = new MobEffectInstance(effect, duration, amplifier, ambient, showParticles);
        if (effectInst != null) effectInst.update(newEffect);
        else entity.addEffect(newEffect);
    }
}
