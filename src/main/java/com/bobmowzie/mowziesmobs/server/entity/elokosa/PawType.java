package com.bobmowzie.mowziesmobs.server.entity.elokosa;

import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleData;
import com.bobmowzie.mowziesmobs.server.potion.EffectHandler;
import com.google.common.base.Defaults;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.EnumMap;
import java.util.Locale;

public enum PawType {
    FULL(MobEffects.WEAKNESS, ParticleHandler.MOON_FULL.get()),
    GIBBOUS(MobEffects.MOVEMENT_SLOWDOWN, ParticleHandler.MOON_GIBBOUS.get()),
    HALF(MobEffects.DIG_SLOWDOWN, ParticleHandler.MOON_HALF.get()),
    CRESCENT(EffectHandler.FRAGILITY.get(), ParticleHandler.MOON_CRESCENT.get()),
    NEW(EffectHandler.ECLIPSED.get(), ParticleHandler.MOON_NEW.get());

    public static final int COUNT = PawType.values().length;

    private final MobEffect potion;
    private final ParticleType<AdvancedParticleData> particleType;

    private final String name;

    PawType(MobEffect potion, ParticleType<AdvancedParticleData> particleType) {
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

    public MobEffect getPotion() {
        return potion;
    }

    public ParticleType<AdvancedParticleData> getParticleType() {
        return particleType;
    }
}
