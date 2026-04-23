package com.bobmowzie.mowziesmobs.server.world.feature.structure.processor;

import com.bobmowzie.mowziesmobs.MMCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ProcessorHandler {
    public static final DeferredRegister<StructureProcessorType<?>> MM_STRUCTURE_PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, MMCommon.MODID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<BaseProcessor>> BASE_PROCESSOR = MM_STRUCTURE_PROCESSORS.register("base_processor", () -> () -> BaseProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<BlockSwapProcessor>> BLOCK_SWAP_PROCESSOR = MM_STRUCTURE_PROCESSORS.register("block_swap_processor", () -> () -> BlockSwapProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<RootsProcessor>> ROOTS_PROCESSOR = MM_STRUCTURE_PROCESSORS.register("roots_processor", () -> () -> RootsProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<BaseDecoProcessor>> BASE_DECO_PROCESSOR = MM_STRUCTURE_PROCESSORS.register("base_deco_processor", () -> () -> BaseDecoProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<ChestProcessor>> CHEST_PROCESSOR = MM_STRUCTURE_PROCESSORS.register("chest_processor", () -> () -> ChestProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<MonasteryStairsProcessor>> STAIRS_PROCESSOR = MM_STRUCTURE_PROCESSORS.register("stairs_processor", () -> () -> MonasteryStairsProcessor.CODEC);
}
