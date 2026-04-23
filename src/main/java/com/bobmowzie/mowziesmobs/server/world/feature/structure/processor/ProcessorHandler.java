package com.bobmowzie.mowziesmobs.server.world.feature.structure.processor;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class ProcessorHandler {
    public static StructureProcessorType<BaseProcessor> BASE_PROCESSOR = () -> BaseProcessor.CODEC;
    public static StructureProcessorType<BlockSwapProcessor> BLOCK_SWAP_PROCESSOR = () -> BlockSwapProcessor.CODEC;
    public static StructureProcessorType<RootsProcessor> ROOTS_PROCESSOR = () -> RootsProcessor.CODEC;
    public static StructureProcessorType<BaseDecoProcessor> BASE_DECO_PROCESSOR = () -> BaseDecoProcessor.CODEC;
    public static StructureProcessorType<ChestProcessor> CHEST_PROCESSOR = () -> ChestProcessor.CODEC;
    public static StructureProcessorType<MonasteryStairsProcessor> STAIRS_PROCESSOR = () -> MonasteryStairsProcessor.CODEC;

    public static void registerStructureProcessors() {
        register("base_processor", BASE_PROCESSOR);
        register("block_swap_processor", BLOCK_SWAP_PROCESSOR);
        register("roots_processor", ROOTS_PROCESSOR);
        register("base_deco_processor", BASE_DECO_PROCESSOR);
        register("chest_processor", CHEST_PROCESSOR);
        register("stairs_processor", STAIRS_PROCESSOR);
    }

    private static void register(String name, StructureProcessorType<?> codec) {
        Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, new ResourceLocation(MowziesMobs.MODID, name), codec);
    }
}
