package com.bobmowzie.mowziesmobs.server.entity.bluff;

import com.bobmowzie.mowziesmobs.client.particle.AdvancedTerrainParticle;
import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleBase;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.client.render.entity.player.GeckoPlayer;
import com.bobmowzie.mowziesmobs.server.ability.Ability;
import com.bobmowzie.mowziesmobs.server.ability.AbilitySection;
import com.bobmowzie.mowziesmobs.server.ability.AbilityType;
import com.bobmowzie.mowziesmobs.server.ability.abilities.mob.DieAbility;
import com.bobmowzie.mowziesmobs.server.ability.abilities.mob.HurtAbility;
import com.bobmowzie.mowziesmobs.server.ai.UseAbilityAI;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.MowzieEntity;
import com.bobmowzie.mowziesmobs.server.entity.MowzieGeckoEntity;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityFissure;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityFissurePiece;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityPillar;
import com.bobmowzie.mowziesmobs.server.entity.sculptor.EntitySculptor;
import com.bobmowzie.mowziesmobs.server.loot.LootTableHandler;
import com.bobmowzie.mowziesmobs.server.potion.EffectGeomancy;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.EnumSet;
import java.util.List;

public class EntityBluff extends MowzieGeckoEntity {
    private float allowedHeightOffset = 0.5F;
    private int nextHeightOffsetChangeTick;

    @OnlyIn(Dist.CLIENT)
    public Vec3[] feetPos;
    @OnlyIn(Dist.CLIENT)
    public Vec3[] corePos;

    private GroundPathNavigation groundNav;
    private MoveControl groundMoveControl;
    private FlyingPathNavigation flyingNav;
    private FlyingMoveControl flyingMoveControl;

    // -- ABILITIES -- //
    public static final AbilityType<EntityBluff, HurtAbility<EntityBluff>> HURT_ABILITY = new AbilityType<>("bluff_hurt", (type, entity) -> new HurtAbility<>(type, entity, RawAnimation.begin().thenPlay("hurt"), 7, 0));
    public static final AbilityType<EntityBluff, DieAbility<EntityBluff>> DIE_ABILITY = new AbilityType<>("bluff_die", (type, entity) -> new DieAbility<>(type, entity, RawAnimation.begin().thenPlay("death"), 50));
    public static final AbilityType<EntityBluff, BluffAttackAbility> ATTACK_ABILITY = new AbilityType<>("bluff_attack", BluffAttackAbility::new);

    public EntityBluff(EntityType<? extends MowzieEntity> type, Level world) {
        super(type, world);
        this.xpReward = 14;

        groundMoveControl = this.moveControl;
        flyingMoveControl = new FlyingMoveControl(this, 10, true);

        if (world.isClientSide) {
            feetPos = new Vec3[]{new Vec3(0, 0, 0)};
            corePos = new Vec3[]{new Vec3(0, 0, 0)};
        }
    }

    @Override
    public AbilityType getHurtAbility() {
        return HURT_ABILITY;
    }

    @Override
    public AbilityType getDeathAbility() {
        return DIE_ABILITY;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_21239_) {
        return MMSounds.ENTITY_BLUFF_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        playSound(MMSounds.ENTITY_BLUFF_DEATH.get(), 1, 1.1f);
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return MMSounds.ENTITY_BLUFF_IDLE.get();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new UseAbilityAI<>(this, ATTACK_ABILITY));
        this.goalSelector.addGoal(1, new UseAbilityAI<>(this, DIE_ABILITY));
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, HURT_ABILITY, false));
        this.goalSelector.addGoal(3, new BluffNoPathGoal(this));
        this.goalSelector.addGoal(4, new BluffAttackGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this){
            @Override
            protected boolean canAttack(@Nullable LivingEntity entity, TargetingConditions conditions) {
                return !(entity instanceof EntityBluff) && super.canAttack(entity, conditions);
            }
        }.setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, EntitySculptor.class, true));
    }

    @Override
    protected <E extends GeoEntity> void loopingAnimations(AnimationState<E> event) {
        super.loopingAnimations(event);
        event.getController().transitionLength(5);
    }

    @Override
    public AbilityType<?, ?>[] getAbilities() {
        return new AbilityType[] {HURT_ABILITY, DIE_ABILITY, ATTACK_ABILITY};
    }

    public static AttributeSupplier.Builder createAttributes() {
        return MowzieEntity.createAttributes().add(Attributes.ATTACK_DAMAGE, 18)
                .add(Attributes.MAX_HEALTH, 45)
                .add(Attributes.MOVEMENT_SPEED, 0.23f)
                .add(Attributes.FLYING_SPEED, 0.23f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1f)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        groundNav = new GroundPathNavigation(this, level) {
            @Override
            protected boolean canUpdatePath() {
                return super.canUpdatePath() || navigation == flyingNav;
            }
        };
        flyingNav = new FlyingPathNavigation(this, level);
        return groundNav;
    }

    public void setFlying(boolean flying) {
        if (flying) {
            moveControl = flyingMoveControl;
            navigation = flyingNav;
            setNoGravity(true);
        }
        else {
            moveControl = groundMoveControl;
            navigation = groundNav;
            setNoGravity(false);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.level().getDifficulty() == Difficulty.PEACEFUL)
        {
            this.discard() ;
        }

        if (getActiveAbilityType() == DIE_ABILITY && getActiveAbility().getTicksInUse() < 14) {
            this.yBodyRot = this.yHeadRot = this.yRotO;
            if (level().isClientSide) {
                for (int i = 0; i < 4; i++) {
                    if (random.nextFloat() < 0.1f) {
                        AdvancedParticleBase.spawnParticle(level(), ParticleHandler.PIXEL.get(), getRandomX(0.4f), getY() + 1f, getRandomZ(0.4f), 0f, random.nextFloat() / 15f, 0f, true, 0f, 0, 0f, 0, 1.3 + (random.nextFloat()*1f), 163d / 256d, 247d / 256d, 74d / 256d, 0.5, 0.9, 17 + random.nextFloat() * 10, true, true, new ParticleComponent[]{
                                new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, new ParticleComponent.KeyTrack(
                                        new float[]{1f, 0},
                                        new float[]{0.0f, 1}
                                ), false),
                                new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.MOTION_Y, new ParticleComponent.KeyTrack(
                                        new float[]{0.1f, 0},
                                        new float[]{0.0f, 1}
                                ), false)
                        });
                    }
                }
            }
        }

        if (this.level().isClientSide && isAlive()) {
            if (feetPos != null && feetPos.length > 0) {
                feetPos[0] = position().add(0, 0.05f, 0);
                if (tickCount % 4 == 0) {
                    AdvancedParticleBase.spawnParticle(level(), ParticleHandler.RING2.get(), feetPos[0].x(), feetPos[0].y(), feetPos[0].z(), 0, 0, 0, false, 0, Math.PI/2f, 0, 0, 1.5F, 0.83f, 1, 0.39f, 1, 1, 20, true, false, new ParticleComponent[]{
                            new ParticleComponent.PinLocation(feetPos),
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, ParticleComponent.KeyTrack.startAndEnd(1f, 0f), false),
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, ParticleComponent.KeyTrack.startAndEnd(1f, 7f), false),
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.POS_Y, ParticleComponent.KeyTrack.startAndEnd(0f, 1.5f), true)
                    });
                }
            }
            BlockState state = level().getBlockState(getOnPos());
            if (EffectGeomancy.isBlockUseable(state) && feetPos != null && feetPos.length > 0) {
                if (tickCount % 2 == 0) {
                    Vec3 pos = new Vec3(1, 0, 0).yRot((float) (random.nextDouble() * Math.PI * 2.0)).scale(random.nextFloat());
                    float phaseOffset = random.nextFloat();
                    float scale = (float)random.nextGaussian() * 0.2f + 0.3f;
                    AdvancedTerrainParticle.spawnTerrainParticle(level(), ParticleHandler.TERRAIN.get(), getX() + pos.x(), getY() + pos.y() + 1, getZ() + pos.z(), 0, 0 ,0, 0, 1f, 1f, 25 + random.nextFloat() * 5, state, new ParticleComponent[]{
                            new ParticleComponent.Orbit(feetPos, ParticleComponent.KeyTrack.startAndEnd(0 + phaseOffset, 0.8f + phaseOffset), ParticleComponent.KeyTrack.startAndEnd(random.nextFloat() * 0.75f, 0.1f + random.nextFloat()), ParticleComponent.constant(0), ParticleComponent.constant(1), ParticleComponent.constant(0), false),
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.POS_Y, ParticleComponent.KeyTrack.startAndEnd(0f, 1.1f), true),
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.KeyTrack(
                                    new float[]{0, scale, scale, 0},
                                    new float[]{0, 0.1f, 0.9f, 1}
                            ), false)
                    });
                }
            }
        }

//        if (getActiveAbility() == null && tickCount % 120 == 0){
//            sendAbilityMessage(ATTACK_ABILITY);
//        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source == damageSources().fall()) return false;
        return super.hurt(source, damage);
    }

    public void aiStep() {
        if (!this.onGround() && this.getDeltaMovement().y < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }

        super.aiStep();
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return LootTableHandler.BLUFF;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason) {
        return super.checkSpawnRules(world, reason) && getEntitiesNearby(EntityPillar.EntityPillarSculptor.class, 8,  8, 8, 8).isEmpty() && getEntitiesNearby(EntitySculptor.class, 8,  8, 8, 8).isEmpty() && world.getDifficulty() != Difficulty.PEACEFUL;
    }

    protected void checkFallDamage(double p_29370_, boolean p_29371_, BlockState p_29372_, BlockPos p_29373_) {
    }

    public static class BluffAttackAbility extends Ability<EntityBluff> {
        private static int STARTUP_DURATION = 9;

        private Vec3 prevTargetPos;

        public static AbilitySection[] SECTION_TRACK = new AbilitySection[] {
            new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 11),
            new AbilitySection.AbilitySectionInfinite(AbilitySection.AbilitySectionType.MISC),
            new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
            new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, 34)
        };

        public BluffAttackAbility(AbilityType abilityType, EntityBluff user) {
            super(abilityType, user, SECTION_TRACK);
        }

        private static final RawAnimation ATTACK_START_ANIMATION = RawAnimation.begin().then("attack_start", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private static final RawAnimation ATTACK_END_ANIMATION = RawAnimation.begin().then("attack_end", Animation.LoopType.HOLD_ON_LAST_FRAME);

        @Override
        public void start() {
            super.start();
            getUser().setFlying(false);
            LivingEntity entityTarget = getUser().getTarget();
            if (entityTarget != null) {
                prevTargetPos = entityTarget.position().add(0, entityTarget.getBbHeight() / 2.0, 0);
            }
            playAnimation(ATTACK_START_ANIMATION);
            getUser().playSound(MMSounds.ENTITY_BLUFF_ATTACK.get(), 1, 1.2f);
        }

        @Override
        public void tickUsing() {
            super.tickUsing();

            LivingEntity entityTarget = getUser().getTarget();
            if (entityTarget != null) {
                getUser().getLookControl().setLookAt(entityTarget, 30, 30);
                getUser().setYRot(getUser().getYHeadRot());
            }
            if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.STARTUP) {
                getUser().setDeltaMovement(0, 0, 0);
            }
            if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.MISC || getCurrentSection().sectionType == AbilitySection.AbilitySectionType.ACTIVE) {
                double fallSpeed = getUser().getDeltaMovement().y;
                fallSpeed -= 2;
                fallSpeed = Math.max(fallSpeed, -7);
                getUser().setDeltaMovement(0, fallSpeed, 0);
                getUser().hasImpulse = true;
            }
            if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.MISC) {
                if (getUser().onGround()) {
                    jumpToSection(2);
                }
            }
        }

        @Override
        public <E extends GeoEntity> PlayState animationPredicate(AnimationState<E> e, GeckoPlayer.Perspective perspective) {
            if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.STARTUP) {
                e.getController().transitionLength(4);
            }
            return super.animationPredicate(e, perspective);
        }

        @Override
        protected void beginSection(AbilitySection section) {
            super.beginSection(section);
            if (section.sectionType == AbilitySection.AbilitySectionType.ACTIVE) {
                getUser().playSound(MMSounds.EFFECT_GEOMANCY_HIT_MEDIUM.get(0).get(), 1, 1.2f);
                if (!getLevel().isClientSide()) {
                    shootFissureAtTarget(getUser().getTarget(), prevTargetPos, 0.1f);

                    List<Entity> entitiesHit = getLevel().getEntities(getUser(), getUser().getBoundingBox().inflate(0.4), e -> e != getUser());
                    double damage = 10;
                    AttributeInstance attrib = getUser().getAttribute(Attributes.ATTACK_DAMAGE);
                    if (attrib != null) {
                        damage = attrib.getValue();
                    }
                    damage = damage * ConfigHandler.COMMON.MOBS.BLUFF.combatConfig.attackMultiplier.get();
                    for (Entity entity : entitiesHit) {
                        if (entity instanceof EntityBluff) continue;
                        entity.hurt(getUser().damageSources().mobAttack(getUser()), (float) damage);
                    }
                }

                playAnimation(ATTACK_END_ANIMATION);
                if (getLevel().isClientSide()) {
                    BlockState blockBeneath = getUser().level().getBlockState(getUser().getOnPos());
                    for (byte i = 0; i < 30; i++) {
                        getLevel().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockBeneath), getUser().getX(), getUser().getBlockY() + 0.1f, getUser().getZ(), getUser().random.nextFloat() * 3f - 1.5f, 2.2d, getUser().random.nextFloat() * 3f - 1.5f);
                    }
                }
            }
        }

        public void shootFissureAtTarget(LivingEntity target, Vec3 prevTargetPos, float timeScale) {
            EntityFissure fissure = new EntityFissure(EntityHandler.FISSURE.get(), getLevel());
            fissure.setOwner(getUser());
            fissure.setPos(getUser().position());

            Vec3 shootVec;
            if (target != null) {
                float speed = EntityFissurePiece.PIECE_SIZE / (float) EntityFissure.TICKS_PER_PIECE;
                Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
                double timeToReach = fissure.position().subtract(targetPos).length() / speed;
                Vec3 targetMovement = targetPos.subtract(prevTargetPos).scale(timeToReach * timeScale * 1.0 / 4.0);
                targetMovement = targetMovement.multiply(1, 0, 1);
                Vec3 futureTargetPos = targetPos.add(targetMovement);
                Vec3 projectileMid = fissure.position().add(0, fissure.getBbHeight() / 2.0, 0);
                shootVec = futureTargetPos.subtract(projectileMid).normalize();
            }
            else {
                shootVec = getUser().getForward();
            }

            fissure.shoot(shootVec.x, shootVec.z);
            getLevel().addFreshEntity(fissure);
        }
    }

    public static class BluffNoPathGoal extends Goal {
        private final EntityBluff bluff;

        public BluffNoPathGoal(EntityBluff bluff) {
            this.bluff = bluff;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.bluff.getTarget();
            if (target == null) return false;
            if (!bluff.onGround()) return false;

            Path path = bluff.groundNav.createPath(target, 0);
            return path == null || path.getEndNode() == null || path.getEndNode().asVec3().add(0.5, 0.5, 0.5).distanceToSqr(path.getTarget().getCenter()) > 4;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.bluff.getTarget();
            if (target == null) return false;

            Path path = bluff.groundNav.createPath(target, 0);
            return path == null || path.getEndNode() == null || path.getEndNode().asVec3().add(0.5, 0.5, 0.5).distanceToSqr(path.getTarget().getCenter()) > 4;
        }

        @Override
        public void start() {
            super.start();
            bluff.setFlying(true);
        }

        @Override
        public void tick() {
            super.tick();
            LivingEntity target = this.bluff.getTarget();
            if (target != null) {
                this.bluff.getNavigation().moveTo(target, 1.2);
                this.bluff.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }

        @Override
        public void stop() {
            super.stop();
            this.bluff.getNavigation().stop();
            bluff.setFlying(false);
        }
    }

    static class BluffAttackGoal extends Goal {
        private final EntityBluff bluff;
        private final double speedModifier = 1.0;
        private int attackIntervalMin = 50;
        private final float attackMaxRadiusSqr = 12 * 12;
        private final float attackMinRadiusSqr = 6 * 6;
        private int attackTime = -1;
        private int seeTime;
        private boolean strafingClockwise;
        private boolean strafingBackwards;
        private boolean isStrafing;
        private int strafingTime = -1;

        public BluffAttackGoal(EntityBluff bluff) {
            this.bluff = bluff;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            LivingEntity livingentity = this.bluff.getTarget();
            return livingentity != null && livingentity.isAlive() && this.bluff.canAttack(livingentity);
        }

        public void start() {
            super.start();
            this.bluff.setAggressive(true);
        }

        public void stop() {
            super.stop();
            this.bluff.setAggressive(false);
            this.seeTime = 0;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity livingentity = this.bluff.getTarget();
            if (livingentity != null) {
                double d0 = this.bluff.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                boolean flag = this.bluff.getSensing().hasLineOfSight(livingentity);
                boolean flag1 = this.seeTime > 0;
                if (flag != flag1) {
                    this.seeTime = 0;
                }

                if (flag) {
                    ++this.seeTime;
                } else {
                    --this.seeTime;
                }

                if (!(d0 > (double)this.attackMaxRadiusSqr) && this.seeTime >= 20) {
                    this.bluff.getNavigation().stop();
                    ++this.strafingTime;
                } else {
                    this.bluff.getNavigation().moveTo(livingentity, this.speedModifier);
                    this.strafingTime = -1;
                }

                if (this.strafingTime >= 20) {
                    if ((double)this.bluff.getRandom().nextFloat() < 0.3D) {
                        this.isStrafing = !this.isStrafing;
                    }

                    if ((double)this.bluff.getRandom().nextFloat() < 0.3D) {
                        this.strafingClockwise = !this.strafingClockwise;
                    }

                    if ((double)this.bluff.getRandom().nextFloat() < 0.3D) {
                        this.strafingBackwards = !this.strafingBackwards;
                    }

                    this.strafingTime = 0;
                }

                if (this.strafingTime > -1) {
                    if (d0 > (double)(this.attackMaxRadiusSqr)) {
                        this.strafingBackwards = false;
                    } else if (d0 < (double)(this.attackMinRadiusSqr)) {
                        this.strafingBackwards = true;
                    }

                    if (isStrafing) {
                        this.bluff.getMoveControl().strafe(this.strafingBackwards ? -0.3F : 0.3F, this.strafingClockwise ? 0.3F : -0.3F);
                    }
                    else {
                        this.bluff.getMoveControl().strafe(0, 0);
                    }
                    this.bluff.lookAt(livingentity, 30.0F, 30.0F);
                }
                this.bluff.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);

                if (--this.attackTime <= 0 && this.seeTime >= -60 && d0 < attackMaxRadiusSqr) {
                    bluff.sendAbilityMessage(ATTACK_ABILITY);
                    this.attackTime = attackIntervalMin + bluff.random.nextInt(40);
                }
            }
        }
    }
}


