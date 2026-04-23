package com.bobmowzie.mowziesmobs.server.ability.abilities.player.geomancy;

import com.bobmowzie.mowziesmobs.server.ability.AbilitySection;
import com.bobmowzie.mowziesmobs.server.ability.AbilityType;
import com.bobmowzie.mowziesmobs.server.ability.PlayerAbility;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.core.animation.Animation;

public class HitBoulderAbility extends PlayerAbility {

    public HitBoulderAbility(AbilityType<Player, HitBoulderAbility> abilityType, Player user) {
        super(abilityType, user, new AbilitySection[] {
                new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, 10)
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
        boolean usingMainHand = getActiveHand() == InteractionHand.MAIN_HAND;

        playAnimation("hit_boulder", Animation.LoopType.DEFAULT, true, false);

        // Held items
        if (usingMainHand) {
            heldItemMainHandVisualOverride = getUser().getItemInHand(InteractionHand.MAIN_HAND);
        }
        else {
            heldItemOffHandVisualOverride = getUser().getItemInHand(InteractionHand.OFF_HAND);
        }
    }
}
