package com.bobmowzie.mowziesmobs.server.entity.elokosa;

import com.bobmowzie.mowziesmobs.server.ai.ElokosaHurtByTargetGoal;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class EntityElokosaFollowerToHowler extends EntityElokosaFollower<EntityElokosaHowler> {

    public EntityElokosaFollowerToHowler(EntityType<? extends EntityElokosaFollowerToHowler> type, Level world) {
        this(type, world, null);
    }

    public EntityElokosaFollowerToHowler(EntityType<? extends EntityElokosaFollowerToHowler> type, Level world, EntityElokosaHowler leader) {
        super(type, world, EntityElokosaHowler.class, leader);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new ElokosaHurtByTargetGoal(this, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (leader != null) {
            setTarget(leader.getTarget());
        }
        if (getLeaderUUID() == ABSENT_LEADER && !addedHuntingTargetGoals) {
            registerHuntingTargetGoals();
        }

        if (!this.level().isClientSide && this.level().getDifficulty() == Difficulty.PEACEFUL)
        {
            this.discard();
        }
    }

    @Override
    protected int getPackSize() {
        if (leader == null) return 0;
        return leader.getPackSize();
    }

    @Override
    protected void addAsPackMember() {
        if (leader == null) return;
        leader.addPackMember(this);
    }

    @Override
    protected void removeAsPackMember() {
        if (leader == null) return;
        leader.removePackMember(this);
    }

    public void removeLeader() {
        this.setLeaderUUID(ABSENT_LEADER);
        this.leader = null;
        this.setTarget(null);
    }

    @Override
    public void setLeaderUUID(Optional<UUID> uuid) {
        super.setLeaderUUID(uuid);
        if (uuid == ABSENT_LEADER && !addedHuntingTargetGoals) registerHuntingTargetGoals();
    }
}
