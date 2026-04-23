package com.bobmowzie.mowziesmobs.client.particle;

import com.bobmowzie.mowziesmobs.client.model.tools.MathUtils;
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
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Created by BobMowzie on 6/2/2017.
 */
public class ParticleSnowFlake extends TextureSheetParticle {
    private int swirlTick;
    private final float spread;
    boolean swirls;

    public ParticleSnowFlake(ClientLevel world, double x, double y, double z, double vX, double vY, double vZ, double duration, boolean swirls) {
        super(world, x, y, z);
        setSize(1, 1);
        xd = vX;
        yd = vY;
        zd = vZ;
        lifetime = (int) duration;
        swirlTick = random.nextInt(120);
        spread = random.nextFloat();
        this.swirls = swirls;
    }

    @Override
    protected float getU1() {
        return super.getU1() - (super.getU1() - super.getU0())/8f;
    }

    @Override
    protected float getV1() {
        return super.getV1() - (super.getV1() - super.getV0())/8f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return MMRenderType.PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH;
    }

    @Override
    public void tick() {
        super.tick();

        if (swirls) {
            Vector3f motionVec = new Vector3f((float)xd, (float)yd, (float)zd);
            motionVec.normalize();
            float yaw = (float) Math.atan2(motionVec.x(), motionVec.z());
            float pitch = (float) Math.atan2(motionVec.y(), 1);
            float swirlRadius = 4f * (age / (float) lifetime) * spread;
            Quaternionf quatSpin = new Quaternionf(new AxisAngle4f(swirlTick * 0.2f, motionVec));
            Quaternionf quatOrient = MathUtils.quatFromRotationXYZ(pitch, yaw, 0, false);
            Vector3f vec = new Vector3f(swirlRadius, 0, 0);
            quatOrient.transform(vec);
            quatSpin.transform(vec);
            x += vec.x();
            y += vec.y();
            z += vec.z();
        }

        if (age >= lifetime) {
            remove();
        }
        age++;
        swirlTick++;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        float var = (age + partialTicks)/(float)lifetime;
        alpha = (float) (1 - Math.exp(10 * (var - 1)) - Math.pow(2000, -var));
        if (alpha < 0.01) alpha = 0.01f;
        super.render(buffer, renderInfo, partialTicks);
    }

    public static final class Provider implements ParticleProvider<Data> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(Data typeIn, @NotNull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleSnowFlake particle = new ParticleSnowFlake(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.duration(), typeIn.swirls());
            particle.pickSprite(spriteSet);
            return particle;
        }
    }

    public record Data(float duration, boolean swirls) implements ParticleOptions {
        public static final MapCodec<ParticleSnowFlake.Data> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.FLOAT.fieldOf("duration").forGetter(ParticleSnowFlake.Data::duration),
                        Codec.BOOL.fieldOf("swirls").forGetter(ParticleSnowFlake.Data::swirls)
                ).apply(instance, ParticleSnowFlake.Data::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, ParticleSnowFlake.Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, ParticleSnowFlake.Data::duration,
                ByteBufCodecs.BOOL, ParticleSnowFlake.Data::swirls,
                ParticleSnowFlake.Data::new
        );

        @Override
        public @NotNull ParticleType<ParticleSnowFlake.Data> getType() {
            return ParticleHandler.SNOWFLAKE.get();
        }
    }
}
