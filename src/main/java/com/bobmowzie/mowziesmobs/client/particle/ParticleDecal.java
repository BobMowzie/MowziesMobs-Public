package com.bobmowzie.mowziesmobs.client.particle;

import com.bobmowzie.mowziesmobs.client.particle.types.AdvancedParticleType;
import com.bobmowzie.mowziesmobs.client.particle.types.DecalParticleType;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleBase;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleRotation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;
import java.util.OptionalDouble;

public class ParticleDecal extends AdvancedParticleBase {
    protected int spriteSize = 8;
    protected int bufferSize = 32;
    private final SpriteSet sprites;

    protected ParticleDecal(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double motionX, double motionY, double motionZ, ParticleRotation rotation, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, SpriteSet sprites, ParticleComponent[] components) {
        this(worldIn, xCoordIn, yCoordIn, zCoordIn, motionX, motionY, motionZ, rotation, scale, r, g, b, a, drag, duration, emissive, sprites, 8, 32, components);
    }

    protected ParticleDecal(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double motionX, double motionY, double motionZ, ParticleRotation rotation, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, SpriteSet sprites, int spriteSize, int bufferSize, ParticleComponent[] components) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, motionX, motionY, motionZ, rotation, scale, r, g, b, a, drag, duration, emissive, false, components);
        this.spriteSize = spriteSize;
        this.bufferSize = bufferSize;
        this.setSpriteFromAge(sprites);
        this.sprites = sprites;
    }

    private static OptionalDouble max(double... v) {
        return Arrays.stream(v).max();
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        alpha = prevAlpha + (alpha - prevAlpha) * partialTicks;
        if (alpha < 0.01) alpha = 0.01f;
        rCol = prevRed + (red - prevRed) * partialTicks;
        gCol = prevGreen + (green - prevGreen) * partialTicks;
        bCol = prevBlue + (blue - prevBlue) * partialTicks;
        particleScale = prevScale + (scale - prevScale) * partialTicks;

        for (ParticleComponent component : components) {
            component.preRender(this, partialTicks);
        }

        if (!doRender) return;

        this.setSprite(sprites.get(Math.min(this.age, 5), 5));

        float decalRot = 0.0f;
        if (rotation instanceof ParticleRotation.EulerAngles) {
            ParticleRotation.EulerAngles eulerRot = (ParticleRotation.EulerAngles) rotation;
            float rotY = eulerRot.prevYaw + (eulerRot.yaw - eulerRot.prevYaw) * partialTicks;
            decalRot = rotY;
        }
        else if (rotation instanceof ParticleRotation.FaceCamera faceCamera) {
            float rotY = faceCamera.prevAngle + (faceCamera.angle - faceCamera.prevAngle) * partialTicks;
            decalRot = rotY;
        }

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int lightColor = this.getLightColor(partialTicks);

        float spriteScale = (float) spriteSize / (float) bufferSize;
        Vec3 corner0 = new Vec3(-particleScale/2, 0, -particleScale/2).yRot(decalRot);
        Vec3 corner1 = new Vec3(particleScale/2, 0, particleScale/2).yRot(decalRot);
        double extent = max(corner0.x(), corner1.x(), corner0.z(), corner1.z()).orElse(1);
        Vec3 minCorner = new Vec3(-extent, -particleScale, -extent).add(x, y, z);
        Vec3 maxCorner = new Vec3(extent, particleScale, extent).add(x, y, z);

        for(BlockPos blockpos : BlockPos.betweenClosed(BlockPos.containing(minCorner), BlockPos.containing(maxCorner))) {
            renderBlockDecal(buffer, renderInfo, level, blockpos, x, y, z, u0, u1, v0, v1, particleScale, spriteScale, this.alpha, decalRot, this.rCol, this.gCol, this.bCol, lightColor);
        }

        for (ParticleComponent component : components) {
            component.postRender(this, buffer, renderInfo, partialTicks, lightColor);
        }
    }

    private static Vec2 rotateVec2(Vec2 v, float angle) {
        return new Vec2(v.x * (float) Math.cos(angle) - v.y * (float) Math.sin(angle),
                v.x * (float) Math.sin(angle) + v.y * (float) Math.cos(angle));
    }

    private static void renderBlockDecal(VertexConsumer buffer, Camera renderInfo, LevelReader level, BlockPos blockPos, double x, double y, double z, float u0, float u1, float v0, float v1, float scale, float spriteScale, float alpha, float rotation, float r, float g, float b, int lightColor) {
        Vec2 center = new Vec2((float) x, (float) z);
        BlockPos blockpos = blockPos.below();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            if (blockstate.isCollisionShapeFullBlock(level, blockpos)) {
                VoxelShape voxelshape = blockstate.getShape(level, blockPos.below());
                if (!voxelshape.isEmpty()) {
                    float f = alpha;
                    if (f >= 0.0F) {
                        if (f > 1.0F) {
                            f = 1.0F;
                        }

                        double rad2 = Math.sqrt(2.0);
                        double minX = x - scale * spriteScale * rad2;
                        double minZ = z - scale * spriteScale * rad2;
                        double maxX = x + scale * spriteScale * rad2;
                        double maxZ = z + scale * spriteScale * rad2;
                        AABB aabb = voxelshape.bounds();
                        float d0 = blockPos.getX() + (float) aabb.minX;
                        float d1 = blockPos.getX() + (float) aabb.maxX;
                        float d2 = blockPos.getY() + (float) aabb.minY + 0.005625f;
                        float d3 = blockPos.getZ() + (float) aabb.minZ;
                        float d4 = blockPos.getZ() + (float) aabb.maxZ;
                        if (d0 < minX) d0 = (float) minX;
                        if (d1 > maxX) d1 = (float) maxX;
                        if (d3 < minZ) d3 = (float) minZ;
                        if (d4 > maxZ) d4 = (float) maxZ;
                        Vec2 corners[] = new Vec2[] {
                                new Vec2(d0, d3),
                                new Vec2(d1, d3),
                                new Vec2(d1, d4),
                                new Vec2(d0, d4),
                        };
                        for (Vec2 corner : corners) {
                            Vec2 cornerRelative = rotateVec2(corner.add(center.negated()), -rotation);
                            Vec2 uv = new Vec2((cornerRelative.x / (2.0f * scale) + 0.5f) * (u1 - u0) + u0, (cornerRelative.y / (2.0f * scale) + 0.5f) * (v1 - v0) + v0);
                            decalVertex(buffer, renderInfo, f, corner.x, d2, corner.y, uv.x, uv.y, r, g, b, lightColor);
                        }
                    }

                }
            }
        }
    }

    private static void decalVertex(VertexConsumer buffer, Camera renderInfo, float alpha, float x, float y, float z, float u, float v, float r, float g, float b, int lightColor) {
        Vec3 vector3d = renderInfo.getPosition();
//        Vector3d = new Vec3(0, 1, 0);
        buffer.addVertex((float) (x - vector3d.x()), (float) (y - vector3d.y()), (float) (z - vector3d.z())).setUv(u, v).setColor(r, g, b, alpha).setLight(lightColor);
    }

    public static class Provider implements ParticleProvider<DecalParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(DecalParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleDecal particle = new ParticleDecal(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.rotation(), typeIn.scale(), typeIn.red(), typeIn.green(), typeIn.blue(), typeIn.alpha(), typeIn.airDrag(), typeIn.duration(), typeIn.emissive(), spriteSet, typeIn.spriteSize(), typeIn.bufferSize(), typeIn.components());
            particle.setColor(typeIn.red(), typeIn.green(), typeIn.blue());
            return particle;
        }
    }

    public static void spawnDecal(Level world, Holder<ParticleType<?>> particle, double x, double y, double z, double motionX, double motionY, double motionZ, double angle, double scale, double red, double green, double blue, double alpha, double airDrag, double duration, boolean emissive, int spriteSize, int bufferSize, ParticleComponent[] components) {
        AdvancedParticleType base = new AdvancedParticleType(particle, new ParticleRotation.FaceCamera((float) angle), components, (float) red, (float) green, (float) blue, (float) alpha, (float) scale, (float) duration, (float) airDrag, emissive, false);
        world.addParticle(new DecalParticleType(base, spriteSize, bufferSize), x, y, z, motionX, motionY, motionZ);
    }
}
