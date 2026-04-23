package com.bobmowzie.mowziesmobs.client.particle.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TerrainParticleData extends AdvancedParticleData {
    public static final Deserializer<TerrainParticleData> DESERIALIZER = new Deserializer<TerrainParticleData>() {
        public TerrainParticleData fromCommand(ParticleType<TerrainParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            double airDrag = reader.readDouble();
            reader.expect(' ');
            double scale = reader.readDouble();
            reader.expect(' ');
            double angle = reader.readDouble();
            reader.expect(' ');
            double duration = reader.readDouble();
            reader.expect(' ');
            BlockState blockState = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), reader, false).blockState();
            reader.expect(' ');
            return new TerrainParticleData(particleTypeIn, angle, scale, airDrag, duration, blockState);
        }

        public TerrainParticleData fromNetwork(ParticleType<TerrainParticleData> particleTypeIn, FriendlyByteBuf buffer) {
            double airDrag = buffer.readFloat();
            double scale = buffer.readFloat();
            double angle = buffer.readFloat();
            double duration = buffer.readFloat();
            BlockState state = buffer.readById(Block.BLOCK_STATE_REGISTRY);
            return new TerrainParticleData(particleTypeIn, angle, scale, airDrag, duration, state);
        }
    };

    private final BlockState state;

    public TerrainParticleData(ParticleType<? extends TerrainParticleData> type, double rotation, double scale, double drag, double duration, BlockState state) {
        this(type, rotation, scale, drag, duration, state, new ParticleComponent[]{});
    }

    public TerrainParticleData(ParticleType<? extends TerrainParticleData> type, double angle, double scale, double drag, double duration, BlockState state, ParticleComponent[] components) {
        super(type, new ParticleRotation.EulerAngles((float) angle, 0, 0), scale, 1.0, 1.0, 1.0, 1.0, drag, duration, false, true, components);
        this.state = state;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        super.writeToNetwork(buffer);
        buffer.writeId(Block.BLOCK_STATE_REGISTRY, this.state);
    }

    public String writeToString() {
        return super.writeToString() + " " + BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + BlockStateParser.serialize(this.state);
    }

    @OnlyIn(Dist.CLIENT)
    public double getAngle() {
        if (this.getRotation() instanceof ParticleRotation.EulerAngles) {
            return ((ParticleRotation.EulerAngles) this.getRotation()).yaw;
        }
        return 0;
    }

    public BlockState getState() {
        return this.state;
    }

    public static Codec<TerrainParticleData> CODEC_TERRAIN(ParticleType<TerrainParticleData> particleType) {
        return RecordCodecBuilder.create((codecBuilder) -> codecBuilder.group(
                Codec.DOUBLE.fieldOf("scale").forGetter(TerrainParticleData::getScale),
                Codec.DOUBLE.fieldOf("drag").forGetter(TerrainParticleData::getAirDrag),
                Codec.DOUBLE.fieldOf("duration").forGetter(TerrainParticleData::getDuration),
                Codec.DOUBLE.fieldOf("angle").forGetter(TerrainParticleData::getAngle),
                BlockState.CODEC.fieldOf("state").forGetter(TerrainParticleData::getState)
                ).apply(codecBuilder, (scale, drag, duration, angle, state) ->
                    new TerrainParticleData(particleType, angle, scale, drag, duration, state, new ParticleComponent[]{}))
        );
    }

    //FORGE: Add a source pos property, so we can provide models with additional model data
    private net.minecraft.core.BlockPos pos;
    public TerrainParticleData setPos(net.minecraft.core.BlockPos pos) {
        this.pos = pos;
        return this;
    }

    public net.minecraft.core.BlockPos getPos() {
        return pos;
    }
}
