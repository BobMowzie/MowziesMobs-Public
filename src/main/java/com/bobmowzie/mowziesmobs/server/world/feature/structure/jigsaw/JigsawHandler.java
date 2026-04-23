package com.bobmowzie.mowziesmobs.server.world.feature.structure.jigsaw;

import com.bobmowzie.mowziesmobs.MMCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class JigsawHandler {
    public static final DeferredRegister<StructurePoolElementType<?>> MM_STRUCTURE_POOLS = DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, MMCommon.MODID);

    public static final DeferredHolder<StructurePoolElementType<?>, StructurePoolElementType<MowziePoolElement>> MOWZIE_ELEMENT = MM_STRUCTURE_POOLS.register("mowzie_element", () -> () -> MowziePoolElement.CODEC);
    public static final DeferredHolder<StructurePoolElementType<?>, StructurePoolElementType<FallbackPoolElement>> FALLBACK_ELEMENT = MM_STRUCTURE_POOLS.register("fallback_element", () -> () -> FallbackPoolElement.CODEC);
}
