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
public record MessageRightMouseUp() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageRightMouseUp> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "message_right_mouse_up"));
    public static final StreamCodec<ByteBuf, MessageRightMouseUp> STREAM_CODEC = StreamCodec.unit(new MessageRightMouseUp());

    public static void handleServer(final MessageRightMouseUp packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            PlayerData data = DataHandler.getData(player, DataHandler.PLAYER_DATA);
            data.setMouseRightDown(false);

            for (Power power : data.getPowers()) {
                power.onRightMouseUp(player);
            }

            AbilityData abilityData = DataHandler.getData(player, DataHandler.ABILITY_DATA);

            if (abilityData != null) {
                for (Ability<?> ability : abilityData.getAbilities()) {
                    if (ability instanceof PlayerAbility playerAbility) {
                        playerAbility.onRightMouseUp(player);
                    }
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}