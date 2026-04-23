package com.bobmowzie.mowziesmobs.server.message;

import com.bobmowzie.mowziesmobs.server.message.mouse.MessageLeftMouseDown;
import com.bobmowzie.mowziesmobs.server.message.mouse.MessageLeftMouseUp;
import com.bobmowzie.mowziesmobs.server.message.mouse.MessageRightMouseDown;
import com.bobmowzie.mowziesmobs.server.message.mouse.MessageRightMouseUp;
import com.ilexiconn.llibrary.server.network.AnimationMessage;
import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function15;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    public static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // -> Client
        registrar.playToClient(MessageUseAbility.TYPE, MessageUseAbility.STREAM_CODEC, MessageUseAbility::handleClient);
        registrar.playToClient(MessageUpdateBossBar.TYPE, MessageUpdateBossBar.STREAM_CODEC, MessageUpdateBossBar::handleClient);
        registrar.playToClient(MessageSunblockEffect.TYPE, MessageSunblockEffect.STREAM_CODEC, MessageSunblockEffect::handleClient);
        registrar.playToClient(MessageLinkEntities.TYPE, MessageLinkEntities.STREAM_CODEC, MessageLinkEntities::handleClient);
        registrar.playToClient(MessageInterruptAbility.TYPE, MessageInterruptAbility.STREAM_CODEC, MessageInterruptAbility::handleClient);
        registrar.playToClient(MessageFreezeEffect.TYPE, MessageFreezeEffect.STREAM_CODEC, MessageFreezeEffect::handleClient);
        registrar.playToClient(MessageBlackPinkInYourArea.TYPE, MessageBlackPinkInYourArea.STREAM_CODEC, MessageBlackPinkInYourArea::handleClient);
        registrar.playToClient(MessageJumpToAbilitySectionServerToClient.TYPE, MessageJumpToAbilitySectionServerToClient.STREAM_CODEC, MessageJumpToAbilitySectionServerToClient::handleClient);
        registrar.playToClient(AnimationMessage.TYPE, AnimationMessage.STREAM_CODEC, AnimationMessage::handleClient);
        registrar.playToClient(MessageAddInProgressCooldown.TYPE, MessageAddInProgressCooldown.STREAM_CODEC, MessageAddInProgressCooldown::handleClient);

        // -> Server
        registrar.playToServer(MessageUmvuthiTrade.TYPE, MessageUmvuthiTrade.STREAM_CODEC, MessageUmvuthiTrade::handleServer);
        registrar.playToServer(MessageSculptorTrade.TYPE, MessageSculptorTrade.STREAM_CODEC, MessageSculptorTrade::handleServer);
        registrar.playToServer(MessagePlayerUseAbility.TYPE, MessagePlayerUseAbility.STREAM_CODEC, MessagePlayerUseAbility::handleServer);
        registrar.playToServer(MessageRightMouseUp.TYPE, MessageRightMouseUp.STREAM_CODEC, MessageRightMouseUp::handleServer);
        registrar.playToServer(MessageRightMouseDown.TYPE, MessageRightMouseDown.STREAM_CODEC, MessageRightMouseDown::handleServer);
        registrar.playToServer(MessageLeftMouseUp.TYPE, MessageLeftMouseUp.STREAM_CODEC, MessageLeftMouseUp::handleServer);
        registrar.playToServer(MessageLeftMouseDown.TYPE, MessageLeftMouseDown.STREAM_CODEC, MessageLeftMouseDown::handleServer);
    }

    public static StreamCodec<FriendlyByteBuf, Vec3[]> VEC3_ARRAY = new StreamCodec<>() {
        public Vec3 @NotNull [] decode(@NotNull FriendlyByteBuf buffer) {
            return NetworkHandler.readVec3Array(buffer);
        }

        public void encode(@NotNull FriendlyByteBuf buffer, Vec3 @NotNull [] value) {
            NetworkHandler.writeVec3Array(buffer, value);
        }
    };

    private static void writeVec3Array(FriendlyByteBuf buffer, Vec3[] array) {
        VarInt.write(buffer, array.length);

        for (Vec3 vec3 : array) {
            buffer.writeVec3(vec3);
        }
    }

    private static Vec3[] readVec3Array(FriendlyByteBuf buffer) {
        int size = VarInt.read(buffer);
        Vec3[] vec3s = new Vec3[size];

        // should result in keeping the intended order
        for (int index = 0; index < size; index++) {
            vec3s[index] = buffer.readVec3();
        }

        return vec3s;
    }

    // 10 fields
    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2,
            final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3,
            final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4,
            final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5,
            final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6,
            final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7,
            final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8,
            final Function<C, T8> getter8,
            final StreamCodec<? super B, T9> codec9,
            final Function<C, T9> getter9,
            final StreamCodec<? super B, T10> codec10,
            final Function<C, T10> getter10,
            final Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(@NotNull B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
            }

            @Override
            public void encode(@NotNull B buffer, @NotNull C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
            }
        };
    }

    // 11 fields
    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2,
            final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3,
            final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4,
            final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5,
            final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6,
            final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7,
            final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8,
            final Function<C, T8> getter8,
            final StreamCodec<? super B, T9> codec9,
            final Function<C, T9> getter9,
            final StreamCodec<? super B, T10> codec10,
            final Function<C, T10> getter10,
            final StreamCodec<? super B, T11> codec11,
            final Function<C, T11> getter11,
            final Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(@NotNull B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                T11 t11 = codec11.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
            }

            @Override
            public void encode(@NotNull B buffer, @NotNull C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
                codec11.encode(buffer, getter11.apply(object));
            }
        };
    }

    // 12 fields
    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2,
            final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3,
            final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4,
            final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5,
            final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6,
            final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7,
            final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8,
            final Function<C, T8> getter8,
            final StreamCodec<? super B, T9> codec9,
            final Function<C, T9> getter9,
            final StreamCodec<? super B, T10> codec10,
            final Function<C, T10> getter10,
            final StreamCodec<? super B, T11> codec11,
            final Function<C, T11> getter11,
            final StreamCodec<? super B, T12> codec12,
            final Function<C, T12> getter12,
            final Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(@NotNull B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                T11 t11 = codec11.decode(buffer);
                T12 t12 = codec12.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
            }

            @Override
            public void encode(@NotNull B buffer, @NotNull C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
                codec11.encode(buffer, getter11.apply(object));
                codec12.encode(buffer, getter12.apply(object));
            }
        };
    }

    // 15 fields
    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2,
            final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3,
            final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4,
            final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5,
            final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6,
            final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7,
            final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8,
            final Function<C, T8> getter8,
            final StreamCodec<? super B, T9> codec9,
            final Function<C, T9> getter9,
            final StreamCodec<? super B, T10> codec10,
            final Function<C, T10> getter10,
            final StreamCodec<? super B, T11> codec11,
            final Function<C, T11> getter11,
            final StreamCodec<? super B, T12> codec12,
            final Function<C, T12> getter12,
            final StreamCodec<? super B, T13> codec13,
            final Function<C, T13> getter13,
            final StreamCodec<? super B, T14> codec14,
            final Function<C, T14> getter14,
            final StreamCodec<? super B, T15> codec15,
            final Function<C, T15> getter15,
            final Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(@NotNull B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                T11 t11 = codec11.decode(buffer);
                T12 t12 = codec12.decode(buffer);
                T13 t13 = codec13.decode(buffer);
                T14 t14 = codec14.decode(buffer);
                T15 t15 = codec15.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
            }

            @Override
            public void encode(@NotNull B buffer, @NotNull C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
                codec11.encode(buffer, getter11.apply(object));
                codec12.encode(buffer, getter12.apply(object));
                codec13.encode(buffer, getter13.apply(object));
                codec14.encode(buffer, getter14.apply(object));
                codec15.encode(buffer, getter15.apply(object));
            }
        };
    }
}