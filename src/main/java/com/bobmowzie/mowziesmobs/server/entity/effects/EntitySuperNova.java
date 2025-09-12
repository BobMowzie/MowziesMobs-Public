package com.bobmowzie.mowziesmobs.server.entity.effects;

import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleBase;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent.KeyTrack;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent.PropertyControl.EnumParticleProperty;
import com.bobmowzie.mowziesmobs.client.particle.util.RibbonComponent;
import com.bobmowzie.mowziesmobs.client.particle.util.RibbonComponent.PropertyOverLength;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.damage.DamageTypes;
import com.bobmowzie.mowziesmobs.server.damage.DamageUtil;
import com.bobmowzie.mowziesmobs.server.entity.LeaderSunstrikeImmune;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthi;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EntitySuperNova extends EntityMagicEffect {
    public static int DURATION = 40;

    public EntitySuperNova(EntityType<? extends EntitySuperNova> type, Level world) {
        super(type, world);
    }

    public EntitySuperNova(EntityType<? extends EntitySuperNova> type, Level world, LivingEntity caster, double x, double y, double z) {
        super(type, world, caster);
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (getCaster() == null || getCaster().isRemoved() || !getCaster().isAlive()) this.discard();

        if (tickCount == 1) {
            EntityCameraShake.cameraShake(level(), position(), 30, 0.05f, 10, 30);
            playSound(MMSounds.ENTITY_SUPERNOVA_END.get(), 3f, 1f);
            if (level().isClientSide) {
                float scale = 8.2f;
                for (int i = 0; i < 15; i++) {
                    float phaseOffset = random.nextFloat();
                    AdvancedParticleBase.spawnParticle(level(), ParticleHandler.ARROW_HEAD.get(), getX(), getY(), getZ(), 0, 0, 0, false, 0, 0, 0, 0, 8F, 0.95, 0.9, 0.35, 1, 1, 30, true, true, new ParticleComponent[]{
                            new ParticleComponent.Orbit(new Vec3[]{position().add(0, getBbHeight() / 2, 0)}, KeyTrack.startAndEnd(0 + phaseOffset, 1.6f + phaseOffset), new ParticleComponent.KeyTrack(
                                    new float[]{0.2f * scale, 0.63f * scale, 0.87f * scale, 0.974f * scale, 0.998f * scale, 1f * scale},
                                    new float[]{0, 0.15f, 0.3f, 0.45f, 0.6f, 0.75f}
                            ), KeyTrack.startAndEnd(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1), KeyTrack.startAndEnd(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1), KeyTrack.startAndEnd(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1), false),
                            new RibbonComponent(ParticleHandler.RIBBON_FLAT.get(), 10, 0, 0, 0, 0.2F, 0.95, 0.9, 0.35, 1, true, true, new ParticleComponent[]{
                                    new PropertyOverLength(PropertyOverLength.EnumRibbonProperty.SCALE, KeyTrack.startAndEnd(1, 0)),
                                    new ParticleComponent.PropertyControl(EnumParticleProperty.ALPHA, KeyTrack.startAndEnd(1, 0), false)
                            }),
                            new ParticleComponent.PropertyControl(EnumParticleProperty.ALPHA, KeyTrack.startAndEnd(1, 0), false),
                            new ParticleComponent.FaceMotion()
                    });
                }
            }
        }

        if (getCaster() != null) {
            float ageFrac = tickCount / (float)(EntitySuperNova.DURATION);
            float scale = (float) Math.pow(ageFrac, 0.5) * 5f;
            setBoundingBox(getBoundingBox().inflate(scale));
            setPos(xo, yo, zo);
            List<Entity> hitList = getEntitiesNearbyCube(Entity.class, scale);

            Holder<DamageType> heliomancyDamageTypeHolder = level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.HELIOMANCY);
            DamageSource damageSourceHeliomancy = new DamageSource(heliomancyDamageTypeHolder, this, getCaster());

            for (Entity entity : hitList) {
            	if (getCaster() == entity) continue;
                if (entity instanceof ItemEntity) continue;
                if (getCaster() instanceof EntityUmvuthi && entity instanceof LeaderSunstrikeImmune) continue;
                if (entity instanceof LivingEntity livingEntity) {
                    if (getCaster().canAttack(livingEntity)) {
                        float damageHeliomancy = 4f;
                        float damageMob = 4f;
                        if (getCaster() instanceof EntityUmvuthi) {
                            damageHeliomancy *= ConfigHandler.COMMON.MOBS.UMVUTHI.combatConfig.attackMultiplier.get();
                            damageMob *= ConfigHandler.COMMON.MOBS.UMVUTHI.combatConfig.attackMultiplier.get();
                        }
                        if (getCaster() instanceof Player) {
                            damageHeliomancy *= ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SUNS_BLESSING.sunsBlessingAttackMultiplier.get() * 0.8;
                            damageMob *= ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SUNS_BLESSING.sunsBlessingAttackMultiplier.get() * 0.8;
                        }

                        boolean hitWithLight = DamageUtil.dealMixedDamage(livingEntity, damageSourceHeliomancy, damageHeliomancy, damageSources().mobProjectile(this, getCaster()), damageMob).getRight();
                        if (hitWithLight) {
                            Vec3 diff = livingEntity.position().subtract(position());
                            diff = diff.normalize();
                            livingEntity.knockback(0.4f, -diff.x, -diff.z);
                            livingEntity.setSecondsOnFire(5);
                        }
                    }
                }
                else {
                    entity.hurt(damageSources().mobProjectile(this, getCaster()), 4);
                }
            }
        }
        if (tickCount > DURATION) discard() ;
    }
    
    @Override
    public float getLightLevelDependentMagicValue() {
        return 15728880;
    }
}
