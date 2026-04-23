package com.bobmowzie.mowziesmobs.server.world.feature.structure.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ChestProcessor extends StructureProcessor {
    public static final Codec<ChestProcessor> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.STRING.fieldOf("loot_table").forGetter(config -> config.lootTable)
            ).apply(instance, instance.stable(ChestProcessor::new)));

    private String lootTable;

    public ChestProcessor(String lootTable) {
        this.lootTable = lootTable;
    }

    protected StructureProcessorType<?> getType() {
        return ProcessorHandler.BASE_PROCESSOR;
    }

    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader levelReader, BlockPos jigsawPiecePos, BlockPos jigsawPieceBottomCenterPos, StructureTemplate.StructureBlockInfo blockInfoLocal, StructureTemplate.StructureBlockInfo blockInfoGlobal, StructurePlaceSettings structurePlacementData, StructureTemplate template) {
        RandomSource random = structurePlacementData.getRandom(blockInfoGlobal.pos());
        ResourceLocation lootTableLocation = new ResourceLocation(lootTable);
        RandomizableContainerBlockEntity.setLootTable(levelReader, random, blockInfoGlobal.pos(), lootTableLocation);
        return blockInfoGlobal;
    }

}
