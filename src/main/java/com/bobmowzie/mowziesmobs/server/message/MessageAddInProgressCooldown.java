package com.bobmowzie.mowziesmobs.server.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class MessageAddInProgressCooldown {
    private Item item;
    private int startTime;
    private int endTime;

    public MessageAddInProgressCooldown() {
    }

    public MessageAddInProgressCooldown(Item item, int startTime, int endTime) {
        this.item = item;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static void serialize(final MessageAddInProgressCooldown message, final FriendlyByteBuf buf) {
        buf.writeRegistryId(ForgeRegistries.ITEMS, message.item);
        buf.writeVarInt(message.startTime);
        buf.writeVarInt(message.endTime);
    }

    public static MessageAddInProgressCooldown deserialize(final FriendlyByteBuf buf) {
        final MessageAddInProgressCooldown message = new MessageAddInProgressCooldown();
        message.item = buf.readRegistryId();
        message.startTime = buf.readVarInt();
        message.endTime = buf.readVarInt();
        return message;
    }

    public static class Handler implements BiConsumer<MessageAddInProgressCooldown, Supplier<NetworkEvent.Context>> {
        @Override
        @OnlyIn(Dist.CLIENT)
        public void accept(final MessageAddInProgressCooldown message, final Supplier<NetworkEvent.Context> contextSupplier) {
            final NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    player.getCooldowns().cooldowns.put(message.item, new ItemCooldowns.CooldownInstance(message.startTime, message.endTime));
                }
            });
            context.setPacketHandled(true);
        }
    }
}
