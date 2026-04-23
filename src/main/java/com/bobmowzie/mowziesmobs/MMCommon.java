package com.bobmowzie.mowziesmobs;

import com.bobmowzie.mowziesmobs.client.ClientProxy;
import com.bobmowzie.mowziesmobs.client.model.tools.MowzieModelFactory;
import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.server.ServerEventHandler;
import com.bobmowzie.mowziesmobs.server.ServerProxy;
import com.bobmowzie.mowziesmobs.server.ability.AbilityCommonEventHandler;
import com.bobmowzie.mowziesmobs.server.advancement.AdvancementHandler;
import com.bobmowzie.mowziesmobs.server.block.BlockHandler;
import com.bobmowzie.mowziesmobs.server.block.entity.BlockEntityHandler;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.creativetab.CreativeTabHandler;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.inventory.ContainerHandler;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import com.bobmowzie.mowziesmobs.server.item.MaterialHandler;
import com.bobmowzie.mowziesmobs.server.loot.LootTableHandler;
import com.bobmowzie.mowziesmobs.server.potion.EffectHandler;
import com.bobmowzie.mowziesmobs.server.potion.PotionTypeHandler;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import com.bobmowzie.mowziesmobs.server.world.BiomeModifiersHandler;
import com.bobmowzie.mowziesmobs.server.world.feature.structure.StructureTypeHandler;
import com.bobmowzie.mowziesmobs.server.world.feature.structure.jigsaw.JigsawHandler;
import com.bobmowzie.mowziesmobs.server.world.feature.structure.processor.ProcessorHandler;
import com.bobmowzie.mowziesmobs.server.world.spawn.SpawnHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.stream.Stream;

@Mod(MMCommon.MODID)
public final class MMCommon {
    public static final String MODID = "mowziesmobs";
    public static final Logger LOGGER = LogManager.getLogger();
    public static ServerProxy PROXY;

    public MMCommon(IEventBus modBus, ModContainer container) {
        GeckoLibUtil.addCustomBakedModelFactory(MODID, new MowzieModelFactory());

        PROXY = FMLLoader.getDist().isClient() ? new ClientProxy() : new ServerProxy();
        BlockHandler.REG.register(modBus);
        EntityHandler.REG.register(modBus);
        EntityHandler.SERIALIZER_REG.register(modBus);
        MaterialHandler.MM_ARMOR_MATERIALS.register(modBus);
        ItemHandler.REG.register(modBus);
        MMSounds.REG.register(modBus);
        BlockEntityHandler.REG.register(modBus);
        ParticleHandler.REG.register(modBus);
        JigsawHandler.MM_STRUCTURE_POOLS.register(modBus);
        ProcessorHandler.MM_STRUCTURE_PROCESSORS.register(modBus);
        StructureTypeHandler.STRUCTURE_TYPE_REG.register(modBus);
        StructureTypeHandler.STRUCTURE_PIECE_TYPE_REG.register(modBus);
        ContainerHandler.REG.register(modBus);
        EffectHandler.REG.register(modBus);
        PotionTypeHandler.REG.register(modBus);
        BiomeModifiersHandler.REG.register(modBus);
        LootTableHandler.LOOT_CONDITION_TYPE_REG.register(modBus);
        LootTableHandler.LOOT_FUNCTION_TYPE_REG.register(modBus);
        AdvancementHandler.MM_TRIGGERS.register(modBus);
        DataHandler.MM_ATTACHMENT_TYPES.register(modBus);
        CreativeTabHandler.register(modBus);

        PROXY.init();
        modBus.addListener(this::handleLoadComplete);
        modBus.addListener(this::onModConfigEvent);
        modBus.addListener(SpawnHandler::registerSpawnPlacementTypes);
        modBus.addListener(ItemHandler::modifyComponents);

        NeoForge.EVENT_BUS.register(new ServerEventHandler());
        NeoForge.EVENT_BUS.register(new AbilityCommonEventHandler());
        NeoForge.EVENT_BUS.addListener(PotionTypeHandler::addMixes);

        container.registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_CONFIG);
    }

    public void onModConfigEvent(final ModConfigEvent event) {
        final ModConfig config = event.getConfig();
        // Rebake the configs when they change
        if (config.getSpec() == ConfigHandler.COMMON_CONFIG) {
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.AXE_OF_A_THOUSAND_METALS.toolConfig.attackDamageValue = 
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.AXE_OF_A_THOUSAND_METALS.toolConfig.attackDamage.get().floatValue();
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.AXE_OF_A_THOUSAND_METALS.toolConfig.attackSpeedValue = 
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.AXE_OF_A_THOUSAND_METALS.toolConfig.attackSpeed.get().floatValue();
        	
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SPEAR.toolConfig.attackDamageValue = 
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SPEAR.toolConfig.attackDamage.get().floatValue();
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SPEAR.toolConfig.attackSpeedValue = 
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SPEAR.toolConfig.attackSpeed.get().floatValue();
        	
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.NAGA_FANG_DAGGER.toolConfig.attackDamageValue = 
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.NAGA_FANG_DAGGER.toolConfig.attackDamage.get().floatValue();
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.NAGA_FANG_DAGGER.toolConfig.attackSpeedValue = 
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.NAGA_FANG_DAGGER.toolConfig.attackSpeed.get().floatValue();     
        	
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.EARTHREND_GAUNTLET.toolConfig.attackDamageValue =
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.EARTHREND_GAUNTLET.toolConfig.attackDamage.get().floatValue();
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.EARTHREND_GAUNTLET.toolConfig.attackSpeedValue =
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.EARTHREND_GAUNTLET.toolConfig.attackSpeed.get().floatValue();
        	
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.ICE_CRYSTAL.durabilityValue = 
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.ICE_CRYSTAL.durability.get();
        	
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.EARTHREND_GAUNTLET.durabilityValue =
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.EARTHREND_GAUNTLET.durability.get();
        	
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.WROUGHT_HELM.armorConfig.damageReductionMultiplierValue =
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.WROUGHT_HELM.armorConfig.damageReductionMultiplier.get().floatValue();
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.WROUGHT_HELM.armorConfig.toughnessMultiplierValue =
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.WROUGHT_HELM.armorConfig.toughnessMultiplier.get().floatValue();
        	
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.UMVUTHANA_MASK.armorConfig.damageReductionMultiplierValue =
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.UMVUTHANA_MASK.armorConfig.damageReductionMultiplier.get().floatValue();
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.UMVUTHANA_MASK.armorConfig.toughnessMultiplierValue =
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.UMVUTHANA_MASK.armorConfig.toughnessMultiplier.get().floatValue();
        	
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SOL_VISAGE.armorConfig.damageReductionMultiplierValue =
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SOL_VISAGE.armorConfig.damageReductionMultiplier.get().floatValue();
        	ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SOL_VISAGE.armorConfig.toughnessMultiplierValue =
        			ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SOL_VISAGE.armorConfig.toughnessMultiplier.get().floatValue();

            ConfigHandler.COMMON.TOOLS_AND_ABILITIES.GEOMANCER_ARMOR.armorConfig.damageReductionMultiplierValue =
                    ConfigHandler.COMMON.TOOLS_AND_ABILITIES.GEOMANCER_ARMOR.armorConfig.damageReductionMultiplier.get().floatValue();
            ConfigHandler.COMMON.TOOLS_AND_ABILITIES.GEOMANCER_ARMOR.armorConfig.toughnessMultiplierValue =
                    ConfigHandler.COMMON.TOOLS_AND_ABILITIES.GEOMANCER_ARMOR.armorConfig.toughnessMultiplier.get().floatValue();
        }
    }

    private void handleLoadComplete(FMLLoadCompleteEvent event) {
        ItemHandler.initializeDispenserBehaviors();
        BlockHandler.init();
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static Stream<EntityType<? extends LivingEntity>> getLivingEntityTypes() {
        // Logic to collect living entity types is from EntityAttributeModificationEvent
        return BuiltInRegistries.ENTITY_TYPE.stream().filter(DefaultAttributes::hasSupplier).map(entityType -> (EntityType<? extends LivingEntity>) entityType);
    }
}
