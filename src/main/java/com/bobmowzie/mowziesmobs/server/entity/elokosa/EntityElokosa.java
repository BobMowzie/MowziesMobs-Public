package com.bobmowzie.mowziesmobs.server.entity.elokosa;

import com.bobmowzie.mowziesmobs.client.model.tools.dynamics.GeckoDynamicChain;
import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieAnimationController;
import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleBase;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.server.ability.Ability;
import com.bobmowzie.mowziesmobs.server.ability.AbilityHandler;
import com.bobmowzie.mowziesmobs.server.ability.AbilitySection;
import com.bobmowzie.mowziesmobs.server.ability.AbilityType;
import com.bobmowzie.mowziesmobs.server.ability.abilities.mob.HurtAbility;
import com.bobmowzie.mowziesmobs.server.ability.abilities.mob.MeleeAttackAbility;
import com.bobmowzie.mowziesmobs.server.ability.abilities.player.SimpleAnimationAbility;
import com.bobmowzie.mowziesmobs.server.ai.Cooldown;
import com.bobmowzie.mowziesmobs.server.ai.ElokosaLeapGoal;
import com.bobmowzie.mowziesmobs.server.ai.MMAIAvoidEntity;
import com.bobmowzie.mowziesmobs.server.ai.UseAbilityAI;
import com.bobmowzie.mowziesmobs.server.capability.AbilityData;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.MowzieEntity;
import com.bobmowzie.mowziesmobs.server.entity.MowzieGeckoEntity;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntityBlockSwapper;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntityCameraShake;
import com.bobmowzie.mowziesmobs.server.entity.foliaath.EntityFoliaath;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthana;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthi;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import com.bobmowzie.mowziesmobs.server.loot.LootTableHandler;
import com.bobmowzie.mowziesmobs.server.potion.EffectHandler;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import com.bobmowzie.mowziesmobs.server.util.MowzieLongJumpUtil;
import com.google.common.collect.Lists;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public abstract class EntityElokosa extends MowzieGeckoEntity implements Enemy {
    public static final EntityDimensions LONG_JUMP_DIMENSIONS = EntityDimensions.scalable(0.7F, 0.7F);
    public static final EntityDimensions DAY_FORM_DIMENSIONS = EntityDimensions.scalable(0.7F, 0.7F);

    protected static final int SCREAM_DURATION = 56;
    protected static final int SCREAM_CANCEL_TICK = 37;

    private static final RawAnimation HURT_GROUND_ANIMATION = RawAnimation.begin().then("hurt_ground", Animation.LoopType.PLAY_ONCE);
    public static final AbilityType<EntityElokosa, HurtAbility<EntityElokosa>> HURT_ABILITY = new AbilityType<>("elokosa_hurt", (type, entity) -> new HurtAbility<>(type, entity, HURT_GROUND_ANIMATION, 12, 0) {
        private static final RawAnimation HURT_WALL_ANIMATION = RawAnimation.begin().then("hurt_wall", Animation.LoopType.PLAY_ONCE);
        private static final RawAnimation HURT_CEILING_ANIMATION = RawAnimation.begin().then("hurt_ceiling", Animation.LoopType.PLAY_ONCE);

        @Override
        public RawAnimation getAnimation() {
            if (getUser().getClingDirection().getAxis().isHorizontal()) {
                return HURT_WALL_ANIMATION;
            }
            else if (getUser().getClingDirection() == Direction.UP) {
                return HURT_CEILING_ANIMATION;
            }
            return super.getAnimation();
        }
    });
    public static final AbilityType<EntityElokosa, ElokosaDieAbility> DIE_ABILITY = new AbilityType<>("elokosa_die", ElokosaDieAbility::new);
    public static final AbilityType<EntityElokosa, EntityElokosa.ElokosaLeapAbility> LEAP_ABILITY = new AbilityType<>("elokosa_leap", EntityElokosa.ElokosaLeapAbility::new);
    public static final AbilityType<EntityElokosa, EntityElokosa.ElokosaLeapToTargetAbility> LEAP_TO_TARGET_ABILITY = new AbilityType<>("elokosa_leap_to_target", EntityElokosa.ElokosaLeapToTargetAbility::new);
    public static final AbilityType<EntityElokosa, EntityElokosa.ElokosaDayToNightAbility> DAY_TO_NIGHT_ABILITY = new AbilityType<>("elokosa_day_to_night", EntityElokosa.ElokosaDayToNightAbility::new);
    public static final AbilityType<EntityElokosa, EntityElokosa.ElokosaNightToDayAbility> NIGHT_TO_DAY_ABILITY = new AbilityType<>("elokosa_night_to_day", EntityElokosa.ElokosaNightToDayAbility::new);
    public static final AbilityType<EntityElokosa, SimpleAnimationAbility<EntityElokosa>> SCREAM_ABILITY = new AbilityType<>("elokosa_scream", (type, entity) -> new SimpleAnimationAbility<>(type, entity, RawAnimation.begin().thenPlayAndHold("scream_ground"), SCREAM_DURATION, false) {
        @Override
        public void start() {
            super.start();
            getUser().getNavigation().stop();
            getUser().playSound(MMSounds.ENTITY_ELOKOSA_NIGHT_SCREAM_LONG.get(), 3f, 0.95f + getUser().random.nextFloat() * 0.1f);
            EntityCameraShake.cameraShake(getUser().level(), getUser().position(), 20, 0.02f, SCREAM_DURATION - 10, 20);
            List<EntityElokosa> entitiesNearby = getUser().getEntitiesNearby(EntityElokosa.class, 45);
            for (EntityElokosa elokosa : entitiesNearby) {
                if (elokosa.getTarget() == getUser().getTarget()) {
                    elokosa.endStalkingSoon(6);
                }
            }
            getUser().screamTimer = SCREAM_DURATION;
        }

        private static final RawAnimation SCREAM_GROUND_ANIMATION = RawAnimation.begin().then("scream_ground", Animation.LoopType.PLAY_ONCE);
        private static final RawAnimation SCREAM_WALL_ANIMATION = RawAnimation.begin().then("scream_wall", Animation.LoopType.PLAY_ONCE);
        private static final RawAnimation SCREAM_CEILING_ANIMATION = RawAnimation.begin().then("scream_ceiling", Animation.LoopType.PLAY_ONCE);

        @Override
        public RawAnimation getAnimation() {
            if (getUser().getClingDirection().getAxis().isHorizontal()) {
                return SCREAM_WALL_ANIMATION;
            }
            else if (getUser().getClingDirection() == Direction.UP) {
                return SCREAM_CEILING_ANIMATION;
            }
            return super.getAnimation();
        }

        @Override
        public void tickUsing() {
            super.tickUsing();
            if (getUser().getTarget() != null) {
                getUser().getLookControl().setLookAt(getUser().getTarget());
            }
            getUser().getNavigation().stop();
        }
    });
    public static final AbilityType<EntityElokosa, EntityElokosa.ElokosaAttackComboAbility> ATTACK_COMBO_ABILITY = new AbilityType<>("elokosa_attack_combo", EntityElokosa.ElokosaAttackComboAbility::new);
    public static final AbilityType<EntityElokosa, EntityElokosa.ElokosaAttackComboRoarAbility> ATTACK_COMBO_ROAR_ABILITY = new AbilityType<>("elokosa_attack_combo_roar", EntityElokosa.ElokosaAttackComboRoarAbility::new);
    public static final AbilityType<EntityElokosa, EntityElokosa.ElokosaScytheAttackAbility> SCYTHE_ATTACK_ABILITY = new AbilityType<>("elokosa_scythe_attack", EntityElokosa.ElokosaScytheAttackAbility::new);
    public static final AbilityType<EntityElokosa, EntityElokosa.ElokosaBackflipAbility> BACKFLIP_ABILITY = new AbilityType<>("elokosa_backflip", EntityElokosa.ElokosaBackflipAbility::new);
    public static final AbilityType<EntityElokosa, EntityElokosa.ElokosaLeapAttackAbility> LEAP_ATTACK_ABILITY = new AbilityType<>("elokosa_leap_attack", EntityElokosa.ElokosaLeapAttackAbility::new);
    public static final AbilityType<EntityElokosa, EntityElokosa.ElokosaAttackAbility> ATTACK_ABILITY = new AbilityType<>("elokosa_attack", EntityElokosa.ElokosaAttackAbility::new);
    public static final AbilityType<EntityElokosa, EntityElokosa.ElokosaSitAbility> SIT_ABILITY = new AbilityType<>("elokosa_sit", EntityElokosa.ElokosaSitAbility::new);

    private static final EntityDataAccessor<Integer> CLING_DIRECTION = SynchedEntityData.defineId(EntityElokosa.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> NIGHT_FORM = SynchedEntityData.defineId(EntityElokosa.class, EntityDataSerializers.BOOLEAN);

    public Vec3 jumpVel;
    public Vec3 jumpGoalPos;

    private boolean failedFleePath = false;

    private static final int STALKING_TIMER_MAX = 700;
    private static final int TIME_UNTIL_STALK_AGAIN = 300;
    private static final int LOOKING_AT_ME_TIMER_MAX = 16;
    private int stalkingTimer = STALKING_TIMER_MAX;
    private int lookingAtMeTimer = LOOKING_AT_ME_TIMER_MAX;
    private boolean isStalking = true;

    private boolean circleDirection = true;

    protected float damageThreat = 0;
    protected float damageThreatDecayRate = 0.01f;
    protected float damageThreatPerDamage = 0.05f;

    protected int screamTimer = 0;

    private MMAIAvoidEntity<EntityElokosa, LivingEntity> fleeGoal;

    protected MowzieAnimationController<MowzieGeckoEntity> walkRunController = new MowzieAnimationController<>(this, "walk_run_controller", 4, this::predicateWalkRun, 0);
    protected MowzieAnimationController<MowzieGeckoEntity> jawScreamController = new MowzieAnimationController<>(this, "jaw_scream_controller", 0, this::predicateJawScream, 0);

    private static final ResourceLocation DAY_SPEED_MODIFIER_ID = ResourceLocation.withDefaultNamespace("day_form_speed");
    private static final AttributeModifier SPEED_MODIFIER_DAY = new AttributeModifier(
            DAY_SPEED_MODIFIER_ID, -0.03F, AttributeModifier.Operation.ADD_VALUE
    );
    private static final ResourceLocation DAY_HEALTH_MODIFIER_ID = ResourceLocation.withDefaultNamespace("day_form_health");
    private static final AttributeModifier HEALTH_MODIFIER_DAY = new AttributeModifier(
            DAY_HEALTH_MODIFIER_ID, -14F, AttributeModifier.Operation.ADD_VALUE
    );

    public GeckoDynamicChain tailChain;

    public boolean missingWallClingUpperBlock;

    public Cooldown dodgeCooldown = new Cooldown(this, 100, 180, "dodge_cooldown");
    public Cooldown attackCooldown = new Cooldown(this, 100, 180, "attack_cooldown");
    public Cooldown sitCooldown = new Cooldown(this, 100, 880, "sit_cooldown");
    private double knockBackAngle = -1;

    public Vec3[] droolPos1;
    public Vec3[] droolPos2;
    public Vec3[] droolPos3;
    public Vec3[] droolPos4;
    public Vec3[] droolPos5;
    public Vec3[] droolPos6;
    public List<Vec3[]> droolPositions = new ArrayList<>();

    protected boolean addedHuntingTargetGoals = false;

    protected boolean preventTransform = false;

    public EntityElokosa(EntityType<? extends MowzieEntity> type, Level world) {
        super(type, world);
        this.xpReward = 7;

        if (world.isClientSide) {
            tailChain = new GeckoDynamicChain(this);
            tailChain.setDoAttract(true);
            tailChain.setAttractStrength(2f);
            tailChain.setAttractFalloff(0.25f);
            tailChain.setChainDirection(new Vector3d(0, 0, 1));
            tailChain.setDamping(0.04f);
            dynamicChains = new GeckoDynamicChain[] {
                    tailChain
            };
            droolPos1 = new Vec3[]{new Vec3(0, 0, 0)};
            droolPos2 = new Vec3[]{new Vec3(0, 0, 0)};
            droolPos3 = new Vec3[]{new Vec3(0, 0, 0)};
            droolPos4 = new Vec3[]{new Vec3(0, 0, 0)};
            droolPos5 = new Vec3[]{new Vec3(0, 0, 0)};
            droolPos6 = new Vec3[]{new Vec3(0, 0, 0)};
            droolPositions.add(droolPos1);
            droolPositions.add(droolPos2);
            droolPositions.add(droolPos3);
            droolPositions.add(droolPos4);
            droolPositions.add(droolPos5);
            droolPositions.add(droolPos6);
        }

        cooldowns = new Cooldown[] {
                attackCooldown,
                dodgeCooldown
        };
        sitCooldown.startCooldown(0, 780);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WanderLeapGoal(this));
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, SCREAM_ABILITY, true, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && getActiveAbility().getTicksInUse() < 40;
            }
        });
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, ATTACK_COMBO_ABILITY, true));
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, ATTACK_COMBO_ROAR_ABILITY, true));
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, LEAP_ATTACK_ABILITY, true));
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, SCYTHE_ATTACK_ABILITY, true));
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, ATTACK_ABILITY, true));
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, BACKFLIP_ABILITY, true));
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, DAY_TO_NIGHT_ABILITY, true));
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, NIGHT_TO_DAY_ABILITY, true));
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, SIT_ABILITY, false));
        this.goalSelector.addGoal(1, new UseAbilityAI<>(this, DIE_ABILITY));
        this.goalSelector.addGoal(2, new UseAbilityAI<>(this, HURT_ABILITY, false));
        this.goalSelector.addGoal(4, new CombatBehaviorGoal(this));
        this.goalSelector.addGoal(3, new StalkingLeapGoal(this));
        this.goalSelector.addGoal(3, new FleeingLeapGoal(this));
        this.goalSelector.addGoal(3, new CombatLeapGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new MMAIAvoidEntity<>(this, EntityFoliaath.class, 8f, 2, 2.8));
        fleeGoal = new MMAIAvoidEntity<>(this, LivingEntity.class,
                (e) -> !(e instanceof EntityElokosa) && (e.getAttribute(Attributes.ATTACK_DAMAGE) != null && e.getAttributeValue(Attributes.ATTACK_DAMAGE) > 0.0D),
                19f, 2, 2.8, 4, 12, 7, 12.0) {
            private int fleeCheckCounter = 0;

            @Override
            public void start() {
                super.start();
                failedFleePath = false;
            }

            @Override
            protected void onSafe() {
                fleeCheckCounter = 0;
            }

            @Override
            protected void onPathNotFound() {
                failedFleePath = true;
                if (fleeCheckCounter < 4) {
                    fleeCheckCounter++;
                }
            }

            @Override
            public boolean canUse() {
                return !getNightForm() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !getNightForm() && super.canContinueToUse();
            }
        };
        goalSelector.addGoal(5, fleeGoal);
        goalSelector.addGoal(3, new MMAIAvoidEntity<>(this, LivingEntity.class, (e) -> e == getTarget(), 6f, 1, 2, 3, 10, 7) {
            @Override
            public boolean canUse() {
                return isStalking && stalkingTimer > 0 && super.canUse();
            }
        });
        goalSelector.addGoal(2, new ChangeFormGoal(this));
        goalSelector.addGoal(6, new ElokosaSitGoal(this));

        registerTargetGoals();
    }

    protected void registerTargetGoals() {
    }

    protected void registerHuntingTargetGoals() {
        addedHuntingTargetGoals = true;
        this.targetSelector.addGoal(4, new ElokosaAttackTargetGoal(this, Player.class, false));
        this.targetSelector.addGoal(4, new ElokosaAttackTargetGoal(this, EntityUmvuthana.class, false));
        this.targetSelector.addGoal(4, new ElokosaAttackTargetGoal(this, EntityUmvuthi.class, false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLING_DIRECTION, Direction.DOWN.get3DDataValue());
        builder.define(NIGHT_FORM, false);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        super.registerControllers(controllers);
        controllers.add(walkRunController);
        controllers.add(jawScreamController);
    }

    @Override
    public AbilityType getHurtAbility() {
        return HURT_ABILITY;
    }

    @Override
    public AbilityType getDeathAbility() {
        return DIE_ABILITY;
    }

    private static RawAnimation RUN_SWITCH_ANIM = RawAnimation.begin().thenLoop("run_switch");
    private static RawAnimation WALK_SWITCH_ANIM = RawAnimation.begin().thenLoop("walk_switch");
    protected <E extends GeoEntity> PlayState predicateWalkRun(AnimationState<E> event)
    {
        float threshold = getNightForm() ? 0.9f : 0.7f;
        AnimationProcessor.QueuedAnimation currentAnim = event.getController().getCurrentAnimation();
        if (currentAnim != null && currentAnim.animation().name().equals("run_switch")) {
            threshold = threshold * 0.777f;
        }

        if (event.getLimbSwingAmount() > threshold && !isStrafing()) {
            event.getController().setAnimation(RUN_SWITCH_ANIM);
        }
        else {
            event.getController().setAnimation(WALK_SWITCH_ANIM);
        }
        return PlayState.CONTINUE;
    }

    private static RawAnimation SCREAM_JAW_ANIM = RawAnimation.begin().thenPlay("scream_jaw_only");
    private PlayState predicateJawScream(AnimationState<MowzieGeckoEntity> event) {
        if (screamTimer > 0) {
            event.getController().setAnimation(SCREAM_JAW_ANIM);
            return PlayState.CONTINUE;
        }
        else {
            return PlayState.STOP;
        }
    }

    private static final RawAnimation IDLE_WALL_ANIMATION = RawAnimation.begin().then("idle_wall", Animation.LoopType.LOOP);
    private static final RawAnimation IDLE_CEILING_ANIMATION = RawAnimation.begin().then("idle_ceiling", Animation.LoopType.LOOP);
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");

    @Override
    protected <E extends GeoEntity> void loopingAnimations(AnimationState<E> event) {
        event.getController().transitionLength(4);
        if (getClingDirection() != Direction.DOWN && getClingDirection() != Direction.UP) {
            event.getController().setAnimation(IDLE_WALL_ANIMATION);
        }
        else if (getClingDirection() == Direction.UP) {
            event.getController().setAnimation(IDLE_CEILING_ANIMATION);
        }
        else {
            if (event.isMoving()) {
                event.getController().setAnimation(WALK_ANIM);
            }
            else {
                super.loopingAnimations(event);
            }
        }
    }

    @Override
    public AbilityType<?, ?>[] getAbilities() {
        return new AbilityType[] {HURT_ABILITY, DIE_ABILITY, LEAP_ABILITY, LEAP_TO_TARGET_ABILITY, DAY_TO_NIGHT_ABILITY, NIGHT_TO_DAY_ABILITY, SCREAM_ABILITY, ATTACK_COMBO_ABILITY, ATTACK_COMBO_ROAR_ABILITY, BACKFLIP_ABILITY, SCYTHE_ATTACK_ABILITY, LEAP_ATTACK_ABILITY, ATTACK_ABILITY, SIT_ABILITY };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return MowzieEntity.createAttributes().add(Attributes.ATTACK_DAMAGE, 5)
                .add(Attributes.MAX_HEALTH, 17)
                .add(Attributes.MOVEMENT_SPEED, 0.2f)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.JUMP_STRENGTH, 0.42F)
                .add(Attributes.GRAVITY, 0.095F)
                .add(Attributes.STEP_HEIGHT, 1)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.5);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source == damageSources().fall()) return false;
        damageThreat += damage * damageThreatPerDamage;
        boolean attacked = super.hurt(source, damage);
        return attacked;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (getNightForm() && (isStalking || getActiveAbilityType() == SCREAM_ABILITY)) {
            return null;
        }
        return getNightForm() ? MMSounds.ENTITY_ELOKOSA_NIGHT_IDLE.get(random.nextInt(MMSounds.ENTITY_ELOKOSA_NIGHT_IDLE.size())).get() : MMSounds.ENTITY_ELOKOSA_DAY_IDLE.get(random.nextInt(MMSounds.ENTITY_ELOKOSA_DAY_IDLE.size())).get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return getNightForm() ? MMSounds.ENTITY_ELOKOSA_NIGHT_HURT.get(random.nextInt(MMSounds.ENTITY_ELOKOSA_NIGHT_HURT.size())).get() : MMSounds.ENTITY_ELOKOSA_DAY_HURT.get(random.nextInt(MMSounds.ENTITY_ELOKOSA_DAY_HURT.size())).get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getNightForm() ? MMSounds.ENTITY_ELOKOSA_NIGHT_DEATH.get() : MMSounds.ENTITY_ELOKOSA_DAY_DEATH.get();
    }

    protected void checkFallDamage(double p_29370_, boolean p_29371_, BlockState p_29372_, BlockPos p_29373_) {
    }

    @Override
    protected boolean doDirectionalWalk() {
        return true;
    }

    @Override
    public MoveControl getMoveControl() {
        return super.getMoveControl();
    }

    public void setStalking(boolean stalking) {
        isStalking = stalking;
    }

    @Override
    protected ConfigHandler.SpawnConfig getSpawnConfig() {
        return ConfigHandler.COMMON.MOBS.ELOKOSA.spawnConfig;
    }

    @Override
    protected ConfigHandler.CombatConfig getCombatConfig() {
        return ConfigHandler.COMMON.MOBS.ELOKOSA.combatConfig;
    }

    @Override
    public void tick() {
        if (getClingDirection() != Direction.DOWN) {
            setDeltaMovement(0, 0, 0);
            setPos(xo, yo, zo);
        }

        super.tick();
        screamTimer = Math.max(0, screamTimer - 1);

        if (getClingDirection() != Direction.DOWN) {
            if (getClingDirection() != Direction.UP) {
                setYRot(getClingDirection().getOpposite().toYRot());
                yRotO = getYRot();
                yBodyRot = getYRot();
                yBodyRotO = yBodyRot;
            }
        }

        if (!level().isClientSide()) {
            if (getClingDirection() != Direction.DOWN) {
                if (!canContinueClinging()) {
//                    System.out.println("STOPPED CLINGING DUE TO MISSING BLOCK CONNECTION");
                    stopClinging();
                }
            }

            if (!getNightForm() && fleeGoal != null && !fleeGoal.isActive() && getClingDirection() == Direction.DOWN) {
                sitCooldown.tick();
            }

            // Curse status effect
            boolean isDay = isDayTime(level());
            boolean isNewMoon = level().getMoonPhase() == 4;
            if (!isDay && !isNewMoon) {
                addEffect(new MobEffectInstance(EffectHandler.MOONS_CURSE, 10, 0, false, false));
            }
            else {
                removeEffect(EffectHandler.MOONS_CURSE);
            }
        }
        else {
            if (getClingDirection().getAxis().isHorizontal()) {
                Vec3i clingDirVec = getClingDirection().getNormal();
                BlockState state = level().getBlockState(BlockPos.containing(position().add(0, getBbHeight(), 0)).offset(clingDirVec));
                missingWallClingUpperBlock = !state.isSolid();
            }
            else {
                missingWallClingUpperBlock = false;
            }
        }

        damageThreat -= damageThreatDecayRate;
        damageThreat = Mth.clamp(damageThreat, 0, 1);

        if (getTarget() != null && isStalking) {
            stalkingTimer -= 1;
            // Reduce timer faster if target is looking at the elokosa
            if (isLookingAtMe(getTarget())) {
                lookingAtMeTimer -= 1;
                lookingAtMeTimer = Math.max(lookingAtMeTimer, 0);
            }
            else {
                lookingAtMeTimer += 1;
                lookingAtMeTimer = Math.min(lookingAtMeTimer, LOOKING_AT_ME_TIMER_MAX);
            }
            if (getTarget() instanceof Mob mob && mob.getTarget() == this) {
                stalkingTimer = 0;
            }
            if (lookingAtMeTimer == 0) {
                stalkingTimer = 0;
            }
        }
        else {
            lookingAtMeTimer = LOOKING_AT_ME_TIMER_MAX;
        }

        if (getActiveAbilityType() != null && getActiveAbilityType() != HURT_ABILITY && getActiveAbilityType() != SCREAM_ABILITY) {
            getNavigation().stop();
            yHeadRot = yBodyRot = getYRot();
        }

        // Drool particles
        if (getNightForm() && level().isClientSide()) {
            if (random.nextInt(15) == 0) {
                AdvancedParticleBase.spawnParticle(level(), ParticleHandler.PIXEL, getX(), getY(), getZ(), 0, 0, 0, true, 0, 0, 0, 0, 0F, 1, 1, 1, 0.4, 1, 80, false, true, new ParticleComponent[]{
                        new ParticleComponent.Gravity(0.5f),
                        new ParticleComponent.PinLocation(droolPositions.get(random.nextInt(droolPositions.size())), 20).setVelocityOnRelease(0, -0.08f, 0),
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.KeyTrack(
                                new float[]{0f, 2},
                                new float[]{0.0f, 0.125f}
                        ), false)
                });
            }
        }

//        if (getActiveAbility() == null) {
//            sendAbilityMessage(BACKFLIP_ABILITY);
//        }

    }

    public boolean isHowler() {
        return false;
    }

    private static boolean isDayTime(Level level) {
        return level.getTimeOfDay(0) >= 0.7609 || level.getTimeOfDay(0) < 0.23918849;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength, x, z);
        knockBackAngle = Math.toDegrees(Math.atan2(z, x));
    }

    @Override
    protected int getDeathDuration() {
        AbilityType deathAbilityType = getDeathAbility();
        Ability<?>deathAbility = getAbility(deathAbilityType);
        if (deathAbility == null || !deathAbility.isUsing()) {
            return 9;
        }
        else if (deathAbility.getCurrentSection().sectionType == AbilitySection.AbilitySectionType.RECOVERY) {
            return deathAbility.getTicksInUse() - deathAbility.getTicksInSection() + 19;
        }
        return 60;
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(speed);
    }

    private boolean canContinueClinging() {
        BlockState blockstate = getInBlockState();
        if (blockstate.isLadder(level(), blockPosition(), this)) {
            return true;
        }
        Vec3 clingDirVec = Vec3.atLowerCornerOf(getClingDirection().getNormal());
        Iterable<VoxelShape> blockCollisions = level().getBlockCollisions(this, getBoundingBox().inflate(0.2).expandTowards(clingDirVec.scale(0.5)));
        return blockCollisions.iterator().hasNext();
    }

    @Override
    public boolean onClimbable() {
        return super.onClimbable() && getPose() != Pose.LONG_JUMPING;
    }

    @Nullable
    public static Vec3 generateRandomPos(Supplier<BlockPos> posSupplier, ToDoubleFunction<BlockPos> toDoubleFunction, int attempts) {
        double d0 = Double.NEGATIVE_INFINITY;
        BlockPos blockpos = null;

        for (int i = 0; i < attempts; i++) {
            BlockPos blockpos1 = posSupplier.get();
            if (blockpos1 != null) {
                double d1 = toDoubleFunction.applyAsDouble(blockpos1);
                if (d1 > d0) {
                    d0 = d1;
                    blockpos = blockpos1;
                }
            }
        }

        return blockpos != null ? Vec3.atBottomCenterOf(blockpos) : null;
    }

    @Nullable
    public static Vec3 getTreePos(EntityElokosa mob, Vec3 destination, float acceptableAngle, int radius, int yRange, int yOffset, boolean avoidTarget) {
        boolean flag = GoalUtils.mobRestricted(mob, radius);
        return generateRandomPos(() -> {
//            BlockPos randomDirection1 = RandomPos.generateRandomDirection(mob.getRandom(), radius, yRange);
            Vec3 randomDirection;
            if (destination != null) {
                randomDirection = destination.subtract(mob.position()).normalize();
                randomDirection = randomDirection.yRot((mob.random.nextFloat() * 2f - 1f) * acceptableAngle);
            }
            else {
                randomDirection = new Vec3(1, 0, 0).yRot(mob.random.nextFloat() * Mth.PI * 2);
            }
            Vec3 randomOffset = randomDirection.scale(radius).add(0, mob.random.nextInt(2 * yRange + 1) - yRange + yOffset, 0);
//            BlockPos blockpos1 = EntityElokosa.generateRandomPosTowardDirection(mob, radius, flag, blockpos);
            BlockPos blockPos1 = BlockPos.containing(mob.position().add(randomOffset));
            if (blockPos1 == null) return null;
//            EntityBlockSwapper.swapBlock(mob.level(), blockPos1, Blocks.TALL_GRASS.defaultBlockState(), 10, false, false);
            BlockPos blockPos2 = findClosestPosWithRaycast(mob, blockPos1);
//            EntityBlockSwapper.swapBlock(mob.level(), blockPos2, Blocks.GLASS.defaultBlockState(), 10, false, false);
            if (avoidTarget && mob.getTarget() != null) {
                if (mob.getTarget().distanceToSqr(Vec3.atCenterOf(blockPos2)) < 36) return null;
                if (mob.getTarget().distanceToSqr(Vec3.atCenterOf(blockPos2)) < 400 && Vec3.atCenterOf(blockPos2).y() < mob.getTarget().getY() + 7) return null;
            }
            return isChosenBlockOkay(mob, blockPos2) ? blockPos2 : null;
        }, mob::getTreeTargetValue, 20);
    }

    public static boolean isChosenBlockOkay(PathfinderMob mob, BlockPos blockpos) {
        if (!mob.level().getBlockState(blockpos).isSolid()) return false;
        if (mob.distanceToSqr(blockpos.getBottomCenter()) < 6) return false;
        PathNavigation pathnavigation = mob.getNavigation();
        Path path = pathnavigation.createPath(blockpos, 0, 2);
        if (path == null || path.getDistToTarget() > 5) {
            return true;
        }
//        EntityBlockSwapper.swapBlock(mob.level(), path.getTarget(), Blocks.TALL_GRASS.defaultBlockState(), 10, false, false);
        return false;
    }

    public boolean checkForClingInDirection(Vec3 direction) {
        Iterable<VoxelShape> blockCollisions = level().getBlockCollisions(this, getBoundingBox().expandTowards(getDeltaMovement().scale(0.5).add(direction)).inflate(0.2));
        int attempts = 0;
        while (blockCollisions.iterator().hasNext() && attempts < 8) {
            // If you find a collision, raycast to its center to find the block face you collided with
            VoxelShape shape = blockCollisions.iterator().next();
            Vec3 center = shape.bounds().getCenter();
            Vec3 start = position().add(0, getBbHeight() / 2f, 0);
            BlockHitResult result = level().clip(new ClipContext(start, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (result.getType() == HitResult.Type.BLOCK) {
                clingToSurface(result.getLocation(), result.getDirection().getOpposite());
                return true;
            }
            attempts++;
        }
        return false;
    }
    
    public boolean checkForCling(int ticksInAir) {
        if (!onGround() && ticksInAir > 1 && (getDeltaMovement().y() < 0 || jumpGoalPos == null || distanceToSqr(jumpGoalPos) < 2)) {
            BlockState blockstate = getInBlockState();
            if (blockstate.isLadder(level(), blockPosition(), this)) {
                setClingDirection(getDirection());
            } else {
                // Check for block collisions in the direction of movement
                Vec3 towardsGoal;
                if (jumpGoalPos != null) {
                    towardsGoal = jumpGoalPos.subtract(position()).normalize();
                }
                else {
                    towardsGoal = getDeltaMovement().normalize();
                }
                return checkForClingInDirection(towardsGoal);
            }
        }
        return false;
    }

    @Nullable
    public static BlockPos generateRandomPosTowardDirection(PathfinderMob mob, int radius, boolean shortCircuit, BlockPos pos) {
        BlockPos blockpos = RandomPos.generateRandomPosTowardDirection(mob, radius, mob.getRandom(), pos);
        return !GoalUtils.isOutsideLimits(blockpos, mob)
                && !GoalUtils.isRestricted(shortCircuit, mob, blockpos)
                ? blockpos
                : null;
    }

    public static BlockPos findClosestPosWithRaycast(PathfinderMob mob, BlockPos pos) {
        BlockHitResult result = mob.level().clip(new ClipContext(mob.position().add(0, mob.getBbHeight()/2, 0), pos.getCenter(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getBlockPos();//.offset(result.getDirection().getNormal());
        }
        return pos;
    }

    public static BlockPos moveAnyDirectionOutOfSolid(PathfinderMob mob, BlockPos pos, int maxDist) {
        Predicate<BlockPos> posPredicate = p -> GoalUtils.isSolid(mob, p);
        if (!posPredicate.test(pos)) {
            return pos;
        }
        else {
            Map<Direction, BlockPos.MutableBlockPos> posForDirection = Map.of(
                    Direction.DOWN, pos.mutable(),
                    Direction.UP, pos.mutable(),
                    Direction.NORTH, pos.mutable(),
                    Direction.SOUTH, pos.mutable(),
                    Direction.WEST, pos.mutable(),
                    Direction.EAST, pos.mutable()
            );
            for (int dist = 0; dist < maxDist; dist++) {
                Collection<Direction> directions = Direction.allShuffled(mob.getRandom());
                for (Direction dir : directions) {
                    BlockPos.MutableBlockPos thisPos = posForDirection.get(dir);
                    BlockPos nextPos = thisPos.relative(dir);
                    if (!posPredicate.test(nextPos)) {
                        return thisPos;
                    }
                    thisPos.move(dir);
                }
            }

            return null;
        }
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        if (!getNightForm()) return DAY_FORM_DIMENSIONS;
        return pose == Pose.LONG_JUMPING ? LONG_JUMP_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    public float getTreeTargetValue(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        float treeBonus = state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) ? 10 : 0;
        return (float) ((pos.getY() - getY()) - this.level().getPathfindingCostFromLightLevels(pos) + treeBonus);
    }

    public Direction getClingDirection() {
        return Direction.from3DDataValue(getEntityData().get(CLING_DIRECTION));
    }

    public void setClingDirection(Direction clingDirection) {
        getEntityData().set(CLING_DIRECTION, clingDirection.get3DDataValue());
    }

    public boolean getNightForm() {
        return getEntityData().get(NIGHT_FORM);
    }

    public void setNightForm(boolean nightForm) {
        getEntityData().set(NIGHT_FORM, nightForm);

        if (nightForm) {
            getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(DAY_SPEED_MODIFIER_ID);
            float healthFraction = getHealthRatio();
            getAttribute(Attributes.MAX_HEALTH).removeModifier(DAY_HEALTH_MODIFIER_ID);
            setHealth(getMaxHealth() * healthFraction);
            setPose(Pose.STANDING);
            refreshDimensions();
        }
        else {
            if (!getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(DAY_SPEED_MODIFIER_ID)) {
                getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(SPEED_MODIFIER_DAY);
            }
            float healthFraction = getHealthRatio();
            if (!getAttribute(Attributes.MAX_HEALTH).hasModifier(DAY_HEALTH_MODIFIER_ID)) {
                getAttribute(Attributes.MAX_HEALTH).addTransientModifier(HEALTH_MODIFIER_DAY);
            }
            setHealth(getMaxHealth() * healthFraction);
            setPose(Pose.CROUCHING);
            refreshDimensions();
            if (getClingDirection() == Direction.UP) {
                float heightDiff = this.getType().getDimensions().height() - DAY_FORM_DIMENSIONS.height();
                setPos(getX(), getY() + heightDiff, getZ());
            }
        }
    }

    public void clingToSurface(Vec3 location, Direction direction) {
        if (direction == Direction.DOWN) {
            return;
        }
//        System.out.println("START CLING " + direction);
        setClingDirection(direction);
        setDeltaMovement(0, 0, 0);
        setYRot(direction.getOpposite().toYRot());
        yRotO = getYRot();
        yBodyRot = getYRot();

        if (direction != Direction.DOWN) {
            float width = getBbWidth()/2f;
            float height = getBbHeight();
            if (direction == Direction.UP && getNightForm() && tickCount == 0) {
                setPos(getX(), getY() - height * 0.5, getZ());
            }
            else if (getPose() != Pose.STANDING) {
                setPos(location.add(Vec3.atLowerCornerOf(direction.getOpposite().getNormal()).scale(width)).subtract(0, height / 2f, 0));
            }
        }
    }

    public void stopClinging() {
        setClingDirection(Direction.DOWN);
        setPose(Pose.STANDING);
        refreshDimensions();
    }

    public boolean isOnGroundOrClinging() {
        return getClingDirection() != Direction.DOWN || onGround() || isInWater() || isInLava() || isInPowderSnow;
    }

    public boolean findAndComputeLeap(Supplier<Vec3> posSelector) {
        jumpGoalPos = posSelector.get(); //getTreePos(this, 16, 16, 4);
        if (jumpGoalPos == null) return false;
        jumpVel = calculateOptimalJumpVector(this, jumpGoalPos.add(0, 0.5, 0));
        if (jumpVel == null) {
            EntityBlockSwapper.swapBlock(level(), BlockPos.containing(jumpGoalPos), Blocks.REDSTONE_BLOCK.defaultBlockState(), 70, false, false);
        }
        else {
            EntityBlockSwapper.swapBlock(level(), BlockPos.containing(jumpGoalPos), Blocks.IRON_BLOCK.defaultBlockState(), 70, false, false);
        }
        return jumpVel != null;
    }

    // TODO separate angles for different cling directions
    private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList(-20, -10, -5, 0, 5, 20, 35, 50, 65, 70, 75, 80);
    protected final float maxJumpVelocityMultiplier = 3.5F;

    @Nullable
    public Vec3 calculateOptimalJumpVector(Mob mob, Vec3 target) {
        List<Integer> list = Lists.newArrayList(ALLOWED_ANGLES);
        Collections.shuffle(list);
        float f = (float)(mob.getAttributeValue(Attributes.JUMP_STRENGTH) * (double)this.maxJumpVelocityMultiplier);

        for (int i : list) {
            Optional<Vec3> optional = MowzieLongJumpUtil.calculateJumpVectorForAngle(mob, target, f, i, true, 1f);
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        return null;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new ElokosaNavigator(this, level);
    }

    public void endStalkingSoon(int maxTicksToEndIn) {
        if (isStalking && getTarget() != null) {
            stalkingTimer = random.nextInt(maxTicksToEndIn);
        }
    }

    boolean isLookingAtMe(LivingEntity entity) {
        Vec3 vec3 = entity.getViewVector(1.0F).normalize();
        Vec3 vec31 = new Vec3(this.getX() - entity.getX(), this.getEyeY() - entity.getEyeY(), this.getZ() - entity.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 0.9 && entity.hasLineOfSight(this);
    }

    protected Vec3 updateCirclingPosition(float speed) {
        LivingEntity target = getTarget();
        if (target != null) {
            if (random.nextInt(200) == 0) {
                circleDirection = !circleDirection;
            }
            Vec3 vecBetween = target.position().subtract(this.position());
            vecBetween = vecBetween.normalize();
            Vec3 crossProd = vecBetween.cross(new Vec3(0, 1, 0)).normalize();
            return position().add(crossProd.scale(speed * (circleDirection ? 1 : -1)));
        }
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("cling_direction", getClingDirection().get3DDataValue());
        compound.putInt("stalking_timer", stalkingTimer);
        compound.putBoolean("is_stalking", isStalking);
        compound.putBoolean("is_night_form", getNightForm());
        compound.putBoolean("prevent_transform", preventTransform);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setClingDirection(Direction.from3DDataValue(compound.getInt("cling_direction")));
        stalkingTimer = compound.getInt("stalking_timer");
        isStalking = compound.getBoolean("is_stalking");
        setNightForm(compound.getBoolean("is_night_form"));
        preventTransform = compound.getBoolean("prevent_transform");
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return !isDayTime(level()) || super.requiresCustomPersistence();
    }

    @Override
    public @org.jetbrains.annotations.Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @org.jetbrains.annotations.Nullable SpawnGroupData spawnGroupData) {
        boolean isDay = isDayTime(level());;
        boolean isNewMoon = level().getMoonPhase() == 4;
        if (!isDay && !isNewMoon) {
            addEffect(new MobEffectInstance(EffectHandler.MOONS_CURSE, 10, 0, false, false));
        }
        setNightForm(hasEffect(EffectHandler.MOONS_CURSE));

        if (spawnType == MobSpawnType.SPAWN_EGG) {
            // Try to guess which player spawned it, use the vector to them to see if it should spawn clinging
            List<Player> players = getPlayersNearby(5, 5, 5, 5);
            if (!players.isEmpty()) {
                Player closestPlayer = players.get(0);
                float closestPlayerDist = 6;
                for (Player player : players) {
                    if (player.getMainHandItem().getItem() == ItemHandler.UMVUTHI_SPAWN_EGG.get() || player.getMainHandItem().getItem() == ItemHandler.UMVUTHI_SPAWN_EGG.get()) {
                        float thisDist = this.distanceTo(player);
                        if (thisDist < closestPlayerDist) {
                            closestPlayer = player;
                            closestPlayerDist = thisDist;
                        }
                    }
                }
                checkForClingInDirection(this.position().subtract(closestPlayer.position()).normalize());
            }
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return LootTableHandler.ELOKOSA;
    }

    static class ElokosaNavigator extends GroundPathNavigation {
        public ElokosaNavigator(Mob mob, Level level) {
            super(mob, level);
        }

        @Override
        public boolean isStableDestination(BlockPos pos) {
            BlockPos blockpos = pos.below();
            return this.level.getBlockState(blockpos).isSolid();
        }
    }

    public static class ElokosaLeapAbility extends Ability<EntityElokosa> {
        public static AbilitySection[] SECTION_TRACK = new AbilitySection[]{
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 2),
                new AbilitySection.AbilitySectionInfinite(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, 17)
        };
        private static final RawAnimation LEAP_START_GROUND_ANIMATION = RawAnimation.begin().then("leap_start_ground", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private static final RawAnimation LEAP_END_GROUND_ANIMATION = RawAnimation.begin().then("leap_end_ground", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private static final RawAnimation LEAP_START_WALL_ANIMATION = RawAnimation.begin().then("leap_start_wall", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private static final RawAnimation LEAP_END_WALL_ANIMATION = RawAnimation.begin().then("leap_end_wall", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private static final RawAnimation LEAP_START_CEILING_ANIMATION = RawAnimation.begin().then("leap_start_ceiling", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private static final RawAnimation LEAP_END_CEILING_ANIMATION = RawAnimation.begin().then("leap_end_ceiling", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private static final RawAnimation LEAP_AIR_ANIMATION = RawAnimation.begin().then("leap_air", Animation.LoopType.LOOP);

        private int ticksInAir;
        private boolean playedEndAnim = false;

        public ElokosaLeapAbility(AbilityType abilityType, EntityElokosa user) {
            super(abilityType, user, SECTION_TRACK);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && getUser().jumpVel != null;
        }

        @Override
        protected boolean canContinueUsing() {
            return super.canContinueUsing() && getUser().jumpGoalPos != null && getUser().jumpVel != null;
        }

        @Override
        public void start() {
            super.start();
            if (getUser().getClingDirection() == Direction.DOWN) {
                playAnimation(LEAP_START_GROUND_ANIMATION);
            }
            else if (getUser().getClingDirection().getAxis().isHorizontal()) {
                playAnimation(LEAP_START_WALL_ANIMATION);
            }
            else {
                playAnimation(LEAP_START_CEILING_ANIMATION);
            }
            playedEndAnim = false;
        }

        protected boolean doClingCheck() {
            return true;
        }

        @Override
        protected void beginSection(AbilitySection section) {
            super.beginSection(section);
            EntityElokosa elokosa = getUser();

            if (section.sectionType == AbilitySection.AbilitySectionType.ACTIVE) {
                if (!getLevel().isClientSide()) {
                    elokosa.setYRot(elokosa.yBodyRot);
                    elokosa.setDiscardFriction(true);
                    double d0 = elokosa.jumpVel.length();
                    double d1 = d0 + (double) elokosa.getJumpBoostPower();
                    elokosa.setDeltaMovement(elokosa.jumpVel.scale(d1 / d0).scale(1.05));
                    elokosa.setPose(Pose.LONG_JUMPING);
                    elokosa.refreshDimensions();
                    elokosa.setClingDirection(Direction.DOWN);
                    ticksInAir = 0;
                }
            }

            if (section.sectionType == AbilitySection.AbilitySectionType.RECOVERY) {
                if (!getLevel().isClientSide()) {
                    if (elokosa.onGround() || elokosa.getClingDirection() != Direction.DOWN) {
                        double horizSpeed = elokosa.getDeltaMovement().multiply(1, 0, 1).length();
                        if (horizSpeed > 0.010) horizSpeed = 0.010;
                        elokosa.setDeltaMovement(elokosa.getDeltaMovement().multiply(1F, 0, 1F).normalize().scale(horizSpeed));
                    }

                    elokosa.setDiscardFriction(false);
                    elokosa.setPose(Pose.STANDING);
                    elokosa.refreshDimensions();
                    if (elokosa.getTarget() != null && elokosa.getTarget().distanceToSqr(elokosa) < 14 && !elokosa.isStalking) {
                        elokosa.sendAbilityMessage(LEAP_ATTACK_ABILITY);
                    }
                }
            }
        }

        public void doLookAt() {
            if (getUser().jumpGoalPos != null && getCurrentSection().sectionType != AbilitySection.AbilitySectionType.RECOVERY) {
                getUser().lookAt(EntityAnchorArgument.Anchor.EYES, getUser().jumpGoalPos);
            }
        }

        @Override
        public void tickUsing() {
            super.tickUsing();
            EntityElokosa elokosa = getUser();
            if (Double.isNaN(elokosa.getDeltaMovement().y())) {
                elokosa.setDeltaMovement(0, 0, 0);
                AbilityHandler.INSTANCE.sendInterruptAbilityMessage(elokosa, getAbilityType());
            }

            doLookAt();
            elokosa.yBodyRot = elokosa.getYRot();

            if (!getLevel().isClientSide() && elokosa.jumpGoalPos != null) {
                if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.ACTIVE) {
                    ticksInAir++;

                    // Cling detection
                    if (doClingCheck()) {
                        getUser().checkForCling(ticksInAir);
                    }

                    if (elokosa.onGround() || elokosa.getClingDirection() != Direction.DOWN) {
                        AbilityHandler.INSTANCE.sendJumpToSectionMessage(elokosa, getAbilityType(), 2);
                    }
                }
            }
            else {
                if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.RECOVERY && !playedEndAnim) {
                    // Doesnt work because entity data is syncing too late
                    if (elokosa.getClingDirection().getAxis().isHorizontal()) {
                        playAnimation(LEAP_END_WALL_ANIMATION);
                        playedEndAnim = true;
                    }
                    else if (elokosa.onGround()) {
                        playAnimation(LEAP_END_GROUND_ANIMATION);
                        playedEndAnim = true;
                    }
                    else if (elokosa.getClingDirection() == Direction.UP) {
                        playAnimation(LEAP_END_CEILING_ANIMATION);
                        playedEndAnim = true;
                    }
                }
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
            getUser().setDiscardFriction(false);
            getUser().setPose(Pose.STANDING);
            getUser().refreshDimensions();
        }

        @Override
        public void end() {
            super.end();
            getUser().jumpVel = null;
            getUser().jumpGoalPos = null;
            ticksInAir = 0;
        }

        @Override
        public boolean canCancelActiveAbility() {
            return getActiveAbility().getAbilityType() == SCREAM_ABILITY && getActiveAbility().getTicksInUse() >= SCREAM_CANCEL_TICK;
        }
    }

    public static class ElokosaLeapToTargetAbility extends ElokosaLeapAbility {
        public ElokosaLeapToTargetAbility(AbilityType abilityType, EntityElokosa user) {
            super(abilityType, user);
        }

        @Override
        protected boolean doClingCheck() {
            return false;
        }

        @Override
        public void doLookAt() {
            if (getUser().getTarget() != null) {
                getUser().getLookControl().setLookAt(getUser().getTarget(), 30f, 30f);
            }
        }
    }

    private static void spawnTransformParticle(LivingEntity elokosa, float heightDivider) {
        AABB bounds = elokosa.getBoundingBox();
        float x = (float) (elokosa.getX() + elokosa.getRandom().nextGaussian() * (bounds.maxX - bounds.minX)/3.0);
        float y = (float) (elokosa.getY() + elokosa.getRandom().nextGaussian() * (bounds.maxY - bounds.minY)/3.0 + elokosa.getBbHeight()/heightDivider);
        float z = (float) (elokosa.getZ() + elokosa.getRandom().nextGaussian() * (bounds.maxZ - bounds.minZ)/3.0);
        AdvancedParticleBase.spawnParticle(elokosa.level(), ParticleHandler.PIXEL, x, y, z, 0, 0.05, 0, true, 0, 0 ,0, 0, 1f, 7d / 256d, 7d / 256d, 7d / 256d, 1, 0.9, 25 + elokosa.getRandom().nextFloat() * 10, true, true, new ParticleComponent[]{
                new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.KeyTrack(
                        new float[]{0, 2.5f, 0},
                        new float[]{0, 0.1f, 1}
                ), false),
                new ParticleComponent.CurlNoise(0.015f, 4f),
                new ParticleComponent.ForceOverTime(new Vec3(0, 0.003, 0)),
        });
    }

    public static class ElokosaDayToNightAbility extends SimpleAnimationAbility<EntityElokosa> {
        public ElokosaDayToNightAbility(AbilityType abilityType, EntityElokosa user) {
            super(abilityType, user, RawAnimation.begin().thenPlay("transform_day_to_night"), 55);
        }

        @Override
        public void end() {
            super.end();
        }

        @Override
        public void tickUsing() {
            super.tickUsing();
            if (getUser().getClingDirection() == Direction.DOWN) {
                getUser().setYHeadRot(getUser().getYRot());
                getUser().setYBodyRot(getUser().getYRot());
                if (getTicksInUse() > 32 && getTicksInUse() < 42) {
                    getUser().setDeltaMovement(getUser().getDeltaMovement().add(getUser().getForward().scale(0.025)));
                }
            }

            if (getTicksInUse() == 15) {
                getUser().setNightForm(true);
            }

            if (getTicksInUse() == 3) getUser().playSound(MMSounds.ENTITY_ELOKOSA_DAY_TRANSFORM.get(), 1, 1);
            if (getTicksInUse() == 16) getUser().playSound(MMSounds.ENTITY_ELOKOSA_NIGHT_SCREAM, 1.5f, 0.7f + getUser().random.nextFloat() * 0.1f);

            if (getLevel().isClientSide() && getTicksInUse() > 6 && getTicksInUse() < 14) {
                for (int i = 0; i < 18; i++) {
                    spawnTransformParticle(getUser(), 6.0f);
                }
            }
        }

        private static final RawAnimation TRANSFORM_WALL_ANIMATION = RawAnimation.begin().then("transform_day_to_night_wall", Animation.LoopType.PLAY_ONCE);
        private static final RawAnimation TRANSFORM_CEILING_ANIMATION = RawAnimation.begin().then("transform_day_to_night_ceiling", Animation.LoopType.PLAY_ONCE);

        @Override
        public RawAnimation getAnimation() {
            if (getUser().getClingDirection().getAxis().isHorizontal()) {
                return TRANSFORM_WALL_ANIMATION;
            }
            else if (getUser().getClingDirection() == Direction.UP) {
                return TRANSFORM_CEILING_ANIMATION;
            }
            return super.getAnimation();
        }
    }

    public static class ElokosaNightToDayAbility extends SimpleAnimationAbility<EntityElokosa> {
        public ElokosaNightToDayAbility(AbilityType abilityType, EntityElokosa user) {
            super(abilityType, user, RawAnimation.begin().thenPlay("transform_night_to_day"), 25);
        }

        @Override
        public void tickUsing() {
            super.tickUsing();
            if (getTicksInUse() == 10) {
                getUser().setNightForm(false);
            }
            if (getTicksInUse() == 1) getUser().playSound(MMSounds.ENTITY_ELOKOSA_NIGHT_TRANSFORM.get(), 1, 1);

            if (getLevel().isClientSide() && getTicksInUse() > 1 && getTicksInUse() < 9) {
                for (int i = 0; i < 26; i++) {
                    spawnTransformParticle(getUser(), 3.0f);
                }
            }
        }

        private static final RawAnimation TRANSFORM_WALL_ANIMATION = RawAnimation.begin().then("transform_night_to_day_wall", Animation.LoopType.PLAY_ONCE);
        private static final RawAnimation TRANSFORM_CEILING_ANIMATION = RawAnimation.begin().then("transform_night_to_day_ceiling", Animation.LoopType.PLAY_ONCE);

        @Override
        public RawAnimation getAnimation() {
            if (getUser().getClingDirection().getAxis().isHorizontal()) {
                return TRANSFORM_WALL_ANIMATION;
            }
            else if (getUser().getClingDirection() == Direction.UP) {
                return TRANSFORM_CEILING_ANIMATION;
            }
            return super.getAnimation();
        }

        //        @Override
//        public void end() {
//            super.end();
//            getUser().setNightForm(false);
//        }
    }

    private static class ElokosaAttackAbility extends MeleeAttackAbility<EntityElokosa> {
        double distanceMult = 1;
        public ElokosaAttackAbility(AbilityType<EntityElokosa, ? extends MeleeAttackAbility<EntityElokosa>> abilityType, EntityElokosa user) {
            super(abilityType, user, new RawAnimation[]{RawAnimation.begin().thenPlayAndHold("attack_single")}, null, null, 1, 3.0f, 1, 16, 10, true);
        }

        @Override
        public void start() {
            super.start();
            if (getUser().getTarget() != null) {
                double distToTarget = this.getUser().position().multiply(1, 0, 1).distanceToSqr(getUser().getTarget().position().multiply(1, 0, 1));
                distanceMult = Mth.clampedMap(distToTarget, 4, 12, 0.01, 1.0);
            }
        }

        @Override
        public void tickUsing() {
            super.tickUsing();
            if (getTicksInUse() == 3) getUser().setDeltaMovement(getUser().getDeltaMovement().add(getUser().getForward().normalize().scale(0.65 * distanceMult)));
            if (getTicksInUse() >= 9 && getTicksInUse() <= 11) getUser().setDeltaMovement(getUser().getDeltaMovement().add(getUser().getForward().normalize().scale(0.15 * distanceMult)));
            if (getTicksInUse() == 13) getUser().playSound(MMSounds.ENTITY_ELOKOSA_NIGHT_ATTACK, 1, getUser().getVoicePitch());
            if (getTicksInUse() == 4) getUser().playSound(MMSounds.ENTITY_ELOKOSA_NIGHT_SCREAM, 1, 0.9f + getUser().random.nextFloat() * 0.2f);
        }
    }

    public static class ElokosaAttackComboAbility extends MeleeAttackAbility<EntityElokosa> {
        private static final AbilitySection[] SECTION_TRACK = new AbilitySection[] {
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 12),
                new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 10),
                new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, 17)
        };
        private static final RawAnimation ATTACK_COMBO_ANIM = RawAnimation.begin().thenPlayAndHold("attack_combo");

        public ElokosaAttackComboAbility(AbilityType<EntityElokosa, ? extends MeleeAttackAbility<EntityElokosa>> abilityType, EntityElokosa user) {
            this(abilityType, user, SECTION_TRACK, ATTACK_COMBO_ANIM);
        }

        public ElokosaAttackComboAbility(AbilityType<EntityElokosa, ? extends MeleeAttackAbility<EntityElokosa>> abilityType, EntityElokosa user, AbilitySection[] sectionTrack, RawAnimation animation) {
            super(abilityType, user, sectionTrack, new RawAnimation[] {animation}, null, null, 1, 3.0f, 1, true);
        }

        @Override
        public void tickUsing() {
            super.tickUsing();
            if (getTicksInUse() >= 2 && getTicksInUse() <= 6) {
                getUser().setDeltaMovement(getUser().getDeltaMovement().add(getUser().getForward().scale(0.06)));
            }
            if (getTicksInUse() >= 7 && getTicksInUse() <= 11 || getTicksInUse() >= 17 && getTicksInUse() <= 21) {
                getUser().setDeltaMovement(getUser().getDeltaMovement().add(getUser().getForward().scale(0.12)));
            }
            if (getTicksInUse() > scootFrameStart() && getTicksInUse() < scootFrameEnd()) {
                getUser().setDeltaMovement(getUser().getDeltaMovement().add(getUser().getForward().scale(scootAmount())));
            }

            if (!getLevel().isClientSide()) {
                if (getUser().isHowler() && getTicksInUse() == 28) {
                    if (getUser().getTarget() != null && getUser().getTarget().distanceToSqr(getUser()) < 25) {
                        AbilityHandler.INSTANCE.sendAbilityMessage(getUser(), SCYTHE_ATTACK_ABILITY);
                    }
                }
            }

            if (getTicksInUse() == 10) getUser().playSound(MMSounds.ENTITY_ELOKOSA_NIGHT_ATTACK, 1, getUser().getVoicePitch());
            if (getTicksInUse() == 22) getUser().playSound(MMSounds.ENTITY_ELOKOSA_NIGHT_ATTACK, 1, getUser().getVoicePitch());
        }

        protected int scootFrameStart() {
            return 27;
        }

        protected int scootFrameEnd() {
            return 32;
        }

        protected double scootAmount() {
            return 0.04;
        }

        @Override
        public boolean canBeCanceledByAbility(Ability<?> ability) {
            return ability.getAbilityType() == SCYTHE_ATTACK_ABILITY;
        }
    }

    public static class ElokosaAttackComboRoarAbility extends ElokosaAttackComboAbility {
        private static final AbilitySection[] SECTION_TRACK = new AbilitySection[] {
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 12),
                new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 10),
                new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.MISC, 13),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, 12)
        };
        private static final RawAnimation ATTACK_COMBO_ROAR_ANIM = RawAnimation.begin().thenPlayAndHold("attack_combo_roar");

        public ElokosaAttackComboRoarAbility(AbilityType<EntityElokosa, ? extends MeleeAttackAbility<EntityElokosa>> abilityType, EntityElokosa user) {
            super(abilityType, user, SECTION_TRACK, ATTACK_COMBO_ROAR_ANIM);
        }

        @Override
        protected int scootFrameStart() {
            return 27;
        }

        @Override
        protected int scootFrameEnd() {
            return 37;
        }

        @Override
        protected double scootAmount() {
            return 0.025;
        }

        @Override
        public void tickUsing() {
            super.tickUsing();
            if (getTicksInUse() == 35) getUser().playSound(MMSounds.ENTITY_ELOKOSA_NIGHT_SCREAM, 2f, 0.9f + getUser().random.nextFloat() * 0.2f);
        }
    }

    public static class ElokosaScytheAttackAbility extends MeleeAttackAbility<EntityElokosa> {
        private static final RawAnimation SCYTHE_ATTACK_ANIM = RawAnimation.begin().thenPlay("attack_tail_scythe");
        public ElokosaScytheAttackAbility(AbilityType<EntityElokosa, ? extends MeleeAttackAbility<EntityElokosa>> abilityType, EntityElokosa user) {
            super(abilityType, user, new RawAnimation[] {SCYTHE_ATTACK_ANIM}, null, null, 1.6f, 3.5f, 1.5f, 11, 16, false);
        }

        @Override
        public void tickUsing() {
            super.tickUsing();
            if (getTicksInUse() >= 6 && getTicksInUse() <= 9) {
                getUser().setDeltaMovement(getUser().getDeltaMovement().add(getUser().getForward().scale(0.3)));
            }

            if (getTicksInUse() == 3) getUser().playSound(MMSounds.ENTITY_ELOKOSA_NIGHT_ATTACK_BIG.get(), 1, getUser().getVoicePitch());
        }
    }

    public static class ElokosaLeapAttackAbility extends MeleeAttackAbility<EntityElokosa> {
        private static final RawAnimation LEAP_SLASH_ANIM = RawAnimation.begin().thenPlay("leap_end_slash");
        public ElokosaLeapAttackAbility(AbilityType<EntityElokosa, ? extends MeleeAttackAbility<EntityElokosa>> abilityType, EntityElokosa user) {
            super(abilityType, user, new RawAnimation[] {LEAP_SLASH_ANIM}, null, null, 1.5f, 3.5f, 1.3f, 4, 16, false);
        }

        @Override
        public void start() {
            super.start();
            getUser().playSound(MMSounds.ENTITY_ELOKOSA_NIGHT_ATTACK, 1, getUser().getVoicePitch());
        }

        @Override
        public boolean canCancelActiveAbility() {
            return super.canCancelActiveAbility() || getUser().getActiveAbility() instanceof ElokosaLeapAbility;
        }
    }

    public static class ElokosaBackflipAbility extends Ability<EntityElokosa> {
        private boolean playedEndAnim = false;

        public ElokosaBackflipAbility(AbilityType<EntityElokosa, ? extends Ability> abilityType, EntityElokosa user) {
            super(abilityType, user, new AbilitySection[] {
                    new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 1),
                    new AbilitySection.AbilitySectionInfinite(AbilitySection.AbilitySectionType.ACTIVE),
                    new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, 10),
            }, 0);
        }
        private static final RawAnimation BACKFLIP_ANIMATION = RawAnimation.begin().thenPlayAndHold("backflip");
        private static final RawAnimation BACKFLIP_LAND_ANIMATION = RawAnimation.begin().thenPlayAndHold("backflip_land");
        private static final RawAnimation LEAP_END_WALL_ANIMATION = RawAnimation.begin().then("leap_end_wall", Animation.LoopType.HOLD_ON_LAST_FRAME);
        private static final RawAnimation LEAP_END_CEILING_ANIMATION = RawAnimation.begin().then("leap_end_ceiling", Animation.LoopType.HOLD_ON_LAST_FRAME);

        @Override
        public void start() {
            super.start();
            playAnimation(BACKFLIP_ANIMATION);
            playedEndAnim = false;
        }

        @Override
        protected void beginSection(AbilitySection section) {
            super.beginSection(section);
            if (section.sectionType == AbilitySection.AbilitySectionType.ACTIVE) {
                getUser().addDeltaMovement(getUser().getForward().multiply(1, 0, 1).normalize().scale(-2).add(0, 0.75, 0));
            }
        }

        @Override
        public void tickUsing() {
            super.tickUsing();
            if (!getLevel().isClientSide()) {
                if (getUser().getTarget() != null) {
                    getUser().lookAt(getUser().getTarget(), 30f, 30f);
                }
                if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.ACTIVE) {
                    getUser().checkForCling(getTicksInSection());

                    // Cling detection
                    getUser().checkForCling(getTicksInSection());

                    if (getUser().onGround() && getUser().prevPrevOnGround || getUser().getClingDirection() != Direction.DOWN) {
                        AbilityHandler.INSTANCE.sendJumpToSectionMessage(getUser(), getAbilityType(), 2);
                    }
                }
            }
            if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.RECOVERY && !playedEndAnim) {
                // Doesnt work because entity data is syncing too late
                if (getUser().getClingDirection().getAxis().isHorizontal()) {
                    playAnimation(LEAP_END_WALL_ANIMATION);
                    playedEndAnim = true;
                }
                else if (getUser().onGround()) {
                    playAnimation(BACKFLIP_LAND_ANIMATION);
                    playedEndAnim = true;
                }
                else if (getUser().getClingDirection() == Direction.UP) {
                    playAnimation(LEAP_END_CEILING_ANIMATION);
                    playedEndAnim = true;
                }
            }
        }

        @Override
        public boolean canUse() {
            return super.canUse() && getUser().getClingDirection() == Direction.DOWN;
        }
    }

    public static class ElokosaDieAbility extends Ability<EntityElokosa> {
        private static AbilitySection.AbilitySectionDuration END_SECTION = new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, 20);

        public ElokosaDieAbility(AbilityType<EntityElokosa, ElokosaDieAbility> abilityType, EntityElokosa user) {
            super(abilityType, user, new AbilitySection[] {
                    new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 9),
                    new AbilitySection.AbilitySectionInfinite(AbilitySection.AbilitySectionType.ACTIVE),
                    END_SECTION
            });
        }

        private static final RawAnimation DEATH_START = RawAnimation.begin().thenPlayAndHold("death_start");
        private static final RawAnimation DEATH_END = RawAnimation.begin().thenPlayAndHold("death_splat");

        @Override
        public void start() {
            super.start();
            if (getUser().knockBackAngle != -1) {
                getUser().setYRot((float) getUser().knockBackAngle - 90);
            }
            else {
                if (getUser().getClingDirection().getAxis().isHorizontal()) {
                    getUser().setYRot(getUser().getClingDirection().toYRot());
                }
            }
            getUser().stopClinging();
            playAnimation(DEATH_START);
            getUser().setDeltaMovement(getUser().getForward().multiply(1, 0, 1).normalize().scale(-0.75).add(0, 0.65, 0).scale(4));
            getUser().playHurtSound(getUser().damageSources().generic());
        }

        @Override
        public void tickUsing() {
            super.tickUsing();
            if (getTicksInUse() == 0) {
                getUser().setDeltaMovement(getUser().getForward().multiply(1, 0, 1).normalize().scale(-0.75).add(0, 0.2, 0).scale(0.1));
            }
            if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.ACTIVE) {
                if (getUser().prevPrevOnGround) nextSection();
            }
        }

        @Override
        protected void beginSection(AbilitySection section) {
            super.beginSection(section);
            if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.RECOVERY) {
                playAnimation(DEATH_END);
                getUser().playSound(MMSounds.MISC_GROUNDHIT_1.get(), 1, getUser().getNightForm() ? 1.2f : 1.6f);
            }
        }

        @Override
        public boolean canCancelActiveAbility() {
            return true;
        }
    }

    public static class ElokosaSitAbility extends Ability<EntityElokosa> {
        private int timer;
        private boolean hasScratched;

        public ElokosaSitAbility(AbilityType<EntityElokosa, ? extends Ability> abilityType, EntityElokosa user) {
            super(abilityType, user, new AbilitySection[]{
                    new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 14),
                    new AbilitySection.AbilitySectionInfinite(AbilitySection.AbilitySectionType.ACTIVE),
                    new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.MISC, 70),
                    new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, 16),
            });
        }

        private static final RawAnimation SIT_START = RawAnimation.begin().thenPlayAndHold("sit_start");
        private static final RawAnimation SIT_LOOP = RawAnimation.begin().thenLoop("sit");
        private static final RawAnimation SIT_SCRATCH = RawAnimation.begin().thenPlayAndHold("sit_scratch");
        private static final RawAnimation SIT_END = RawAnimation.begin().thenPlayAndHold("sit_end");

        @Override
        protected void beginSection(AbilitySection section) {
            super.beginSection(section);
            if (section.sectionType == AbilitySection.AbilitySectionType.STARTUP) {
                playAnimation(SIT_START);
                timer = getUser().random.nextInt(45, 240);
                hasScratched = false;
            }
            else if (section.sectionType == AbilitySection.AbilitySectionType.ACTIVE) {
                playAnimation(SIT_LOOP);
            }
            else if (section.sectionType == AbilitySection.AbilitySectionType.MISC) {
                playAnimation(SIT_SCRATCH);
            }
            else {
                playAnimation(SIT_END);
            }
        }

        @Override
        public void tickUsing() {
            super.tickUsing();
            if (!getLevel().isClientSide()) {
                if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.ACTIVE) {
                    timer--;
                    if (timer <= 0) {
                        AbilityHandler.INSTANCE.sendJumpToSectionMessage(getUser(), getAbilityType(), 3);
                    }
                    if (!hasScratched && getUser().random.nextInt(100) == 0) {
                        hasScratched = true;
                        AbilityHandler.INSTANCE.sendJumpToSectionMessage(getUser(), getAbilityType(), 2);
                    }
                }

                if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.MISC && getTicksInSection() == ((AbilitySection.AbilitySectionDuration) getCurrentSection()).duration) {
                    AbilityHandler.INSTANCE.sendJumpToSectionMessage(getUser(), getAbilityType(), 1);
                }
            }
        }

        @Override
        public boolean canBeCanceledByAbility(Ability<?> ability) {
            return true;
        }

        @Override
        public boolean damageInterrupts() {
            return true;
        }
    }

    public static class WanderLeapGoal extends ElokosaLeapGoal {
        public WanderLeapGoal(EntityElokosa elokosa) {
            super(elokosa, UniformInt.of(25, 200));
        }

        @Override
        public boolean canUse() {
            return elokosa.getTarget() == null && super.canUse();
        }

        @Override
        protected int scoreJump(BlockPos pos, Direction dir) {
            if (elokosa instanceof EntityElokosaFollower<? extends LivingEntity> elokosaFollower && elokosaFollower.getLeader() != null) {
                LivingEntity leader = elokosaFollower.getLeader();
                Vec3 vecBetween = leader.position().subtract(pos.getBottomCenter());
                double distance = vecBetween.lengthSqr();
                int distScore = (int) Mth.clampedMap(distance, 10 * 10, 25 * 25, 30, 0);
                return super.scoreJump(pos, dir) + distScore;
            }
            else {
                return super.scoreJump(pos, dir);
            }
        }
    }

    public static class StalkingLeapGoal extends ElokosaLeapGoal {
        public StalkingLeapGoal(EntityElokosa elokosa) {
            super(elokosa, UniformInt.of(13, 200));
        }

        @Override
        protected int scoreJumpRandomnessAmount() {
            return 20;
        }

        @Override
        protected int scoreJump(BlockPos pos, Direction dir) {
            if (elokosa.getTarget() == null) {
                return super.scoreJump(pos, dir);
            }
            else {
                Vec3 vecBetween = elokosa.getTarget().position().add(0, 12, 0).subtract(pos.getBottomCenter());
                double distance = vecBetween.lengthSqr();
                int distScore = (int) Mth.clampedMap(distance, 10 * 10, 25 * 25, 30, 0);
                return super.scoreJump(pos, dir) + distScore;
            }
        }

        @Override
        protected boolean isAcceptableLandingPosition(PossibleJump jump) {
            // Can't leap to block within 6 blocks of target
            if (elokosa.getTarget() != null && jump.getJumpTarget().distToCenterSqr(elokosa.getTarget().position()) < 36) {
                return false;
            }
            return super.isAcceptableLandingPosition(jump);
        }

        @Override
        public boolean canUse() {
            return elokosa.getNightForm() && elokosa.getTarget() != null && elokosa.isStalking && elokosa.stalkingTimer > 30 && super.canUse();
        }

        @Override
        protected boolean ignoreTimerCondition() {
            if (elokosa.getTarget() == null) {
                return false;
            }
            if (elokosa.stalkingTimer <= 30) {
                return false;
            }
            // if elokosa is too far away horizontally
            else if (elokosa.position().subtract(elokosa.getTarget().position()).horizontalDistanceSqr() > 144) {
//                System.out.println("Too far");
                return true;
            }
            // if elokosa is too low
            else if (elokosa.getY() < elokosa.getTarget().getY() + 6) {
//                System.out.println("Too low");
                return true;
            }
            else if (!elokosa.getSensing().hasLineOfSight(elokosa.getTarget())) {
//                System.out.println("Cant see");
                return true;
            }
            return false;
        }
    }

    public static class CombatLeapGoal extends ElokosaLeapGoal {
        public CombatLeapGoal(EntityElokosa elokosa) {
            super(elokosa, UniformInt.of(30, 300));
        }

        @Override
        protected int scoreJumpRandomnessAmount() {
            return 20;
        }

        @Override
        protected int scoreJump(BlockPos pos, Direction dir) {
            if (elokosa.getTarget() == null) {
                return super.scoreJump(pos, dir);
            }
            else {
                Vec3 vecBetween = elokosa.getTarget().position().add(0, 12, 0).subtract(pos.getBottomCenter());
                double distance = vecBetween.lengthSqr();
                int distScore = (int) Mth.clampedMap(distance, 10 * 10, 25 * 25, 30, 0);
                return super.scoreJump(pos, dir) + distScore;
            }
        }

        @Override
        protected boolean isAcceptableLandingPosition(PossibleJump jump) {
            // Can't leap to block within 6 blocks of target
            if (elokosa.getTarget() != null && jump.getJumpTarget().distToCenterSqr(elokosa.getTarget().position()) < 36) {
                return false;
            }
            return super.isAcceptableLandingPosition(jump);
        }

        @Override
        public boolean canUse() {
            if (elokosa.getTarget() == null || elokosa.isStalking || !elokosa.getNightForm()) return false;
            Path path = this.elokosa.getNavigation().createPath(elokosa.getTarget(), 0);
            return path != null && super.canUse();
        }
    }

    public static class FleeingLeapGoal extends ElokosaLeapGoal {
        public FleeingLeapGoal(EntityElokosa elokosa) {
            super(elokosa, UniformInt.of(13, 15));/*, () -> {
                if (!elokosa.fleeGoal.isActive() && !elokosa.failedFleePath) return null;
                LivingEntity entityEvading = elokosa.fleeGoal.getEntityEvading();
                if (elokosa.fleeGoal.getEntityEvading() == null) return null;
                Vec3 direction = elokosa.position().subtract(entityEvading.position()).scale(-1).normalize();
                return getTreePos(elokosa, elokosa.position().add(direction.scale(15)), 25 * Mth.DEG_TO_RAD, 26, 16, 4, false);
            });*/
        }

        @Override
        protected int scoreJumpRandomnessAmount() {
            return 13;
        }

        @Override
        protected int scoreJump(BlockPos pos, Direction dir) {
            if (!elokosa.fleeGoal.isActive() && !elokosa.failedFleePath) {
                return super.scoreJump(pos, dir);
            }
            else {
                LivingEntity entityEvading = elokosa.fleeGoal.getEntityEvading();
                Vec3 vecBetween = entityEvading.position().subtract(pos.getBottomCenter());
                double distance = vecBetween.lengthSqr();
                int distScore = (int) Mth.clampedMap(distance, 7 * 7, 25 * 25, 0, 30);
                return super.scoreJump(pos, dir) + distScore;
            }
        }

        @Override
        protected boolean isAcceptableLandingPosition(PossibleJump jump) {
            if (!elokosa.fleeGoal.isActive() && !elokosa.failedFleePath) {
                return super.isAcceptableLandingPosition(jump);
            }
            // Can't leap to block within 6 blocks of target
            if (elokosa.fleeGoal.getEntityEvading() != null && jump.getJumpTarget().distToCenterSqr(elokosa.fleeGoal.getEntityEvading().position()) < 36) {
                return false;
            }
            return super.isAcceptableLandingPosition(jump);
        }

        @Override
        public boolean canUse() {
            return (elokosa.fleeGoal.isActive() || elokosa.failedFleePath) && elokosa.fleeGoal.getEntityEvading() != null && super.canUse();
        }

        @Override
        protected boolean ignoreTimerCondition() {
            if (elokosa.fleeGoal.getEntityEvading() == null) {
                return false;
            }
            return elokosa.fleeGoal.getEntityEvading().position().subtract(elokosa.position()).lengthSqr() < 144;
        }

        @Override
        public void start() {
            super.start();
            elokosa.failedFleePath = false;
        }
    }

    public static class ChangeFormGoal extends Goal {
        protected final EntityElokosa elokosa;
        protected int randomDelay;

        public ChangeFormGoal(EntityElokosa elokosa) {
            this.elokosa = elokosa;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (elokosa.preventTransform) {
                return false;
            }
            if (elokosa.hasEffect(EffectHandler.MOONS_CURSE) == !elokosa.getNightForm()) {
                if (randomDelay == -1) randomDelay = elokosa.random.nextInt(5) + 5;
                if (randomDelay == 0) {
                    randomDelay = -1;
                    return true;
                }
                else {
                    randomDelay--;
                }
            }
            else {
                randomDelay = -1;
                return false;
            }
            return false;
        }

        @Override
        public void start() {
            super.start();
            if (elokosa.getNightForm()) {
                AbilityHandler.INSTANCE.sendAbilityMessage(elokosa, EntityElokosa.NIGHT_TO_DAY_ABILITY);
            }
            else {
                AbilityHandler.INSTANCE.sendAbilityMessage(elokosa, EntityElokosa.DAY_TO_NIGHT_ABILITY);
            }
        }

        @Override
        public boolean canContinueToUse() {
            Ability<?> abilityDayToNight = AbilityHandler.INSTANCE.getAbility(elokosa, DAY_TO_NIGHT_ABILITY);
            Ability<?> abilityNightToDay = AbilityHandler.INSTANCE.getAbility(elokosa, NIGHT_TO_DAY_ABILITY);
            return (abilityDayToNight != null && abilityDayToNight.isUsing()) || (abilityNightToDay != null && abilityNightToDay.isUsing());
        }
    }

    public static class ElokosaAttackTargetGoal extends NearestAttackableTargetGoal<EntityElokosa> {
        private EntityElokosa elokosa;
        public ElokosaAttackTargetGoal(EntityElokosa mob, Class targetType, boolean mustSee) {
            super(mob, targetType, mustSee);
            elokosa = mob;
        }

        @Override
        protected double getFollowDistance() {
            return super.getFollowDistance() + 20;
        }

        @Override
        public boolean canUse() {
            return elokosa.getNightForm() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return elokosa.getNightForm() && super.canContinueToUse();
        }
    }

    public static class CombatBehaviorGoal extends Goal {
        protected final EntityElokosa elokosa;
        private float attackRadius = 6.5f;
        private static final float ATTACK_RADIUS_MIN = 6.3f;
        private static final float ATTACK_RADIUS_MAX = 10f;
        private int strafingLeftRightMul;
        private int strafingFrontBackMul;
        private boolean chasing = false;
        private boolean fleeing = false;
        private int fleeingTimer = 0;
        private boolean hasReturnedToCircleAfterAttacking = true;
        private int numberOfAttacksLeft = 0;

        protected boolean attacking = false;
        protected boolean doCombo = false;

        public CombatBehaviorGoal(EntityElokosa elokosa) {
            this.elokosa = elokosa;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!elokosa.getNightForm()) {
                return false;
            }
            if (elokosa.getTarget() != null) {
                return true;
            }
            else {
                if (elokosa.stalkingTimer >= TIME_UNTIL_STALK_AGAIN) {
                    elokosa.isStalking = true;
                }
                if (elokosa.stalkingTimer < STALKING_TIMER_MAX) {
                    elokosa.stalkingTimer += 1;
                }
                return false;
            }
        }

        @Override
        public void stop() {
            super.stop();
            this.elokosa.setXxa(0);
            elokosa.setStrafing(false);
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            super.tick();

            LivingEntity target = elokosa.getTarget();
            if (target != null) {
                double distToTarget = this.elokosa.distanceTo(target);

                elokosa.getLookControl().setLookAt(target);

                // Stalking
                if (elokosa.isStalking) {
                    if (elokosa.stalkingTimer <= 0 && elokosa.getActiveAbility() == null) {
                        elokosa.isStalking = false;
                        AbilityHandler.INSTANCE.sendAbilityMessage(elokosa, SCREAM_ABILITY);
                    }
                    if (target.tickCount - target.getLastHurtByMobTimestamp() < 20 && target.getLastHurtByMob() instanceof EntityElokosa) {
                        elokosa.isStalking = false;
                    }
                }
                // Not stalking
                else {
                    Path path = this.elokosa.getNavigation().createPath(target, 0);
                    if (path == null) {
                        // Leap to target
                        if (elokosa.isOnGroundOrClinging() && (elokosa.getActiveAbility() == null || elokosa.getActiveAbility().getAbilityType() == SCREAM_ABILITY && elokosa.getActiveAbility().getTicksInUse() >= SCREAM_CANCEL_TICK)) {
                            Vec3 vecBetween = target.position().multiply(1, 0, 1).subtract(elokosa.position().multiply(1, 0, 1)).normalize();
                            elokosa.jumpGoalPos = target.position().add(vecBetween.scale(-2));
                            elokosa.jumpVel = elokosa.calculateOptimalJumpVector(elokosa, elokosa.jumpGoalPos);
                            if (elokosa.jumpVel != null) {
                                elokosa.sendAbilityMessage(LEAP_TO_TARGET_ABILITY);
                            }
                        }
                    }
                    else {
                        float frontBackDistBuffer = 2f;
                        float leftRightDistBuffer = 1.5f;
                        if (chasing && distToTarget <= attackRadius) {
                            chasing = false;
                        }
                        if (!chasing && distToTarget >= attackRadius + frontBackDistBuffer + 2) {
                            chasing = true;
                            attackRadius = elokosa.random.nextFloat() * (ATTACK_RADIUS_MAX - ATTACK_RADIUS_MIN) + ATTACK_RADIUS_MIN;
                        }

                        if (fleeing && distToTarget >= attackRadius || fleeingTimer >= 100) {
                            fleeing = false;
                            fleeingTimer = 0;
                        }
                        if (!fleeing && distToTarget <= attackRadius - frontBackDistBuffer && !hasReturnedToCircleAfterAttacking && !attacking) {
                            fleeing = true;
                            attackRadius = elokosa.random.nextFloat() * (ATTACK_RADIUS_MAX - ATTACK_RADIUS_MIN) + ATTACK_RADIUS_MIN;
                        }


                        if (!attacking && elokosa.getActiveAbility() == null && distToTarget <= 4.5 && elokosa.getDotProductBodyFacingEntity(target) > 0.3 && (elokosa.damageThreat > 0.5 || elokosa.random.nextFloat() > 0.7) && elokosa.dodgeCooldown.isFinished()) {
                            elokosa.dodgeCooldown.startCooldown();
                            AbilityHandler.INSTANCE.sendAbilityMessage(elokosa, BACKFLIP_ABILITY);
                            return;
                        }

                        // Chasing
                        if (chasing) {
                            this.elokosa.setXxa(0);
                            this.elokosa.getNavigation().moveTo(target, 2);

                            this.elokosa.getLookControl().setLookAt(target, 30.0F, 30.0F);
                            elokosa.setStrafing(false);
                        }
                        else if (fleeing && !attacking) {
                            this.elokosa.setXxa(0);
                            Vec3 vecBetween = target.position().subtract(elokosa.position());
                            vecBetween = vecBetween.normalize();
                            Vec3 fleePos = elokosa.position().add(vecBetween.scale(-5));
                            this.elokosa.getNavigation().moveTo(fleePos.x, fleePos.y, fleePos.z, 2);
                            this.elokosa.getLookControl().setLookAt(target, 30.0F, 30.0F);
                            elokosa.setStrafing(false);
                            fleeingTimer++;
                        }
                        else {
                            // In range
                            if (!attacking && elokosa.getActiveAbility() == null) {
                                this.elokosa.getNavigation().stop();
                                float strafeSpeed = 1f;
                                Vec3 circlePos = elokosa.updateCirclingPosition(1f);
                                double distToCirclePos = this.elokosa.position().distanceTo(circlePos);

                                if (distToCirclePos <= leftRightDistBuffer) {
                                    hasReturnedToCircleAfterAttacking = true;
                                    elokosa.setStrafing(true);

                                    if (distToTarget > this.attackRadius + 0.5) {
                                        this.strafingFrontBackMul = 1;
                                    } else if (distToTarget < this.attackRadius - 0.5) {
                                        this.strafingFrontBackMul = -1;
                                    } else {
                                        this.strafingFrontBackMul = 0;
                                    }

                                    Vec3 toTarget = target.position().subtract(this.elokosa.position()).multiply(1, 0, 1).normalize();
                                    Vec3 toCirclePos = circlePos.subtract(this.elokosa.position()).multiply(1, 0, 1).normalize();
                                    Vec3 cross = toTarget.cross(toCirclePos);
                                    if (cross.y > 0) strafingLeftRightMul = 1;
                                    else if (cross.y < 0) strafingLeftRightMul = -1;
                                    else strafingLeftRightMul = 0;

                                    float distScale = (float) Math.min(Math.pow(distToCirclePos * 1f / leftRightDistBuffer, 0.7), 1.0);

                                    this.elokosa.getMoveControl().strafe(this.strafingFrontBackMul * strafeSpeed, this.strafingLeftRightMul * strafeSpeed * distScale);
                                    this.elokosa.lookAt(target, 30.0F, 30.0F);

                                    if (elokosa.random.nextFloat() < 0.002) {
                                        elokosa.sendAbilityMessage(SCREAM_ABILITY);
                                    }
                                } else {
                                    this.elokosa.setXxa(0);
                                    elokosa.setStrafing(false);
                                    this.elokosa.getNavigation().moveTo(circlePos.x, circlePos.y, circlePos.z, 1.7);
                                    this.elokosa.getLookControl().setLookAt(target, 30.0F, 30.0F);
                                }
                            } else {
                                this.elokosa.setXxa(0);
                                elokosa.setStrafing(false);
                            }

                            // Attacking logic
                            if (elokosa.random.nextInt(50) == 0 && elokosa.attackCooldown.isFinished() && elokosa.getSensing().hasLineOfSight(target)) {
                                attacking = true;
                                numberOfAttacksLeft = elokosa.random.nextInt(1, 3);
                                hasReturnedToCircleAfterAttacking = false;
                            }
                            if ((attacking || hasReturnedToCircleAfterAttacking && elokosa.random.nextInt(30) == 0 && distToTarget <= 4) && elokosa.getActiveAbility() == null) {
                                this.elokosa.setXxa(0);
                                elokosa.setStrafing(false);
                                elokosa.getNavigation().moveTo(target, 2);
                                float distanceThreshold = doCombo ? 3.75f : 4.75f;
                                if (distToTarget <= distanceThreshold && elokosa.getSensing().hasLineOfSight(target)) {
                                    doCombo = elokosa.random.nextFloat() > 0.5;
                                    // 1/3 chance to attack again with no cooldown
                                    if (numberOfAttacksLeft <= 0) {
                                        attacking = false;
                                        elokosa.attackCooldown.startCooldown();
                                    }
                                    if (doCombo) {
                                        if (elokosa.random.nextFloat() < 0.25) {
                                            AbilityHandler.INSTANCE.sendAbilityMessage(elokosa, ATTACK_COMBO_ROAR_ABILITY);
                                        } else {
                                            AbilityHandler.INSTANCE.sendAbilityMessage(elokosa, ATTACK_COMBO_ABILITY);
                                        }
                                    }
                                    else {
                                        AbilityHandler.INSTANCE.sendAbilityMessage(elokosa, ATTACK_ABILITY);
                                    }
                                    numberOfAttacksLeft--;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static class ElokosaSitGoal extends Goal {
        private final EntityElokosa elokosa;

        public ElokosaSitGoal(EntityElokosa elokosa) {
            this.elokosa = elokosa;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return elokosa.sitCooldown.isFinished() && !elokosa.getNightForm() && elokosa.getClingDirection() == Direction.DOWN;
        }

        @Override
        public void start() {
            super.start();
            AbilityHandler.INSTANCE.sendAbilityMessage(elokosa, SIT_ABILITY);
        }

        @Override
        public void stop() {
            super.stop();
            AbilityHandler.INSTANCE.sendInterruptAbilityMessage(elokosa, SIT_ABILITY);
            elokosa.sitCooldown.startCooldown();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && elokosa.getActiveAbilityType() == SIT_ABILITY;
        }
    }
}
