package com.bobmowzie.mowziesmobs.server.world.feature.structure.jigsaw;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FallbackPoolElement extends StructurePoolElement {
    public static final MapCodec<FallbackPoolElement> CODEC = MapCodec.unit(() -> FallbackPoolElement.INSTANCE);
    public static final FallbackPoolElement INSTANCE = new FallbackPoolElement();

    private FallbackPoolElement() {
        super(StructureTemplatePool.Projection.TERRAIN_MATCHING);
    }

    public Vec3i getSize(StructureTemplateManager p_210191_, Rotation p_210192_) {
        return Vec3i.ZERO;
    }

    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager p_210198_, BlockPos p_210199_, Rotation p_210200_, RandomSource p_210201_) {
        return Collections.emptyList();
    }

    public BoundingBox getBoundingBox(StructureTemplateManager p_210194_, BlockPos p_210195_, Rotation p_210196_) {
        throw new IllegalStateException("Invalid call to MowzieFallbackElement.getBoundingBox, filter me!");
    }

    @Override
    public boolean place(@NotNull StructureTemplateManager structureTemplateManager, @NotNull WorldGenLevel level, @NotNull StructureManager structureManager, @NotNull ChunkGenerator generator, @NotNull BlockPos offset, @NotNull BlockPos pos, @NotNull Rotation rotation, @NotNull BoundingBox box, @NotNull RandomSource random, @NotNull LiquidSettings liquidSettings, boolean keepJigsaws) {
        return true;
    }

    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.EMPTY;
    }

    public String toString() {
        return "Fallback";
    }
}
