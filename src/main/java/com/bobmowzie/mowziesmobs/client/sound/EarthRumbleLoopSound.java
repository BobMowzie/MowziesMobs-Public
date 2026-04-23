package com.bobmowzie.mowziesmobs.client.sound;

import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import com.ilexiconn.llibrary.client.model.tools.ControlledAnimation;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EarthRumbleLoopSound extends AbstractTickableSoundInstance {
    private final IGeomancyRumbler rumbler;
    int ticksExisted = 0;
    ControlledAnimation volumeControl;
    boolean active = false;

    public EarthRumbleLoopSound(IGeomancyRumbler rumbler) {
        super(MMSounds.EFFECT_GEOMANCY_RUMBLE_LOOP.get(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.rumbler = rumbler;
        volume = 0F;
        pitch = 1f;
        x = rumbler.getRumblerX();
        y = rumbler.getRumblerY();
        z = rumbler.getRumblerZ();
        volumeControl = new ControlledAnimation(10);
        volumeControl.setTimer(0);
        looping = true;
    }

    @Override
    public void tick() {
        if (active) volumeControl.increaseTimer();
        else volumeControl.decreaseTimer();
        volume = volumeControl.getAnimationFraction() * 3 * rumbler.getRumbleVolume();
        pitch = rumbler.getRumblePitch();
        if (volumeControl.getAnimationFraction() <= 0.05 && (rumbler == null || rumbler.isFinishedRumbling()))
            stop();
        if (rumbler != null && rumbler.isRumbling()) {
            active = true;
            x = rumbler.getRumblerX();
            y = rumbler.getRumblerY();
            z = rumbler.getRumblerZ();
            if (!rumbler.isRumbling()) {
                active = false;
            }
        }
        else {
            active = false;
        }
        ticksExisted++;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
