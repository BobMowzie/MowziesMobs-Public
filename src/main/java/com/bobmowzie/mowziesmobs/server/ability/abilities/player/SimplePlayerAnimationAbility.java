package com.bobmowzie.mowziesmobs.server.ability.abilities.player;

import com.bobmowzie.mowziesmobs.server.ability.AbilitySection;
import com.bobmowzie.mowziesmobs.server.ability.AbilityType;
import com.bobmowzie.mowziesmobs.server.ability.PlayerAbility;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.core.animation.Animation;

public class SimplePlayerAnimationAbility extends PlayerAbility {
    private String animationName;
    private boolean separateLeftAndRight3rdPerson;
    private boolean separateLeftAndRight1stPerson;
    private boolean lockHeldItemActiveHand;

    public SimplePlayerAnimationAbility(AbilityType<Player, SimplePlayerAnimationAbility> abilityType, Player user, String animationName, int duration) {
        super(abilityType, user, new AbilitySection[] {
                new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, duration)
        });
        this.animationName = animationName;
    }

    public SimplePlayerAnimationAbility(AbilityType<Player, SimplePlayerAnimationAbility> abilityType, Player user, String animationName, int duration, boolean separateLeftAndRight1stPerson, boolean separateLeftAndRight3rdPerson, boolean lockHeldItemMainHand) {
        super(abilityType, user, new AbilitySection[] {
                new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, duration)
        });
        this.animationName = animationName;
        this.separateLeftAndRight3rdPerson = separateLeftAndRight3rdPerson;
        this.separateLeftAndRight1stPerson = separateLeftAndRight1stPerson;
        this.lockHeldItemActiveHand = lockHeldItemMainHand;
    }

    @Override
    public void start() {
        super.start();
        boolean usingSide = getActiveHand() == InteractionHand.MAIN_HAND;
        boolean mainSide = getUser().getMainArm() == HumanoidArm.RIGHT;

        playAnimation(animationName, Animation.LoopType.DEFAULT, separateLeftAndRight1stPerson, separateLeftAndRight3rdPerson);

        // Held items
        if (lockHeldItemActiveHand) {
            if (usingSide) {
                if (getUser().isUsingItem()) {
                    heldItemMainHandVisualOverride = getUser().getUseItem();
                }
                else {
                    heldItemMainHandVisualOverride = getUser().getMainHandItem();
                }
            }
            else {
                if (getUser().isUsingItem()) {
                    heldItemOffHandVisualOverride = getUser().getUseItem();
                }
                else {
                    heldItemOffHandVisualOverride = getUser().getOffhandItem();
                }
            }
        }
    }
}
