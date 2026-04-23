package com.bobmowzie.mowziesmobs.server.entity.umvuthana;

import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleBase;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.server.ability.AbilityHandler;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.bobmowzie.mowziesmobs.server.capability.PlayerData;
import com.bobmowzie.mowziesmobs.server.entity.MowzieEntity;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import com.bobmowzie.mowziesmobs.server.item.ItemUmvuthanaMask;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class EntityUmvuthanaFollowerToPlayer extends EntityUmvuthanaFollower<Player> {
    private static final EntityDataAccessor<ItemStack> MASK_STORED = SynchedEntityData.defineId(EntityUmvuthanaFollowerToPlayer.class, EntityDataSerializers.ITEM_STACK);
    public Vec3[] feetPos;

    public EntityUmvuthanaFollowerToPlayer(EntityType<? extends EntityUmvuthanaFollowerToPlayer> type, Level world) {
        this(type, world, null);
    }

    public EntityUmvuthanaFollowerToPlayer(EntityType<? extends EntityUmvuthanaFollowerToPlayer> type, Level world, Player leader) {
        super(type, world, Player.class, leader);
        xpReward = 0;
        if (world.isClientSide) {
            feetPos = new Vec3[]{new Vec3(0, 0, 0)};
        }
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MASK_STORED, new ItemStack(ItemHandler.UMVUTHANA_MASK_FURY.get(), 1));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (random.nextFloat() < 0.5) return null;
        return super.getAmbientSound();
    }

    @Override
    public int getAttackChance() {
        if (getPackSize() < 0) return super.getAttackChance();
        return 8 * getPackSize() + 30;
    }

    @Override
    public int getAttackCooldown() {
        if (getPackSize() < 0) return super.getAttackCooldown();
        return 8 * getPackSize() + 30;
    }

    @Override
    public void tick() {
        if (tickCount > 30 && (getLeader() == null || getLeader().getHealth() <= 0)) {
            deactivate();
        }
        super.tick();
        if (level().isClientSide && feetPos != null && feetPos.length > 0 && active) {
            feetPos[0] = position().add(0, 0.05f, 0);
            if (tickCount % 10 == 0) AdvancedParticleBase.spawnParticle(level(), ParticleHandler.RING2, feetPos[0].x(), feetPos[0].y(), feetPos[0].z(), 0, 0, 0, false, 0, Math.PI/2f, 0, 0, 1.5F, 1, 223 / 255f, 66 / 255f, 1, 1, 15, true, false, new ParticleComponent[]{
                    new ParticleComponent.PinLocation(feetPos),
                    new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, ParticleComponent.KeyTrack.startAndEnd(1f, 0f), false),
                    new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, ParticleComponent.KeyTrack.startAndEnd(1f, 10f), false)
            });
        }
    }

    @Override
    protected InteractionResult mobInteract(Player playerIn, InteractionHand hand) {
        if (playerIn == leader) {
            deactivate();
        }
        return super.mobInteract(playerIn, hand);
    }

    private void deactivate() {
        if (getActive() && getActiveAbilityType() != DEACTIVATE_ABILITY) {
            AbilityHandler.INSTANCE.sendAbilityMessage(this, DEACTIVATE_ABILITY);
            playSound(MMSounds.ENTITY_UMVUTHANA_RETRACT.get(), 1, 1);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return MowzieEntity.createAttributes().add(Attributes.ATTACK_DAMAGE, 7)
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.STEP_HEIGHT, 1);
    }

    @Override
    protected int getGroupCircleTick() {
        PlayerData data = getPlayerData();
        if (data == null) return 0;
        return data.getPackCircleTick();
    }

    @Override
    protected int getPackSize() {
        PlayerData data = getPlayerData();
        if (data == null) return 0;
        return data.getPackSize();
    }

    @Override
    protected void addAsPackMember() {
        PlayerData data = getPlayerData();
        if (data == null) return;
        data.addPackMember(this);
    }

    @Override
    protected void removeAsPackMember() {
        PlayerData data = getPlayerData();
        if (data == null) return;
        data.removePackMember(this);
    }

    private @Nullable PlayerData getPlayerData() {
        if (leader instanceof Player) {
            return DataHandler.getData(leader, DataHandler.PLAYER_DATA);
        }

        return null;
    }

    @Override
    public boolean isUmvuthiDevoted() {
        return false;
    }

    @Override
    protected @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    public boolean isTeleportFriendlyBlock(int x, int z, int y, int xOffset, int zOffset)
    {
        BlockPos blockpos = new BlockPos(x + xOffset, y - 1, z + zOffset);
        BlockState iblockstate = this.level().getBlockState(blockpos);
        return iblockstate.isValidSpawn(this.level(), blockpos, this.getType()) && this.level().isEmptyBlock(blockpos.above()) && this.level().isEmptyBlock(blockpos.above(2));
    }

    public ItemStack getStoredMask() {
        return getEntityData().get(MASK_STORED);
    }

    public void setStoredMask(ItemStack mask) {
        getEntityData().set(MASK_STORED, mask);
        setItemSlot(EquipmentSlot.HEAD, mask);
    }

    @Override
    protected ItemStack getDeactivatedMask(ItemUmvuthanaMask mask) {
        return getStoredMask();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        CompoundTag compoundnbt = compound.getCompound("storedMask");
        this.setStoredMask(ItemStack.parse(registryAccess(), compoundnbt).orElse(ItemStack.EMPTY));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (!this.getStoredMask().isEmpty()) {
            compound.put("storedMask", this.getStoredMask().save(registryAccess(), new CompoundTag()));
        }
    }
}
