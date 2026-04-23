package com.bobmowzie.mowziesmobs.server.entity.effects.geomancy;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.client.particle.AdvancedTerrainParticle;
import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.client.sound.IGeomancyRumbler;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.bluff.EntityBluff;
import com.bobmowzie.mowziesmobs.server.potion.EffectGeomancy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityFissure extends Projectile implements IGeomancyRumbler {
    public static int TICKS_PER_PIECE = 5;
    private static final EntityDataAccessor<Boolean> TRAVELLING = SynchedEntityData.defineId(EntityFissure.class, EntityDataSerializers.BOOLEAN);
    private int despawnTimer = 0;

    public EntityFissure(EntityType<? extends EntityFissure> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void defineSynchedData() {
        getEntityData().define(TRAVELLING, true);
    }

    public boolean isTravelling() {
        return getEntityData().get(TRAVELLING);
    }

    public void setTravelling(boolean travelling) {
        getEntityData().set(TRAVELLING, travelling);
    }

    public void shoot(double vx, double vz) {
        float speed = EntityFissurePiece.PIECE_SIZE / (float) TICKS_PER_PIECE;
        Vec3 vec3 = (new Vec3(vx, 0, vz)).normalize().scale(speed);
        this.setDeltaMovement(vec3);
        this.setYRot(-(float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
        this.yRotO = this.getYRot();
    }

    @Override
    public void tick() {
        super.tick();
        if (isTravelling()) {
            stepForwardTrace();
        }

        if (tickCount > 60 && isTravelling()) spawnSpike();
        if (despawnTimer > 0) {
            despawnTimer--;
            if (despawnTimer == 1) discard();
        }

        if (!level().isClientSide()) {
            if (isTravelling() && tickCount % TICKS_PER_PIECE == 1f) {
                EntityFissurePiece piece = new EntityFissurePiece(EntityHandler.FISSURE_PIECE.get(), level());
                piece.setPos(position().add(getDeltaMovement().scale(TICKS_PER_PIECE/3f)));
                piece.setYRot(getYRot());
                piece.setOwner(this);
                level().addFreshEntity(piece);
            }

            if (isTravelling() && !level().getEntities(this, getBoundingBox().inflate(0.3), e -> e.canBeHitByProjectile() && e != getOwner() && !(e instanceof EntityBluff)).isEmpty()) {
                spawnSpike();
            }
        }
        else {
            if (tickCount == 1) {
                MowziesMobs.PROXY.playGeomancyRumbleSound(this);
            }
            if (isTravelling()) {
                BlockState blockBeneath = level().getBlockState(getOnPos());
                for (int i = 0; i < 10; i++) {
                    Vec3 offset = new Vec3(random.nextFloat() * 0.45, 0, 0).yRot(random.nextFloat() * (float) Math.PI * 2f);
                    Vec3 vel = offset.normalize().scale(random.nextGaussian() * 0.12).yRot(random.nextFloat() * 0.2f - 0.1f).add(0, random.nextDouble() * 0.25 + 0.02, 0).add(getDeltaMovement().scale(0.4));
                    AdvancedTerrainParticle.spawnTerrainParticle(level(), ParticleHandler.TERRAIN.get(), getX() + offset.x, getY(), getZ() + offset.z, vel.x, vel.y, vel.z, 0, 0.4f + random.nextGaussian() * 0.3, 0.94f, 20 + random.nextFloat() * 5, blockBeneath, new ParticleComponent[]{
                            new ParticleComponent.Gravity(1)
                    });
                }
            }
        }
    }

    public void stepForwardTrace() {
        Vec3 forwardPos = position().add(getDeltaMovement());
        Vec3 startPos = forwardPos.add(0, 1.1, 0);
        Vec3 endPos = forwardPos.add(0, -1.1, 0);
        BlockHitResult result = level().clip(new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        if (result.getType() != HitResult.Type.BLOCK || result.isInside()) {
            spawnSpike();
            return;
        }
        setPos(result.getLocation());
        if (this.level() instanceof ServerLevel) {
            ((ServerLevel) this.level()).getChunkSource().broadcast(this, new ClientboundTeleportEntityPacket(this));
        }
    }

    private void spawnSpike() {
        if (isTravelling()) {
            if (!level().isClientSide()) {
                BlockState state = level().getBlockState(getOnPos());
                if (!EffectGeomancy.isBlockUseable(state)) {
                    state = Blocks.DIRT.defaultBlockState();
                }
                EntityEarthSpike spike = new EntityEarthSpike(EntityHandler.EARTH_SPIKE.get(), level(), (LivingEntity) getOwner(), state);
                spike.setPos(position());
                spike.setYRot(getYRot());
                level().addFreshEntity(spike);
            }
            setTravelling(false);
            despawnTimer = 180;
            setDeltaMovement(0,0,0);
        }
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putShort("despawnTimer", (short)this.despawnTimer);
        compound.putBoolean("travelling", isTravelling());
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.despawnTimer = compound.getShort("despawnTimer");
        setTravelling(compound.getBoolean("travelling"));
    }

    @Override
    public boolean isRumbling() {
        return isTravelling();
    }

    @Override
    public boolean isFinishedRumbling() {
        return !isTravelling();
    }

    @Override
    public float getRumblerX() {
        return (float) getX();
    }

    @Override
    public float getRumblerY() {
        return (float) getY();
    }

    @Override
    public float getRumblerZ() {
        return (float) getZ();
    }

    @Override
    public float getRumblePitch() {
        return 1.3f;
    }

    @Override
    public float getRumbleVolume() {
        return 0.5f;
    }
}
