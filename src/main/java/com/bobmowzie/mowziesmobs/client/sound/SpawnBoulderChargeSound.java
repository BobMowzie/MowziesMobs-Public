package com.bobmowzie.mowziesmobs.client.sound;

import com.bobmowzie.mowziesmobs.server.ability.AbilityHandler;
import com.bobmowzie.mowziesmobs.server.ability.AbilitySection;
import com.bobmowzie.mowziesmobs.server.ability.abilities.player.geomancy.SpawnBoulderAbility;
import com.bobmowzie.mowziesmobs.server.capability.AbilityData;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public class SpawnBoulderChargeSound extends AbstractTickableSoundInstance {
    private final LivingEntity user;
    private final SpawnBoulderAbility ability;

    public SpawnBoulderChargeSound(LivingEntity user) {
        super(MMSounds.EFFECT_GEOMANCY_BOULDER_CHARGE.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.user = user;
        volume = 1F;
        pitch = 1f;
        x = (float) user.getX();
        y = (float) user.getY();
        z = (float) user.getZ();

        AbilityData data = DataHandler.getData(user, DataHandler.ABILITY_DATA);
        ability = (SpawnBoulderAbility) data.getAbilityMap().get(AbilityHandler.SPAWN_BOULDER_ABILITY);
    }

    @Override
    public void tick() {
        if (ability == null) {
            stop();
            return;
        }
        if (!ability.isUsing() || ability.getCurrentSection().sectionType != AbilitySection.AbilitySectionType.STARTUP) {
            stop();
        }
    }
}
