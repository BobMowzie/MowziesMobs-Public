package com.bobmowzie.mowziesmobs.client.particle.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public abstract class ParticleRotation {
    public abstract void setPrevValues();

    public abstract Type type();

    public enum Type implements StringRepresentable {
        FACE_CAMERA("face_camera"),
        EULER_ANGLES("euler_angles"),
        ORIENT_VECTOR("orient_vector");

        private final String id;

        Type(final String id) {
            this.id = id;
        }

        @Override
        public @NotNull String getSerializedName() {
            return id;
        }
    }

    public static final Codec<ParticleRotation> CODEC = Codec.either(
                    FaceCamera.CODEC,
                    Codec.either(EulerAngles.CODEC, OrientVector.CODEC)
            )
            .flatXmap(
                    either -> either.map(
                            DataResult::success, other -> other.map(DataResult::success, DataResult::success)),
                    rotation -> {
                        if (rotation instanceof FaceCamera faceCamera) {
                            return DataResult.success(Either.left(faceCamera));
                        }

                        if (rotation instanceof EulerAngles eulerAngles) {
                            return DataResult.success(Either.right(Either.left(eulerAngles)));
                        }

                        if (rotation instanceof OrientVector orientVector) {
                            return DataResult.success(Either.right(Either.right(orientVector)));
                        }

                        return DataResult.error(() -> "Invalid rotation type: [" + rotation.getClass().getName() + "]");
                    }
            );

    public static class FaceCamera extends ParticleRotation {
        public static final Codec<FaceCamera> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("angle").forGetter(value -> value.angle)
        ).apply(instance, FaceCamera::new));

        public static final Type TYPE = Type.FACE_CAMERA;

        public float angle;
        public float prevAngle;

        public FaceCamera(float angle) {
            this.angle = angle;
        }

        @Override
        public void setPrevValues() {
            prevAngle = angle;
        }

        @Override
        public Type type() {
            return TYPE;
        }
    }

    public static class EulerAngles extends ParticleRotation {
        public static final Codec<EulerAngles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("yaw").forGetter(value -> value.yaw),
                Codec.FLOAT.fieldOf("pitch").forGetter(value -> value.pitch),
                Codec.FLOAT.fieldOf("roll").forGetter(value -> value.roll)
        ).apply(instance, EulerAngles::new));

        public static final Type TYPE = Type.EULER_ANGLES;

        public float yaw, pitch, roll;
        public float prevYaw, prevPitch, prevRoll;

        public EulerAngles(float yaw, float pitch, float roll) {
            this.yaw = this.prevYaw = yaw;
            this.pitch = this.prevPitch = pitch;
            this.roll = this.prevRoll = roll;
        }

        @Override
        public void setPrevValues() {
            prevYaw = yaw;
            prevPitch = pitch;
            prevRoll = roll;
        }

        @Override
        public Type type() {
            return TYPE;
        }
    }

    public static class OrientVector extends ParticleRotation {
        public static final Codec<OrientVector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.fieldOf("orientation").forGetter(value -> value.orientation)
        ).apply(instance, OrientVector::new));

        public static final Type TYPE = Type.ORIENT_VECTOR;

        public Vec3 orientation;
        public Vec3 prevOrientation;

        public OrientVector(Vec3 orientation) {
            this.orientation = this.prevOrientation = orientation;
        }

        @Override
        public void setPrevValues() {
            prevOrientation = orientation;
        }

        @Override
        public Type type() {
            return TYPE;
        }
    }
}
