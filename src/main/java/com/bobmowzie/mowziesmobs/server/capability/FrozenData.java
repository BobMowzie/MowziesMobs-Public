package com.bobmowzie.mowziesmobs.server.capability;

import com.bobmowzie.mowziesmobs.client.particle.ParticleCloud;
import com.bobmowzie.mowziesmobs.client.particle.ParticleSnowFlake;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.frostmaw.EntityFrozenController;
import com.bobmowzie.mowziesmobs.server.potion.EffectHandler;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FrozenData implements INBTSerializable<CompoundTag> {
    public static int MAX_FREEZE_DECAY_DELAY = 10;

    public boolean frozen;
    public float freezeProgress = 0;
    public float frozenYaw;
    public float frozenPitch;
    public float frozenYawHead;
    public float frozenRenderYawOffset;
    public float frozenSwingProgress;
    public float frozenWalkAnimSpeed;
    public float frozenWalkAnimPosition;
    public boolean prevHasAI = true;
    public UUID prevAttackTarget;

    // After taking freeze progress, this timer needs to reach zero before freeze progress starts to fade
    public int freezeDecayDelay;

    public boolean prevFrozen = false;
    public EntityFrozenController frozenController;

    public boolean getFrozen() {
        return frozen;
    }

    public float getFreezeProgress() {
        return freezeProgress;
    }

    public void setFreezeProgress(float freezeProgress) {
        this.freezeProgress = freezeProgress;
    }

    public float getFrozenYaw() {
        return frozenYaw;
    }

    public void setFrozenYaw(float frozenYaw) {
        this.frozenYaw = frozenYaw;
    }

    public float getFrozenPitch() {
        return frozenPitch;
    }

    public void setFrozenPitch(float frozenPitch) {
        this.frozenPitch = frozenPitch;
    }

    public float getFrozenYawHead() {
        return frozenYawHead;
    }

    public void setFrozenYawHead(float frozenYawHead) {
        this.frozenYawHead = frozenYawHead;
    }

    public float getFrozenRenderYawOffset() {
        return frozenRenderYawOffset;
    }

    public void setFrozenRenderYawOffset(float frozenRenderYawOffset) {
        this.frozenRenderYawOffset = frozenRenderYawOffset;
    }

    public float getFrozenSwingProgress() {
        return frozenSwingProgress;
    }

    public void setFrozenSwingProgress(float frozenSwingProgress) {
        this.frozenSwingProgress = frozenSwingProgress;
    }

    public float getFrozenWalkAnimSpeed() {
        return frozenWalkAnimSpeed;
    }

    public void setFrozenWalkAnimSpeed(float frozenWalkAnimPosition) {
        this.frozenWalkAnimSpeed = frozenWalkAnimPosition;
    }

    public float getFrozenWalkAnimPosition() {
        return frozenWalkAnimPosition;
    }

    public void setFrozenWalkAnimPosition(float frozenWalkAnimPosition) {
        this.frozenWalkAnimPosition = frozenWalkAnimPosition;
    }

    public boolean prevHasAI() {
        return prevHasAI;
    }

    public void setPrevHasAI(boolean prevHasAI) {
        this.prevHasAI = prevHasAI;
    }

    public int getFreezeDecayDelay() {
        return freezeDecayDelay;
    }

    public void setFreezeDecayDelay(int freezeDecayDelay) {
        this.freezeDecayDelay = freezeDecayDelay;
    }

    public boolean getPrevFrozen() {
        return prevFrozen;
    }

    public void setPrevFrozen(boolean prevFrozen) {
        this.prevFrozen = prevFrozen;
    }

    public UUID getPreAttackTarget() {
        return prevAttackTarget;
    }

    public void setPreAttackTarget(UUID livingEntity) {
        prevAttackTarget = livingEntity;
    }

    public EntityFrozenController getFrozenController() {
        return frozenController;
    }

    public void setFrozenController(EntityFrozenController frozenController) {
        this.frozenController = frozenController;
    }

    public void addFreezeProgress(LivingEntity entity, float amount) {
        if (!entity.level().isClientSide && !entity.hasEffect(EffectHandler.FROZEN)) {
            freezeProgress += amount;
            freezeDecayDelay = MAX_FREEZE_DECAY_DELAY;
        }
    }

    public void onFreeze(LivingEntity entity) {
        if (entity != null) {
            frozen = true;
            frozenController = new EntityFrozenController(EntityHandler.FROZEN_CONTROLLER.get(), entity.level());
            frozenController.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
            entity.level().addFreshEntity(frozenController);
            frozenController.setYBodyRot(entity.yBodyRot);
            frozenYaw = entity.getYRot();
            frozenPitch = entity.getXRot();
            frozenYawHead = entity.yHeadRot;
            frozenWalkAnimSpeed = entity.walkAnimation.speed();
            frozenWalkAnimPosition = entity.walkAnimation.position();
            frozenRenderYawOffset = entity.yBodyRot;
            frozenSwingProgress = entity.attackAnim;
            entity.startRiding(frozenController, true);
            entity.stopUsingItem();

            if (entity instanceof Mob) {
                Mob mobEntity = (Mob) entity;
                if (mobEntity.getTarget() != null) setPreAttackTarget(mobEntity.getTarget().getUUID());
                prevHasAI = !((Mob) entity).isNoAi();
                mobEntity.setNoAi(true);
            }

            if (entity.level().isClientSide) {
                int particleCount = (int) (10 + 1 * entity.getBbHeight() * entity.getBbWidth() * entity.getBbWidth());
                for (int i = 0; i < particleCount; i++) {
                    double snowX = entity.getX() + entity.getBbWidth() * entity.getRandom().nextFloat() - entity.getBbWidth() / 2;
                    double snowZ = entity.getZ() + entity.getBbWidth() * entity.getRandom().nextFloat() - entity.getBbWidth() / 2;
                    double snowY = entity.getY() + entity.getBbHeight() * entity.getRandom().nextFloat();
                    Vec3 motion = new Vec3(snowX - entity.getX(), snowY - (entity.getY() + entity.getBbHeight() / 2), snowZ - entity.getZ()).normalize();
                    entity.level().addParticle(new ParticleSnowFlake.Data(40, false), snowX, snowY, snowZ, 0.1d * motion.x, 0.1d * motion.y, 0.1d * motion.z);
                }
            }
            entity.playSound(MMSounds.ENTITY_FROSTMAW_FROZEN_CRASH.get(), 1, 1);
        }
    }

    public void onUnfreeze(LivingEntity entity) {
        if (entity != null) {
            freezeProgress = 0;
            if (frozen) {
                entity.removeEffectNoUpdate(EffectHandler.FROZEN);
                frozen = false;
                if (frozenController != null) {
                    Vec3 oldPosition = entity.position();
                    entity.stopRiding();
                    entity.teleportTo(oldPosition.x(), oldPosition.y(), oldPosition.z());
                    frozenController.discard();
                }
                entity.playSound(MMSounds.ENTITY_FROSTMAW_FROZEN_CRASH.get(), 1, 0.5f);
                if (entity.level().isClientSide) {
                    int particleCount = (int) (10 + 1 * entity.getBbHeight() * entity.getBbWidth() * entity.getBbWidth());
                    for (int i = 0; i < particleCount; i++) {
                        double particleX = entity.getX() + entity.getBbWidth() * entity.getRandom().nextFloat() - entity.getBbWidth() / 2;
                        double particleZ = entity.getZ() + entity.getBbWidth() * entity.getRandom().nextFloat() - entity.getBbWidth() / 2;
                        double particleY = entity.getY() + entity.getBbHeight() * entity.getRandom().nextFloat() + 0.3f;
                        entity.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()), particleX, particleY, particleZ, 0, 0, 0);
                    }
                }
                if (entity instanceof Mob) {
                    if (((Mob) entity).isNoAi() && prevHasAI) {
                        ((Mob) entity).setNoAi(false);
                    }
                    if (getPreAttackTarget() != null) {
                        Player target = entity.level().getPlayerByUUID(getPreAttackTarget());
                        if (target != null) {
                            ((Mob) entity).setTarget(target);
                        }
                    }
                }
            }
        }
    }

    public void tick(LivingEntity entity) {
        // Freeze logic
        if (getFreezeProgress() >= 1 && !entity.hasEffect(EffectHandler.FROZEN)) {
            entity.addEffect(new MobEffectInstance(EffectHandler.FROZEN, 50, 0, false, false));
            freezeProgress = 1f;
        } else if (freezeProgress > 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 9, Mth.floor(freezeProgress * 5 + 1), false, false));
        }

        if (frozenController == null) {
            Entity riding = entity.getVehicle();
            if (riding instanceof EntityFrozenController) frozenController = (EntityFrozenController) riding;
        }

        if (frozen) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 50, false, false));
            entity.setShiftKeyDown(false);

            if (entity.level().isClientSide && entity.tickCount % 2 == 0) {
                double cloudX = entity.getX() + entity.getBbWidth() * entity.getRandom().nextFloat() - entity.getBbWidth() / 2;
                double cloudZ = entity.getZ() + entity.getBbWidth() * entity.getRandom().nextFloat() - entity.getBbWidth() / 2;
                double cloudY = entity.getY() + entity.getBbHeight() * entity.getRandom().nextFloat();
                entity.level().addParticle(new ParticleCloud.Data(0.75f, 0.75f, 1f, 15f, 25, ParticleCloud.EnumCloudBehavior.CONSTANT, 1f), cloudX, cloudY, cloudZ, 0f, -0.01f, 0f);

                double snowX = entity.getX() + entity.getBbWidth() * entity.getRandom().nextFloat() - entity.getBbWidth() / 2;
                double snowZ = entity.getZ() + entity.getBbWidth() * entity.getRandom().nextFloat() - entity.getBbWidth() / 2;
                double snowY = entity.getY() + entity.getBbHeight() * entity.getRandom().nextFloat();
                entity.level().addParticle(new ParticleSnowFlake.Data(40, false), snowX, snowY, snowZ, 0d, -0.01d, 0d);
            }
        } else {
            if (!entity.level().isClientSide && getPrevFrozen()) {
                onUnfreeze(entity);
            }
        }

        if (freezeDecayDelay <= 0) {
            freezeProgress -= 0.1f;
            if (freezeProgress < 0) freezeProgress = 0;
        } else {
            freezeDecayDelay--;
        }
        prevFrozen = entity.hasEffect(EffectHandler.FROZEN);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull HolderLookup.Provider lookup) {
        CompoundTag compound = new CompoundTag();
        compound.putFloat("freezeProgress", getFreezeProgress());
        compound.putInt("freezeDecayDelay", getFreezeDecayDelay());
        compound.putFloat("frozenWalkAnimSpeed", getFrozenWalkAnimSpeed());
        compound.putFloat("frozenWalkAnimPosition", getFrozenWalkAnimPosition());
        compound.putFloat("frozenRenderYawOffset", getFrozenRenderYawOffset());
        compound.putFloat("frozenSwingProgress", getFrozenSwingProgress());
        compound.putFloat("frozenPitch", getFrozenPitch());
        compound.putFloat("frozenYaw", getFrozenYaw());
        compound.putFloat("frozenYawHead", getFrozenYawHead());
        compound.putBoolean("prevHasAI", prevHasAI());
        if (getPreAttackTarget() != null) {
            compound.putUUID("prevAttackTarget", getPreAttackTarget());
        }
        compound.putBoolean("frozen", frozen);
        compound.putBoolean("prevFrozen", prevFrozen);
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull HolderLookup.Provider lookup, CompoundTag compound) {
        setFreezeProgress(compound.getFloat("freezeProgress"));
        setFreezeDecayDelay(compound.getInt("freezeDecayDelay"));
        setFrozenWalkAnimSpeed(compound.getFloat("frozenWalkAnimSpeed"));
        setFrozenWalkAnimPosition(compound.getFloat("frozenWalkAnimPosition"));
        setFrozenRenderYawOffset(compound.getFloat("frozenRenderYawOffset"));
        setFrozenSwingProgress(compound.getFloat("frozenSwingProgress"));
        setFrozenPitch(compound.getFloat("frozenPitch"));
        setFrozenYaw(compound.getFloat("frozenYaw"));
        setFrozenYawHead(compound.getFloat("frozenYawHead"));
        setPrevHasAI(compound.getBoolean("prevHasAI"));
        try {
            setPreAttackTarget(compound.getUUID("prevAttackTarget"));
        } catch (NullPointerException ignored) {
        }
        frozen = compound.getBoolean("frozen");
        prevFrozen = compound.getBoolean("prevFrozen");
    }
}
