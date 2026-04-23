package com.bobmowzie.mowziesmobs.server.ability.abilities.player.geomancy;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleBase;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.server.ability.*;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityBoulderBase;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityBoulderProjectile;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityGeomancyBase;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import com.bobmowzie.mowziesmobs.server.potion.EffectGeomancy;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import software.bernie.geckolib.core.animation.Animation;

public class SpawnBoulderAbility extends PlayerAbility {
    private static int MAX_CHARGE = 60;
    public static final double SPAWN_BOULDER_REACH = 5;

    public BlockPos spawnBoulderPos = new BlockPos(0, 0, 0);
    public Vec3 lookPos = new Vec3(0, 0, 0);
    private BlockState spawnBoulderBlock = Blocks.DIRT.defaultBlockState();
    private int spawnBoulderCharge = 0;
    private EntityGeomancyBase.GeomancyTier boulderSize = EntityGeomancyBase.GeomancyTier.SMALL;

    private static final AbilitySection NO_CHARGE_SECTION = new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 5);
    private static final AbilitySection SMALL_CHARGE_SECTION = new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 8);
    private static final AbilitySection MEDIUM_CHARGE_SECTION = new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 8);
    private static final AbilitySection LARGE_CHARGE_SECTION = new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 8);
    private static final AbilitySection HUGE_CHARGE_SECTION = new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 20);

    public SpawnBoulderAbility(AbilityType<Player, ? extends Ability> abilityType, Player user) {
        super(abilityType, user,  new AbilitySection[] {
                NO_CHARGE_SECTION,
                SMALL_CHARGE_SECTION,
                MEDIUM_CHARGE_SECTION,
                LARGE_CHARGE_SECTION,
                HUGE_CHARGE_SECTION,
                new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, 12)
        });
    }

    @Override
    public InteractionHand getActiveHand() {
        if (getUser().getMainHandItem().is(ItemHandler.EARTHREND_GAUNTLET.get())) return InteractionHand.MAIN_HAND;
        if (getUser().getOffhandItem().is(ItemHandler.EARTHREND_GAUNTLET.get())) return InteractionHand.OFF_HAND;
        return InteractionHand.MAIN_HAND;
    }

    @Override
    public void start() {
        super.start();
        boulderSize = EntityGeomancyBase.GeomancyTier.SMALL;
        if (getLevel().isClientSide()) {
            playAnimation("spawn_boulder_start", Animation.LoopType.DEFAULT, true, false);
            if (getUser().getUsedItemHand() == InteractionHand.MAIN_HAND) {
                heldItemMainHandVisualOverride = getUser().getUseItem();
            }
            else {
                heldItemOffHandVisualOverride = getUser().getUseItem();
            }
        }
    }

    @Override
    public boolean tryAbility() {
        Vec3 from = getUser().getEyePosition(1.0f);
        Vec3 to = from.add(getUser().getLookAngle().scale(SPAWN_BOULDER_REACH));
        BlockHitResult result = getUser().level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, getUser()));
        if (result.getType() == HitResult.Type.BLOCK) {
            this.lookPos = result.getLocation();
        }

        this.spawnBoulderPos = result.getBlockPos();
        this.spawnBoulderBlock = getUser().level().getBlockState(spawnBoulderPos);
        if (result.getDirection() == Direction.DOWN) return false;
        BlockState blockAbove = getUser().level().getBlockState(spawnBoulderPos.above());
        if (blockAbove.blocksMotion())
            return false;
        return EffectGeomancy.isBlockUseable(spawnBoulderBlock);
    }

    @Override
    public void tickUsing() {
        super.tickUsing();
        if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.STARTUP) {
            spawnBoulderCharge++;
            if (spawnBoulderCharge > 2) getUser().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3, 0, false, false));
            if (spawnBoulderCharge == 1 && getUser().level().isClientSide) MowziesMobs.PROXY.playBoulderChargeSound(getUser());
            if (spawnBoulderCharge == 45) {
                if (getUser().level().isClientSide) {
                    AdvancedParticleBase.spawnParticle(getUser().level(), ParticleHandler.RING2.get(), (float) getUser().getX(), (float) getUser().getY() + getUser().getBbHeight() / 2f, (float) getUser().getZ(), 0, 0, 0, true, 0, 0, 0, 0, 3.5F, 0.83f, 1, 0.39f, 1, 1, 20, true, true, new ParticleComponent[]{
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, ParticleComponent.KeyTrack.startAndEnd(0.7f, 0f), false),
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, ParticleComponent.KeyTrack.startAndEnd(0, 40f), false)
                    });
                }
                getUser().playSound(MMSounds.EFFECT_GEOMANCY_MAGIC_SMALL.get(), 1, 1f);
            }

            int size = getBoulderSize().index + 1;
            if (size > 4) size = 4;
            EntityDimensions dim = EntityBoulderBase.SIZE_MAP.get(EntityGeomancyBase.GeomancyTier.values()[size]);
            if (
                    !getUser().level().noCollision(dim.makeBoundingBox(spawnBoulderPos.getX() + 0.5F, spawnBoulderPos.getY() + 2, spawnBoulderPos.getZ() + 0.5F))
                    || getUser().distanceToSqr(spawnBoulderPos.getX(), spawnBoulderPos.getY(), spawnBoulderPos.getZ()) > 36
            ) {
                jumpToSection(5);
            }
        }
    }

    @Override
    protected void beginSection(AbilitySection section) {
        if (section.sectionType == AbilitySection.AbilitySectionType.STARTUP && section != NO_CHARGE_SECTION) {
            if (getUser().level().isClientSide) {
                float scale = 5;
                if (section == MEDIUM_CHARGE_SECTION) scale = 8;
                else if (section == LARGE_CHARGE_SECTION) scale = 12;
                else if (section == HUGE_CHARGE_SECTION) scale = 16;

                AdvancedParticleBase.spawnParticle(getUser().level(), ParticleHandler.RING2.get(), (float) spawnBoulderPos.getX() + 0.5f, (float) spawnBoulderPos.getY() + 1.01, (float) spawnBoulderPos.getZ() + 0.5f, 0, 0, 0, false, 0, Math.PI / 2f, 0, 0, scale, 0.83f, 1, 0.39f, 1, 1, 10, true, true, new ParticleComponent[]{
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, ParticleComponent.KeyTrack.startAndEnd(0.7f, 0.0f), false)
                });
            }
        }

        if (section == MEDIUM_CHARGE_SECTION) boulderSize = EntityGeomancyBase.GeomancyTier.MEDIUM;
        if (section == LARGE_CHARGE_SECTION) boulderSize = EntityGeomancyBase.GeomancyTier.LARGE;
        if (section == HUGE_CHARGE_SECTION) boulderSize = EntityGeomancyBase.GeomancyTier.HUGE;

        if (section.sectionType == AbilitySection.AbilitySectionType.ACTIVE) {
            spawnBoulder();
        }
    }

    private EntityGeomancyBase.GeomancyTier getBoulderSize() {
        return boulderSize;
    }

    private void spawnBoulder() {
        if (spawnBoulderCharge <= 2) {
            playAnimation("spawn_boulder_instant", Animation.LoopType.DEFAULT, true, false);
        }
        else {
            playAnimation("spawn_boulder_end", Animation.LoopType.DEFAULT, true, false);
        }

        EntityBoulderProjectile boulder = new EntityBoulderProjectile(EntityHandler.BOULDER_PROJECTILE.get(), getUser().level(), getUser(), spawnBoulderBlock, spawnBoulderPos, getBoulderSize());
        boulder.setPos(spawnBoulderPos.getX() + 0.5F, spawnBoulderPos.getY() + 2, spawnBoulderPos.getZ() + 0.5F);
        if (!getUser().level().isClientSide && boulder.checkCanSpawn()) {
            getUser().level().addFreshEntity(boulder);
        }

        if (spawnBoulderCharge > 2) {
            Vec3 playerEyes = getUser().getEyePosition(1);
            Vec3 vec = playerEyes.subtract(lookPos).normalize();
            float yaw = (float) Math.atan2(vec.z, vec.x);
            float pitch = (float) Math.asin(vec.y);
            getUser().setYRot((float) (yaw * 180f / Math.PI + 90));
            getUser().setXRot((float) (pitch * 180f / Math.PI));
        }
        spawnBoulderCharge = 0;
    }

    @Override
    public void onRightMouseUp(Player player) {
        super.onRightMouseUp(player);
        if (isUsing() && getCurrentSection().sectionType == AbilitySection.AbilitySectionType.STARTUP && getCurrentSection() != HUGE_CHARGE_SECTION) {
            if (player.distanceToSqr(spawnBoulderPos.getX(), spawnBoulderPos.getY(), spawnBoulderPos.getZ()) < 36) {
                jumpToSection(5);
            } else {
                spawnBoulderCharge = 0;
            }
        }
    }

    @Override
    public boolean canUse() {
        return EffectGeomancy.canUse(getUser()) && super.canUse();
    }

    @Override
    public void end() {
        spawnBoulderCharge = 0;
        super.end();
    }

    @Override
    public void readNBT(Tag nbt) {
        super.readNBT(nbt);
        if (getCurrentSection().sectionType == AbilitySection.AbilitySectionType.STARTUP) spawnBoulderCharge = getTicksInUse();
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        super.onRightClickBlock(event);
        if (!event.getLevel().isClientSide()) AbilityHandler.INSTANCE.sendAbilityMessage(event.getEntity(), AbilityHandler.SPAWN_BOULDER_ABILITY);
    }

    @Override
    public void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        super.onRightClickEmpty(event);
        AbilityHandler.INSTANCE.sendPlayerTryAbilityMessage(event.getEntity(), AbilityHandler.SPAWN_BOULDER_ABILITY);
    }

    @Override
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        super.onRenderTick(event);
        if (isUsing() && getCurrentSection().sectionType == AbilitySection.AbilitySectionType.STARTUP && getTicksInUse() > 1) {
            Vec3 playerEyes = getUser().getEyePosition(Minecraft.getInstance().getFrameTime());
            Vec3 vec = playerEyes.subtract(lookPos).normalize();
            float yaw = (float) Math.atan2(vec.z, vec.x);
            float pitch = (float) Math.asin(vec.y);
            getUser().setYRot((float) (yaw * 180f / Math.PI + 90));
            getUser().setXRot((float) (pitch * 180f / Math.PI));
            getUser().yHeadRot = getUser().getYRot();
            getUser().yRotO = getUser().getYRot();
            getUser().xRotO = getUser().getXRot();
            getUser().yHeadRotO = getUser().yHeadRot;
        }
    }
}
