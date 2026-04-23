package com.bobmowzie.mowziesmobs.server.ai;

import com.bobmowzie.mowziesmobs.server.ability.Ability;
import com.bobmowzie.mowziesmobs.server.ability.AbilityHandler;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntityBlockSwapper;
import com.bobmowzie.mowziesmobs.server.entity.elokosa.EntityElokosa;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ElokosaLeapGoal extends Goal {
    protected final EntityElokosa elokosa;
    private int timer;
    private final UniformInt timeBetweenLongJumps;

    private int ticksCantFindLeap = 0;

    protected List<PossibleJump> jumpCandidates = Lists.newArrayList();

    public ElokosaLeapGoal(EntityElokosa elokosa, UniformInt cooldown) {
        this.elokosa = elokosa;
        timeBetweenLongJumps = cooldown;
        timer = (int) (timeBetweenLongJumps.sample(elokosa.getRandom()) * 0.2f);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    protected boolean ignoreTimerCondition() {
        return false;
    }

    protected Stream<BlockPos> generateStream() {
        BlockPos blockpos = elokosa.blockPosition();
        int i = blockpos.getX();
        int j = blockpos.getY() + 6;
        int k = blockpos.getZ();
        int maxLongJumpWidth = 26;
        int maxLongJumpHeight = 20;
        return BlockPos.betweenClosedStream(
                i - maxLongJumpWidth,
                j - maxLongJumpHeight,
                k - maxLongJumpWidth,
                i + maxLongJumpWidth,
                j + maxLongJumpHeight,
                k + maxLongJumpWidth
        ).filter(p -> p.distSqr(blockpos) <= maxLongJumpWidth * maxLongJumpWidth);
    }

    protected void generateCandidates() {
        BlockPos blockpos = elokosa.blockPosition();
        this.jumpCandidates = generateStream()
                .filter(p -> !p.equals(blockpos)
//                                && elokosa.getRandom().nextFloat() < 0.2
                                && (elokosa.level().getBlockState(p).isSolid() || elokosa.level().getBlockState(p).isLadder(elokosa.level(), p, elokosa)))
                .<PossibleJump>mapMulti((p, downstream) -> {
                    if (elokosa.level().getBlockState(p).isLadder(elokosa.level(), p, elokosa)) {
                        downstream.accept(new PossibleJump(p.immutable(), Direction.UP, scoreJump(p, Direction.UP)));
                    }
                    else {
                        downstream.accept(new PossibleJump(p.immutable(), Direction.UP, scoreJump(p, Direction.UP)));
                        downstream.accept(new PossibleJump(p.immutable(), Direction.DOWN, scoreJump(p, Direction.DOWN)));
                        downstream.accept(new PossibleJump(p.immutable(), Direction.NORTH, scoreJump(p, Direction.NORTH)));
                        downstream.accept(new PossibleJump(p.immutable(), Direction.SOUTH, scoreJump(p, Direction.SOUTH)));
                        downstream.accept(new PossibleJump(p.immutable(), Direction.EAST, scoreJump(p, Direction.EAST)));
                        downstream.accept(new PossibleJump(p.immutable(), Direction.WEST, scoreJump(p, Direction.WEST)));
                    }
                })
                .filter(j -> !elokosa.level().getBlockState(j.getJumpTarget().offset(j.getSurfaceDirection().getNormal())).isSolid())
                .sorted(Comparator.comparingInt(p -> -p.getWeight().asInt()))
                .collect(Collectors.toCollection(Lists::newArrayList));
    }

    protected int scoreJumpRandomnessAmount() {
        return 30;
    }

    protected int scoreJump(BlockPos pos, Direction dir) {
        int baseValue = 16;
        BlockState state = elokosa.level().getBlockState(pos);
        float treeBonus = state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) ? 6 : 0;
        int randomnessAmount = scoreJumpRandomnessAmount();
        int randomnessScore = elokosa.getRandom().nextIntBetweenInclusive(-randomnessAmount, randomnessAmount);
        float heightScore = (int) (pos.getY() - elokosa.getY());
        float pathfindingCost = elokosa.level().getPathfindingCostFromLightLevels(pos);
        int rawScore = baseValue + Mth.ceil(heightScore - pathfindingCost + treeBonus) + randomnessScore;
        return Math.max(rawScore, 1);
    }

    @Override
    public boolean canUse() {
        if (elokosa.isOnGroundOrClinging()) {
            timer -= 1;
            if (timer <= 0 || ignoreTimerCondition()) {
//                if (ignoreTimerCondition()) System.out.println("Ignoring timer");
                if (jumpCandidates.isEmpty()) {
                    generateCandidates();
                }
                boolean foundLeap = pickCandidate();
                if (foundLeap) {
                    ticksCantFindLeap = 0;
                }
                else {
                    ticksCantFindLeap++;
                }
                return foundLeap;
            }
        }
        return false;
    }

    protected Optional<PossibleJump> getJumpCandidate() {
        if (jumpCandidates.isEmpty()) return Optional.empty();
        Optional<PossibleJump> optional = this.jumpCandidates.stream().findFirst();//WeightedRandom.getRandomItem(elokosa.getRandom(), this.jumpCandidates);
        optional.ifPresent(this.jumpCandidates::remove);
        return optional;
    }

    private static final int ATTEMPTS_PER_TICK = 20;

    protected boolean pickCandidate() {
        int attempts = 0;
        while (!this.jumpCandidates.isEmpty()) {
            if (attempts > ATTEMPTS_PER_TICK) return false;
            Optional<PossibleJump> optional = this.getJumpCandidate();
            if (optional.isPresent()) {
                PossibleJump possiblejump = optional.get();
                BlockPos blockpos = possiblejump.getJumpTarget();
//                EntityBlockSwapper.swapBlock(elokosa.level(), possiblejump.getJumpTarget().offset(possiblejump.getSurfaceDirection().getNormal()), Blocks.TALL_GRASS.defaultBlockState(), 10, false, false);
                if (this.isAcceptableLandingPosition(possiblejump)) {
                    Vec3 vec3 = getLandingPositionFromJump(possiblejump);
                    if (elokosa.getNightForm() && possiblejump.getSurfaceDirection() == Direction.DOWN) {
                        vec3 = vec3.add(0, 0.5, 0);
                    }
                    Vec3 vec31 = elokosa.calculateOptimalJumpVector(elokosa, vec3);
                    if (vec31 != null) {
                        PathNavigation pathnavigation = elokosa.getNavigation();
                        Path path = pathnavigation.createPath(blockpos, 2);
                        if (path == null || !path.canReach()) {
                            elokosa.jumpVel = vec31;
                            elokosa.jumpGoalPos = vec3;
//                            EntityBlockSwapper.swapBlock(elokosa.level(), possiblejump.getJumpTarget(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 200, false, false);
                            return true;
                        }
                    }
                }
            }
            attempts++;
        }
        return false;
    }

    protected final Vec3 getLandingPositionFromJump(PossibleJump jump) {
        EntityDimensions entityDimensions = elokosa.getDimensions(Pose.STANDING);
        Vec3 leapLocation;
        if (jump.getSurfaceDirection().getAxis().isHorizontal()) {
            leapLocation = jump.getJumpTarget().offset(jump.getSurfaceDirection().getNormal()).getBottomCenter();
        }
        else if (jump.getSurfaceDirection() == Direction.DOWN) {
            leapLocation = jump.getJumpTarget().getBottomCenter().subtract(0, entityDimensions.height(), 0);
        }
        else {
            leapLocation = jump.getJumpTarget().above().getBottomCenter();
        }
        return leapLocation;//.add(0, 0.5, 0);
    }

    protected boolean isAcceptableLandingPosition(PossibleJump jump) {
        Level level = elokosa.level();
        if (!level.getBlockState(jump.getJumpTarget()).isSolid() && !level.getBlockState(jump.getJumpTarget()).isLadder(level, jump.getJumpTarget(), elokosa)) return false;
        if (elokosa.distanceToSqr(jump.getJumpTarget().getBottomCenter()) < 6) return false;

        EntityDimensions entityDimensions = elokosa.getDimensions(Pose.STANDING);
        Vec3 leapLocation = getLandingPositionFromJump(jump);

        AABB bounds = entityDimensions.makeBoundingBox(leapLocation);
        return level.noCollision(elokosa, bounds) && level.isUnobstructed(elokosa, Shapes.create(bounds)) && !level.containsAnyLiquid(bounds);
    }

    @Override
    public void start() {
        super.start();
        jumpCandidates.clear();
//        EntityBlockSwapper.swapBlock(elokosa.level(), BlockPos.containing(elokosa.jumpGoalPos), Blocks.TALL_GRASS.defaultBlockState(), 100, false, false);
        AbilityHandler.INSTANCE.sendAbilityMessage(elokosa, EntityElokosa.LEAP_ABILITY);
        timer = this.timeBetweenLongJumps.sample(elokosa.getRandom());
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean canContinueToUse() {
        Ability<?> ability = AbilityHandler.INSTANCE.getAbility(elokosa, EntityElokosa.LEAP_ABILITY);
        return ability != null && ability.isUsing();
    }

    @Override
    public void stop() {
        super.stop();
    }

    public static class PossibleJump extends WeightedEntry.IntrusiveBase {
        private final BlockPos jumpTarget;
        private final Direction surfaceDirection;

        public PossibleJump(BlockPos jumpTarget, Direction surfaceDirection, int weight) {
            super(weight);
            this.jumpTarget = jumpTarget;
            this.surfaceDirection = surfaceDirection;
        }

        public BlockPos getJumpTarget() {
            return this.jumpTarget;
        }

        public Direction getSurfaceDirection() {
            return surfaceDirection;
        }
    }
}