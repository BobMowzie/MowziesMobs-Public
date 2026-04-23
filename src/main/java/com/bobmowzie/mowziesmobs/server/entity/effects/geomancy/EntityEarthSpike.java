package com.bobmowzie.mowziesmobs.server.entity.effects.geomancy;

import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieAnimationController;
import com.bobmowzie.mowziesmobs.client.particle.AdvancedTerrainParticle;
import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.bluff.EntityBluff;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntityMagicEffect;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.List;

public class EntityEarthSpike extends EntityGeomancyBase {
    private boolean emerged = false;
    private int damageDelay = -1;
    protected MowzieAnimationController<EntityEarthSpike> controller = new MowzieAnimationController<>(this, "controller", 0, this::predicate, 0);

    public EntityEarthSpike(EntityType<? extends EntityMagicEffect> type, Level worldIn) {
        super(type, worldIn);
    }

    public EntityEarthSpike(EntityType<? extends EntityMagicEffect> type, Level worldIn, LivingEntity caster, BlockState blockState) {
        super(type, worldIn, caster, blockState, null);
        setDeathTime(180);
        setTier(GeomancyTier.SMALL);
    }

    @Override
    public void tick() {
        super.tick();

        if (damageDelay >= 0) --damageDelay;

        if (!emerged) {
            emerged = true;
            damageDelay = 2;
            playSound(MMSounds.ENTITY_BLUFF_SPIKE_EMERGE.get(), 1, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            if (level().isClientSide()) {
                for (int i = 0; i < 30; i++) {
                    Vec3 offset = new Vec3(0.6 + random.nextFloat() * 0.2, 0.1, 0).yRot(random.nextFloat() * (float) Math.PI * 2f);
                    Vec3 vel = offset.normalize().scale(random.nextGaussian() * 0.12).yRot(random.nextFloat() * 0.2f - 0.1f).add(0, random.nextDouble() * 0.45 + 0.2, 0).add(getForward().scale(0.1));
                    AdvancedTerrainParticle.spawnTerrainParticle(level(), ParticleHandler.TERRAIN.get(), getX() + offset.x, getY(), getZ() + offset.z, vel.x, vel.y, vel.z, 0, 0.4f + random.nextGaussian() * 0.3, 0.94f, 25 + random.nextFloat() * 10, getBlock(), new ParticleComponent[]{
                            new ParticleComponent.Gravity(1)
                    });
                }
            }
        }

        if (damageDelay == 0 && !level().isClientSide()) {
            damageDelay = -1;
            List<Entity> entitiesHit = level().getEntities(this, getBoundingBox().inflate(0.4), e -> e.canBeHitByProjectile() && e != getCaster() && !(e instanceof ItemEntity));
            double damage = 10;
            if (getCaster() != null) {
                if (getCaster() instanceof EntityBluff) {
                    AttributeInstance attrib = getCaster().getAttribute(Attributes.ATTACK_DAMAGE);
                    if (attrib != null) {
                        damage = attrib.getValue();
                    }
                    damage = damage * ConfigHandler.COMMON.MOBS.BLUFF.combatConfig.attackMultiplier.get();
                }
            }
            for (Entity entity : entitiesHit) {
                if (getCaster() instanceof EntityBluff && entity instanceof EntityBluff) continue;
                if (entity instanceof ItemEntity) continue;
                entity.hurt(damageSources().mobProjectile(this, getCaster()), (float) damage);
                float applyKnockbackResistance = 0;
                if (entity instanceof LivingEntity) {
                    applyKnockbackResistance = (float) ((LivingEntity) entity).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
                }
                double y = 0;
                if (entity.onGround()) {
                    y += 0.15 * (1 - applyKnockbackResistance);
                }
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, y, 0));
                if (entity instanceof ServerPlayer) {
                    ((ServerPlayer) entity).connection.send(new ClientboundSetEntityMotionPacket(entity));
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        super.registerControllers(controllers);
        controllers.add(controller);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        emerged = compound.getBoolean("emerged");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("emerged", emerged);
    }

    private static RawAnimation EMERGE = RawAnimation.begin().thenPlay("emerge");
    protected <E extends GeoEntity> PlayState predicate(AnimationState<E> event) {
        event.getController().setAnimation(EMERGE);
        return PlayState.CONTINUE;
    }

    @Override
    protected void explode() {
        super.explode();
//        playSound(MMSounds.EFFECT_GEOMANCY_BREAK.get(), 1, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
    }
}
