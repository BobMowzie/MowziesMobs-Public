package com.bobmowzie.mowziesmobs.server.world.feature.structure.processor;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class MonasteryStairsProcessor extends StructureProcessor {
    public static final MonasteryStairsProcessor INSTANCE = new MonasteryStairsProcessor();
    public static final Codec<MonasteryStairsProcessor> CODEC = Codec.unit(() -> INSTANCE);

    private static final BlockState andesiteStairs = Blocks.ANDESITE_STAIRS.defaultBlockState();
    private static final BlockState cobbledDeepslate = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
    private static final BlockState cobbledDeepslateWall = Blocks.COBBLED_DEEPSLATE_WALL.defaultBlockState();
    private static final BlockState air = Blocks.AIR.defaultBlockState();

    private static final BlockState[] STAIR = { Blocks.AIR.defaultBlockState(), andesiteStairs };
    private static final BlockState[] RAIL = { cobbledDeepslateWall, cobbledDeepslate };

    protected StructureProcessorType<?> getType() {
        return ProcessorHandler.STAIRS_PROCESSOR;
    }

    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader levelReader, BlockPos jigsawPiecePos, BlockPos jigsawPieceBottomCenterPos, StructureTemplate.StructureBlockInfo blockInfoLocal, StructureTemplate.StructureBlockInfo blockInfoGlobal, StructurePlaceSettings structurePlacementData, StructureTemplate template) {
        BlockState startingState = blockInfoGlobal.state();
        BlockState[] blocksToPlace;
        if (startingState.is(Blocks.END_STONE_BRICK_STAIRS) || startingState.is(Blocks.NETHER_BRICK_STAIRS)) {

            Direction facing = blockInfoGlobal.state().getValue(StairBlock.FACING).getOpposite();
            facing = structurePlacementData.getRotation().rotate(facing);
            RandomSource random = structurePlacementData.getRandom(blockInfoGlobal.pos());

            if (startingState.is(Blocks.END_STONE_BRICK_STAIRS)) {
                blocksToPlace = STAIR;
                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), andesiteStairs.setValue(StairBlock.FACING, facing), blockInfoGlobal.nbt());
            }
            else {
                blocksToPlace = RAIL;
                blockInfoGlobal = new StructureTemplate.StructureBlockInfo(blockInfoGlobal.pos(), cobbledDeepslate, blockInfoGlobal.nbt());
            }
            if (levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(new ChunkPos(blockInfoGlobal.pos()))) {
                return blockInfoGlobal;
            }

            // March stairs forward and down on i
            for (int i = 0; i < 20; i++) {
                BlockPos.MutableBlockPos mutable = blockInfoGlobal.pos().mutable().move(Direction.DOWN);
                BlockState currBlockState = levelReader.getBlockState(mutable);

                int j = 0;
                // Loop down to the ground
                while (mutable.getY() > levelReader.getMinBuildHeight()
                        && mutable.getY() < levelReader.getMaxBuildHeight()
                        && !currBlockState.isSolid()) {
                    BlockState newState;
                    // Use the array blocks for the first two, then switch to random base generation
                    if (j < blocksToPlace.length) {
                        newState = blocksToPlace[j];
                        j++;
                    }
                    else {
                        newState = chooseRandomState(random);
                    }
                    levelReader.getChunk(mutable).setBlockState(mutable, newState, false);

                    // Update to next position
                    mutable.move(Direction.DOWN);
                    mutable.move(facing);
                    currBlockState = levelReader.getBlockState(mutable);
                }
            }
        }
        return blockInfoGlobal;
    }

    public BlockState chooseRandomState(RandomSource random) {
        float v = random.nextFloat();
        if (v > 0.7) return Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        else return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
    }

}
