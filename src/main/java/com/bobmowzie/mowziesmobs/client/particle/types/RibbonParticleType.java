package com.bobmowzie.mowziesmobs.client.particle.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class RibbonParticleType extends AdvancedParticleType {
    public static final MapCodec<RibbonParticleType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    AdvancedParticleType.CODEC.fieldOf("base").forGetter(identity -> identity),
                    Codec.INT.fieldOf("length").forGetter(RibbonParticleType::length)
            ).apply(instance, RibbonParticleType::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RibbonParticleType> STREAM_CODEC = StreamCodec.composite(
            AdvancedParticleType.STREAM_CODEC, identity -> identity,
            ByteBufCodecs.VAR_INT, RibbonParticleType::length,
            RibbonParticleType::new
    );

    private final int length;

    public RibbonParticleType(final AdvancedParticleType base, final int length) {
        super(base);
        this.length = length;
    }

    public int length() {
        return this.length;
    }
}
