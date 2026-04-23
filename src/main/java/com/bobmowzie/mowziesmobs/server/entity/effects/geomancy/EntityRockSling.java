package com.bobmowzie.mowziesmobs.server.entity.effects.geomancy;

import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class EntityRockSling extends EntityBoulderProjectile implements GeoEntity {
    private Vec3 launchVec;

    public EntityRockSling(EntityType<? extends EntityRockSling> type, Level worldIn) {
        super(type, worldIn);
        setDamage(3);
    }

    public EntityRockSling(EntityType<? extends EntityBoulderProjectile> type, Level world, LivingEntity caster, BlockState blockState, BlockPos pos, GeomancyTier tier) {
        super(type, world, caster, blockState, pos, tier);
        setDamage(3);
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return this.dimensions.makeBoundingBox(this.position());
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if(tickCount > 15 + random.nextInt(10) && launchVec != null) {
            setDeltaMovement(launchVec.normalize().multiply(2f + random.nextFloat()/5, 2f, 2f + random.nextFloat()/5));
            setTravelling(true);
        }

        if (tickCount > 45) discard();
    }

    @Override
    protected double getDamageMult() {
        return ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SCULPTOR_STAFF.attackMultiplier.get();
    }

    public void setLaunchVec(Vec3 vec){
        this.launchVec = vec;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void findRidingEntities() {
    }

    @Override
    protected void doPopupEntities() {
    }

    private static final RawAnimation ROLL_ANIM = RawAnimation.begin().thenLoop("roll");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        super.registerControllers(controllers);
        AnimationController<EntityRockSling> controller = new AnimationController<>(this, "controller", 0,
                event -> {
                    event.getController()
                            .setAnimation(ROLL_ANIM);
                    return PlayState.CONTINUE;
                });
        controllers.add(controller);
    }
}
