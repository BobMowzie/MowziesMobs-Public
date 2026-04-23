package com.bobmowzie.mowziesmobs.server.entity.effects;

import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.bobmowzie.mowziesmobs.server.capability.PlayerData;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by BobMowzie on 7/15/2017.
 */
public class EntityAxeAttack extends EntityMagicEffect {
    private static final EntityDataAccessor<Boolean> VERTICAL = SynchedEntityData.defineId(EntityAxeAttack.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> AXE_STACK = SynchedEntityData.defineId(EntityAxeAttack.class, EntityDataSerializers.ITEM_STACK);

    public static int SWING_DURATION_HOR = 24;
    public static int SWING_DURATION_VER = 30;
    private float quakeAngle = 0;
    private AABB quakeBB = new AABB(0, 0, 0, 1, 1, 1);

    public EntityAxeAttack(EntityType<? extends EntityAxeAttack> type, Level world) {
        super(type, world);
    }

    public EntityAxeAttack(EntityType<? extends EntityAxeAttack> type, Level world, LivingEntity caster, boolean vertical) {
        super(type, world, caster);
        setVertical(vertical);
        setAxeStack(caster.getMainHandItem());
    }
    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VERTICAL, false);
        builder.define(AXE_STACK, ItemHandler.WROUGHT_AXE.get().getDefaultInstance());
    }

    @Override
    public void tick() {
        super.tick();
        if (getCaster() != null) {
            if (!getCaster().isAlive()) discard();
            absMoveTo(getCaster().getX(), getCaster().getY() + getCaster().getEyeHeight(), getCaster().getZ(), getCaster().getYRot(), getCaster().getXRot());
        }
        if (!level().isClientSide && tickCount == 7) playSound(MMSounds.ENTITY_WROUGHT_WHOOSH.get(), 0.7F, 1.1f);
        if (!level().isClientSide && getCaster() != null) {
            if (!getVertical() && tickCount == SWING_DURATION_HOR /2 - 1) dealDamage(7.0f * ConfigHandler.COMMON.TOOLS_AND_ABILITIES.AXE_OF_A_THOUSAND_METALS.toolConfig.attackDamage.get().floatValue() / 9.0f, 4f, 160, 1.2f);
            else if (getVertical() && tickCount == SWING_DURATION_VER /2 - 1) {
                dealDamage(ConfigHandler.COMMON.TOOLS_AND_ABILITIES.AXE_OF_A_THOUSAND_METALS.toolConfig.attackDamage.get().floatValue(), 4.5f, 40, 0.8f);
                quakeAngle = getYRot();
                quakeBB = getBoundingBox().move(0, -getCaster().getEyeHeight(), 0);
                playSound(MMSounds.ENTITY_WROUGHT_AXE_LAND.get(), 0.3F, 0.5F);
                playSound(SoundEvents.GENERIC_EXPLODE.value(), 2, 0.9F + random.nextFloat() * 0.1F);
            }
            else if (getVertical() && tickCount == SWING_DURATION_VER /2 + 1) {
                EntityCameraShake.cameraShake(level(), position(), 10, 0.05f, 0, 10);
            }
        }

        if (getVertical() && getCaster() != null) {
            if (tickCount >= SWING_DURATION_VER /2) {
                int maxDistance = 16;
                double perpFacing = quakeAngle * (Math.PI / 180);
                double facingAngle = perpFacing + Math.PI / 2;
                int hitY = Mth.floor(quakeBB.minY - 0.5);
                int distance = tickCount - 15;
                double spread = Math.PI * 0.35F;
                int arcLen = Mth.ceil(distance * spread);
                double minY = quakeBB.minY;
                double maxY = quakeBB.maxY;
                for (int i = 0; i < arcLen; i++) {
                    double theta = (i / (arcLen - 1.0) - 0.5) * spread + facingAngle;
                    double vx = Math.cos(theta);
                    double vz = Math.sin(theta);
                    double px = getX() + vx * distance;
                    double pz = getZ() + vz * distance;
                    float factor = 1 - distance / (float) maxDistance;
                    AABB selection = new AABB(px - 1.5, minY, pz - 1.5, px + 1.5, maxY, pz + 1.5);
                    List<Entity> hit = level().getEntitiesOfClass(Entity.class, selection);
                    for (Entity entity : hit) {
                        if (entity.onGround()) {
                            if (entity == this || entity instanceof FallingBlockEntity || entity == getCaster()) {
                                continue;
                            }
                            float applyKnockbackResistance = 0;
                            boolean hitEntity = false;
                            if (!raytraceCheckEntity(entity)) continue;

                            if (getCaster() instanceof Player)
                                hitEntity = entity.hurt(damageSources().playerAttack((Player) getCaster()), (factor * 5 + 1) * (ConfigHandler.COMMON.TOOLS_AND_ABILITIES.AXE_OF_A_THOUSAND_METALS.toolConfig.attackDamage.get().floatValue() / 9.0f));
                            else
                                hitEntity = entity.hurt(damageSources().mobAttack(getCaster()), (factor * 5 + 1) * (ConfigHandler.COMMON.TOOLS_AND_ABILITIES.AXE_OF_A_THOUSAND_METALS.toolConfig.attackDamage.get().floatValue() / 9.0f));
                            if (entity instanceof LivingEntity) {
                                applyKnockbackResistance = (float) ((LivingEntity) entity).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
                            }
                            if (hitEntity) {
                                double magnitude = -4;
                                double x = vx * (1 - factor) * magnitude * (1 - applyKnockbackResistance);
                                double y = 0;
                                if (entity.onGround()) {
                                    y += 0.15 * (1 - applyKnockbackResistance);
                                }
                                double z = vz * (1 - factor) * magnitude * (1 - applyKnockbackResistance);
                                entity.setDeltaMovement(entity.getDeltaMovement().add(x, y, z));
                                if (entity instanceof ServerPlayer) {
                                    ((ServerPlayer) entity).connection.send(new ClientboundSetEntityMotionPacket(entity));
                                }
                            }
                        }
                    }
                    if (level().random.nextBoolean()) {
                        int hitX = Mth.floor(px);
                        int hitZ = Mth.floor(pz);
                        BlockPos pos = new BlockPos(hitX, hitY, hitZ);
                        BlockPos abovePos = new BlockPos(pos).above();
                        BlockState block = level().getBlockState(pos);
                        BlockState blockAbove = level().getBlockState(abovePos);
                        if (!block.isAir() && block.isRedstoneConductor(level(), pos) && !block.hasBlockEntity() && !blockAbove.blocksMotion()) {
                            EntityFallingBlock fallingBlock = new EntityFallingBlock(EntityHandler.FALLING_BLOCK.get(), level(), block, 0.3f);
                            fallingBlock.setPos(hitX + 0.5, hitY + 1, hitZ + 0.5);
                            level().addFreshEntity(fallingBlock);
                        }
                    }
                }
            }
        }
        if (tickCount > SWING_DURATION_HOR) discard();
    }

    private void dealDamage(float damage, float range, float arc, float applyKnockback) {
        boolean hit = false;
        List<Entity> entitiesHit = getEntitiesNearby(range, 2, range, range);
        for (Entity entityHit : entitiesHit) {
            if (entityHit instanceof ItemEntity) continue;
            float entityHitAngle = (float) ((Math.atan2(entityHit.getZ() - getZ(), entityHit.getX() - getX()) * (180 / Math.PI) - 90) % 360);
            float entityAttackingAngle = getYRot() % 360;
            if (entityHitAngle < 0) {
                entityHitAngle += 360;
            }
            if (entityAttackingAngle < 0) {
                entityAttackingAngle += 360;
            }
            float entityRelativeAngle = entityHitAngle - entityAttackingAngle;
            float entityHitDistance = (float) Math.sqrt((entityHit.getZ() - getZ()) * (entityHit.getZ() - getZ()) + (entityHit.getX() - getX()) * (entityHit.getX() - getX())) - entityHit.getBbWidth() / 2f;
            if (entityHit != getCaster() && (!(entityHit instanceof Parrot) || entityHit.getVehicle() != getCaster()) && entityHitDistance <= range && entityRelativeAngle <= arc / 2 && entityRelativeAngle >= -arc / 2 || entityRelativeAngle >= 360 - arc / 2 || entityRelativeAngle <= -360 + arc / 2) {
                // Do raycast check to prevent damaging through walls
                if (!raytraceCheckEntity(entityHit)) continue;

                if (getCaster() instanceof Player player) {
                    PlayerData data = DataHandler.getData(player, DataHandler.PLAYER_DATA);
                    data.setAxeCanAttack(true);
                    attackTargetEntityWithCurrentItem(entityHit, player, damage / ConfigHandler.COMMON.TOOLS_AND_ABILITIES.AXE_OF_A_THOUSAND_METALS.toolConfig.attackDamage.get().floatValue(), applyKnockback);
                    data.setAxeCanAttack(false);
                }

                hit = true;
            }
        }
        if (hit) {
            playSound(MMSounds.ENTITY_WROUGHT_AXE_HIT.get(), 0.3F, 0.5F);
        }
    }

    public void setVertical(boolean vertical) {
        getEntityData().set(VERTICAL, vertical);
    }

    public boolean getVertical() {
        return getEntityData().get(VERTICAL);
    }

    private List<Entity> getEntitiesNearby(double distanceX, double distanceY, double distanceZ, double radius) {
        List<Entity> list = level().getEntities(this, getBoundingBox().inflate(distanceX, distanceY, distanceZ));
        ArrayList<Entity> nearEntities = list.stream().filter(entityNeighbor -> entityNeighbor != null && distanceTo(entityNeighbor) <= radius + entityNeighbor.getBbWidth() / 2f).collect(Collectors.toCollection(ArrayList::new));
        return nearEntities;
    }

    public void setAxeStack(ItemStack axeStack) {
        getEntityData().set(AXE_STACK, axeStack);
    }

    public ItemStack getAxeStack() {
        return getEntityData().get(AXE_STACK);
    }

    /**
     * Copied from player entity, with modification
     */
    public void attackTargetEntityWithCurrentItem(Entity targetEntity, Player player, float damageMult, float knockbackMult) {
        if (!CommonHooks.onPlayerAttackTarget(player, targetEntity) || !targetEntity.isAttackable()) {
            return;
        }

        ItemStack oldStack = player.getMainHandItem();
        ItemStack newStack = getAxeStack();
        resetModifiers(player, oldStack, newStack);
        player.setItemInHand(InteractionHand.MAIN_HAND, newStack);

        // Check this after swapping main hand in cases the main hand item impacts the result
        if (targetEntity.skipAttackInteraction(player)) {
            resetModifiers(player, newStack, oldStack);
            player.setItemInHand(InteractionHand.MAIN_HAND, oldStack);
            return;
        }

        if (targetEntity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && targetEntity instanceof Projectile projectile && projectile.deflect(ProjectileDeflection.AIM_DEFLECT, this, this, true)) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource());
            resetModifiers(player, newStack, oldStack);
            player.setItemInHand(InteractionHand.MAIN_HAND, oldStack);
            return;
        }

        DamageSource damageSource = damageSources().playerAttack(player);

        float attackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMult;
        float enchantedDamage = player.getEnchantedDamage(targetEntity, attackDamage, damageSource);

        if (attackDamage > 0 || enchantedDamage > 0) {
            boolean wasSprinting = false;

            if (player.isSprinting()) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, player.getSoundSource(), 1.0F, 1.0F);
                wasSprinting = true;
            }

            attackDamage += newStack.getItem().getAttackDamageBonus(targetEntity, attackDamage, damageSource);
            float damage = attackDamage + enchantedDamage;
            float targetHealth = targetEntity instanceof LivingEntity livingTarget ? livingTarget.getHealth() : 0;

            Vec3 targetMovement = targetEntity.getDeltaMovement();
            boolean wasHurt = targetEntity.hurt(damageSource, damage);

            if (wasHurt) {
                float knockback = player.getKnockback(targetEntity, damageSource) + (wasSprinting ? 1 : 0);

                if (knockback > 0) {
                    if (targetEntity instanceof LivingEntity livingTarget) {
                        livingTarget.knockback(knockback * 0.5F * knockbackMult, Mth.sin(player.getYRot() * ((float) Math.PI / 180F)), -Mth.cos(player.getYRot() * ((float) Math.PI / 180F)));
                    } else {
                        targetEntity.push(-Mth.sin(player.getYRot() * ((float) Math.PI / 180F)) * knockback * 0.5F * knockbackMult, 0.1D, Mth.cos(player.getYRot() * ((float) Math.PI / 180F)) * knockback * 0.5F * knockbackMult);
                    }

                    player.setDeltaMovement(player.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                    player.setSprinting(false);
                }

                if (targetEntity instanceof ServerPlayer && targetEntity.hurtMarked) {
                    ((ServerPlayer) targetEntity).connection.send(new ClientboundSetEntityMotionPacket(targetEntity));
                    targetEntity.hurtMarked = false;
                    targetEntity.setDeltaMovement(targetMovement);
                }

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, player.getSoundSource(), 1.0F, 1.0F);

                if (enchantedDamage > 0) {
                    player.magicCrit(targetEntity);
                }

                player.setLastHurtMob(targetEntity);
                Entity entity = targetEntity;

                if (targetEntity instanceof PartEntity<?> part) {
                    entity = part.getParent();
                }

                ItemStack copy = newStack.copy();
                boolean hurtEnemy = false;

                if (level() instanceof ServerLevel serverLevel) {
                    if (entity instanceof LivingEntity livingEntity) {
                        hurtEnemy = newStack.hurtEnemy(livingEntity, player);
                    }

                    EnchantmentHelper.doPostAttackEffects(serverLevel, targetEntity, damageSource);
                }

                if (!level().isClientSide() && !copy.isEmpty() && entity instanceof LivingEntity livingEntity) {
                    if (hurtEnemy) {
                        newStack.postHurtEnemy(livingEntity, player);
                    }

                    if (newStack.isEmpty()) {
                        EventHooks.onPlayerDestroyItem(player, copy, InteractionHand.MAIN_HAND);
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }

                if (targetEntity instanceof LivingEntity livingTarget) {
                    float healthDifference = targetHealth - livingTarget.getHealth();
                    player.awardStat(Stats.DAMAGE_DEALT, Math.round(healthDifference * 10));

                    if (level() instanceof ServerLevel serverLevel && healthDifference > 2) {
                        int particleCount = (int) ((double) healthDifference * 0.5);
                        serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, targetEntity.getX(), targetEntity.getY(0.5), targetEntity.getZ(), particleCount, 0.1, 0.0, 0.1, 0.2);
                    }
                }

                player.causeFoodExhaustion(0.1F);
            } else {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
            }
        }

        resetModifiers(player, newStack, oldStack);
        player.setItemInHand(InteractionHand.MAIN_HAND, oldStack);
    }

    private void resetModifiers(Player player, ItemStack removeFrom, ItemStack addFrom) {
        removeFrom.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            AttributeInstance instance = player.getAttribute(attribute);

            if (instance != null && instance.hasModifier(modifier.id())) {
                instance.removeModifier(modifier);
            }
        });

        addFrom.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            AttributeInstance instance = player.getAttribute(attribute);

            if (instance != null && !instance.hasModifier(modifier.id())) {
                instance.addTransientModifier(modifier);
            }
        });
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setAxeStack(ItemStack.parseOptional(registryAccess(), compound.getCompound("axe_stack")));
        setVertical(compound.getBoolean("vertical"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("axe_stack", getAxeStack().save(registryAccess(), new CompoundTag()));
        compound.putBoolean("vertical", getVertical());
    }
}
