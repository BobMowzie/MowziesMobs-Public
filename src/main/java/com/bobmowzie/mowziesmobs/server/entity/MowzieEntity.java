package com.bobmowzie.mowziesmobs.server.entity;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.client.model.tools.IntermittentAnimation;
import com.bobmowzie.mowziesmobs.client.sound.BossMusic;
import com.bobmowzie.mowziesmobs.client.sound.BossMusicPlayer;
import com.bobmowzie.mowziesmobs.server.ai.Cooldown;
import com.bobmowzie.mowziesmobs.server.bossinfo.MMBossInfoServer;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.world.spawn.SpawnHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class MowzieEntity extends PathfinderMob implements IEntityWithComplexSpawn, IntermittentAnimatableEntity {
    private static final byte START_IA_HEALTH_UPDATE_ID = 4;
    private static final byte MUSIC_PLAY_ID = 67;
    private static final byte MUSIC_STOP_ID = 68;

    public int frame;
    public float targetDistance = -1;
    public float targetAngle = -1;
    public boolean active;
    public LivingEntity blockingEntity = null;
    public boolean playsHurtAnimation = true;
    protected boolean dropAfterDeathAnim = true;
    public boolean hurtInterruptsAnimation = false;
    private final List<IntermittentAnimation<?>> intermittentAnimations = new ArrayList<>();

    public Vec3[] socketPosArray;

    protected boolean prevOnGround;
    protected boolean prevPrevOnGround;
    protected boolean willLandSoon;
    protected Vec3 prevDeltaMovement = Vec3.ZERO;
    
    private int killDataRecentlyHit;
    private DamageSource killDataCause;
    private Player killDataAttackingPlayer;

    protected final MMBossInfoServer bossInfo = initBossInfo();

    private static final ResourceLocation HEALTH_CONFIG_MODIFIER = ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "health_config_modifier");
    private static final ResourceLocation ATTACK_CONFIG_MODIFIER = ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "attack_config_modifier");

    private static final EntityDataAccessor<Boolean> STRAFING = SynchedEntityData.defineId(MowzieEntity.class, EntityDataSerializers.BOOLEAN);

    public boolean renderingInGUI = false;

    public Cooldown[] cooldowns;

    public MowzieEntity(EntityType<? extends MowzieEntity> type, Level world) {
        super(type, world);
        if (world.isClientSide) {
            socketPosArray = new Vec3[]{};
        }

        // Load config attribute multipliers
        ConfigHandler.CombatConfig combatConfig = getCombatConfig();
        if (combatConfig != null) {
            AttributeInstance maxHealthAttr = getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr != null) {
                double difference = maxHealthAttr.getBaseValue() * getCombatConfig().healthMultiplier.get() - maxHealthAttr.getBaseValue();
                maxHealthAttr.addTransientModifier(new AttributeModifier(HEALTH_CONFIG_MODIFIER, difference, AttributeModifier.Operation.ADD_VALUE));
                this.setHealth(this.getMaxHealth());
            }

            AttributeInstance attackDamageAttr = getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamageAttr != null) {
                double difference = attackDamageAttr.getBaseValue() * getCombatConfig().attackMultiplier.get() - attackDamageAttr.getBaseValue();
                attackDamageAttr.addTransientModifier(new AttributeModifier(ATTACK_CONFIG_MODIFIER, difference, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STRAFING, false);
    }

    public void setStrafing(boolean strafing) {
        entityData.set(STRAFING, strafing);
        if (!strafing) setXxa(0);
    }

    public boolean isStrafing() {
        return entityData.get(STRAFING);
    }

    protected ConfigHandler.SpawnConfig getSpawnConfig() {
        return null;
    }

    protected ConfigHandler.CombatConfig getCombatConfig() {
        return null;
    }

    public static boolean spawnPredicate(EntityType type, LevelAccessor world, MobSpawnType reason, BlockPos spawnPos, RandomSource rand) {
        if (!(world instanceof ServerLevelAccessor)) return false;
        ConfigHandler.SpawnConfig spawnConfig = SpawnHandler.SPAWN_CONFIGS.get(type);
        if (spawnConfig != null) {
            if (rand.nextDouble() > spawnConfig.extraRarity.get()) return false;

            // Dimension check
            if (reason != MobSpawnType.SPAWNER) {
                if (world instanceof ServerLevel serverLevel) {
                    List<? extends String> dimensionNames = spawnConfig.dimensions.get();
                    if (serverLevel.dimension() != null) {
                        ResourceLocation currDimensionName = serverLevel.dimension().location();
                        if (!dimensionNames.contains(currDimensionName.toString())) {
                            return false;
                        }
                    }
                }
            }

            // Height check
            float heightMax = spawnConfig.heightMax.get();
            float heightMin = spawnConfig.heightMin.get();
            if (spawnPos.getY() > heightMax && heightMax >= -64) {
                return false;
            }
            if (spawnPos.getY() < heightMin && heightMin >= -64) {
                return false;
            }

            // Light level check
            if (spawnConfig.needsDarkness.get() && !Monster.isDarkEnoughToSpawn((ServerLevelAccessor) world, spawnPos, rand)) {
                return false;
            }

            // Block check
            BlockState block = world.getBlockState(spawnPos.below());
            ResourceLocation blockName = block.getBlock().builtInRegistryHolder().key().location();
            List<? extends String> allowedBlocks = spawnConfig.allowedBlocks.get();
            List<? extends String> allowedBlockTags = spawnConfig.allowedBlockTags.get();
            if (blockName == null) return false;
            if (allowedBlocks.isEmpty() && allowedBlockTags.isEmpty()) {
                // If both lists are empty, use default block validation instead
                if (!block.isValidSpawn(world, spawnPos.below(), type)) {
                    return false;
                }
            }
            else {
                boolean isBlockAllowed = false;
                // if the block is in the block list, it's allowed
                if (!allowedBlocks.isEmpty() && (allowedBlocks.contains(blockName.toString()) || allowedBlocks.contains(blockName.getPath())))
                    isBlockAllowed = true;
                // if the block is already allowed, no need to check its tags. But if its not, see if it's in the tags.
                if (!isBlockAllowed && !allowedBlockTags.isEmpty() && isBlockTagAllowed(allowedBlockTags, block)) isBlockAllowed = true;
                // if after checking both, its still not allowed, return false
                if (!isBlockAllowed) {
                    return false;
                }
            }

            // See sky
            if (spawnConfig.needsSeeSky.get() && !world.canSeeSkyFromBelowWater(spawnPos)) {
                return false;
            }
            if (spawnConfig.needsCantSeeSky.get() && world.canSeeSkyFromBelowWater(spawnPos)) {
                return false;
            }

            List<? extends String> avoidStructures = spawnConfig.avoidStructures.get();
            Registry<StructureSet> structureSetRegistry = world.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
            ServerLevel serverLevel = (ServerLevel) world;
            ChunkGeneratorStructureState generatorState = serverLevel.getChunkSource().getGeneratorState();
            ChunkPos chunkPos = new ChunkPos(spawnPos);
            for (String structureName : avoidStructures) {
                Optional<StructureSet> structureSetOptional = structureSetRegistry.getOptional(ResourceLocation.tryParse(structureName));
                if (structureSetOptional.isEmpty()) continue;
                Optional<ResourceKey<StructureSet>> resourceKeyOptional = structureSetRegistry.getResourceKey(structureSetOptional.get());
                if (resourceKeyOptional.isEmpty()) continue;
                Optional<Holder.Reference<StructureSet>> holderOptional = structureSetRegistry.getHolder(resourceKeyOptional.get());
                if (holderOptional.isEmpty()) continue;
                if (generatorState.hasStructureChunkInRange(holderOptional.get(), chunkPos.x, chunkPos.z, 3)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isBlockTagAllowed(List<? extends String> allowedBlockTags, BlockState block) {
        for (String allowedBlockTag : allowedBlockTags) {
            ResourceLocation location = ResourceLocation.tryParse(allowedBlockTag);

            if (location == null) {
                continue;
            }

            TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, location);
            if (block.is(tagKey)) return true;
        }
        return false;
    }

    protected boolean isWithinDistance(BlockPos pos, int distance) {
        return pos.closerThan(this.blockPosition(), (double)distance);
    }

    @Override // Copied from Mob class file
    public void checkDespawn() {
        if (EventHooks.checkMobDespawn(this)) return;
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
        } else if (!this.isPersistenceRequired() && !this.requiresCustomPersistence()) {
            Entity entity = this.level().getNearestPlayer(this, -1);

            if (entity != null) {
                double distance = entity.distanceToSqr(this);
                int despawnDistance = getDespawnDistance();
                int despawnRadius = despawnDistance * despawnDistance;

                if (distance > despawnRadius && this.removeWhenFarAway(distance)) {
                    this.discard();
                }

                int noDespawnDistance = getNoDespawnDistance();
                int noDespawnRadius = noDespawnDistance * noDespawnDistance;

                if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && distance > noDespawnRadius && this.removeWhenFarAway(distance)) {
                    this.discard();
                } else if (distance < noDespawnRadius) {
                    this.noActionTime = 0;
                }
            }
        } else {
            this.noActionTime = 0;
        }
    }

    public int getDespawnDistance() {
        return this.getType().getCategory().getDespawnDistance();
    }

    public int getNoDespawnDistance() {
        return this.getType().getCategory().getNoDespawnDistance();
    }

    public Vec3 getPrevDeltaMovement() {
        return prevDeltaMovement;
    }

    @Override
    public void tick() {
        prevPrevOnGround = prevOnGround;
        prevOnGround = onGround();
        prevDeltaMovement = getDeltaMovement();
        super.tick();
        frame++;
        if (tickCount % 4 == 0) bossInfo.update();
        if (getTarget() != null) {
            targetDistance = distanceTo(getTarget()) - getTarget().getBbWidth() / 2f;
            targetAngle = (float) getAngleBetweenEntities(this, getTarget());
        }
        willLandSoon = !onGround() && level().noCollision(getBoundingBox().move(getDeltaMovement()));

        if (!level().isClientSide && hasBossMusic()) {
            if (canPlayMusic()) {
                this.level().broadcastEntityEvent(this, MUSIC_PLAY_ID);
            }
            else {
                this.level().broadcastEntityEvent(this, MUSIC_STOP_ID);
            }
        }

        if (cooldowns != null) {
            for (Cooldown cooldown : cooldowns) {
                cooldown.tick();
            }
        }
    }

    protected boolean canPlayMusic() {
        return !isSilent() && getTarget() instanceof Player;
    }

    public boolean canPlayerHearMusic(Player player) {
        return player != null
                && canAttack(player)
                && distanceTo(player) < 2500;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
    }

    @Override
    public void writeSpawnData(@NotNull RegistryFriendlyByteBuf buffer) {

    }

    @Override
    public void readSpawnData(@NotNull RegistryFriendlyByteBuf buffer) {
        yRotO = getYRot();
        yBodyRotO = yBodyRot = yHeadRotO = yHeadRot;
    }

    @Override
    public boolean doHurtTarget(Entity entityIn) {
        return this.doHurtTarget(entityIn, 1.0F, 1.0f);
    }

    public boolean doHurtTarget(Entity entityIn, float damageMultiplier, float applyKnockbackMultiplier) {
        return doHurtTarget(entityIn, damageMultiplier, applyKnockbackMultiplier, false);
    }

    /** Mostly a copy from {@link Mob#doHurtTarget(Entity)} */
    public boolean doHurtTarget(Entity target, float damageMultiplier, float applyKnockbackMultiplier, boolean canDisableShield) {
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
        DamageSource damagesource = damageSources().mobAttack(this);

        if (level() instanceof ServerLevel serverLevel) {
            damage = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), target, damagesource, damage);
        }

        boolean wasHurt = target.hurt(damagesource, damage);

        if (wasHurt) {
            if (target instanceof LivingEntity livingTarget) {
                // FIXME 1.21 :: this no longer just increases the attribute value but rather the total result
                float knockback = getKnockback(livingTarget, damagesource) * applyKnockbackMultiplier;

                if (knockback > 0) {
                    livingTarget.knockback(
                            knockback * 0.5F,
                            Mth.sin(getYRot() * (float) (Math.PI / 180.0)),
                            -Mth.cos(getYRot() * (float) (Math.PI / 180.0))
                    );

                    setDeltaMovement(getDeltaMovement().multiply(0.6, 1, 0.6));
                }
            }

            if (level() instanceof ServerLevel serverLevel) {
                // Handle fire aspect etc.
                EnchantmentHelper.doPostAttackEffects(serverLevel, target, damagesource);
            }

            setLastHurtMob(target);
            playAttackSound();
        }

        return wasHurt;
    }

    public float getHealthRatio() {
        return this.getHealth() / this.getMaxHealth();
    }

    public double getAngleBetweenEntities(Entity first, Entity second) {
        return Math.atan2(second.getZ() - first.getZ(), second.getX() - first.getX()) * (180 / Math.PI) + 90;
    }
    
    public double getDotProductBodyFacingEntity(Entity second) {
        Vec3 vecBetween = second.position().subtract(this.position());
        vecBetween = vecBetween.normalize();
        return vecBetween.dot(Vec3.directionFromRotation(0, yBodyRot).normalize());
    }

    public List<Player> getPlayersNearby(double distanceX, double distanceY, double distanceZ, double radius) {
        List<Entity> nearbyEntities = level().getEntities(this, getBoundingBox().inflate(distanceX, distanceY, distanceZ));
        List<Player> listEntityPlayers = nearbyEntities.stream().filter(entityNeighbor -> entityNeighbor instanceof Player && distanceTo(entityNeighbor) <= radius + entityNeighbor.getBbWidth() / 2f).map(entityNeighbor -> (Player) entityNeighbor).collect(Collectors.toList());
        return listEntityPlayers;
    }

    public List<LivingEntity> getAttackableEntityLivingBaseNearby(double distanceX, double distanceY, double distanceZ, double radius) {
        List<Entity> nearbyEntities = level().getEntities(this, getBoundingBox().inflate(distanceX, distanceY, distanceZ));
        List<LivingEntity> listEntityLivingBase = nearbyEntities.stream().filter(entityNeighbor -> entityNeighbor instanceof LivingEntity && ((LivingEntity)entityNeighbor).attackable() && (!(entityNeighbor instanceof Player) || !((Player)entityNeighbor).isCreative()) && distanceTo(entityNeighbor) <= radius + entityNeighbor.getBbWidth() / 2f).map(entityNeighbor -> (LivingEntity) entityNeighbor).collect(Collectors.toList());
        return listEntityLivingBase;
    }

    public  List<LivingEntity> getEntityLivingBaseNearby(double distanceX, double distanceY, double distanceZ, double radius) {
        return getEntitiesNearby(LivingEntity.class, distanceX, distanceY, distanceZ, radius);
    }

    public <T extends Entity> List<T> getEntitiesNearby(Class<T> entityClass, double r) {
        return level().getEntitiesOfClass(entityClass, getBoundingBox().inflate(r, r, r), e -> e != this && distanceTo(e) <= r + e.getBbWidth() / 2f);
    }

    public <T extends Entity> List<T> getEntitiesNearby(Class<T> entityClass, double dX, double dY, double dZ, double r) {
        return level().getEntitiesOfClass(entityClass, getBoundingBox().inflate(dX, dY, dZ), e -> e != this && distanceTo(e) <= r + e.getBbWidth() / 2f && e.getY() <= getY() + dY);
    }

    @Override
    protected void tickDeath() { // Copied from entityLiving
        ++this.deathTime;
        int deathDuration = getDeathDuration();
        if (this.deathTime >= deathDuration && level() instanceof ServerLevel serverLevel) {
            lastHurtByPlayer = killDataAttackingPlayer;
            lastHurtByPlayerTime = killDataRecentlyHit;
            if (dropAfterDeathAnim && killDataCause != null) {
                dropAllDeathLoot(serverLevel, killDataCause);
            }
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    protected abstract int getDeathDuration();

    @Override
    protected void dropAllDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source) {
        if (!dropAfterDeathAnim || deathTime > 0) {
            super.dropAllDeathLoot(level, source);
        }
    }

    @Override
    public void die(DamageSource cause) {
        if (!this.dead) {
            killDataCause = cause;
            killDataRecentlyHit = this.lastHurtByPlayerTime;
            killDataAttackingPlayer = lastHurtByPlayer;
        }
        super.die(cause);
        if (!this.isRemoved()) {
            bossInfo.update();
        }
    }

    protected void addIntermittentAnimation(IntermittentAnimation animation) {
        animation.setID((byte) intermittentAnimations.size());
        intermittentAnimations.add(animation);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id >= START_IA_HEALTH_UPDATE_ID && id - START_IA_HEALTH_UPDATE_ID < intermittentAnimations.size()) {
            intermittentAnimations.get(id - START_IA_HEALTH_UPDATE_ID).start();
        }
        else if (id == MUSIC_PLAY_ID) {
            BossMusicPlayer.requestBossMusic(this);
        }
        else if (id == MUSIC_STOP_ID) {
            BossMusicPlayer.stopBossMusic(this);
        }
        else super.handleEntityEvent(id);
    }

    @Override
    public byte getOffsetEntityState() {
        return START_IA_HEALTH_UPDATE_ID;
    }

    public Vec3 circleEntityPosition(Entity target, float radius, float speed, boolean direction, int circleFrame, float offset) {
        int directionInt = direction ? 1 : -1;
        double t = directionInt * circleFrame * 0.5 * speed / radius + offset;
        Vec3 movePos = target.position().add(radius * Math.cos(t), 0, radius * Math.sin(t));
        return movePos;
    }

    protected void repelEntities(float x, float y, float z, float radius) {
        List<LivingEntity> nearbyEntities = getEntityLivingBaseNearby(x, y, z, radius);
        for (Entity entity : nearbyEntities) {
            if (entity.isPickable() && !entity.noPhysics) {
                double angle = (getAngleBetweenEntities(this, entity) + 90) * Math.PI / 180;
                entity.setDeltaMovement(-0.1 * Math.cos(angle), entity.getDeltaMovement().y, -0.1 * Math.sin(angle));
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (this.hasCustomName()) {
            this.bossInfo.setName(this.getDisplayName());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (cooldowns != null) {
            for (Cooldown cooldown : cooldowns) {
                compound.putInt(cooldown.getName(), cooldown.getTimer());
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (cooldowns != null) {
            for (Cooldown cooldown : cooldowns) {
                cooldown.setTimer(compound.getInt(cooldown.getName()));
            }
        }
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossInfo.setName(this.getDisplayName());
    }

    public boolean hasBossBar() {
        return false;
    }

    protected MMBossInfoServer initBossInfo() {
        return new MMBossInfoServer(this);
    }

    public boolean resetHealthOnPlayerRespawn() {
        return false;
    }

    public BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.PURPLE;
    }

    public void setSocketPosArray(int index, Vec3 pos) {
        if (socketPosArray != null && socketPosArray.length > index) {
            socketPosArray[index] = pos;
        }
    }

    public boolean canBePushedByEntity(Entity entity) {
        return true;
    }

    // TODO: Copied from parent classes
    @Override
    public void push(Entity entityIn) {
        if (!this.isSleeping()) {
            if (!this.isPassengerOfSameVehicle(entityIn)) {
                if (!entityIn.noPhysics && !this.noPhysics) {
                    double d0 = entityIn.getX() - this.getX();
                    double d1 = entityIn.getZ() - this.getZ();
                    double d2 = Mth.absMax(d0, d1);
                    if (d2 >= (double)0.01F) {
                        d2 = Math.sqrt(d2);
                        d0 = d0 / d2;
                        d1 = d1 / d2;
                        double d3 = 1.0D / d2;
                        if (d3 > 1.0D) {
                            d3 = 1.0D;
                        }

                        d0 = d0 * d3;
                        d1 = d1 * d3;
                        d0 = d0 * (double)0.05F;
                        d1 = d1 * (double)0.05F;
                        if (!this.isVehicle()) {
                            if (canBePushedByEntity(entityIn)) {
                                this.push(-d0, 0.0D, -d1);
                            }
                        }

                        if (!entityIn.isVehicle()) {
                            entityIn.push(d0, 0.0D, d1);
                        }
                    }

                }
            }
        }
    }

    /** For common usage (loading the BossMusic class loads the client 'SoundInstance' class for some reason) */
    public boolean hasBossMusic() {
        return false;
    }

    public BossMusic<?> getBossMusic() {
        return null;
    }

    public void playSound(List<DeferredHolder<SoundEvent, SoundEvent>> sounds, float volume, float pitch) {
        SoundEvent sound = sounds.get(random.nextInt(sounds.size())).get();
        playSound(sound, volume, pitch);
    }
}
