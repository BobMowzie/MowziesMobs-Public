package com.bobmowzie.mowziesmobs.client.render.entity;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityFissure;
import com.bobmowzie.mowziesmobs.server.entity.effects.geomancy.EntityFissurePiece;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalDouble;

public class RenderFissurePiece extends EntityRenderer<EntityFissurePiece> {
    private static final ResourceLocation TEXTURE0 = new ResourceLocation(MowziesMobs.MODID, "textures/particle/crack_0.png");
    private static final ResourceLocation TEXTURE1 = new ResourceLocation(MowziesMobs.MODID, "textures/particle/crack_1.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation(MowziesMobs.MODID, "textures/particle/crack_2.png");
    private static final ResourceLocation TEXTURE3 = new ResourceLocation(MowziesMobs.MODID, "textures/particle/crack_3.png");
    private static final ResourceLocation TEXTURE4 = new ResourceLocation(MowziesMobs.MODID, "textures/particle/crack_4.png");
    private static final ResourceLocation TEXTURE5 = new ResourceLocation(MowziesMobs.MODID, "textures/particle/crack_5.png");
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            TEXTURE0,
            TEXTURE1,
            TEXTURE2,
            TEXTURE3,
            TEXTURE4,
            TEXTURE5
    };
    private static final float SPRITE_SCALE = 2f;

    public RenderFissurePiece(EntityRendererProvider.Context mgr) {
        super(mgr);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFissurePiece entity) {
        int fullGrownTick = EntityFissure.TICKS_PER_PIECE;
        if (entity.getGrowTick() < fullGrownTick) {
            int whichTex = (int) (5 * (double) entity.getGrowTick() / (double) fullGrownTick);
            return TEXTURES[whichTex];
        }
        return TEXTURE5;
    }

    private static OptionalDouble max(double... v) {
        return Arrays.stream(v).max();
    }

    @Override
    public void render(EntityFissurePiece entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        Vec3 corner0 = new Vec3(-SPRITE_SCALE/2, 0, -SPRITE_SCALE/2).yRot(entityYaw);
        Vec3 corner1 = new Vec3(SPRITE_SCALE/2, 0, SPRITE_SCALE/2).yRot(entityYaw);
        double extent = max(corner0.x(), corner1.x(), corner0.z(), corner1.z()).orElse(1);
        Vec3 minCorner = new Vec3(-extent, -1, -extent).add(entityIn.getX(), entityIn.getY(), entityIn.getZ());
        Vec3 maxCorner = new Vec3(extent, 1, extent).add(entityIn.getX(), entityIn.getY(), entityIn.getZ());

        matrixStackIn.pushPose();
        VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entityTranslucent(getTextureLocation(entityIn)));

        for(BlockPos blockpos : BlockPos.betweenClosed(BlockPos.containing(minCorner), BlockPos.containing(maxCorner))) {
            BlockState block = entityIn.level().getBlockState(blockpos.below());
            renderBlockDecal(entityIn, entityIn.level(), block, blockpos, entityIn.getX(), entityIn.getY(), entityIn.getZ(), matrixStackIn, ivertexbuilder, packedLightIn);
        }
        matrixStackIn.popPose();
    }

    private static Vec2 rotateVec2(Vec2 v, float angle) {
        return new Vec2(v.x * (float) Math.cos(angle) - v.y * (float) Math.sin(angle),
                v.x * (float) Math.sin(angle) + v.y * (float) Math.cos(angle));
    }

    public static Vec3 Vec3WithAxis(double axis1, double axis2, Direction.Axis axis1Dir) {
        Vec3 v = new Vec3(0, 0 ,0);
        if (axis1Dir == Direction.Axis.X) {
            v.add(axis1, 0, axis2);
        }
        else {
            v.add(axis2, 0, axis1);
        }
        return v;
    }

    private static final double DISTANCE_THRESHOLD = 0.0001;

    public static Optional<Vec2> getLineSegmentIntersection(BlockPos pos, Direction whichBorder, Vec3 segmentP, Vec3 segmentQ) {
        if (whichBorder.getAxis().isVertical()) return Optional.empty();

        Direction.Axis axis = whichBorder.getClockWise().getAxis();

        Vec3 segmentVec = segmentQ.subtract(segmentP);
        // Get the axis perpendicular to the axis the block border runs along
        Direction.Axis perp = whichBorder.getAxis();
        // If the line segment has no component along the perpendicular axis, then collision cannot occur
        if (segmentVec.get(perp) == 0) return Optional.empty();

        whichBorder.step();
        Vec3 colliderCenter = pos.getCenter().add(whichBorder.getStepX() / 2d, whichBorder.getStepY() / 2d, whichBorder.getStepZ() / 2d);
        double segmentPos = colliderCenter.get(perp);
        Vec3 intersect;
        if (segmentVec.get(axis) == 0) {
            intersect = Vec3WithAxis(segmentP.get(axis), segmentPos, axis);
        }
        else {
            // Get the equation for this line in slope-intercept form
            double slope = segmentVec.get(perp) / segmentVec.get(axis);
            double intercept = segmentP.get(perp) - slope * segmentP.get(axis);
            // Ball's path can be represented by 'p = slope * a + intercept'
            // where a and p are the positions along the parallel and perpendicular segment axes.
            // Solve for a given p from the line segment's center.
            double intersectAxis = (segmentPos - intercept) / slope;
            intersect = Vec3WithAxis(intersectAxis, segmentPos, axis);
        }

        // If the intersection is at the ball's position (within threshold), it's not valid
        double distanceFromBall = intersect.subtract(segmentP).length();
        if (distanceFromBall <= DISTANCE_THRESHOLD) return Optional.empty();

        // The line segments are not infinite, so make sure this intersection point lies on both
        // Check if it's on the block border
        if (Math.abs(intersect.get(axis) - colliderCenter.get(axis)) > 0.5) return Optional.empty();
        // Check if it's on the line segment.
        // We already know the points are collinear, just need to check if intersect is between ball start and ball end.
        // Use the dot products
        double dot = intersect.subtract(segmentP).dot(segmentVec);
        if (dot < 0 || dot > segmentVec.dot(segmentVec)) return Optional.empty();

        // Passed! Return the intersected point
        return Optional.of(new Vec2((float) intersect.x, (float) intersect.z));
    }

    public static Vec2 getRelativeCornerPos(Vec2 corner, float rotation) {
        return rotateVec2(corner, (float) -Math.toRadians(rotation + 180));
    }

    public static Vec2 getRelativeUVs(Vec2 corner, float rotation) {
        Vec2 relativeCorner = rotateVec2(corner, (float) -Math.toRadians(rotation + 180));
        return new Vec2(relativeCorner.x / (2.0f * SPRITE_SCALE) + 0.5f, relativeCorner.y / (2.0f * SPRITE_SCALE) + 0.5f);
    }

    private static void renderBlockDecal(EntityFissurePiece entity, Level level, BlockState blockstate, BlockPos blockpos, double x, double y, double z, PoseStack matrixStack, VertexConsumer builder, int packedLightIn) {
        PoseStack.Pose matrixstack$entry = matrixStack.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();
        Vec2 center = new Vec2((float) x, (float) z);
        double ex = entity.xOld + (entity.getX() - entity.xOld);
        double ey = entity.yOld + (entity.getY() - entity.yOld);
        double ez = entity.zOld + (entity.getZ() - entity.zOld);
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {

            if (blockstate.isCollisionShapeFullBlock(level, blockpos) && !level.getBlockState(blockpos).isCollisionShapeFullBlock(level, blockpos)) {
                VoxelShape voxelshape = blockstate.getShape(level, blockpos);
                if (!voxelshape.isEmpty()) {
                    float alpha = 1.0f;
                    if (alpha >= 0.0F) {
                        if (alpha > 1.0F) {
                            alpha = 1.0F;
                        }

                        double rad2 = Math.sqrt(1.0);
                        double minX = -SPRITE_SCALE * rad2;
                        double minZ = -SPRITE_SCALE * rad2;
                        double maxX = SPRITE_SCALE * rad2;
                        double maxZ = SPRITE_SCALE * rad2;
                        AABB aabb = voxelshape.bounds();
                        float d0 = blockpos.getX() + (float) (aabb.minX - ex);
                        float d1 = blockpos.getX() + (float) (aabb.maxX - ex);
                        float d2 = blockpos.getY() + (float) (aabb.minY - ey) + 0.0015625f;
                        float d3 = blockpos.getZ() + (float) (aabb.minZ - ez);
                        float d4 = blockpos.getZ() + (float) (aabb.maxZ - ez);
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
                            Vec2 uv = getRelativeUVs(corner, entity.getYRot());
                            drawVertex(matrix4f, matrix3f, builder, corner.x, d2, corner.y, uv.x, uv.y, 0.65f, packedLightIn);
                        }

                        Direction[] directions = new Direction[]{Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH};
                        for (int i = 0; i < corners.length; i++) {
                            Direction direction = directions[i];
                            BlockPos overAndDown = blockpos.relative(direction).below(2);
                            if (!level.getBlockState(overAndDown).isCollisionShapeFullBlock(level, overAndDown)) {
                                continue;
                            }

                            Vec2 corner = corners[i];
                            Vec2 uv = getRelativeUVs(corner, entity.getYRot());

                            int prevIndex = i == 0 ? 3 : i - 1;
                            Vec2 prevCorner = corners[prevIndex];
                            Vec2 prevUv = getRelativeUVs(prevCorner, entity.getYRot());

                            Vector3f offset = direction.step().mul(0.0015625f); // To prevent z-fighting
                            drawVertex(matrix4f, matrix3f, builder, prevCorner.x + offset.x(), d2, prevCorner.y + offset.z(), prevUv.x, prevUv.y, 0.65f, packedLightIn);
                            drawVertex(matrix4f, matrix3f, builder, corner.x + offset.x(), d2, corner.y + offset.z(), uv.x, uv.y, 0.65f, packedLightIn);
                            drawVertex(matrix4f, matrix3f, builder, corner.x + offset.x(), d2 - 1, corner.y + offset.z(), uv.x, uv.y, 0.65f, packedLightIn);
                            drawVertex(matrix4f, matrix3f, builder, prevCorner.x + offset.x(), d2 - 1, prevCorner.y + offset.z(), prevUv.x, prevUv.y, 0.65f, packedLightIn);
                        }
                    }

                    /*float verticalOffset = 0.015625f;
                    Vec3 corners[] = new Vec3[] {
                            new Vec3(-1, verticalOffset, -1),
                            new Vec3(1, verticalOffset, -1),
                            new Vec3(1, verticalOffset, 1),
                            new Vec3(-1, verticalOffset, 1),
                    };
                    Vec2 uvs[] = new Vec2[] {
                            new Vec2(0, 0),
                            new Vec2(1, 0),
                            new Vec2(1, 1),
                            new Vec2(0, 1),
                    };
                    AABB aabb = voxelshape.bounds();
                    List<Vec3> points = new ArrayList<>();
                    for (int i = 0; i < corners.length; i++) {
                        Vec3 cornerWorldspace = corners[i].yRot(entity.getYRot()).add(entity.position());
                        if (aabb.move(0, 0.5, 0).contains(cornerWorldspace)) points.add(cornerWorldspace);
                        else {
                            int prevIndex = i == 0 ? 3 : i - 1;
                            Vec3 prevCorner = corners[prevIndex].yRot(entity.getYRot()).add(entity.position());
                            for (Iterator<Direction> it = Direction.Plane.HORIZONTAL.stream().iterator(); it.hasNext();) {
                                Optional<Vec2> intersection = getLineSegmentIntersection(blockpos, Direction.NORTH, prevCorner, cornerWorldspace);
                                intersection.ifPresent(vec2 -> points.add(new Vec3(vec2.x, cornerWorldspace.y, vec2.y)));
                            }
                        }
                        Vec2 uv = uvs[i];
//                        drawVertex(matrix4f, matrix3f, builder, (float) (cornerWorldspace.x - entity.getX()), (float) (cornerWorldspace.y - entity.getY()), (float) (cornerWorldspace.z - entity.getZ()), uv.x, uv.y, alpha, packedLightIn);
                    }
                    // Copy the last point if there are fewer than 4
                    while (points.size() < 4) {
                        points.add(points.get(points.size() - 1));
                    }
                    if (points.size() > 4) System.out.println("Decal has more than 4 intersections");
                    for (Vec3 point : points) {
                        drawVertex(matrix4f, matrix3f, builder, (float) (point.x - entity.getX()), (float) (point.y - entity.getY()), (float) (point.z - entity.getZ()), 0, 0, alpha, packedLightIn);
                    }*/
                }
            }
        }
    }

    public static void drawVertex(Matrix4f matrix, Matrix3f normals, VertexConsumer vertexBuilder, float offsetX, float offsetY, float offsetZ, float textureX, float textureY, float alpha, int packedLightIn) {
        vertexBuilder.vertex(matrix, offsetX, offsetY, offsetZ).color(0, 0, 0, 1 * alpha).uv(textureX, textureY).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(normals, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
