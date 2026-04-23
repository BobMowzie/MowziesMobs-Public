package com.bobmowzie.mowziesmobs.server.entity.elokosa;

import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.server.potion.EffectHandler;
import com.google.common.base.Defaults;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.EnumMap;
import java.util.Locale;

public enum PawType {
    FULL(MobEffects.WEAKNESS, ParticleHandler.MOON_FULL),
    GIBBOUS(MobEffects.MOVEMENT_SLOWDOWN, ParticleHandler.MOON_GIBBOUS),
    HALF(MobEffects.DIG_SLOWDOWN, ParticleHandler.MOON_HALF),
    CRESCENT(EffectHandler.FRAGILITY, ParticleHandler.MOON_CRESCENT),
    NEW(EffectHandler.ECLIPSED, ParticleHandler.MOON_NEW);

    public static final int COUNT = PawType.values().length;

    private final Holder<MobEffect> potion;
    private final Holder<ParticleType<?>> particleType;

    private final String name;

    PawType(Holder<MobEffect> potion, Holder<ParticleType<?>> particleType) {
        this.potion = potion;
        this.particleType = particleType;
        name = name().toLowerCase(Locale.ENGLISH);
    }

    public static PawType from(int id) {
        if (id < 0 || id >= COUNT) {
            return NEW;
        }
        return values()[id];
    }

    public static <T> EnumMap<PawType, T> newEnumMap(Class<T> type, T... defaultValues) {
        EnumMap map = new EnumMap<PawType, T>(PawType.class);
        PawType[] paws = values();
        for (int i = 0; i < paws.length; i++) {
            map.put(paws[i], i >= 0 && i < defaultValues.length ? defaultValues[i] : Defaults.defaultValue(type));
        }
        return map;
    }

    public String getName() {
        return name;
    }

    public Holder<MobEffect> getPotion() {
        return potion;
    }

    public Holder<ParticleType<?>> getParticleType() {
        return particleType;
    }
}
