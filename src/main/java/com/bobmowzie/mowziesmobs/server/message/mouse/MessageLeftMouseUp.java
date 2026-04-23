package com.bobmowzie.mowziesmobs.server.message.mouse;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.ability.Ability;
import com.bobmowzie.mowziesmobs.server.ability.PlayerAbility;
import com.bobmowzie.mowziesmobs.server.capability.AbilityData;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.bobmowzie.mowziesmobs.server.capability.PlayerData;
import com.bobmowzie.mowziesmobs.server.power.Power;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by BobMowzie on 5/25/2017.
 */
public record MessageLeftMouseUp() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageLeftMouseUp> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "message_left_mouse_up"));
    public static final StreamCodec<ByteBuf, MessageLeftMouseUp> STREAM_CODEC = StreamCodec.unit(new MessageLeftMouseUp());

    public static void handleServer(final MessageLeftMouseUp packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            PlayerData data = DataHandler.getData(player, DataHandler.PLAYER_DATA);
            data.setMouseLeftDown(false);

            for (Power power : data.getPowers()) {
                power.onLeftMouseUp(player);
            }

            AbilityData abilityData = DataHandler.getData(player, DataHandler.ABILITY_DATA);

            if (abilityData != null) {
                for (Ability<?> ability : abilityData.getAbilities()) {
                    if (ability instanceof PlayerAbility playerAbility) {
                        playerAbility.onLeftMouseUp(player);
                    }
                }
            }
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}