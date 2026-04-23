package com.bobmowzie.mowziesmobs.server;

import com.bobmowzie.mowziesmobs.client.sound.BossMusicSound;
import com.bobmowzie.mowziesmobs.client.sound.IGeomancyRumbler;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySolarBeam;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySunstrike;
import com.bobmowzie.mowziesmobs.server.entity.naga.EntityNaga;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ServerProxy {
    public void init() {
    }

    public void playSunstrikeSound(EntitySunstrike strike) {
    }

    public void playIceBreathSound(Entity entity) {
    }

    public void playBoulderChargeSound(LivingEntity player) {
    }

    public void playNagaSwoopSound(EntityNaga naga) {
    }

    public void playBlackPinkSound(AbstractMinecart entity) {
    }

    public void playSunblockSound(LivingEntity entity) {
    }

    public void playSolarBeamSound(EntitySolarBeam entity) {
    }

    public void playGeomancyRumbleSound(IGeomancyRumbler rumbler) {
    }

    public void minecartParticles(Level world, AbstractMinecart minecart, float scale, double x, double y,
                                  double z, BlockState state, BlockPos pos) {
    }

    public void setTPS(float tickRate) {
    }

    public Entity getReferencedMob() {
        return null;
    }

    public void setReferencedMob(Entity referencedMob) {
    }

    public void sculptorMarkBlock(int id, BlockPos pos) {
    }

    public void updateMarkedBlocks() {
    }

    public @Nullable Player getLocalPlayer() {
        return null;
    }

    public @Nullable Level getClientLevel() {
        return null;
    }

    public void stopMusic() {}

    public void playMusic(final BossMusicSound music) {}
}
