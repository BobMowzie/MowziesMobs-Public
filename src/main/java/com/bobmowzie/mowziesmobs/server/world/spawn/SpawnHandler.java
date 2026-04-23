package com.bobmowzie.mowziesmobs.server.world.spawn;

import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.MowzieEntity;
import com.bobmowzie.mowziesmobs.server.world.BiomeChecker;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

import java.util.HashMap;
import java.util.Map;

public class SpawnHandler {
    public static final Map<EntityType<?>, ConfigHandler.SpawnConfig> SPAWN_CONFIGS = new HashMap<>();

    public static BiomeChecker FOLIAATH_BIOME_CHECKER;
    public static BiomeChecker UMVUTHANA_RAPTOR_BIOME_CHECKER;
    public static BiomeChecker GROTTOL_BIOME_CHECKER;
    public static BiomeChecker LANTERN_BIOME_CHECKER;
    public static BiomeChecker NAGA_BIOME_CHECKER;
    public static BiomeChecker BLUFF_BIOME_CHECKER;
    public static BiomeChecker ELOKOSA_HOWLER_BIOME_CHECKER;

    private static final SpawnPlacementType MM_SPAWN = (level, position, type) -> {
        BlockState below = level.getBlockState(position.below());

        if (type == null || below.is(Blocks.BEDROCK) || below.is(Blocks.BARRIER) || !below.blocksMotion()) {
            return false;
        }

        BlockState state = level.getBlockState(position);

        if (!NaturalSpawner.isValidEmptySpawnBlock(level, position, state, state.getFluidState(), type)) {
            return false;
        }

        BlockState up = level.getBlockState(position.above());

        return NaturalSpawner.isValidEmptySpawnBlock(level, position.above(), up, up.getFluidState(), type);
    };

    static {
        SPAWN_CONFIGS.put(EntityHandler.FOLIAATH.get(), ConfigHandler.COMMON.MOBS.FOLIAATH.spawnConfig);
        SPAWN_CONFIGS.put(EntityHandler.UMVUTHANA_RAPTOR.get(), ConfigHandler.COMMON.MOBS.UMVUTHANA.spawnConfig);
        SPAWN_CONFIGS.put(EntityHandler.LANTERN.get(), ConfigHandler.COMMON.MOBS.LANTERN.spawnConfig);
        SPAWN_CONFIGS.put(EntityHandler.NAGA.get(), ConfigHandler.COMMON.MOBS.NAGA.spawnConfig);
        SPAWN_CONFIGS.put(EntityHandler.GROTTOL.get(), ConfigHandler.COMMON.MOBS.GROTTOL.spawnConfig);
        SPAWN_CONFIGS.put(EntityHandler.BLUFF.get(), ConfigHandler.COMMON.MOBS.BLUFF.spawnConfig);
        SPAWN_CONFIGS.put(EntityHandler.ELOKOSA_HOWLER.get(), ConfigHandler.COMMON.MOBS.ELOKOSA.spawnConfig);
    }

    public static void registerSpawnPlacementTypes(RegisterSpawnPlacementsEvent event) {
        event.register(EntityHandler.FOLIAATH.get(), MM_SPAWN, Heightmap.Types.MOTION_BLOCKING, MowzieEntity::spawnPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EntityHandler.LANTERN.get(), MM_SPAWN, Heightmap.Types.MOTION_BLOCKING, MowzieEntity::spawnPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EntityHandler.UMVUTHANA_RAPTOR.get(), MM_SPAWN, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MowzieEntity::spawnPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EntityHandler.NAGA.get(), MM_SPAWN, Heightmap.Types.MOTION_BLOCKING, MowzieEntity::spawnPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EntityHandler.GROTTOL.get(), MM_SPAWN, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MowzieEntity::spawnPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EntityHandler.UMVUTHANA_CRANE.get(), MM_SPAWN, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MowzieEntity::spawnPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EntityHandler.BLUFF.get(), MM_SPAWN, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MowzieEntity::spawnPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EntityHandler.ELOKOSA_HOWLER.get(), MM_SPAWN, Heightmap.Types.MOTION_BLOCKING, MowzieEntity::spawnPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public static void addBiomeSpawns(Holder<Biome> biomeKey, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (FOLIAATH_BIOME_CHECKER == null) FOLIAATH_BIOME_CHECKER = new BiomeChecker(ConfigHandler.COMMON.MOBS.FOLIAATH.spawnConfig.biomeConfig);
        if (ConfigHandler.COMMON.MOBS.FOLIAATH.spawnConfig.spawnRate.get() > 0 && FOLIAATH_BIOME_CHECKER.isBiomeInConfig(biomeKey)) {
//              System.out.println("Added foliaath biome: " + biomeName.toString());
            registerEntityWorldSpawn(builder, EntityHandler.FOLIAATH.get(), ConfigHandler.COMMON.MOBS.FOLIAATH.spawnConfig, MobCategory.MONSTER);
        }

        if (UMVUTHANA_RAPTOR_BIOME_CHECKER == null) UMVUTHANA_RAPTOR_BIOME_CHECKER = new BiomeChecker(ConfigHandler.COMMON.MOBS.UMVUTHANA.spawnConfig.biomeConfig);
        if (ConfigHandler.COMMON.MOBS.UMVUTHANA.spawnConfig.spawnRate.get() > 0 && UMVUTHANA_RAPTOR_BIOME_CHECKER.isBiomeInConfig(biomeKey)) {
//              System.out.println("Added Barakoa biome: " + biomeName.toString());
            registerEntityWorldSpawn(builder, EntityHandler.UMVUTHANA_RAPTOR.get(), ConfigHandler.COMMON.MOBS.UMVUTHANA.spawnConfig, MobCategory.MONSTER);
        }

        if (GROTTOL_BIOME_CHECKER == null) GROTTOL_BIOME_CHECKER = new BiomeChecker(ConfigHandler.COMMON.MOBS.GROTTOL.spawnConfig.biomeConfig);
        if (ConfigHandler.COMMON.MOBS.GROTTOL.spawnConfig.spawnRate.get() > 0 && GROTTOL_BIOME_CHECKER.isBiomeInConfig(biomeKey)) {
//              System.out.println("Added grottol biome: " + biomeName.toString());
            registerEntityWorldSpawn(builder, EntityHandler.GROTTOL.get(), ConfigHandler.COMMON.MOBS.GROTTOL.spawnConfig, MobCategory.MONSTER);
        }

        if (LANTERN_BIOME_CHECKER == null) LANTERN_BIOME_CHECKER = new BiomeChecker(ConfigHandler.COMMON.MOBS.LANTERN.spawnConfig.biomeConfig);
        if (ConfigHandler.COMMON.MOBS.LANTERN.spawnConfig.spawnRate.get() > 0 && LANTERN_BIOME_CHECKER.isBiomeInConfig(biomeKey)) {
//              System.out.println("Added lantern biome: " + biomeName.toString());
            registerEntityWorldSpawn(builder, EntityHandler.LANTERN.get(), ConfigHandler.COMMON.MOBS.LANTERN.spawnConfig, MobCategory.AMBIENT);
        }

        if (NAGA_BIOME_CHECKER == null) NAGA_BIOME_CHECKER = new BiomeChecker(ConfigHandler.COMMON.MOBS.NAGA.spawnConfig.biomeConfig);
        if (ConfigHandler.COMMON.MOBS.NAGA.spawnConfig.spawnRate.get() > 0 && NAGA_BIOME_CHECKER.isBiomeInConfig(biomeKey)) {
//              System.out.println("Added naga biome: " + biomeName.toString());
            registerEntityWorldSpawn(builder, EntityHandler.NAGA.get(), ConfigHandler.COMMON.MOBS.NAGA.spawnConfig, MobCategory.MONSTER);
        }

        if (BLUFF_BIOME_CHECKER == null) BLUFF_BIOME_CHECKER = new BiomeChecker(ConfigHandler.COMMON.MOBS.BLUFF.spawnConfig.biomeConfig);
        if (ConfigHandler.COMMON.MOBS.BLUFF.spawnConfig.spawnRate.get() > 0 && BLUFF_BIOME_CHECKER.isBiomeInConfig(biomeKey)) {
//              System.out.println("Added bluff biome: " + biomeName.toString());
            registerEntityWorldSpawn(builder, EntityHandler.BLUFF.get(), ConfigHandler.COMMON.MOBS.BLUFF.spawnConfig, MobCategory.MONSTER);
        }

        if (ELOKOSA_HOWLER_BIOME_CHECKER == null) ELOKOSA_HOWLER_BIOME_CHECKER = new BiomeChecker(ConfigHandler.COMMON.MOBS.ELOKOSA.spawnConfig.biomeConfig);
        if (ConfigHandler.COMMON.MOBS.ELOKOSA.spawnConfig.spawnRate.get() > 0 && ELOKOSA_HOWLER_BIOME_CHECKER.isBiomeInConfig(biomeKey)) {
//              System.out.println("Added elokosa biome: " + biomeName.toString());
            registerEntityWorldSpawn(builder, EntityHandler.ELOKOSA_HOWLER.get(), ConfigHandler.COMMON.MOBS.ELOKOSA.spawnConfig, MobCategory.MONSTER);
        }
    }

    private static void registerEntityWorldSpawn(ModifiableBiomeInfo.BiomeInfo.Builder builder, EntityType<?> entity, ConfigHandler.SpawnConfig spawnConfig, MobCategory classification) {
    	builder.getMobSpawnSettings().getSpawner(classification).add(new MobSpawnSettings.SpawnerData(entity, spawnConfig.spawnRate.get(), spawnConfig.minGroupSize.get(), spawnConfig.maxGroupSize.get()));
    }
}