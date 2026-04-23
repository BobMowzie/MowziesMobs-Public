package com.bobmowzie.mowziesmobs.server.entity.effects.geomancy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityFissurePiece extends Entity {
    public static final float PIECE_SIZE = 2f;
    private static final EntityDataAccessor<Integer> GROW_TICK = SynchedEntityData.defineId(EntityFissurePiece.class, EntityDataSerializers.INT);

    @Nullable
    private EntityFissure owner;
    @Nullable
    private UUID ownerUUID;

    public EntityFissurePiece(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (getOwner() == null || getOwner().isRemoved()) {
                discard();
            }
        }

        if (!level().isClientSide() && getGrowTick() < EntityFissure.TICKS_PER_PIECE && owner != null && owner.isTravelling()) {
            getEntityData().set(GROW_TICK, getGrowTick() + 1);
        }
    }

    @Override
    protected void defineSynchedData() {
        getEntityData().define(GROW_TICK, 0);
    }

    public void setOwner(@Nullable EntityFissure owner) {
        this.owner = owner;
        this.ownerUUID = owner == null ? null : owner.getUUID();
    }

    @Nullable
    public EntityFissure getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel) {
            Entity entity = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
            if (entity instanceof EntityFissure) {
                this.owner = (EntityFissure)entity;
            }
        }

        return this.owner;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
        }
        getEntityData().set(GROW_TICK, compound.getInt("growTick"));

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }
        compound.putInt("growTick", getGrowTick());
    }

    public int getGrowTick() {
        return getEntityData().get(GROW_TICK);
    }
}
