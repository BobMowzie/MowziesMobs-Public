package com.bobmowzie.mowziesmobs.server.capability;

import com.google.common.collect.Maps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class LivingData implements INBTSerializable<CompoundTag> {
    float lastDamage = 0;
    boolean hasSunblock;
    private final Map<Holder<MobEffect>, MobEffectInstance> eclipsedEffects = Maps.newHashMap();

    public void setLastDamage(float damage) {
        lastDamage = damage;
    }

    public float getLastDamage() {
        return lastDamage;
    }

    public void setHasSunblock(boolean hasSunblock) {
        this.hasSunblock = hasSunblock;
    }

    public boolean getHasSunblock() {
        return hasSunblock;
    }

    public void tick(LivingEntity entity) {
//            if (!hasSunblock && entity.isPotionActive(EffectHandler.SUNBLOCK)) hasSunblock = true;
    }

    public void eclipseEffect(MobEffectInstance mobEffectInstance) {
        eclipsedEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
    }

    public void unEclipseEffects(LivingEntity entity) {
        if (!this.eclipsedEffects.isEmpty()) {
            for (MobEffectInstance mobeffectinstance : this.eclipsedEffects.values()) {
                entity.addEffect(mobeffectinstance);
            }
            this.eclipsedEffects.clear();
        }
    }

    @Override
    public CompoundTag serializeNBT(@NotNull HolderLookup.Provider lookup) {
        CompoundTag compound = new CompoundTag();
        if (!this.eclipsedEffects.isEmpty()) {
            ListTag listtag = new ListTag();

            for (MobEffectInstance mobeffectinstance : this.eclipsedEffects.values()) {
                listtag.add(mobeffectinstance.save());
            }

            compound.put("eclipsed_effects", listtag);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull HolderLookup.Provider lookup, @NotNull CompoundTag compound) {
        if (compound.contains("eclipsed_effects", 9)) {
            ListTag listtag = compound.getList("eclipsed_effects", 10);

            for (int i = 0; i < listtag.size(); i++) {
                CompoundTag compoundtag = listtag.getCompound(i);
                MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag);
                if (mobeffectinstance != null) {
                    this.eclipsedEffects.put(mobeffectinstance.getEffect(), mobeffectinstance);
                }
            }
        }
    }
}
