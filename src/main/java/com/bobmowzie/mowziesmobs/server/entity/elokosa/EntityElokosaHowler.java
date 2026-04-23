package com.bobmowzie.mowziesmobs.server.entity.elokosa;

import com.bobmowzie.mowziesmobs.server.ai.ElokosaHurtByTargetGoal;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.MowzieEntity;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthi;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityElokosaHowler extends EntityElokosa {
    private final List<EntityElokosaFollowerToHowler> pack = new ArrayList<>();

    public EntityElokosaHowler(EntityType<? extends MowzieEntity> type, Level world) {
        super(type, world);
        this.xpReward = 8;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new ElokosaHurtByTargetGoal(this, true));
    }

    @Override
    protected void registerTargetGoals() {
        super.registerTargetGoals();
        registerHuntingTargetGoals();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityElokosa.createAttributes().add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.MAX_HEALTH, 20);
    }

    @Override
    public boolean isHowler() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        for (int i = 0; i < pack.size(); i++) {
            pack.get(i).index = i;
        }

        if (!this.level().isClientSide && this.level().getDifficulty() == Difficulty.PEACEFUL)
        {
            this.discard();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (tickCount == 0) {
            pack.forEach(EntityElokosaFollowerToHowler::setShouldSetDead);
        }
        pack.forEach(EntityElokosaFollowerToHowler::removeLeader);
        super.remove(reason);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader worldReader) {
        if (tickCount == 0) {
            return !worldReader.containsAnyLiquid(this.getBoundingBox()) && worldReader.noCollision(this);
        }
        else {
            return !worldReader.containsAnyLiquid(this.getBoundingBox()) && worldReader.noCollision(this) && this.level().isUnobstructed(this);
        }
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason) {
        List<LivingEntity> nearby = getEntityLivingBaseNearby(20, 10, 20, 20);
        for (LivingEntity nearbyEntity : nearby) {
            if (nearbyEntity instanceof EntityElokosaHowler || nearbyEntity instanceof Villager || nearbyEntity instanceof EntityUmvuthi || nearbyEntity instanceof Animal) {
                return false;
            }
        }
        return super.checkSpawnRules(world, reason) && world.getDifficulty() != Difficulty.PEACEFUL && (world.getTimeOfDay(0) >= 0.7609 || world.getTimeOfDay(0) < 0.23918849);
    }

    public int getMaxSpawnClusterSize()
    {
        return 1;
    }

    public void removePackMember(EntityElokosaFollowerToHowler member) {
        pack.remove(member);
    }

    public void addPackMember(EntityElokosaFollowerToHowler member) {
        pack.add(member);
    }

    public int getPackSize() {
        return pack.size();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingData) {
        int size = random.nextInt(2) + 2;
        float theta = (2 * (float) Math.PI / size);
        for (int i = 0; i <= size; i++) {
            EntityElokosaFollowerToHowler packMember = new EntityElokosaFollowerToHowler(EntityHandler.ELOKOSA_FOLLOWER_TO_HOWLER.get(), this.level(), this);
            packMember.setPos(getX() + 0.1 * Mth.cos(theta * i), getY(), getZ() + 0.1 * Mth.sin(theta * i));
            world.addFreshEntity(packMember);
            packMember.finalizeSpawn(world, difficulty, reason, livingData);
        }
        return super.finalizeSpawn(world, difficulty, reason, livingData);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        pack.forEach(EntityElokosaFollowerToHowler::removeLeader);
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return new ItemStack(ItemHandler.ELOKOSA_HOWLER_SPAWN_EGG.get());
    }

    @Override
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
                    if (pack != null) {
                        pack.forEach(EntityElokosaFollowerToHowler::setShouldSetDead);
                    }
                    this.discard();
                }

                int noDespawnDistance = getNoDespawnDistance();
                int noDespawnRadius = noDespawnDistance * noDespawnDistance;

                if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && distance > noDespawnRadius && this.removeWhenFarAway(distance)) {
                    if (pack != null) {
                        pack.forEach(EntityElokosaFollowerToHowler::setShouldSetDead);
                    }
                    this.discard();
                } else if (distance < noDespawnRadius) {
                    this.noActionTime = 0;
                }
            }
        } else {
            this.noActionTime = 0;
        }
    }
}
