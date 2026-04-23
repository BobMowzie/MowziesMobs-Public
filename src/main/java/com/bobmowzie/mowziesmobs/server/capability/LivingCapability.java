package com.bobmowzie.mowziesmobs.server.capability;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.google.common.collect.Maps;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.Map;

public class LivingCapability {
    public static ResourceLocation ID = new ResourceLocation(MowziesMobs.MODID, "living_cap");

    public interface ILivingCapability extends INBTSerializable<CompoundTag> {
        void setLastDamage(float damage);
        float getLastDamage();
        void setHasSunblock(boolean hasSunblock);
        boolean getHasSunblock();

        void tick(LivingEntity entity);

        void eclipseEffect(MobEffectInstance mobEffectInstance);

        void unEclipseEffects(LivingEntity entity);
    }

    public static class LivingCapabilityImp implements ILivingCapability {
        float lastDamage = 0;
        boolean hasSunblock;
        private final Map<MobEffect, MobEffectInstance> eclipsedEffects = Maps.newHashMap();

        @Override
        public void setLastDamage(float damage) {
            lastDamage = damage;
        }

        @Override
        public float getLastDamage() {
            return lastDamage;
        }

        @Override
        public void setHasSunblock(boolean hasSunblock) {
            this.hasSunblock = hasSunblock;
        }

        @Override
        public boolean getHasSunblock() {
            return hasSunblock;
        }

        @Override
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
        public CompoundTag serializeNBT() {
            CompoundTag compound = new CompoundTag();
            if (!this.eclipsedEffects.isEmpty()) {
                ListTag listtag = new ListTag();

                for (MobEffectInstance mobeffectinstance : this.eclipsedEffects.values()) {
                    listtag.add(mobeffectinstance.save(new CompoundTag()));
                }

                compound.put("eclipsed_effects", listtag);
            }
            return compound;
        }

        @Override
        public void deserializeNBT(CompoundTag compound) {
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
    
    public static class LivingProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag>
    {
        private final LazyOptional<LivingCapability.ILivingCapability> instance = LazyOptional.of(LivingCapabilityImp::new);

        @Override
        public CompoundTag serializeNBT() {
            return instance.orElseThrow(NullPointerException::new).serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
            return CapabilityHandler.LIVING_CAPABILITY.orEmpty(cap, instance.cast());
        }
    }
}
