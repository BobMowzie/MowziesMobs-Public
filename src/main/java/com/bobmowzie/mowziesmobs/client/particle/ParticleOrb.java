package com.bobmowzie.mowziesmobs.client.particle;

import com.bobmowzie.mowziesmobs.client.model.tools.MathUtils;
import com.bobmowzie.mowziesmobs.client.render.MMRenderType;
import com.bobmowzie.mowziesmobs.server.message.NetworkHandler;
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

public class ParticleOrb extends TextureSheetParticle {
    private double targetX;
    private double targetY;
    private double targetZ;
    private double startX;
    private double startY;
    private double startZ;
    private double signX;
    private double signZ;
    private float red, green, blue;
    private int mode;
    private double duration;

    public ParticleOrb(ClientLevel world, double x, double y, double z, double targetX, double targetZ) {
        super(world, x, y, z);
        this.targetX = targetX;
        this.targetZ = targetZ;
        quadSize = (4.5F + random.nextFloat() * 1.5F) * 0.1f;
        lifetime = 120;
        signX = Math.signum(targetX - x);
        signZ = Math.signum(targetZ - z);
        mode = 0;
        alpha = 0;
        red = green = blue = 1;
    }

    public ParticleOrb(ClientLevel world, double x, double y, double z, double targetX, double targetY, double targetZ, double speed) {
        this(world, x, y, z, targetX, targetZ);
        this.targetY = targetY;
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.duration = speed;
        mode = 1;
        alpha = 0.1f;
    }

    public ParticleOrb(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, double r, double g, double b, double scale, int duration) {
        super(world, x, y, z);
        quadSize = (float) scale * 0.1f;
        lifetime = duration;
        this.duration = duration;
        xd = vx;
        yd = vy;
        zd = vz;
        setColor((float) r, (float) g, (float) b);
        mode = 2;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return MMRenderType.PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH;
    }

    @Override
    public int getLightColor(float delta) {
        return 240 | super.getLightColor(delta) & 0xFF0000;
    }

    @Override
    public void tick() {
        alpha = 0.1f;
        xo = x;
        yo = y;
        zo = z;
        if (mode == 0) {
            double vecX = targetX - x;
            double vecZ = targetZ - z;
            double dist = Math.sqrt(vecX * vecX + vecZ * vecZ);
            if (dist > 2 || Math.signum(vecX) != signX || Math.signum(vecZ) != signZ || age > lifetime) {
                remove();
                return;
            }
            final double peak = 0.5;
            alpha = (float) (dist > peak ? MathUtils.linearTransformd(dist, peak, 2, 1, 0) : MathUtils.linearTransformd(dist, 0.1F, peak, 0, 1));
            final double minVel = 0.05, maxVel = 0.3;
            double progress = Math.sin(-Math.PI / 4 * dist) + 1;
            double magMultipler = (progress * (maxVel - minVel) + minVel) / dist;
            vecX *= magMultipler;
            vecZ *= magMultipler;
            xd = vecX;
            yd = progress;
            zd = vecZ;
            move(xd, yd, zd);
        } else if (mode == 1) {
            alpha = ((float)age/(float)duration);//(float) (1 * Math.sqrt(Math.pow(posX - startX, 2) + Math.pow(posY - startY, 2) + Math.pow(posZ - startZ, 2)) / Math.sqrt(Math.pow(targetX - startX, 2) + Math.pow(targetY - startY, 2) + Math.pow(targetZ - startZ, 2)));
            x = startX + (targetX - startX) / (1 + Math.exp(-(8 / duration) * (age - duration / 2)));
            y = startY + (targetY - startY) / (1 + Math.exp(-(8 / duration) * (age - duration / 2)));
            z = startZ + (targetZ - startZ) / (1 + Math.exp(-(8 / duration) * (age - duration / 2)));
            if (age == duration) {
                remove();
            }
        }
        else if (mode == 2) {
            super.tick();
//            particleAlpha = ((float)age/(float)maxAge);
            if (age >= lifetime) {
                remove();
            }
        }
        age++;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        if (mode == 2) alpha = Math.max(1 - ((float)age + partialTicks)/(float)duration, 0.001f);
        else alpha = ((float)age + partialTicks)/(float)duration;
        rCol = red;
        gCol = green;
        bCol = blue;

        super.render(buffer, renderInfo, partialTicks);
    }

    public static final class Provider implements ParticleProvider<Data> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(Data data, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleOrb particle = switch (data.mode()) {
                case 0:
                    yield new ParticleOrb(level, x, y, z, data.targetX(), data.targetZ());
                case 1:
                    yield new ParticleOrb(level, x, y, z, data.targetX(), data.targetY(), data.targetZ(), data.speed());
                default:
                    yield new ParticleOrb(level, x, y, z, xSpeed, ySpeed, zSpeed, data.red(), data.green(), data.blue(), data.scale(), data.duration());
            };

            particle.setSpriteFromAge(spriteSet);
            return particle;
        }
    }

    public record Data(float red, float green, float blue, float scale, int duration, float targetX, float targetY, float targetZ, float speed, int mode) implements ParticleOptions {
        public static final MapCodec<Data> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.FLOAT.fieldOf("red").forGetter(Data::red),
                        Codec.FLOAT.fieldOf("green").forGetter(Data::green),
                        Codec.FLOAT.fieldOf("blue").forGetter(Data::blue),
                        Codec.FLOAT.fieldOf("scale").forGetter(Data::scale),
                        Codec.INT.fieldOf("duration").forGetter(Data::duration),
                        Codec.FLOAT.fieldOf("targetX").forGetter(Data::targetX),
                        Codec.FLOAT.fieldOf("targetY").forGetter(Data::targetY),
                        Codec.FLOAT.fieldOf("targetZ").forGetter(Data::targetZ),
                        Codec.FLOAT.fieldOf("speed").forGetter(Data::speed),
                        Codec.INT.fieldOf("mode").forGetter(Data::mode)
                ).apply(instance, Data::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = NetworkHandler.composite(
                ByteBufCodecs.FLOAT, Data::red,
                ByteBufCodecs.FLOAT, Data::green,
                ByteBufCodecs.FLOAT, Data::blue,
                ByteBufCodecs.FLOAT, Data::scale,
                ByteBufCodecs.INT, Data::duration,
                ByteBufCodecs.FLOAT, Data::targetX,
                ByteBufCodecs.FLOAT, Data::targetY,
                ByteBufCodecs.FLOAT, Data::targetZ,
                ByteBufCodecs.FLOAT, Data::speed,
                ByteBufCodecs.INT, Data::mode,
                Data::new
        );

        public static Data create(float targetX, float targetZ) {
            return new Data(1, 1, 1, 0, 0, targetX, 0, targetZ, 0, 0);
        }

        public static Data create(float targetX, float targetY, float targetZ, float speed) {
            return new Data(1, 1, 1, 0, 0, targetX, targetY, targetZ, speed, 1);
        }

        public static Data create(float red, float green, float blue, float scale, int duration) {
            return new Data(red, green, blue, scale, duration, 0, 0, 0, 0, 2);
        }

        @Override
        public @NotNull ParticleType<Data> getType() {
            return ParticleHandler.ORB.get();
        }
    }
}
