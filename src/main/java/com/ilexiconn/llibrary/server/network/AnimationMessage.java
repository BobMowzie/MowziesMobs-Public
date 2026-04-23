package com.ilexiconn.llibrary.server.network;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record AnimationMessage(int entityId, int index) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AnimationMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "animation_message"));

    public static final StreamCodec<ByteBuf, AnimationMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            AnimationMessage::entityId,
            ByteBufCodecs.INT,
            AnimationMessage::index,
            AnimationMessage::new
    );

    public static void handleClient(final AnimationMessage packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = MMCommon.PROXY.getClientLevel();

            if (level != null && level.getEntity(packet.entityId()) instanceof IAnimatedEntity entity) {
                if (packet.index() == -1) {
                    entity.setAnimation(IAnimatedEntity.NO_ANIMATION);
                } else {
                    entity.setAnimation(entity.getAnimations()[packet.index()]);
                }
                entity.setAnimationTick(0);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
