package com.bobmowzie.mowziesmobs.server.entity.grottol;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.server.message.MessageBlackPinkInYourArea;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.BiConsumer;

public final class BlackPinkInYourArea implements BiConsumer<Level, AbstractMinecart> {
    private BlackPinkInYourArea() {}

    @Override
    public void accept(Level world, AbstractMinecart minecart) {
        MowziesMobs.NETWORK.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> minecart), new MessageBlackPinkInYourArea(minecart));
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
