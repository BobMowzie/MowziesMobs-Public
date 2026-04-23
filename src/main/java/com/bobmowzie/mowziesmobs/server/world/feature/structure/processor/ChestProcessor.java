package com.bobmowzie.mowziesmobs.server.world.feature.structure.processor;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;

public class ChestProcessor extends StructureProcessor {
    public static final MapCodec<ChestProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    ResourceLocation.CODEC.fieldOf("loot_table").xmap(location -> ResourceKey.create(Registries.LOOT_TABLE, location), ResourceKey::location).forGetter(config -> config.lootTable)
            ).apply(instance, instance.stable(ChestProcessor::new)));

    private final ResourceKey<LootTable> lootTable;

    public ChestProcessor(ResourceKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ProcessorHandler.CHEST_PROCESSOR.value();
    }

    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader levelReader, BlockPos jigsawPiecePos, BlockPos jigsawPieceBottomCenterPos, StructureTemplate.StructureBlockInfo blockInfoLocal, StructureTemplate.StructureBlockInfo blockInfoGlobal, StructurePlaceSettings structurePlacementData, StructureTemplate template) {
        RandomSource random = structurePlacementData.getRandom(blockInfoGlobal.pos());

        if (levelReader.getBlockEntity(blockInfoGlobal.pos()) instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(lootTable, random.nextLong());
        }

        return blockInfoGlobal;
    }

}
