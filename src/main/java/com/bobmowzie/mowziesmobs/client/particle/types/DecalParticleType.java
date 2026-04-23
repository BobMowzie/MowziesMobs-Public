package com.bobmowzie.mowziesmobs.client.particle.types;

import com.bobmowzie.mowziesmobs.client.particle.util.ParticleRotation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class DecalParticleType extends AdvancedParticleType {
    public static final MapCodec<DecalParticleType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    AdvancedParticleType.CODEC.fieldOf("base").forGetter(identity -> identity),
                    Codec.INT.fieldOf("sprite_size").forGetter(DecalParticleType::spriteSize),
                    Codec.INT.fieldOf("buffer_size").forGetter(DecalParticleType::bufferSize)
            ).apply(instance, DecalParticleType::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DecalParticleType> STREAM_CODEC = StreamCodec.composite(
            AdvancedParticleType.STREAM_CODEC, identity -> identity,
            ByteBufCodecs.VAR_INT, DecalParticleType::spriteSize,
            ByteBufCodecs.VAR_INT, DecalParticleType::bufferSize,
            DecalParticleType::new
    );

    private final int spriteSize;
    private final int bufferSize;

    public DecalParticleType(final AdvancedParticleType base, int spriteSize, int bufferSize) {
        super(base);
        this.spriteSize = spriteSize;
        this.bufferSize = bufferSize;
    }

    public float angle() {
        if (this.rotation() instanceof ParticleRotation.EulerAngles euler) {
            return euler.yaw;
        }
        if (this.rotation() instanceof ParticleRotation.FaceCamera faceCamera) {
            return faceCamera.angle;
        }

        return 0;
    }

    public int spriteSize() {
        return spriteSize;
    }

    public int bufferSize() {
        return bufferSize;
    }
}
