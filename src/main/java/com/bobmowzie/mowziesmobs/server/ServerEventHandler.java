package com.bobmowzie.mowziesmobs.server;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.ParticleVanillaCloudExtended;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleBase;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleRotation;
import com.bobmowzie.mowziesmobs.datagen.MMItemTags;
import com.bobmowzie.mowziesmobs.server.ability.AbilityHandler;
import com.bobmowzie.mowziesmobs.server.advancement.AdvancementHandler;
import com.bobmowzie.mowziesmobs.server.ai.AvoidEntityIfNotTamedGoal;
import com.bobmowzie.mowziesmobs.server.block.BlockHandler;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.bobmowzie.mowziesmobs.server.capability.LivingData;
import com.bobmowzie.mowziesmobs.server.capability.PlayerData;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.LeaderSunstrikeImmune;
import com.bobmowzie.mowziesmobs.server.entity.MowzieEntity;
import com.bobmowzie.mowziesmobs.server.entity.MowzieGeckoEntity;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityBoulderProjectile;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityGeomancyBase;
import com.bobmowzie.mowziesmobs.server.entity.foliaath.EntityFoliaath;
import com.bobmowzie.mowziesmobs.server.entity.frostmaw.EntityFrostmaw;
import com.bobmowzie.mowziesmobs.server.entity.naga.EntityNaga;
import com.bobmowzie.mowziesmobs.server.entity.sculptor.EntitySculptor;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.*;
import com.bobmowzie.mowziesmobs.server.entity.wroughtnaut.EntityWroughtnaut;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import com.bobmowzie.mowziesmobs.server.item.ItemNagaFangDagger;
import com.bobmowzie.mowziesmobs.server.item.ItemSpear;
import com.bobmowzie.mowziesmobs.server.item.ItemUmvuthanaMask;
import com.bobmowzie.mowziesmobs.server.message.MessageFreezeEffect;
import com.bobmowzie.mowziesmobs.server.message.MessageSunblockEffect;
import com.bobmowzie.mowziesmobs.server.potion.EffectGeomancy;
import com.bobmowzie.mowziesmobs.server.potion.EffectHandler;
import com.bobmowzie.mowziesmobs.server.power.Power;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import com.bobmowzie.mowziesmobs.server.world.feature.structure.StructureTypeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ServerEventHandler {

    @SubscribeEvent
    public void onJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player) {
            DataHandler.getData(event.getEntity(), DataHandler.PLAYER_DATA).addedToWorld(event);
        }

        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Zombie && !(entity instanceof ZombifiedPiglin)) {
            ((PathfinderMob) entity).targetSelector.addGoal(2, new NearestAttackableTargetGoal<>((PathfinderMob) entity, EntityFoliaath.class, 0, true, false, null));
            ((PathfinderMob) entity).targetSelector.addGoal(3, new NearestAttackableTargetGoal<>((PathfinderMob) entity, EntityUmvuthana.class, 0, true, false, null));
            ((PathfinderMob) entity).targetSelector.addGoal(2, new NearestAttackableTargetGoal<>((PathfinderMob) entity, EntityUmvuthi.class, 0, true, false, null));
        }
        if (entity instanceof AbstractSkeleton) {
            ((PathfinderMob) entity).targetSelector.addGoal(3, new NearestAttackableTargetGoal<>((PathfinderMob) entity, EntityUmvuthana.class, 0, true, false, null));
            ((PathfinderMob) entity).targetSelector.addGoal(2, new NearestAttackableTargetGoal<>((PathfinderMob) entity, EntityUmvuthi.class, 0, true, false, null));
        }

        if (entity instanceof Parrot) {
            ((PathfinderMob) entity).goalSelector.addGoal(3, new AvoidEntityGoal<>((PathfinderMob) entity, EntityFoliaath.class, 6.0F, 1.0D, 1.2D));
        }
        if (entity instanceof Animal) {
            ((PathfinderMob) entity).goalSelector.addGoal(3, new AvoidEntityIfNotTamedGoal<>((PathfinderMob) entity, EntityFoliaath.class, 6.0F, 1.0D, 1.2D));
            ((PathfinderMob) entity).goalSelector.addGoal(3, new AvoidEntityIfNotTamedGoal<>((PathfinderMob) entity, EntityUmvuthana.class, 6.0F, 1.0D, 1.2D));
            ((PathfinderMob) entity).goalSelector.addGoal(3, new AvoidEntityIfNotTamedGoal<>((PathfinderMob) entity, EntityUmvuthi.class, 6.0F, 1.0D, 1.2D));
            ((PathfinderMob) entity).goalSelector.addGoal(3, new AvoidEntityIfNotTamedGoal<>((PathfinderMob) entity, EntityNaga.class, 10.0F, 1.0D, 1.2D));
            ((PathfinderMob) entity).goalSelector.addGoal(3, new AvoidEntityIfNotTamedGoal<>((PathfinderMob) entity, EntityFrostmaw.class, 10.0F, 1.0D, 1.2D));
        }
        if (entity instanceof AbstractVillager) {
            ((PathfinderMob) entity).goalSelector.addGoal(3, new AvoidEntityGoal<>((PathfinderMob) entity, EntityUmvuthana.class, 6.0F, 1.0D, 1.2D));
            ((PathfinderMob) entity).goalSelector.addGoal(3, new AvoidEntityGoal<>((PathfinderMob) entity, EntityUmvuthi.class, 6.0F, 1.0D, 1.2D));
            ((PathfinderMob) entity).goalSelector.addGoal(3, new AvoidEntityGoal<>((PathfinderMob) entity, EntityNaga.class, 10.0F, 1.0D, 1.2D));
            ((PathfinderMob) entity).goalSelector.addGoal(3, new AvoidEntityGoal<>((PathfinderMob) entity, EntityFrostmaw.class, 10.0F, 1.0D, 1.2D));
        }


        if (entity instanceof Pillager) {
            ((PathfinderMob) entity).targetSelector.addGoal(3, new NearestAttackableTargetGoal<>((PathfinderMob) entity, EntitySculptor.class, 0, true, false, null));
        }
    }

    private static final ResourceLocation GEOMANCY_BELT_DEFENSE = ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "geomancy_belt_defense_boost");
    private static final AttributeModifier DEFENSE_MODIFIER_BELT = new AttributeModifier(GEOMANCY_BELT_DEFENSE, 4D, AttributeModifier.Operation.ADD_VALUE);
    private static final ResourceLocation GEOMANCY_BELT_KNOCKBACK_RESISTANCE = ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "geomancy_belt_knockback_resistance_boost");
    private static final AttributeModifier KNOCKBACK_MODIFIER_BELT = new AttributeModifier(GEOMANCY_BELT_KNOCKBACK_RESISTANCE, 1D, AttributeModifier.Operation.ADD_VALUE);

    @SubscribeEvent
    public void onLivingTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (!livingEntity.level().isClientSide) {
                Item headItemStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem();
                if (headItemStack instanceof ItemUmvuthanaMask mask) {
                    EffectHandler.addOrCombineEffect(livingEntity, mask.getPotion(), 50, 0, true, false);
                }
            }

            if (livingEntity instanceof Mob mob && !(livingEntity instanceof EntityUmvuthanaCrane)) {
                if (mob.getTarget() instanceof EntityUmvuthi && mob.getTarget().hasEffect(EffectHandler.SUNBLOCK)) {
                    EntityUmvuthanaCrane sunblocker = mob.level().getNearestEntity(EntityUmvuthanaCrane.class, TargetingConditions.DEFAULT, mob, mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ(), mob.getBoundingBox().inflate(40.0D, 15.0D, 40.0D));
                    mob.setTarget(sunblocker);
                }
            }

            DataHandler.getData(livingEntity, DataHandler.FROZEN_DATA).tick(livingEntity);
            DataHandler.getData(livingEntity, DataHandler.LIVING_DATA).tick(livingEntity);
            DataHandler.getData(livingEntity, DataHandler.ABILITY_DATA).tick(livingEntity);

            // Geomancer Belt mechanics
            AttributeInstance attributeInstanceArmor = livingEntity.getAttribute(Attributes.ARMOR);
            AttributeInstance attributeInstanceKnockbackRes = livingEntity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (livingEntity.getItemBySlot(EquipmentSlot.LEGS).is(ItemHandler.GEOMANCER_BELT.get()) && livingEntity.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                if (attributeInstanceArmor != null && !attributeInstanceArmor.hasModifier(GEOMANCY_BELT_DEFENSE)) {
                    attributeInstanceArmor.addTransientModifier(DEFENSE_MODIFIER_BELT);
                }
                if (attributeInstanceKnockbackRes != null && !attributeInstanceKnockbackRes.hasModifier(GEOMANCY_BELT_KNOCKBACK_RESISTANCE)) {
                    attributeInstanceKnockbackRes.addTransientModifier(KNOCKBACK_MODIFIER_BELT);
                }
            }
            else {
                if (attributeInstanceArmor != null && attributeInstanceArmor.hasModifier(GEOMANCY_BELT_DEFENSE)) {
                    attributeInstanceArmor.removeModifier(DEFENSE_MODIFIER_BELT);
                }
                if (attributeInstanceKnockbackRes != null && attributeInstanceKnockbackRes.hasModifier(GEOMANCY_BELT_KNOCKBACK_RESISTANCE)) {
                    attributeInstanceKnockbackRes.removeModifier(KNOCKBACK_MODIFIER_BELT);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPotionEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity().hasEffect(EffectHandler.ECLIPSED) && !event.getEntity().getEffect(EffectHandler.ECLIPSED).endsWithin(1)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }

        if (event.getEntity().hasEffect(EffectHandler.POISON_RESIST) && event.getEffectInstance().is(MobEffects.POISON)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public void onAddPotionEffect(MobEffectEvent.Added event) {
        if (event.getEffectInstance() == null) {
            return;
        }

        if (event.getEffectInstance().is(EffectHandler.POISON_RESIST)) {
            event.getEntity().removeEffect(MobEffects.POISON);
        }

        if (event.getEffectInstance().is(EffectHandler.ECLIPSED)) {
            for (MobEffectInstance effectInstance : event.getEntity().getActiveEffects()) {
                if (!effectInstance.is(EffectHandler.ECLIPSED)) {
                    LivingData livingData = DataHandler.getData(event.getEntity(), DataHandler.LIVING_DATA);
                    livingData.eclipseEffect(effectInstance);
                }
            }
            event.getEntity().removeAllEffects();
        }

        if (event.getEffectInstance().getEffect() == EffectHandler.SUNBLOCK) {
            if (!event.getEntity().level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(event.getEntity(), new MessageSunblockEffect(event.getEntity().getId(), true));
            }

            MMCommon.PROXY.playSunblockSound(event.getEntity());
        }

        if (event.getEffectInstance().getEffect() == EffectHandler.FROZEN) {
            if (!event.getEntity().level().isClientSide()) {
                DataHandler.getData(event.getEntity(), DataHandler.FROZEN_DATA).onFreeze(event.getEntity());
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(event.getEntity(), new MessageFreezeEffect(event.getEntity().getId(), true));
            }
        }
    }

    @SubscribeEvent
    public void onRemovePotionEffect(MobEffectEvent.Remove event) {
    	if (event.getEffectInstance() == null) {
            return;
        }

        if (event.getEffectInstance().is(EffectHandler.ECLIPSED)) {
            LivingData livingData = DataHandler.getData(event.getEntity(), DataHandler.LIVING_DATA);
            livingData.unEclipseEffects(event.getEntity());
        }

        if (!event.getEntity().level().isClientSide() && event.getEffectInstance().getEffect() == EffectHandler.SUNBLOCK) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(event.getEntity(), new MessageSunblockEffect(event.getEntity().getId(), false));
        }

        if (!event.getEntity().level().isClientSide() && event.getEffectInstance().getEffect() == EffectHandler.FROZEN) {
            DataHandler.getData(event.getEntity(), DataHandler.FROZEN_DATA).onUnfreeze(event.getEntity());
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(event.getEntity(), new MessageFreezeEffect(event.getEntity().getId(), false));
        }
    }

    @SubscribeEvent
    public void onPotionEffectExpire(MobEffectEvent.Expired event) {
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance == null) {
            return;
        }

        if (event.getEffectInstance().is(EffectHandler.ECLIPSED)) {
            LivingData livingData = DataHandler.getData(event.getEntity(), DataHandler.LIVING_DATA);
            livingData.unEclipseEffects(event.getEntity());
        }

        if (!event.getEntity().level().isClientSide() && effectInstance != null && effectInstance.getEffect() == EffectHandler.SUNBLOCK) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(event.getEntity(), new MessageSunblockEffect(event.getEntity().getId(), false));
        }

        if (!event.getEntity().level().isClientSide() && effectInstance != null && effectInstance.getEffect() == EffectHandler.FROZEN) {
            DataHandler.getData(event.getEntity(), DataHandler.FROZEN_DATA).onUnfreeze(event.getEntity());
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(event.getEntity(), new MessageFreezeEffect(event.getEntity().getId(), false));
        }
    }

    @SubscribeEvent
    public void onLivingHurtPre(LivingDamageEvent.Pre event) {
        // Copied from LivingEntity's applyPotionDamageCalculations
        DamageSource source = event.getSource();
        LivingEntity livingEntity = event.getEntity();
        // SUNBLOCK
        float damage = event.getNewDamage();
        if (!source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            if (livingEntity.hasEffect(EffectHandler.SUNBLOCK) && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
                int i = (livingEntity.getEffect(EffectHandler.SUNBLOCK).getAmplifier() + 2) * 5;
                int j = 25 - i;
                float f = damage * (float)j;
                float f1 = damage;
                damage = Math.max(f / 25.0F, 0.0F);
                event.setNewDamage(damage);
                float f2 = f1 - damage;
                if (f2 > 0.0F && f2 < 3.4028235E37F) {
                    if (livingEntity instanceof ServerPlayer) {
                        ((ServerPlayer)livingEntity).awardStat(Stats.DAMAGE_RESISTED, Math.round(f2 * 10.0F));
                    } else if (source.getEntity() instanceof ServerPlayer) {
                        ((ServerPlayer)source.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f2 * 10.0F));
                    }
                }
            }
        }

        // FRAGILITY
        damage = event.getNewDamage();
        if (livingEntity.hasEffect(EffectHandler.FRAGILITY) && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            int i = (livingEntity.getEffect(EffectHandler.FRAGILITY).getAmplifier() + 1) * 5;
            int j = 25 + i;
            float f = damage * (float)j;
            damage = Math.max(f / 25.0F, 0.0F);
            event.setNewDamage(damage);
        }
    }

    @SubscribeEvent
    public void onLivingHurtPost(LivingDamageEvent.Post event) {
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.getEntity().removeEffectNoUpdate(EffectHandler.FROZEN);
            DataHandler.getData(event.getEntity(), DataHandler.FROZEN_DATA).onUnfreeze(event.getEntity());
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(event.getEntity(), new MessageFreezeEffect(event.getEntity().getId(), false));
        }
        if (event.getEntity() instanceof Player player) {
            Power[] powers = DataHandler.getData(player, DataHandler.PLAYER_DATA).getPowers();
            for (Power power : powers) {
                power.onTakeDamage(event);
            }

            if (player.getItemBySlot(EquipmentSlot.CHEST).is(ItemHandler.GEOMANCER_ROBE.get())) {
                spawnBoulderNearPlayer(player);
            }
        }

        DataHandler.getData(event.getEntity(), DataHandler.LIVING_DATA).setLastDamage(event.getNewDamage());
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        PlayerData data = DataHandler.getData(player, DataHandler.PLAYER_DATA);
        data.tick(event);
        Power[] powers = data.getPowers();
        for (Power power : powers) {
            power.tick(event);
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (event.getEntity().getItemBySlot(EquipmentSlot.FEET).is(ItemHandler.GEOMANCER_SANDALS.get())) {
            if (event.getDistance() > 4) {
                EffectHandler.addOrCombineEffect(event.getEntity(), MobEffects.MOVEMENT_SPEED, 60, 0, false, false);
            }
        }
    }

    @SubscribeEvent
    public void onUseItem(LivingEntityUseItemEvent.Start event) {
        LivingEntity living = event.getEntity();
        if (living.hasEffect(EffectHandler.FROZEN)) {
            event.setCanceled(true);
            return;
        }

        if (DataHandler.getData(living, DataHandler.ABILITY_DATA).itemUsePrevented(event.getItem())) {
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public void onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity living) {
            if (living.hasEffect(EffectHandler.FROZEN)) {
                event.setCanceled(true);
                return;
            }

            if (DataHandler.getData(living, DataHandler.ABILITY_DATA).blockBreakingBuildingPrevented()) {
                event.setCanceled(true);
                return;
            }

            if (entity instanceof Player) {
                cheatSculptor((Player) entity);

                BlockState block = event.getPlacedBlock();
                if (
                        block.getBlock() == Blocks.FIRE ||
                        block.getBlock() == Blocks.TNT ||
                        block.getBlock() == Blocks.RESPAWN_ANCHOR ||
                        block.getBlock() == Blocks.DISPENSER ||
                        block.getBlock() == Blocks.CACTUS
                ) {
                    aggroUmvuthana((Player) entity);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerData data = DataHandler.getData(player, DataHandler.PLAYER_DATA);
            EntitySculptor sculptor = data.getTestingSculptor();

            if (sculptor != null && sculptor.getTestingPlayer() == player && event.getSource() == player.damageSources().fall()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    AdvancementHandler.SCULPTOR_FAILURE_TRIGGER.get().trigger(serverPlayer);
                }
            }
        }
    }

    @SubscribeEvent
    public void onFillBucket(PlayerInteractEvent.RightClickItem event) {
        Item item = event.getItemStack().getItem();

        if (item == Items.LAVA_BUCKET) {
            aggroUmvuthana(event.getEntity());
        }

        if (item == Items.WATER_BUCKET) {
            cheatSculptor(event.getEntity());
        }
    }

    @SubscribeEvent
    public void onBreakBlock(BlockEvent.BreakEvent event) {
        if (event.getPlayer().hasEffect(EffectHandler.FROZEN)) {
            event.setCanceled(true);
            return;
        }

        if (DataHandler.getData(event.getPlayer(), DataHandler.ABILITY_DATA).blockBreakingBuildingPrevented()) {
            event.setCanceled(true);
            return;
        }

        cheatSculptor(event.getPlayer());

        BlockState block = event.getState();
        if (block.getBlock() == Blocks.GOLD_BLOCK ||
            block.is(BlockTags.PLANKS) ||
            block.is(BlockTags.LOGS) ||
            block.is(BlockTags.LEAVES) ||
            block.getBlock() == Blocks.LIGHT_GRAY_TERRACOTTA ||
            block.getBlock() == Blocks.RED_TERRACOTTA ||
            block.getBlock() == Blocks.SMOOTH_RED_SANDSTONE_SLAB ||
            block.getBlock() == Blocks.SMOOTH_RED_SANDSTONE ||
            block.getBlock() == Blocks.SMOOTH_RED_SANDSTONE_STAIRS ||
            block.getBlock() == Blocks.CAMPFIRE ||
            block.getBlock() == Blocks.IRON_BARS ||
            block.getBlock() == Blocks.SKELETON_SKULL ||
            block.getBlock() == Blocks.TORCH ||
            block.getBlock() == Blocks.CHEST
        ) {
            aggroUmvuthana(event.getPlayer());
        }
    }

    public <T extends Entity> List<T> getEntitiesNearby(Entity startEntity, Class<T> entityClass, double r) {
        return startEntity.level().getEntitiesOfClass(entityClass, startEntity.getBoundingBox().inflate(r, r, r), e -> e != startEntity && startEntity.distanceTo(e) <= r);
    }

    private List<LivingEntity> getEntityBaseNearby(LivingEntity user, double distanceX, double distanceY, double distanceZ, double radius) {
        List<Entity> list = user.level().getEntities(user, user.getBoundingBox().inflate(distanceX, distanceY, distanceZ));
        return list.stream().filter(entityNeighbor -> entityNeighbor instanceof LivingEntity && user.distanceTo(entityNeighbor) <= radius).map(entityNeighbor -> (LivingEntity) entityNeighbor).collect(Collectors.toCollection(ArrayList::new));
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickEmpty event) {
        if (event.getEntity().hasEffect(EffectHandler.FROZEN)) {
            return;
        }

        if (DataHandler.getData(event.getEntity(), DataHandler.ABILITY_DATA).interactingPrevented()) {
            return;
        }

        Player player = event.getEntity();
        if (event.getLevel().isClientSide && player.getInventory().getSelected().isEmpty() && player.hasEffect(EffectHandler.SUNS_BLESSING)) {
            if (player.isShiftKeyDown()) {
                AbilityHandler.INSTANCE.sendPlayerTryAbilityMessage(event.getEntity(), AbilityHandler.SOLAR_BEAM_ABILITY);
            } else {
                AbilityHandler.INSTANCE.sendPlayerTryAbilityMessage(event.getEntity(), AbilityHandler.SUNSTRIKE_ABILITY);
            }
        }

        Power[] powers = DataHandler.getData(player, DataHandler.PLAYER_DATA).getPowers();
        for (Power power : powers) {
            power.onRightClickEmpty(event);
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().hasEffect(EffectHandler.FROZEN)) {
            event.setCanceled(true);
            return;
        }

        if (DataHandler.getData(event.getEntity(), DataHandler.ABILITY_DATA).interactingPrevented()) {
            event.setCanceled(true);
            return;
        }

        Power[] powers = DataHandler.getData(event.getEntity(), DataHandler.PLAYER_DATA).getPowers();
        for (Power power : powers) {
            power.onRightClickEntity(event);
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().hasEffect(EffectHandler.FROZEN)) {
            event.setCanceled(true);
            return;
        }

        if (DataHandler.getData(event.getEntity(), DataHandler.ABILITY_DATA).interactingPrevented()) {
            event.setCanceled(true);
            return;
        }

        Player player = event.getEntity();
        if (player.level().getBlockState(event.getPos()).getBlock() instanceof ChestBlock) {
            aggroUmvuthana(player);
        }

        ItemStack item = event.getItemStack();

        if (item.getItem() == Items.FLINT_AND_STEEL || item.getItem() == Items.TNT_MINECART) {
            aggroUmvuthana(player);
        }

        if (player.level().isClientSide() && player.getInventory().getSelected().isEmpty() && player.hasEffect(EffectHandler.SUNS_BLESSING) && player.level().getBlockState(event.getPos()).getMenuProvider(player.level(), event.getPos()) == null) {
            if (player.isShiftKeyDown()) {
                AbilityHandler.INSTANCE.sendPlayerTryAbilityMessage(event.getEntity(), AbilityHandler.SOLAR_BEAM_ABILITY);
            } else {
                AbilityHandler.INSTANCE.sendPlayerTryAbilityMessage(event.getEntity(), AbilityHandler.SUNSTRIKE_ABILITY);
            }
        }
        if (player.getMainHandItem().is(ItemHandler.WROUGHT_AXE.get()) && player.level().getBlockState(event.getPos()).getMenuProvider(player.level(), event.getPos()) != null) {
            player.resetAttackStrengthTicker();
            return;
        }

        Power[] powers = DataHandler.getData(player, DataHandler.PLAYER_DATA).getPowers();
        for (Power power : powers) {
            power.onRightClickBlock(event);
        }
    }

    @SubscribeEvent
    public void onPlayerLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        Power[] powers = DataHandler.getData(player, DataHandler.PLAYER_DATA).getPowers();
        for (Power power : powers) {
            power.onLeftClickEmpty(event);
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.getHealth() <= event.getNewDamage() && entity.hasEffect(EffectHandler.FROZEN)) {
            entity.removeEffectNoUpdate(EffectHandler.FROZEN);
            DataHandler.getData(entity, DataHandler.FROZEN_DATA).onUnfreeze(entity);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(event.getEntity(), new MessageFreezeEffect(event.getEntity().getId(), false));
        }

        if (event.getNewDamage() > 0 && event.getSource().getEntity() instanceof Player player) {
            if (player.getItemBySlot(EquipmentSlot.CHEST).is(ItemHandler.GEOMANCER_ROBE.get())) {
                spawnBoulderNearPlayer(player);
            }
        }

        if (entity instanceof Player player && event.getSource() == player.damageSources().fall() && player.getHealth() <= event.getNewDamage()) {
            PlayerData data = DataHandler.getData(player, DataHandler.PLAYER_DATA);
            if (data.getTestingSculptor() != null) {
                EntitySculptor sculptor = data.getTestingSculptor();
                if (sculptor.getTestingPlayer() == player) {
                    if (player instanceof ServerPlayer) {
                        AdvancementHandler.SCULPTOR_FAILURE_TRIGGER.get().trigger((ServerPlayer) player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().hasEffect(EffectHandler.FROZEN)) {
            event.setCanceled(true);
            return;
        }

        if (DataHandler.getData(event.getEntity(), DataHandler.ABILITY_DATA).itemUsePrevented(event.getItemStack())) {
            event.setCanceled(true);
            return;
        }

        Power[] powers = DataHandler.getData(event.getEntity(), DataHandler.PLAYER_DATA).getPowers();
        for (Power power : powers) {
            power.onRightClickWithItem(event);
        }
    }

    @SubscribeEvent
    public void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.hasEffect(EffectHandler.FROZEN)) {
            event.setCanceled(true);
            return;
        }

        if (DataHandler.getData(event.getEntity(), DataHandler.ABILITY_DATA).blockBreakingBuildingPrevented()) {
            event.setCanceled(true);
            return;
        }

        Power[] powers = DataHandler.getData(player, DataHandler.PLAYER_DATA).getPowers();
        for (Power power : powers) {
            power.onLeftClickBlock(event);
        }
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.hasEffect(EffectHandler.FROZEN) && entity.onGround()) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, 0, 1));
        }

        if (event.getEntity() instanceof Player) {
            Power[] powers = DataHandler.getData(event.getEntity(), DataHandler.PLAYER_DATA).getPowers();

            for (Power power : powers) {
                power.onJump(event);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity().hasEffect(EffectHandler.FROZEN)) {
            event.setCanceled(true);
            return;
        }

        if (DataHandler.getData(event.getEntity(), DataHandler.ABILITY_DATA).attackingPrevented()) {
            event.setCanceled(true);
            return;
        }

        PlayerData data = DataHandler.getData(event.getEntity(), DataHandler.PLAYER_DATA);
        data.setPrevCooledAttackStrength(event.getEntity().getAttackStrengthScale(0.5f));

        Power[] powers = data.getPowers();
        for (Power power : powers) {
            power.onLeftClickEntity(event);
        }

        if (event.getTarget() instanceof ItemFrame itemFrame) {
            if (itemFrame.getItem().getItem() instanceof ItemUmvuthanaMask) {
                aggroUmvuthana(event.getEntity());
            }
        }
        if (event.getTarget() instanceof LeaderSunstrikeImmune) {
            aggroUmvuthana(event.getEntity());
        }

        if (!(event.getTarget() instanceof LivingEntity)) return;
        if (event.getTarget() instanceof EntityUmvuthanaFollowerToPlayer) return;
        if (!event.getEntity().level().isClientSide()) {
            for (int i = 0; i < data.getPackSize(); i++) {
                EntityUmvuthanaFollowerToPlayer umvuthana = data.getUmvuthanaPack().get(i);
                LivingEntity living = (LivingEntity) event.getTarget();
                if (umvuthana.getMaskType() != MaskType.FAITH) {
                    if (!living.isInvulnerable()) umvuthana.setTarget(living);
                }
            }
        }
    }

    @SubscribeEvent
    public void checkCritEvent(CriticalHitEvent event) {
        ItemStack weapon = event.getEntity().getMainHandItem();
        Player attacker = event.getEntity();

        if (DataHandler.getData(attacker, DataHandler.PLAYER_DATA).getPrevCooledAttackStrength() == 1 && !weapon.isEmpty() && event.getTarget() instanceof LivingEntity target) {
            if (weapon.getItem() instanceof ItemNagaFangDagger) {
                Vec3 lookDir = new Vec3(target.getLookAngle().x, 0, target.getLookAngle().z).normalize();
                Vec3 vecBetween = new Vec3(target.getX() - event.getEntity().getX(), 0, target.getZ() - event.getEntity().getZ()).normalize();
                double dot = lookDir.dot(vecBetween);
                if (dot > 0.7) {
                    event.setCriticalHit(true);
                    event.setDamageMultiplier(event.getDamageMultiplier() + ConfigHandler.COMMON.TOOLS_AND_ABILITIES.NAGA_FANG_DAGGER.backstabDamageMultiplier.get().floatValue());
                    target.playSound(MMSounds.ENTITY_NAGA_ACID_HIT.get(), 1f, 1.2f);
                    AbilityHandler.INSTANCE.sendAbilityMessage(attacker, AbilityHandler.BACKSTAB_ABILITY);

                    if (target.level().isClientSide()) {
                        Vec3 ringOffset = attacker.getLookAngle().scale(-target.getBbWidth() / 2.f);
                        ParticleRotation.OrientVector rotation = new ParticleRotation.OrientVector(ringOffset);
                        Vec3 pos = target.position().add(0, target.getBbHeight() / 2f, 0).add(ringOffset);
                        AdvancedParticleBase.spawnParticle(target.level(), ParticleHandler.RING_SPARKS, pos.x(), pos.y(), pos.z(), 0, 0, 0, rotation, 3.5F, 0.83f, 1, 0.39f, 1, 1, 6, false, true, new ParticleComponent[]{
                                new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, new ParticleComponent.KeyTrack(new float[]{1f, 1f, 0f}, new float[]{0f, 0.5f, 1f}), false),
                                new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, ParticleComponent.KeyTrack.startAndEnd(0f, 15f), false)
                        });
                        RandomSource rand = attacker.level().getRandom();
                        float explodeSpeed = 2.5f;
                        for (int i = 0; i < 10; i++) {
                            Vec3 particlePos = new Vec3(rand.nextFloat() * 0.25, 0, 0);
                            particlePos = particlePos.yRot((float) (rand.nextFloat() * 2 * Math.PI));
                            particlePos = particlePos.xRot((float) (rand.nextFloat() * 2 * Math.PI));
                            double value = rand.nextFloat() * 0.1f;
                            double life = rand.nextFloat() * 8f + 15f;
                            ParticleVanillaCloudExtended.spawnVanillaCloud(target.level(), pos.x(), pos.y(), pos.z(), particlePos.x * explodeSpeed, particlePos.y * explodeSpeed, particlePos.z * explodeSpeed, 1, 0.25d + value, 0.75d + value, 0.25d + value, 0.6, life);
                        }
                        for (int i = 0; i < 10; i++) {
                            Vec3 particlePos = new Vec3(rand.nextFloat() * 0.25, 0, 0);
                            particlePos = particlePos.yRot((float) (rand.nextFloat() * 2 * Math.PI));
                            particlePos = particlePos.xRot((float) (rand.nextFloat() * 2 * Math.PI));
                            double value = rand.nextFloat() * 0.1f;
                            double life = rand.nextFloat() * 2.5f + 5f;
                            AdvancedParticleBase.spawnParticle(target.level(), ParticleHandler.PIXEL, pos.x(), pos.y(), pos.z(), particlePos.x * explodeSpeed, particlePos.y * explodeSpeed, particlePos.z * explodeSpeed, true, 0, 0, 0, 0, 3f, 0.07d + value, 0.25d + value, 0.07d + value, 1d, 0.6, life * 0.95, false, true);
                        }
                        for (int i = 0; i < 6; i++) {
                            Vec3 particlePos = new Vec3(rand.nextFloat() * 0.25, 0, 0);
                            particlePos = particlePos.yRot((float) (rand.nextFloat() * 2 * Math.PI));
                            particlePos = particlePos.xRot((float) (rand.nextFloat() * 2 * Math.PI));
                            double value = rand.nextFloat() * 0.1f;
                            double life = rand.nextFloat() * 5f + 10f;
                            AdvancedParticleBase.spawnParticle(target.level(), ParticleHandler.BUBBLE, pos.x(), pos.y(), pos.z(), particlePos.x * explodeSpeed, particlePos.y * explodeSpeed, particlePos.z * explodeSpeed, true, 0, 0, 0, 0, 3f, 0.25d + value, 0.75d + value, 0.25d + value, 1d, 0.6, life * 0.95, false, true);
                        }
                    }
                }
            }
            else if (weapon.getItem() instanceof ItemSpear) {
                if (target instanceof Animal && target.getMaxHealth() <= 30 && attacker.level().getRandom().nextFloat() <= 0.334) {
                    event.setCriticalHit(true);
                    event.setDamageMultiplier(400);
                }
            }
        }
    }

//    @SubscribeEvent
//    public void onLivingAttack(LivingAttackEvent event) {
//        Entity attacker = event.getSource().getDirectEntity();
//        if (!event.getSource().isIndirect() && attacker instanceof LivingEntity livingAttacker) {
//            ItemStack weapon = livingAttacker.getMainHandItem();
//            if (livingAttacker.getItemBySlot(EquipmentSlot.HEAD).is(ItemHandler.GEOMANCER_BEADS.get())) {
//                if (weapon.isEmpty() || weapon.is(MMItemTags.HAND_WEAPONS)) {
//                    event.getSource().;
//                }
//            }
//        }
//    }

    private static final AttributeModifier ATTACK_MODIFIER_BEADS = new AttributeModifier(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "geomancy_beads_attack_boost"), 3D, AttributeModifier.Operation.ADD_VALUE);

    @SubscribeEvent
    public void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        LivingEntity equipper = event.getEntity();
        ItemStack weapon = equipper.getItemBySlot(EquipmentSlot.MAINHAND);
        AttributeInstance attributeinstance = equipper.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attributeinstance != null) {
            // If the head or hand slot was modified
            if ((event.getSlot() == EquipmentSlot.HEAD || event.getSlot() == EquipmentSlot.MAINHAND)) {
                // Start by clearing attack boost
                attributeinstance.removeModifier(ATTACK_MODIFIER_BEADS);
                // If wearing beads and unarmed
                if (equipper.getItemBySlot(EquipmentSlot.HEAD).is(ItemHandler.GEOMANCER_BEADS.get()) && (weapon.is(MMItemTags.HAND_WEAPONS) || weapon.isEmpty())) {
                    // Apply or reapply attack boost
                    attributeinstance.addTransientModifier(ATTACK_MODIFIER_BEADS);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRideEntity(EntityMountEvent event) {
        if (
                event.getEntityMounting() instanceof EntityUmvuthi ||
                event.getEntityMounting() instanceof EntityFrostmaw ||
                event.getEntityMounting() instanceof EntityWroughtnaut ||
                event.getEntityMounting() instanceof EntitySculptor
        ) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        List<MowzieEntity> mobs = getEntitiesNearby(event.getEntity(), MowzieEntity.class, 40);
        for (MowzieEntity mob : mobs) {
            if (mob.resetHealthOnPlayerRespawn()) {
                mob.setHealth(mob.getMaxHealth());
            }
        }
    }

    @SubscribeEvent
    public void onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        StructureManager structureManager = event.getLevel().getLevel().structureManager();
        Structure structure = structureManager.registryAccess().registryOrThrow(Registries.STRUCTURE).get(StructureTypeHandler.MONASTERY.getId());
        if (event.getEntityType().getCategory() == MobCategory.MONSTER && structure != null && structureManager.getStructureAt(event.getPos(), structure).isValid()) {
            BlockState ground = event.getLevel().getBlockState(event.getPos().below());
            if (
                    event.getLevel().canSeeSky(event.getPos()) &&
                    (ground.is(Blocks.DARK_OAK_PLANKS) ||
                    ground.is(Blocks.DARK_OAK_SLAB) ||
                    ground.is(Blocks.DARK_OAK_STAIRS))
            ) {
                event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
            }
        }
    }

    @SubscribeEvent
    public void onFurnaceFuelBurnTimeEvent(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().is(BlockHandler.CLAWED_LOG.get().asItem()) || event.getItemStack().is(BlockHandler.PAINTED_ACACIA.get().asItem())) {
            event.setBurnTime(300);
        }
        else if (event.getItemStack().is(BlockHandler.PAINTED_ACACIA_SLAB.get().asItem())) {
            event.setBurnTime(150);
        }
        else if (event.getItemStack().is(BlockHandler.THATCH.get().asItem())) {
            event.setBurnTime(100);
        }
    }

    private void aggroUmvuthana(Player player) {
        List<EntityUmvuthi> barakos = getEntitiesNearby(player, EntityUmvuthi.class, 50);
        for (EntityUmvuthi barako : barakos) {
            if (barako.getTarget() == null || !(barako.getTarget() instanceof Player)) {
                if (!player.isCreative() && !player.isSpectator() && player.blockPosition().distSqr(barako.getRestrictCenter()) < 900) {
                    if (barako.canAttack(player)) barako.setMisbehavedPlayerId(player.getUUID());
                }
            }
        }
        List<EntityUmvuthanaMinion> barakoas = getEntitiesNearby(player, EntityUmvuthanaMinion.class, 50);
        for (EntityUmvuthanaMinion barakoa : barakoas) {
            if (barakoa.getTarget() == null || !(barakoa.getTarget() instanceof Player)) {
                if (player.blockPosition().distSqr(barakoa.getRestrictCenter()) < 900) {
                    if (barakoa.canAttack(player)) barakoa.setMisbehavedPlayerId(player.getUUID());
                }
            }
        }
    }

    private void cheatSculptor(Player player) {
        List<EntitySculptor> sculptors = player.level().getEntitiesOfClass(EntitySculptor.class, player.getBoundingBox().inflate(EntitySculptor.TEST_RADIUS + 3, EntitySculptor.TEST_HEIGHT, EntitySculptor.TEST_RADIUS + 3), s -> s.isTesting() && s.getActiveAbilityType() != EntitySculptor.PASS_TEST && s.getActiveAbilityType() != EntitySculptor.FAIL_TEST);
        for (EntitySculptor sculptor : sculptors) {
            sculptor.playerCheated();
        }
    }

    private void spawnBoulderNearPlayer(Player player) {
        if (player.getRandom().nextFloat() > 0.5) return;
        int i = Mth.floor(player.getX());
        int j = Mth.floor(player.getY());
        int k = Mth.floor(player.getZ());
        for(int l = 0; l < 10; ++l) {
            double radius = Math.pow(player.getRandom().nextFloat(), 0.5) * 10 + 3;
            double angle = player.getRandom().nextFloat() * Math.PI * 2;
            int i1 = i + (int)(Math.cos(angle) * radius);
            int j1 = j + Mth.nextInt(player.getRandom(), 0, 15) * Mth.nextInt(player.getRandom(), -1, 1);
            int k1 = k + (int)(Math.sin(angle) * radius);
            BlockPos spawnBoulderPos = new BlockPos(i1, j1, k1);
            BlockState state = player.level().getBlockState(spawnBoulderPos);
            int searchDist = 0;
            int maxSearchDist = 10;
            // march down to solid ground
            while (state.canBeReplaced() && searchDist < maxSearchDist) {
                spawnBoulderPos = spawnBoulderPos.below();
                state = player.level().getBlockState(spawnBoulderPos);
                searchDist++;
            }
            // march up to air ground
            searchDist = 0;
            while (!state.canBeReplaced() && searchDist < maxSearchDist) {
                spawnBoulderPos = spawnBoulderPos.above();
                state = player.level().getBlockState(spawnBoulderPos);
                searchDist++;
            }
            spawnBoulderPos = spawnBoulderPos.below();
            state = player.level().getBlockState(spawnBoulderPos);

            if (EffectGeomancy.isBlockUseable(state)) {
                EntityBoulderProjectile boulder = new EntityBoulderProjectile(EntityHandler.BOULDER_PROJECTILE.get(), player.level(), player, state, spawnBoulderPos, EntityGeomancyBase.GeomancyTier.SMALL);
                boulder.setPos(spawnBoulderPos.getX() + 0.5F, spawnBoulderPos.getY() + 2, spawnBoulderPos.getZ() + 0.5F);
                if (!player.level().isClientSide && boulder.checkCanSpawn()) {
                    player.level().addFreshEntity(boulder);
                    break;
                }
            }
        }
    }
}
