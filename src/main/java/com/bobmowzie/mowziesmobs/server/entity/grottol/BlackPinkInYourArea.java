package com.bobmowzie.mowziesmobs.server.entity.grottol;

import com.bobmowzie.mowziesmobs.server.message.MessageBlackPinkInYourArea;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.BiConsumer;

public final class BlackPinkInYourArea implements BiConsumer<Level, AbstractMinecart> {
    private BlackPinkInYourArea() {}

    @Override
    public void accept(Level world, AbstractMinecart minecart) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(minecart, MessageBlackPinkInYourArea.fromMinecraft(minecart));
        if (!world.isClientSide) {
            Entity rider = minecart.getFirstPassenger();
            if (rider instanceof EntityGrottol grottol) {
                grottol.setBlackpink(true);
            }
        }
    }

    public static BlackPinkInYourArea create() {
        return new BlackPinkInYourArea();
    }
}
