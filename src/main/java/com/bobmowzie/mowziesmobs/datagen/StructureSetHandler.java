package com.bobmowzie.mowziesmobs.datagen;

import com.bobmowzie.mowziesmobs.MMCommon;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.Optional;

public class StructureSetHandler {

    public static ResourceKey<StructureSet> WROUGHT_CHAMBERS = createSetKey("wrought_chambers");
    public static ResourceKey<StructureSet> UMVUTHANA_GROVES = createSetKey("umvuthana_groves");
    public static ResourceKey<StructureSet> FROSTMAWS = createSetKey("frostmaw_spawns");
    public static ResourceKey<StructureSet> MONASTERIES = createSetKey("monasteries");

    private static ResourceKey<StructureSet> createSetKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE_SET, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, name));
    }

    public static void bootstrap(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        Optional<StructurePlacement.ExclusionZone> villageExclusionZone = Optional.of(new StructurePlacement.ExclusionZone(context.lookup(Registries.STRUCTURE_SET).getOrThrow(BuiltinStructureSets.VILLAGES), 7));

        context.register(WROUGHT_CHAMBERS, new StructureSet(
                structures.getOrThrow(StructureHandler.WROUGHT_CHAMBER),
                new RandomSpreadStructurePlacement(15, 5, RandomSpreadType.TRIANGULAR, 23217347)
        ));

        context.register(UMVUTHANA_GROVES, new StructureSet(
                structures.getOrThrow(StructureHandler.UMVUTHANA_GROVE),
                new RandomSpreadStructurePlacement(
                        Vec3i.ZERO,
                        StructurePlacement.FrequencyReductionMethod.DEFAULT,
                        1,
                        23311138,
                        villageExclusionZone,
                        25,
                        8,
                        RandomSpreadType.TRIANGULAR
                )
        ));

        context.register(FROSTMAWS, new StructureSet(
                structures.getOrThrow(StructureHandler.FROSTMAW),
                new RandomSpreadStructurePlacement(
                        Vec3i.ZERO,
                        StructurePlacement.FrequencyReductionMethod.DEFAULT,
                        1,
                        23317578,
                        villageExclusionZone,
                        25,
                        8,
                        RandomSpreadType.TRIANGULAR
                )
        ));
//        context.register(MONASTERIES, new StructureSet(structures.getOrThrow(StructureHandler.MONASTERY), new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.TRIANGULAR, 25327374)));
    }
}
