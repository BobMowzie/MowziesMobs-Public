package com.bobmowzie.mowziesmobs.client.particle;

import com.bobmowzie.mowziesmobs.client.render.MMRenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

/**
 * Created by BobMowzie on 6/2/2017.
 */
public class ParticleCloud extends TextureSheetParticle {
    private final float red;
    private final float green;
    private final float blue;
    private final float scale;
    private final EnumCloudBehavior behavior;
    private final float airDrag;

    public enum EnumCloudBehavior implements StringRepresentable {
        SHRINK("shrink"),
        GROW("grow"),
        CONSTANT("constant");

        private final String key;

        EnumCloudBehavior(final String key) {
            this.key = key;
        }

        @Override
        public @NotNull String getSerializedName() {
            return key;
        }
    }

    public ParticleCloud(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, double r, double g, double b, double scale, int duration, EnumCloudBehavior behavior, double airDrag) {
        super(world, x, y, z);
        this.scale = (float) scale * 0.5f * 0.1f;
        lifetime = duration;
        xd = vx * 0.5;
        yd = vy * 0.5;
        zd = vz * 0.5;
        red = (float) r;
        green = (float) g;
        blue = (float) b;
        this.behavior = behavior;
        roll = oRoll = (float) (random.nextInt(4) * Math.PI/2);
        this.airDrag = (float) airDrag;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return MMRenderType.PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH;
    }

    @Override
    public void tick() {
        super.tick();
        xd *= airDrag;
        yd *= airDrag;
        zd *= airDrag;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        float var = (age + partialTicks)/(float)lifetime;
        alpha = 0.2f * ((float) (1 - Math.exp(5 * (var - 1)) - Math.pow(2000, -var)));
        if (alpha < 0.01) alpha = 0.01f;
        if (behavior == EnumCloudBehavior.SHRINK) this.quadSize = scale * ((1 - 0.7f * var) + 0.3f);
        else if (behavior == EnumCloudBehavior.GROW) this.quadSize = scale * ((0.7f * var) + 0.3f);
        else this.quadSize = scale;

        super.render(buffer, renderInfo, partialTicks);
    }

    public static final class Provider implements ParticleProvider<Data> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(Data typeIn, @NotNull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleCloud particleCloud = new ParticleCloud(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.red(), typeIn.green(), typeIn.blue(), typeIn.scale(), typeIn.duration(), typeIn.behavior(), typeIn.airDrag());
            particleCloud.setSpriteFromAge(spriteSet);
            particleCloud.setColor(typeIn.red(), typeIn.green(), typeIn.blue());
            return particleCloud;
        }
    }

    public record Data(float red, float green, float blue, float scale, int duration, EnumCloudBehavior behavior, float airDrag) implements ParticleOptions {
        public static final Codec<EnumCloudBehavior> BEHAVIOUR_CODEC = StringRepresentable.fromEnum(EnumCloudBehavior::values);

        public static final MapCodec<Data> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.FLOAT.fieldOf("red").forGetter(data -> data.red),
                        Codec.FLOAT.fieldOf("green").forGetter(data -> data.green),
                        Codec.FLOAT.fieldOf("blue").forGetter(data -> data.blue),
                        Codec.FLOAT.fieldOf("scale").forGetter(data -> data.scale),
                        Codec.INT.fieldOf("duration").forGetter(data -> data.duration),
                        BEHAVIOUR_CODEC.fieldOf("behaviour").forGetter(data -> data.behavior),
                        Codec.FLOAT.fieldOf("air_drag").forGetter(data -> data.airDrag)
                ).apply(instance, Data::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = NeoForgeStreamCodecs.composite(
                ByteBufCodecs.FLOAT, Data::red,
                ByteBufCodecs.FLOAT, Data::green,
                ByteBufCodecs.FLOAT, Data::blue,
                ByteBufCodecs.FLOAT, Data::scale,
                ByteBufCodecs.INT, Data::duration,
                NeoForgeStreamCodecs.enumCodec(EnumCloudBehavior.class), Data::behavior,
                ByteBufCodecs.FLOAT, Data::airDrag,
                Data::new
        );

        @Override
        public @NotNull ParticleType<?> getType() {
            return ParticleHandler.CLOUD.value();
        }
    }
}
