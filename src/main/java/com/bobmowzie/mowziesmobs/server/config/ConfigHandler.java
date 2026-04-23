package com.bobmowzie.mowziesmobs.server.config;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.datagen.MMBiomeTags;
import com.bobmowzie.mowziesmobs.datagen.StructureSetHandler;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.Tags;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public final class ConfigHandler {
    private static final String LANG_PREFIX = "config." + MMCommon.MODID + ".";

    public static final Common COMMON;
    public static final Client CLIENT;

    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    public static ModConfigSpec COMMON_CONFIG;
    public static ModConfigSpec CLIENT_CONFIG;

    private static final Predicate<Object> STRING_PREDICATE = s -> s instanceof String;
    private static final Predicate<Object> RESOURCE_LOCATION_PREDICATE = STRING_PREDICATE.and(s -> ResourceLocation.tryParse((String) s) != null);
    private static final Predicate<Object> BIOME_COMBO_PREDICATE = STRING_PREDICATE.and(s -> {
        String bigString = (String) s;
        String[] typeStrings = bigString.replace(" ", "").split("[,!]");
        for (String string : typeStrings) {
            if (!RESOURCE_LOCATION_PREDICATE.test(string)) {
                return false;
            }
        }
        return true;
    });

    private static final Predicate<Object> ITEM_NAME_PREDICATE = RESOURCE_LOCATION_PREDICATE.and(s -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.tryParse((String) s)));

    static {
        COMMON = new Common(COMMON_BUILDER);
        CLIENT = new Client(CLIENT_BUILDER);

        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    // Config templates
    public static class BiomeConfig {
        BiomeConfig(final ModConfigSpec.Builder builder, List<? extends String> biomeTags, List<? extends String> biomeWhitelist, List<? extends String> biomeBlacklist) {
            builder.push("biome_config");
            builder.comment("Mowzie's Mobs bosses cannot generate in modded or non-overworld biomes unless the biome is added to the 'has_structure/has_mowzie_structure' tag via a datapack!");
            this.biomeTags = builder.comment("Each entry is a combination of allowed biome tags or biome names.", "Separate types with commas to require biomes to have all tags in an entry", "Put a '!' before a biome tag to mean NOT that tag", "A blank entry means all biomes. No entries means no biomes.", "For example, 'minecraft:is_forest,c:is_spooky,!c:is_snowy' would mean all biomes that are spooky forests but not snowy forests", "'!minecraft:is_mountain' would mean all non-mountain biomes")
                    .translation(LANG_PREFIX + "biome_tags")
                    .defineList("biome_tags", biomeTags, BIOME_COMBO_PREDICATE);
            this.biomeWhitelist = builder.comment("Allow spawns in these biomes regardless of the biome tag settings")
                    .translation(LANG_PREFIX + "biome_whitelist")
                    .defineList("biome_whitelist", biomeWhitelist, BIOME_COMBO_PREDICATE);
            this.biomeBlacklist = builder.comment("Prevent spawns in these biomes regardless of the biome tag settings")
                    .translation(LANG_PREFIX + "biome_blacklist")
                    .defineList("biome_blacklist", biomeBlacklist, BIOME_COMBO_PREDICATE);
            builder.pop();
        }

        public final ModConfigSpec.ConfigValue<List<? extends String>> biomeTags;

        public final ModConfigSpec.ConfigValue<List<? extends String>> biomeWhitelist;

        public final ModConfigSpec.ConfigValue<List<? extends String>> biomeBlacklist;
    }

    public static class SpawnConfig {
        SpawnConfig(final ModConfigSpec.Builder builder, int spawnRate, int minGroupSize, int maxGroupSize, double extraRarity, BiomeConfig biomeConfig, List<? extends String> allowedBlocks, List<? extends String> allowedBlockTags, int heightMax, int heightMin, boolean needsDarkness, boolean needsSeeSky, boolean needsCantSeeSky, List<String> avoidStructures) {
            builder.comment("Controls for vanilla-style mob spawning");
            builder.push("spawn_config");
            this.spawnRate = builder.comment("Smaller number causes less spawning, 0 to disable spawning")
                    .translation(LANG_PREFIX + "spawn_rate")
                    .defineInRange("spawn_rate", spawnRate, 0, Integer.MAX_VALUE);
            this.minGroupSize = builder.comment("Minimum number of mobs that appear in a spawn group")
                    .translation(LANG_PREFIX + "min_group_size")
                    .defineInRange("min_group_size", minGroupSize, 1, Integer.MAX_VALUE);
            this.maxGroupSize = builder.comment("Maximum number of mobs that appear in a spawn group")
                    .translation(LANG_PREFIX + "max_group_size")
                    .defineInRange("max_group_size", maxGroupSize, 1, Integer.MAX_VALUE);
            this.extraRarity = builder.comment("Probability of a spawn attempt succeeding. 1 for normal spawning, 0 will prevent spawning. Used to make mobs extra rare.")
                    .translation(LANG_PREFIX + "extra_rarity")
                    .defineInRange("extra_rarity", extraRarity, 0.0, 1.0);
            this.biomeConfig = biomeConfig;
            this.dimensions = builder.comment("Names of dimensions this mob can spawn in")
                    .translation(LANG_PREFIX + "dimensions")
                    .defineList("dimensions", Collections.singletonList(string(BuiltinDimensionTypes.OVERWORLD)), STRING_PREDICATE);
            this.allowedBlocks = builder.comment("Names of blocks this mob is allowed to spawn on. Leave blank to ignore block names.")
                    .translation(LANG_PREFIX + "allowed_blocks")
                    .defineList("allowed_blocks", allowedBlocks, STRING_PREDICATE);
            this.allowedBlockTags = builder.comment("Tags of blocks this mob is allowed to spawn on. Leave blank to ignore block tags.")
                    .translation(LANG_PREFIX + "allowed_block_tags")
                    .defineList("allowed_block_tags", allowedBlockTags, STRING_PREDICATE);
            this.heightMax = builder.comment("Maximum height for this spawn. -65 to ignore.")
                    .translation(LANG_PREFIX + "height_max")
                    .defineInRange("height_max", heightMax, -65, 256);
            this.heightMin = builder.comment("Minimum height for this spawn. -65 to ignore.")
                    .translation(LANG_PREFIX + "height_min")
                    .defineInRange("height_min", heightMin, -65, 256);
            this.needsDarkness = builder.comment("Set to true to only allow this mob to spawn in the dark, like zombies and skeletons.")
                    .translation(LANG_PREFIX + "needs_darkness")
                    .define("needs_darkness", needsDarkness);
            this.needsSeeSky = builder.comment("Set to true to only spawn mob if it can see the sky.")
                    .translation(LANG_PREFIX + "min_group_size")
                    .define("needs_see_sky", needsSeeSky);
            this.needsCantSeeSky = builder.comment("Set to true to only spawn mob if it can't see the sky.")
                    .translation(LANG_PREFIX + "min_group_size")
                    .define("needs_cant_see_sky", needsCantSeeSky);
            this.avoidStructures = builder.comment("Names of structures this mob will avoid spawning near.")
                    .translation(LANG_PREFIX + "avoid_structures")
                    .defineList("avoid_structures", avoidStructures, STRING_PREDICATE);
            builder.pop();
        }

        public final ModConfigSpec.IntValue spawnRate;

        public final ModConfigSpec.IntValue minGroupSize;

        public final ModConfigSpec.IntValue maxGroupSize;

        public final ModConfigSpec.DoubleValue extraRarity;

        public final BiomeConfig biomeConfig;

        public final ModConfigSpec.ConfigValue<List<? extends String>> dimensions;

        public final ModConfigSpec.IntValue heightMin;

        public final ModConfigSpec.IntValue heightMax;

        public final ModConfigSpec.BooleanValue needsDarkness;

        public final ModConfigSpec.BooleanValue needsSeeSky;

        public final ModConfigSpec.BooleanValue needsCantSeeSky;

        public final ModConfigSpec.ConfigValue<List<? extends String>> allowedBlocks;

        public final ModConfigSpec.ConfigValue<List<? extends String>> allowedBlockTags;

        public final ModConfigSpec.ConfigValue<List<? extends String>> avoidStructures;
    }

    public static class GenerationConfig {
        GenerationConfig(final ModConfigSpec.Builder builder, int generationDistance, int generationSeparation, BiomeConfig biomeConfig, float heightMin, float heightMax, List<String> avoidStructures) {
            builder.comment("Controls for spawning structure/mob with world generation");
            builder.push("generation_config");
            this.generationDistance = builder.comment("Smaller number causes more generation, -1 to disable generation", "Maximum number of chunks between placements of this mob/structure.", "NO LONGER USED! USE DATAPACK INSTEAD")
                    .translation(LANG_PREFIX + "generation_distance")
                    .defineInRange("generation_distance", generationDistance, -1, Integer.MAX_VALUE);
            this.generationSeparation = builder.comment("Smaller number causes more generation, -1 to disable generation", "Minimum number of chunks between placements of this mob/structure.", "NO LONGER USED! USE DATAPACK INSTEAD")
                    .translation(LANG_PREFIX + "generation_separation")
                    .defineInRange("generation_separation", generationSeparation, -1, Integer.MAX_VALUE);
            this.biomeConfig = biomeConfig;
            this.heightMax = builder.comment("Maximum height for generation placement. -65 to ignore")
                    .translation(LANG_PREFIX + "height_max")
                    .defineInRange("height_max", heightMax, -65, 256);
            this.heightMin = builder.comment("Minimum height for generation placement. -65 to ignore")
                    .translation(LANG_PREFIX + "height_min")
                    .defineInRange("height_min", heightMin, -65, 256);
            this.avoidStructures = builder.comment("Names of structures this mob/structure will avoid when generating.", "NO LONGER USED! USE DATAPACK INSTEAD")
                    .translation(LANG_PREFIX + "avoid_structures")
                    .defineList("avoid_structures", avoidStructures, STRING_PREDICATE);
            builder.pop();
        }

        public final ModConfigSpec.IntValue generationDistance;

        public final ModConfigSpec.IntValue generationSeparation;

        public final BiomeConfig biomeConfig;

        public final ModConfigSpec.DoubleValue heightMin;

        public final ModConfigSpec.DoubleValue heightMax;

        public final ModConfigSpec.ConfigValue<List<? extends String>> avoidStructures;
    }

    public static class CombatConfig {
        CombatConfig(final ModConfigSpec.Builder builder, float healthMultiplier, float attackMultiplier) {
            builder.push("combat_config");
            this.healthMultiplier = builder.comment("Scale mob health by this value")
                    .translation(LANG_PREFIX + "health_multiplier")
                    .defineInRange("health_multiplier", healthMultiplier, 0d, Double.MAX_VALUE);
            this.attackMultiplier = builder.comment("Scale mob attack damage by this value")
                    .translation(LANG_PREFIX + "attack_multiplier")
                    .defineInRange("attack_multiplier", attackMultiplier, 0d, Double.MAX_VALUE);
            builder.pop();
        }

        public final ModConfigSpec.DoubleValue healthMultiplier;

        public final ModConfigSpec.DoubleValue attackMultiplier;
    }

    public static class ToolConfig {
        ToolConfig(final ModConfigSpec.Builder builder, float attackDamage, float attackSpeed) {
            builder.push("tool_config");
            this.attackDamage = builder.comment("Tool attack damage")
                    .translation(LANG_PREFIX + "attack_damage")
                    .defineInRange("attack_damage", attackDamage, 0d, Double.MAX_VALUE);
            this.attackSpeed = builder.comment("Tool attack speed")
                    .translation(LANG_PREFIX + "attack_speed")
                    .defineInRange("attack_speed", attackSpeed, 0d, Double.MAX_VALUE);
            builder.pop();
        }

        public final ModConfigSpec.DoubleValue attackDamage;
        
        public float attackDamageValue = 9;
        public float attackSpeedValue = 0.9F;

        public final ModConfigSpec.DoubleValue attackSpeed;
    }

    public static class ArmorConfig {
        ArmorConfig(final ModConfigSpec.Builder builder) {
            builder.push("armor_config");
            this.damageReductionMultiplier = builder.comment("Multiply armor damage reduction by this amount. See official Minecraft Wiki for an explanation of how armor damage reduction works.")
                    .translation(LANG_PREFIX + "damage_reduction_multiplier")
                    .defineInRange("damage_reduction_multiplier", 1.0f, 0d, Double.MAX_VALUE);
            this.toughnessMultiplier = builder.comment("Multiply armor toughness by this amount. See official Minecraft Wiki for an explanation of how armor toughness works.")
                    .translation(LANG_PREFIX + "toughness_multiplier")
                    .defineInRange("toughness_multiplier", 1.0f, 0d, Double.MAX_VALUE);
            builder.pop();
        }

        public final ModConfigSpec.DoubleValue damageReductionMultiplier;
        public final ModConfigSpec.DoubleValue toughnessMultiplier;

        public float damageReductionMultiplierValue = 1.0f;
        public float toughnessMultiplierValue = 1.0f;
    }

    // Mob configuration
    public static class Foliaath {
        Foliaath(final ModConfigSpec.Builder builder) {
            builder.push("foliaath");
            spawnConfig = new SpawnConfig(builder,
                    70, 1, 4, 1,
                    new BiomeConfig(builder, Collections.singletonList(string(Tags.Biomes.IS_JUNGLE)), Collections.emptyList(), Collections.emptyList()),
                    Collections.emptyList(),
                    Arrays.asList(string(BlockTags.ANIMALS_SPAWNABLE_ON), string(BlockTags.LEAVES), string(BlockTags.LOGS)),
                    -65, 60, true, false, false,
                    Arrays.asList(string(BuiltinStructureSets.VILLAGES), string(BuiltinStructureSets.PILLAGER_OUTPOSTS))
            );
            combatConfig = new CombatConfig(builder, 1, 1);
            builder.pop();
        }

        public final SpawnConfig spawnConfig;

        public final CombatConfig combatConfig;
    }

    public static class Umvuthana {
        Umvuthana(final ModConfigSpec.Builder builder) {
            builder.push("umvuthana");
            builder.comment("Controls spawning for Umvuthana hunting groups", "Group size controls how many raptors spawn, not followers", "See Umvuthi config for grove structure controls");
            spawnConfig = new SpawnConfig(builder,
                    5, 1, 1, 1,
                    new BiomeConfig(builder, Collections.singletonList(string(Tags.Biomes.IS_SAVANNA)), Collections.emptyList(), Collections.emptyList()),
                    Collections.emptyList(),
                    Arrays.asList(string(BlockTags.ANIMALS_SPAWNABLE_ON), string(BlockTags.SAND)),
                    -65, 60, false, false, false,
                    Arrays.asList(string(BuiltinStructureSets.VILLAGES), string(BuiltinStructureSets.PILLAGER_OUTPOSTS), string(StructureSetHandler.UMVUTHANA_GROVES))
            );
            combatConfig = new CombatConfig(builder,1, 1);
            builder.pop();
        }

        public final SpawnConfig spawnConfig;

        public final CombatConfig combatConfig;
    }

    public static class Naga {
        Naga(final ModConfigSpec.Builder builder) {
            builder.push("naga");
            spawnConfig = new SpawnConfig(builder,
                    20, 1, 2, 1,
                    new BiomeConfig(builder, Arrays.asList(string(Tags.Biomes.IS_BEACH) + "," + string(Tags.Biomes.IS_MOUNTAIN), string(Tags.Biomes.IS_BEACH) + "," + string(BiomeTags.IS_HILL)), Collections.singletonList(string(Biomes.STONY_SHORE)), Collections.emptyList()),
                    Collections.emptyList(),
                    List.of(string(BlockTags.ANIMALS_SPAWNABLE_ON), string(BlockTags.BASE_STONE_OVERWORLD)),
                    -65, 68, true, true, false,
                    Arrays.asList(string(BuiltinStructureSets.VILLAGES), string(BuiltinStructureSets.PILLAGER_OUTPOSTS))
            );
            combatConfig = new CombatConfig(builder,1, 1);
            builder.pop();
        }

        public final SpawnConfig spawnConfig;

        public final CombatConfig combatConfig;
    }

    public static class Lantern {
        Lantern(final ModConfigSpec.Builder builder) {
            builder.push("lantern");
            spawnConfig = new SpawnConfig(builder,
                    5, 2, 4, 1,
                    new BiomeConfig(builder, Collections.singletonList(string(Tags.Biomes.IS_FOREST) + "," + string(MMBiomeTags.IS_MAGICAL) + "," + inverted(Tags.Biomes.IS_SNOWY)), Collections.emptyList(), Collections.emptyList()),
                    Collections.emptyList(),
                    Arrays.asList(string(BlockTags.ANIMALS_SPAWNABLE_ON), string(BlockTags.LEAVES), string(BlockTags.LOGS)),
                    -65, 60, true, false, false,
                    Collections.emptyList()
            );
            combatConfig = new CombatConfig(builder, 1, 1);
            builder.pop();
        }

        public final SpawnConfig spawnConfig;

        public final CombatConfig combatConfig;
    }

    public static class Grottol {
        Grottol(final ModConfigSpec.Builder builder) {
            builder.push("grottol");
            this.spawnConfig = new SpawnConfig(builder,
                    2, 1, 1, 1,
                    new BiomeConfig(builder, Collections.singletonList(inverted(Tags.Biomes.IS_MUSHROOM)), Collections.emptyList(), Collections.emptyList()),
                    Collections.emptyList(),
                    Collections.singletonList(string(BlockTags.BASE_STONE_OVERWORLD)),
                    16, -65, true, false, true,
                    Collections.emptyList()
            );
            combatConfig = new CombatConfig(builder, 1, 1);
            builder.pop();
        }

        public final SpawnConfig spawnConfig;

        public final CombatConfig combatConfig;
    }

    public static class FerrousWroughtnaut {
        FerrousWroughtnaut(final ModConfigSpec.Builder builder) {
            builder.push("ferrous_wroughtnaut");
            generationConfig = new GenerationConfig(builder, 15, 5,
                    new BiomeConfig(builder, Collections.singletonList(inverted(Tags.Biomes.IS_OCEAN)), Collections.emptyList(), Collections.emptyList()),
                    20, 50,
                    Collections.emptyList()
            );
            combatConfig = new CombatConfig(builder, 1, 1);
            this.hasBossBar = builder.comment("Disable/enable Ferrous Wroughtnaut's boss health bar")
                    .translation(LANG_PREFIX + "has_boss_bar")
                    .define("has_boss_bar", true);
            this.healsOutOfBattle = builder.comment("Disable/enable Ferrous Wroughtnaut healing while not active")
                    .translation(LANG_PREFIX + "heals_out_of_battle")
                    .define("heals_out_of_battle", true);
            this.resetHealthWhenRespawn = builder.comment("Disable/enable Ferrous Wroughtnaut resetting health when a player respawns nearby. (Prevents respawn cheese!)")
                    .translation(LANG_PREFIX + "reset_health_when_respawn")
                    .define("reset_health_when_respawn", true);
            builder.pop();
        }

        public final GenerationConfig generationConfig;

        public final CombatConfig combatConfig;
        public final ModConfigSpec.BooleanValue hasBossBar;

        public final ModConfigSpec.BooleanValue healsOutOfBattle;

        public final ModConfigSpec.BooleanValue resetHealthWhenRespawn;
    }

    public static class Umvuthi {
        Umvuthi(final ModConfigSpec.Builder builder) {
            builder.push("umvuthi");
            builder.comment("Generation controls for Umvuthana Groves");
            generationConfig = new GenerationConfig(builder, 25, 8,
                    new BiomeConfig(builder, Collections.singletonList(string(Tags.Biomes.IS_SAVANNA)), Collections.emptyList(), Collections.emptyList()),
                    50, 100,
                    Arrays.asList(string(BuiltinStructureSets.VILLAGES), string(BuiltinStructureSets.PILLAGER_OUTPOSTS))
            );
            combatConfig = new CombatConfig(builder, 1, 1);
            this.hasBossBar = builder.comment("Disable/enable Umvuthi's boss health bar")
                    .translation(LANG_PREFIX + "has_boss_bar")
                    .define("has_boss_bar", true);
            this.healsOutOfBattle = builder.comment("Disable/enable Umvuthi healing while not in combat")
                    .translation(LANG_PREFIX + "heals_out_of_battle")
                    .define("heals_out_of_battle", true);
            this.whichItem = builder.comment("Which item Umvuthi desires in exchange for the Sun's Blessing")
                    .translation(LANG_PREFIX + "trade_which_item")
                    .define("trade_which_item", string(Items.GOLD_BLOCK), ITEM_NAME_PREDICATE);
            this.howMany = builder.comment("How many of the item Umvuthi desires in exchange for the Sun's Blessing")
                    .translation(LANG_PREFIX + "trade_how_many")
                    .defineInRange("trade_how_many", 7, 0, 64);
            this.resetHealthWhenRespawn = builder.comment("Disable/enable Umvuthi resetting health when a player respawns nearby. (Prevents respawn cheese!)")
                    .translation(LANG_PREFIX + "reset_health_when_respawn")
                    .define("reset_health_when_respawn", true);
            this.spawnHealersThreshold = builder.comment("Umvuthi will summon healers whenever he loses this much total health.")
                    .translation(LANG_PREFIX + "spawn_healers_threshold")
                    .defineInRange("spawn_healers_threshold", 45, 0, Integer.MAX_VALUE);
            builder.pop();
        }

        public final GenerationConfig generationConfig;

        public final CombatConfig combatConfig;

        public final ModConfigSpec.BooleanValue hasBossBar;

        public final ModConfigSpec.BooleanValue healsOutOfBattle;

        public final ModConfigSpec.ConfigValue<? extends String> whichItem;

        public final ModConfigSpec.IntValue howMany;

        public final ModConfigSpec.BooleanValue resetHealthWhenRespawn;

        public final ModConfigSpec.IntValue spawnHealersThreshold;
    }

    public static class Frostmaw {
        Frostmaw(final ModConfigSpec.Builder builder) {
            builder.push("frostmaw");
            generationConfig = new GenerationConfig(builder, 25, 8,
                    new BiomeConfig(builder, Collections.singletonList(string(Tags.Biomes.IS_SNOWY) + "," + inverted(Tags.Biomes.IS_OCEAN) + "," + inverted(Tags.Biomes.IS_RIVER) + "," + inverted(Tags.Biomes.IS_BEACH) + "," + inverted(Tags.Biomes.IS_FOREST) + "," + inverted(Tags.Biomes.IS_TAIGA)), Collections.emptyList(), Collections.emptyList()),
                    50, 100,
                    Arrays.asList(string(BuiltinStructureSets.VILLAGES), string(BuiltinStructureSets.PILLAGER_OUTPOSTS))
            );
            combatConfig = new CombatConfig(builder, 1, 1);
            this.hasBossBar = builder.comment("Disable/enable Frostmaw's boss health bar")
                    .translation(LANG_PREFIX + "has_boss_bar")
                    .define("has_boss_bar", true);
            this.healsOutOfBattle = builder.comment("Disable/enable frostmaws healing while asleep")
                    .translation(LANG_PREFIX + "heals_out_of_battle")
                    .define("heals_out_of_battle", true);
            this.stealableIceCrystal = builder.comment("Allow players to steal frostmaws' ice crystals (only using specific means!)")
                    .translation(LANG_PREFIX + "stealable_ice_crystal")
                    .define("stealable_ice_crystal", true);
            this.resetHealthWhenRespawn = builder.comment("Disable/enable frostmaws resetting health when a player respawns nearby. (Prevents respawn cheese!)")
                    .translation(LANG_PREFIX + "reset_health_when_respawn")
                    .define("reset_health_when_respawn", true);
            builder.pop();
        }

        public final GenerationConfig generationConfig;

        public final CombatConfig combatConfig;

        public final ModConfigSpec.BooleanValue stealableIceCrystal;

        public final ModConfigSpec.BooleanValue hasBossBar;

        public final ModConfigSpec.BooleanValue healsOutOfBattle;

        public final ModConfigSpec.BooleanValue resetHealthWhenRespawn;
    }

    public static class Sculptor {
        Sculptor(final ModConfigSpec.Builder builder) {
            builder.push("sculptor");
            generationConfig = new GenerationConfig(builder, 25, 8,
                    new BiomeConfig(builder, Collections.singletonList(string(Tags.Biomes.IS_MOUNTAIN_PEAK)), Collections.emptyList(), Collections.emptyList()),
                    120, 200,
                    Collections.emptyList()
            );
            combatConfig = new CombatConfig(builder, 1, 1);
            this.testHeight = builder.comment("How tall (in blocks) the Sculptor's test will be")
                    .translation(LANG_PREFIX + "test_height")
                    .defineInRange("test_height", 60, 1, 500);
            this.testTimeLimit = builder.comment("The time limit (in seconds) for completing the Sculptor's test")
                    .translation(LANG_PREFIX + "test_time_limit")
                    .defineInRange("test_time_limit", 400, 1, 36000);
            this.healsOutOfBattle = builder.comment("Disable/enable the Sculptor healing while not in combat")
                    .translation(LANG_PREFIX + "heals_out_of_battle")
                    .define("heals_out_of_battle", true);
            this.hasBossBar = builder.comment("Disable/enable the Sculptor's boss health bar")
                    .translation(LANG_PREFIX + "has_boss_bar")
                    .define("has_boss_bar", true);
            this.whichItem = builder.comment("Which item the Sculptor desires in exchange for a chance to try his challenge")
                    .translation(LANG_PREFIX + "trade_which_item")
                    .define("trade_which_item", string(ItemHandler.BLUFF_ROD), ITEM_NAME_PREDICATE);
            this.howMany = builder.comment("How many of the item the Sculptor desires in exchange for a chance to try his challenge")
                    .translation(LANG_PREFIX + "trade_how_many")
                    .defineInRange("trade_how_many", 1, 0, 64);
            this.disappearAfterReward = builder.comment("Set to true for the Sculptor to disappear after a player beats the test and claims the reward.")
                    .translation(LANG_PREFIX + "disappear_after_reward")
                    .define("disappear_after_reward", true);
            builder.pop();
        }

        public final GenerationConfig generationConfig;

        public final CombatConfig combatConfig;

        public final ModConfigSpec.IntValue testHeight;

        public final ModConfigSpec.IntValue testTimeLimit;

        public final ModConfigSpec.BooleanValue healsOutOfBattle;

        public final ModConfigSpec.BooleanValue hasBossBar;

        public final ModConfigSpec.ConfigValue<? extends String> whichItem;

        public final ModConfigSpec.IntValue howMany;

        public final ModConfigSpec.BooleanValue disappearAfterReward;
    }

    public static class Bluff {
        Bluff(final ModConfigSpec.Builder builder) {
            builder.push("bluff");
            spawnConfig = new SpawnConfig(builder,
                    10, 2, 3, 1,
                    new BiomeConfig(builder, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    -65, -65, true, false, true,
                    Collections.emptyList()
            );
            combatConfig = new CombatConfig(builder,1, 1);
            builder.pop();
        }

        public final SpawnConfig spawnConfig;

        public final CombatConfig combatConfig;
    }

    public static class Elokosa {
        Elokosa(final ModConfigSpec.Builder builder) {
            builder.push("Elokosa");
            spawnConfig = new SpawnConfig(builder,
                    5, 1, 1, 1,
                    new BiomeConfig(builder, Collections.singletonList(string(Tags.Biomes.IS_JUNGLE)), Collections.emptyList(), Collections.emptyList()),
                    Collections.emptyList(),
                    Arrays.asList(string(BlockTags.LEAVES), string(BlockTags.LOGS)),
                    -65, 60, false, false, false,
                    Arrays.asList(string(BuiltinStructureSets.VILLAGES), string(BuiltinStructureSets.PILLAGER_OUTPOSTS))
            );
            combatConfig = new CombatConfig(builder,1, 1);
            builder.pop();
        }

        public final SpawnConfig spawnConfig;

        public final CombatConfig combatConfig;
    }

    public static class WroughtHelm {
        WroughtHelm(final ModConfigSpec.Builder builder) {
            builder.push("wrought_helm");
            armorConfig = new ArmorConfig(builder);
            breakable = builder.comment("Set to true for the Wrought Helm to have limited durability.")
                    .translation(LANG_PREFIX + "breakable")
                    .define("breakable", false);
            builder.pop();
        }

        public final ArmorConfig armorConfig;

        public final ModConfigSpec.BooleanValue breakable;
    }

    public static class AxeOfAThousandMetals {
        AxeOfAThousandMetals(final ModConfigSpec.Builder builder) {
            builder.push("axe_of_a_thousand_metals");
            toolConfig = new ToolConfig(builder, 9, 0.9f);
            breakable = builder.comment("Set to true for the Axe of a Thousand Metals to have limited durability.")
                    .translation(LANG_PREFIX + "breakable")
                    .define("breakable", false);
            builder.pop();
        }

        public final ToolConfig toolConfig;

        public final ModConfigSpec.BooleanValue breakable;
    }

    public static class SolVisage {
        SolVisage(final ModConfigSpec.Builder builder) {
            builder.push("sol_visage");
            armorConfig = new ArmorConfig(builder);
            breakable = builder.comment("Set to true for the Sol Visage to have limited durability.")
                    .translation(LANG_PREFIX + "breakable")
                    .define("breakable", false);
            maxFollowers = builder.comment("Maximum number of Umvuthana followers a player can summon at once using the Sol Visage")
                    .translation(LANG_PREFIX + "max_followers")
                    .defineInRange("max_followers", 10, 0, 300);
            builder.pop();
        }

        public final ArmorConfig armorConfig;

        public final ModConfigSpec.BooleanValue breakable;

        public final ModConfigSpec.IntValue maxFollowers;
    }

    public static class UmvuthanaMask {
        UmvuthanaMask(final ModConfigSpec.Builder builder) {
            builder.push("umvuthana_mask");
            armorConfig = new ArmorConfig(builder);
            builder.pop();
        }

        public final ArmorConfig armorConfig;
    }

    public static class GeomancerArmor {
        GeomancerArmor(final ModConfigSpec.Builder builder) {
            builder.push("geomancerArmor");
            armorConfig = new ArmorConfig(builder);
            builder.pop();
        }

        public final ArmorConfig armorConfig;
    }

    public static class IceCrystal {
        IceCrystal(final ModConfigSpec.Builder builder) {
            builder.push("ice_crystal");
            attackMultiplier = builder.comment("Multiply all damage done with the ice crystal by this amount.")
                    .translation(LANG_PREFIX + "attack_multiplier")
                    .defineInRange("attack_multiplier", 1f, 0d, Double.MAX_VALUE);
            breakable = builder.comment("Set to true for the ice crystal to have limited durability.", "Prevents regeneration in inventory.")
                    .translation(LANG_PREFIX + "breakable")
                    .define("breakable", false);
            durability = builder.comment("Ice crystal durability")
                    .translation(LANG_PREFIX + "durability")
                    .defineInRange("durability", 600, 1, Integer.MAX_VALUE);
            freezeDuration = builder.comment("Freeze duration in ticks (applies to frostmaw too!)")
                    .translation(LANG_PREFIX + "freeze_duration")
                    .defineInRange("freeze_duration", 50, 1, Integer.MAX_VALUE);
            builder.pop();
        }

        public final ModConfigSpec.DoubleValue attackMultiplier;

        public final ModConfigSpec.BooleanValue breakable;

        public final ModConfigSpec.IntValue durability;
        public int durabilityValue;

        public final ModConfigSpec.IntValue freezeDuration;
    }

    public static class EarthrendGauntlet {
        EarthrendGauntlet(final ModConfigSpec.Builder builder) {
            builder.push("earthrend_gauntlet");
            attackMultiplier = builder.comment("Multiply all damage done with the Earthrend Gauntlet by this amount.")
                    .translation(LANG_PREFIX + "attack_multiplier")
                    .defineInRange("attack_multiplier", 1f, 0d, Double.MAX_VALUE);
            breakable = builder.comment("Set to true for the Earthrend Gauntlet to have limited durability.", "Prevents regeneration in inventory.")
                    .translation(LANG_PREFIX + "breakable")
                    .define("breakable", false);
            durability = builder.comment("Earthrend Gauntlet durability")
                    .translation(LANG_PREFIX + "durability")
                    .defineInRange("durability", 400, 1, Integer.MAX_VALUE);
            enableTunneling = builder.comment("Set to false to disable the Earthrend Gauntlet's tunneling ability.")
                    .translation(LANG_PREFIX + "enable_tunneling")
                    .define("enable_tunneling", true);
            toolConfig = new ToolConfig(builder, 6, 1.2f);
            builder.pop();
        }

        public final ModConfigSpec.DoubleValue attackMultiplier;

        public final ModConfigSpec.BooleanValue breakable;

        public final ModConfigSpec.IntValue durability;
        public int durabilityValue;

        public final ToolConfig toolConfig;

        public final ModConfigSpec.BooleanValue enableTunneling;
    }

    public static class Spear {
        Spear(final ModConfigSpec.Builder builder) {
            builder.push("spear");
            toolConfig = new ToolConfig(builder, 5, 1.6f);
            builder.pop();
        }

        public final ToolConfig toolConfig;
    }

    public static class NagaFangDagger {
        NagaFangDagger(final ModConfigSpec.Builder builder) {
            builder.push("naga_fang_dagger");
            toolConfig = new ToolConfig(builder, 3, 2);
            poisonDuration = builder.comment("Duration in ticks of the poison effect (20 ticks = 1 second).")
                    .translation(LANG_PREFIX + "poison_duration")
                    .defineInRange("poison_duration", 40, 0, Integer.MAX_VALUE);
            backstabDamageMultiplier = builder.comment("Damage multiplier when attacking from behind")
                    .translation(LANG_PREFIX + "backstab_damage_mult")
                    .defineInRange("backstab_damage_mult", 2f, 0d, Double.MAX_VALUE);
            builder.pop();
        }

        public final ToolConfig toolConfig;

        public final ModConfigSpec.IntValue poisonDuration;

        public final ModConfigSpec.DoubleValue backstabDamageMultiplier;
    }

    public static class Blowgun {
        Blowgun(final ModConfigSpec.Builder builder) {
            builder.push("blowgun");
            poisonDuration = builder.comment("Duration in ticks of the poison effect (20 ticks = 1 second).")
                    .translation(LANG_PREFIX + "poison_duration")
                    .defineInRange("poison_duration", 40, 0, Integer.MAX_VALUE);
            attackDamage = builder.comment("Multiply all damage done with the blowgun/darts by this amount.")
                    .translation(LANG_PREFIX + "attack_damage")
                    .defineInRange("attack_damage", 1d, 0, Double.MAX_VALUE);
            builder.pop();
        }

        public final ModConfigSpec.DoubleValue attackDamage;

        public final ModConfigSpec.IntValue poisonDuration;
    }

    public static class SunsBlessing {
        SunsBlessing(final ModConfigSpec.Builder builder) {
            builder.push("suns_blessing");
            effectDuration = builder.comment("Duration in minutes of the Sun's Blessing effect.")
                    .translation(LANG_PREFIX + "suns_blessing_duration")
                    .defineInRange("suns_blessing_duration", 60, 0, Integer.MAX_VALUE);
            sunsBlessingAttackMultiplier = builder.translation(LANG_PREFIX + "suns_blessing_attack_multiplier")
                    .defineInRange("suns_blessing_attack_multiplier", 1f, 0, Double.MAX_VALUE);
            solarBeamCost = builder.comment("Cost in minutes of using the solar beam ability.")
                    .translation(LANG_PREFIX + "solar_beam_cost")
                    .defineInRange("solar_beam_cost", 5, 0, Integer.MAX_VALUE);
            builder.pop();
            
            supernovaCost = builder.comment("Cost in minutes of using the supernova ability.")
                    .translation(LANG_PREFIX + "supernova_cost")
                    .defineInRange("supernova_cost", 60, 0, Integer.MAX_VALUE);
        }

        public final ModConfigSpec.DoubleValue sunsBlessingAttackMultiplier;

        public final ModConfigSpec.IntValue effectDuration;

        public final ModConfigSpec.IntValue solarBeamCost;

        public final ModConfigSpec.IntValue supernovaCost;
    }

    public static class SculptorStaff {
        SculptorStaff(final ModConfigSpec.Builder builder) {
            builder.push("sculptor_staff");
            toolConfig = new ToolConfig(builder, 3, 1f);
            attackMultiplier = builder.comment("Multiply all damage done with the Sculptor Staff by this amount.")
                    .translation(LANG_PREFIX + "attack_multiplier")
                    .defineInRange("attack_multiplier", 1f, 0d, Double.MAX_VALUE);
            builder.pop();
        }
        public final ModConfigSpec.DoubleValue attackMultiplier;

        public final ToolConfig toolConfig;
    }

    public static class ElokosaPaw {
        ElokosaPaw(final ModConfigSpec.Builder builder) {
            builder.push("elokosa_paw");
            effectDuration = builder.comment("How long effects inflicted by Elokosa Paws last in ticks")
                    .translation(LANG_PREFIX + "effect_duration")
                    .defineInRange("effect_duration", 20 * 30, 1, Integer.MAX_VALUE);
            cooldown = builder.comment("How long players must wait between uses of Elokosa Paws in ticks")
                    .translation(LANG_PREFIX + "cooldown")
                    .defineInRange("cooldown", 20 * 90, 0, Integer.MAX_VALUE);
            numberOfUses = builder.comment("How many times players can use an Elokosa Paw before it breaks")
                    .translation(LANG_PREFIX + "number_of_uses")
                    .defineInRange("number_of_uses", 3, 1, Integer.MAX_VALUE);
            builder.pop();
        }
        
        public final ModConfigSpec.IntValue effectDuration;
        
        public final ModConfigSpec.IntValue cooldown;
        
        public final ModConfigSpec.IntValue numberOfUses;
    }

    public static class Mobs {
        Mobs(final ModConfigSpec.Builder builder) {
            builder.push("mobs");
            FROSTMAW = new Frostmaw(builder);
            UMVUTHI = new Umvuthi(builder);
            FERROUS_WROUGHTNAUT = new FerrousWroughtnaut(builder);
            SCULPTOR = new Sculptor(builder);
            GROTTOL = new Grottol(builder);
            LANTERN = new Lantern(builder);
            UMVUTHANA = new Umvuthana(builder);
            NAGA = new Naga(builder);
            FOLIAATH = new Foliaath(builder);
            BLUFF = new Bluff(builder);
            ELOKOSA = new Elokosa(builder);
            builder.pop();
        }

        public final Frostmaw FROSTMAW;

        public final Umvuthi UMVUTHI;

        public final FerrousWroughtnaut FERROUS_WROUGHTNAUT;

        public final Sculptor SCULPTOR;

        public final Grottol GROTTOL;

        public final Lantern LANTERN;

        public final Umvuthana UMVUTHANA;

        public final Naga NAGA;

        public final Foliaath FOLIAATH;

        public final Bluff BLUFF;

        public final Elokosa ELOKOSA;
    }

    public static class ToolsAndAbilities {
        ToolsAndAbilities(final ModConfigSpec.Builder builder) {
            builder.push("tools_and_abilities");
            SUNS_BLESSING = new SunsBlessing(builder);
            WROUGHT_HELM = new WroughtHelm(builder);
            AXE_OF_A_THOUSAND_METALS = new AxeOfAThousandMetals(builder);
            SOL_VISAGE = new SolVisage(builder);
            ICE_CRYSTAL = new IceCrystal(builder);
            UMVUTHANA_MASK = new UmvuthanaMask(builder);
            GEOMANCER_ARMOR = new GeomancerArmor(builder);
            SPEAR = new Spear(builder);
            NAGA_FANG_DAGGER = new NagaFangDagger(builder);
            BLOW_GUN = new Blowgun(builder);
            EARTHREND_GAUNTLET = new EarthrendGauntlet(builder);
            SCULPTOR_STAFF = new SculptorStaff(builder);
            ELOKOSA_PAW = new ElokosaPaw(builder);
            builder.pop();
        }

        public final SunsBlessing SUNS_BLESSING;

        public final WroughtHelm WROUGHT_HELM;

        public final AxeOfAThousandMetals AXE_OF_A_THOUSAND_METALS;

        public final SolVisage SOL_VISAGE;

        public final IceCrystal ICE_CRYSTAL;

        public final UmvuthanaMask UMVUTHANA_MASK;

        public final GeomancerArmor GEOMANCER_ARMOR;

        public final Spear SPEAR;

        public final NagaFangDagger NAGA_FANG_DAGGER;

        public final Blowgun BLOW_GUN;

        public final EarthrendGauntlet EARTHREND_GAUNTLET;

        public final SculptorStaff SCULPTOR_STAFF;

        public final ElokosaPaw ELOKOSA_PAW;
    }

    public static class Client {

        private Client(final ModConfigSpec.Builder builder) {
            builder.push("client");
            this.glowEffect = builder.comment("Toggles the lantern glow effect, which may look bad with certain shaders.")
                    .translation(LANG_PREFIX + "glow_effect")
                    .define("glow_effect", true);
            this.umvuthanaFootprints = builder.comment("Toggles the Umvuthana footprint effects, which may decrease performance.")
                    .translation(LANG_PREFIX + "umvuthana_footprints")
                    .define("umvuthana_footprints", true);
            this.doCameraShakes = builder.comment("Enable camera shaking during certain mob attacks and abilities.")
                    .translation(LANG_PREFIX + "do_camera_shake")
                    .define("do_camera_shake", true);
            this.playBossMusic = builder.comment("Play boss battle themes during boss encounters.")
                    .translation(LANG_PREFIX + "play_boss_music")
                    .define("play_boss_music", true);
            this.customBossBars = builder.comment("Use custom boss health bar textures, if the boss has them.")
                    .translation(LANG_PREFIX + "custom_boss_bar")
                    .define("custom_boss_bar", true);
            this.customPlayerAnims = builder.comment("Use custom player animations.")
                    .translation(LANG_PREFIX + "custom_player_anims")
                    .define("custom_player_anims", true);
            this.hidePlayerAnimsInFirstPerson = builder.comment("Set to true to hide your own 3rd-person player animations while you are in 1st-person view mode. This is useful with mods that render the 3rd-person model in 1st-person, as some of the Mowzie's Mobs 3rd-person animations can block the camera.")
                    .translation(LANG_PREFIX + "hide_player_anims_in_first_person")
                    .define("hide_player_anims_in_first_person", false);
            this.doUmvuthanaCraneHealSound = builder.comment("Play Umvuthana Crane heal sounds. Turn this off if you are experiencing crashes when Cranes appear during Umvuthi's boss battle.")
                    .translation(LANG_PREFIX + "crane_heal_sounds")
                    .define("crane_heal_sounds", true);
            builder.pop();
        }

        public final ModConfigSpec.BooleanValue glowEffect;

        public final ModConfigSpec.BooleanValue umvuthanaFootprints;

        public final ModConfigSpec.BooleanValue doCameraShakes;

        public final ModConfigSpec.BooleanValue playBossMusic;

        public final ModConfigSpec.BooleanValue customBossBars;

        public final ModConfigSpec.BooleanValue customPlayerAnims;

        public final ModConfigSpec.BooleanValue hidePlayerAnimsInFirstPerson;

        public final ModConfigSpec.BooleanValue doUmvuthanaCraneHealSound;
    }

    public static class Common {
        private Common(final ModConfigSpec.Builder builder) {
            TOOLS_AND_ABILITIES = new ToolsAndAbilities(builder);
            MOBS = new Mobs(builder);
        }

        public final ToolsAndAbilities TOOLS_AND_ABILITIES;

        public final Mobs MOBS;
    }

    private static String inverted(Object object) {
        return "!" + string(object);
    }

    private static String string(Object object) {
        if (object instanceof TagKey<?> tag) {
            return tag.location().toString();
        }

        if (object instanceof Holder<?> holder) {
            return holder.getRegisteredName();
        }

        if (object instanceof ResourceKey<?> key) {
            return key.location().toString();
        }

        if (object instanceof Item item) {
            return string(item.builtInRegistryHolder());
        }

        if (object instanceof Block block) {
            return string(block.builtInRegistryHolder());
        }

        throw new IllegalArgumentException("Cannot handle object type [" + object.getClass() + "]");
    }
}
