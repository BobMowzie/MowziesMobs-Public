package com.bobmowzie.mowziesmobs.client.particle.types;

import com.bobmowzie.mowziesmobs.client.particle.util.ParticleRotation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

public class TerrainParticleType extends AdvancedParticleType {
    public static final MapCodec<TerrainParticleType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    AdvancedParticleType.CODEC.fieldOf("base").forGetter(identity -> identity),
                    BlockState.CODEC.fieldOf("state").forGetter(TerrainParticleType::state)
            ).apply(instance, TerrainParticleType::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TerrainParticleType> STREAM_CODEC = StreamCodec.composite(
            AdvancedParticleType.STREAM_CODEC, identity -> identity,
            ByteBufCodecs.fromCodecWithRegistries(BlockState.CODEC), TerrainParticleType::state,
            TerrainParticleType::new
    );

    private final BlockState state;
    private BlockPos position;

    public TerrainParticleType(final AdvancedParticleType base, final BlockState state) {
        super(base);
        this.state = state;
    }

    public TerrainParticleType setPosition(final BlockPos position) {
        this.position = position;
        return this;
    }

    public float angle() {
        if (this.rotation() instanceof ParticleRotation.EulerAngles angles) {
            return angles.yaw;
        }

        return 0;
    }

    public BlockState state() {
        return state;
    }

    public BlockPos position() {
        return position;
    }
}
