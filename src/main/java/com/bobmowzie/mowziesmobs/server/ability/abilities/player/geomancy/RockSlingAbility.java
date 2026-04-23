package com.bobmowzie.mowziesmobs.server.ability.abilities.player.geomancy;

import com.bobmowzie.mowziesmobs.server.ability.Ability;
import com.bobmowzie.mowziesmobs.server.ability.AbilitySection;
import com.bobmowzie.mowziesmobs.server.ability.AbilityType;
import com.bobmowzie.mowziesmobs.server.ability.PlayerAbility;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityGeomancyBase;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityRockSling;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.Animation;


public class RockSlingAbility extends PlayerAbility {
    public static final double SPAWN_BOULDER_REACH = 5;
    public BlockPos spawnBoulderPos = new BlockPos(0, 0, 0);
    public Vec3 lookPos = new Vec3(0, 0, 0);
    private BlockState spawnBoulderBlock = Blocks.DIRT.defaultBlockState();
    private int damage = 3;

    public RockSlingAbility(AbilityType<Player, ? extends Ability> abilityType, Player user) {
        super(abilityType, user, new AbilitySection[] {
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.STARTUP, 5),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.ACTIVE, 10),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, 5)
        }, 15);
    }

    @Override
    public void start() {
        super.start();
        Vec3 from = getUser().getEyePosition(1.0f);
        Vec3 to = from.add(getUser().getLookAngle().scale(SPAWN_BOULDER_REACH));
        BlockHitResult result = getUser().level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, getUser()));
        if (result.getType() == HitResult.Type.BLOCK) {
            this.lookPos = result.getLocation();
        }

        spawnBoulderPos = result.getBlockPos();
        this.spawnBoulderBlock = getUser().level().getBlockState(spawnBoulderPos);
        playAnimation("rock_sling", Animation.LoopType.DEFAULT, true, true);

        if (!getUser().level().isClientSide()) {
            for(int i = 0; i < 3; i++) {
                Vec3 spawnPos = new Vec3(0D, -1D, 2.5D).yRot((float) Math.toRadians(-getUser().getYRot())).yRot((float) Math.toRadians(-45 + (i * 45))).add(getUser().position());
                EntityRockSling boulder = new EntityRockSling(EntityHandler.ROCK_SLING.get(), getUser().level(), getUser(), spawnBoulderBlock, spawnBoulderPos, EntityGeomancyBase.GeomancyTier.values()[1]);
                boulder.setPos(spawnPos.x() + 0.5F, spawnPos.y() + 2, spawnPos.z() + 0.5F);
                boulder.setLaunchVec(getUser().getViewVector(1f).multiply(1f,0.9f,1f));
                if (!getUser().level().isClientSide && boulder.checkCanSpawn()) {
                    getUser().level().addFreshEntity(boulder);
                }
            }
        }
    }
}
