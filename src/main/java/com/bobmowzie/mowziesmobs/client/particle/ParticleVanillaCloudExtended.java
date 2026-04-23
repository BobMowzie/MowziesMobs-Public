package com.bobmowzie.mowziesmobs.client.particle;

import com.bobmowzie.mowziesmobs.server.message.NetworkHandler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ParticleVanillaCloudExtended extends TextureSheetParticle {
    private final SpriteSet animatedSprite;

    private final float oSize;
    private final float airDrag;
    private final float red;
    private final float green;
    private final float blue;

    private final Vec3[] destination;

    protected ParticleVanillaCloudExtended(ClientLevel worldIn, SpriteSet animatedSprite, double xCoordIn, double yCoordIn, double zCoordIn, double motionX, double motionY, double motionZ, double scale, double r, double g, double b, double drag, double duration, Vec3[] destination) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        this.xd *= 0.10000000149011612D;
        this.yd *= 0.10000000149011612D;
        this.zd *= 0.10000000149011612D;
        this.xd += motionX;
        this.yd += motionY;
        this.zd += motionZ;
        float f1 = 1.0F - this.random.nextFloat() * 0.3F;
        this.red = (float) (f1 * r);
        this.green = (float) (f1 * g);
        this.blue = (float) (f1 * b);
        this.quadSize *= 0.75F;
        this.quadSize *= 2.5F;
        this.oSize = this.quadSize * (float)scale;
        this.lifetime = (int)duration;
        if (lifetime == 0) lifetime = 1;
        airDrag = (float)drag;
        this.destination = destination;
        hasPhysics = false;
        this.animatedSprite = animatedSprite;
        if (destination != null) this.setSprite(animatedSprite.get(this.lifetime - this.age, this.lifetime));
        else this.setSpriteFromAge(this.animatedSprite);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime)
        {
            this.remove();
        }

        this.setSpriteFromAge(this.animatedSprite);

        if (destination != null && destination.length == 1) {
            this.setSprite(animatedSprite.get(this.lifetime - this.age, this.lifetime));

            Vec3 destinationVec = destination[0];
            Vec3 diff = destinationVec.subtract(new Vec3(x, y, z));
            if (diff.length() < 0.5) this.remove();
            float attractScale = 0.7f * ((float)this.age / (float)this.lifetime) * ((float)this.age / (float)this.lifetime);
            xd = diff.x * attractScale;
            yd = diff.y * attractScale;
            zd = diff.z * attractScale;
        }
        this.move(this.xd, this.yd, this.zd);
        this.xd *= airDrag;
        this.yd *= airDrag;
        this.zd *= airDrag;

        if (this.onGround)
        {
            this.xd *= 0.699999988079071D;
            this.zd *= 0.699999988079071D;
        }
    }

    public static final class Provider implements ParticleProvider<Data> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(Data typeIn, @NotNull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleVanillaCloudExtended particle = new ParticleVanillaCloudExtended(worldIn, spriteSet, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.scale(), typeIn.red(), typeIn.green(), typeIn.blue(), typeIn.airDrag(), typeIn.duration(), typeIn.destinations());
            particle.setColor(typeIn.red(), typeIn.green(), typeIn.blue());
            return particle;
        }
    }

    public record Data(float red, float green, float blue, float scale, float airDrag, float duration, Vec3[] destinations) implements ParticleOptions {
        public static final MapCodec<ParticleVanillaCloudExtended.Data> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.FLOAT.fieldOf("red").forGetter(ParticleVanillaCloudExtended.Data::red),
                        Codec.FLOAT.fieldOf("green").forGetter(ParticleVanillaCloudExtended.Data::green),
                        Codec.FLOAT.fieldOf("blue").forGetter(ParticleVanillaCloudExtended.Data::blue),
                        Codec.FLOAT.fieldOf("scale").forGetter(ParticleVanillaCloudExtended.Data::scale),
                        Codec.FLOAT.fieldOf("air_drag").forGetter(ParticleVanillaCloudExtended.Data::airDrag),
                        Codec.FLOAT.fieldOf("duration").forGetter(ParticleVanillaCloudExtended.Data::duration),
                        Vec3.CODEC.listOf().fieldOf("destinations").xmap(list -> list.toArray(new Vec3[]{}), Arrays::asList).forGetter(ParticleVanillaCloudExtended.Data::destinations)
                ).apply(instance, ParticleVanillaCloudExtended.Data::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, ParticleVanillaCloudExtended.Data> STREAM_CODEC = NeoForgeStreamCodecs.composite(
                ByteBufCodecs.FLOAT, ParticleVanillaCloudExtended.Data::red,
                ByteBufCodecs.FLOAT, ParticleVanillaCloudExtended.Data::green,
                ByteBufCodecs.FLOAT, ParticleVanillaCloudExtended.Data::blue,
                ByteBufCodecs.FLOAT, ParticleVanillaCloudExtended.Data::scale,
                ByteBufCodecs.FLOAT, ParticleVanillaCloudExtended.Data::airDrag,
                ByteBufCodecs.FLOAT, ParticleVanillaCloudExtended.Data::duration,
                NetworkHandler.VEC3_ARRAY, ParticleVanillaCloudExtended.Data::destinations,
                ParticleVanillaCloudExtended.Data::new
        );

        @Override
        public @NotNull ParticleType<ParticleVanillaCloudExtended.Data> getType() {
            return ParticleHandler.VANILLA_CLOUD_EXTENDED.get();
        }
    }

    public static void spawnVanillaCloud(Level world, double x, double y, double z, double motionX, double motionY, double motionZ, double scale, double r, double g, double b, double drag, double duration) {
        world.addParticle(new Data((float)r, (float)g, (float)b, (float)scale, (float)drag, (float)duration, null), x, y, z, motionX, motionY, motionZ);
    }

    public static void spawnVanillaCloudDestination(Level world, double x, double y, double z, double motionX, double motionY, double motionZ, double scale, double r, double g, double b, double drag, double duration, Vec3[] destination) {
        world.addParticle(new Data((float)r, (float)g, (float)b, (float)scale, (float)drag, (float)duration, destination), x, y, z, motionX, motionY, motionZ);
    }
}
