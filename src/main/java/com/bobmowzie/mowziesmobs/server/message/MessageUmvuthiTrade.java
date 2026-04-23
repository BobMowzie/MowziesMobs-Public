package com.bobmowzie.mowziesmobs.server.message;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthi;
import com.bobmowzie.mowziesmobs.server.inventory.ContainerUmvuthiTrade;
import com.bobmowzie.mowziesmobs.server.potion.EffectHandler;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by BobMowzie on 11/14/2016.
 */
public record MessageUmvuthiTrade(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageUmvuthiTrade> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "message_umvuthi_trade"));
    public static final StreamCodec<ByteBuf, MessageUmvuthiTrade> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MessageUmvuthiTrade::entityId,
            MessageUmvuthiTrade::new
    );

    public static void handleServer(final MessageUmvuthiTrade packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();

            if (!(player.level().getEntity(packet.entityId()) instanceof EntityUmvuthi umvuthi)) {
                return;
            }

            if (umvuthi.getCustomer() != player) {
                return;
            }

            if (!(player.containerMenu instanceof ContainerUmvuthiTrade trade)) {
                return;
            }

            boolean hasTradedWith = umvuthi.hasTradedWith(player);

            if (!hasTradedWith && umvuthi.fulfillDesire(trade.getSlot(0))) {
                umvuthi.rememberTrade(player);
                trade.returnItems();
                trade.broadcastChanges();
            } else if (hasTradedWith) {
                player.addEffect(new MobEffectInstance(EffectHandler.SUNS_BLESSING, MobEffectInstance.INFINITE_DURATION, 0, false, false));

                if (umvuthi.getActiveAbilityType() != EntityUmvuthi.BLESS_ABILITY) {
                    umvuthi.sendAbilityMessage(EntityUmvuthi.BLESS_ABILITY);
                    umvuthi.playSound(MMSounds.ENTITY_UMVUTHI_BLESS.get(), 2, 1);
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
