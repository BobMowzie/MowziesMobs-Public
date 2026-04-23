package com.bobmowzie.mowziesmobs;

import com.bobmowzie.mowziesmobs.client.ClientLayerRegistry;
import com.bobmowzie.mowziesmobs.client.MMModels;
import com.bobmowzie.mowziesmobs.client.render.entity.FrozenRenderHandler;
import com.bobmowzie.mowziesmobs.server.ability.AbilityClientEventHandler;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.item.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = MMCommon.MODID, dist = Dist.CLIENT)
public class MMClient {
    public MMClient(IEventBus modBus, ModContainer container) {
        modBus.register(MMModels.class);
        modBus.addListener(ClientLayerRegistry::onAddLayers);
        modBus.addListener(this::init);
        modBus.addListener(this::registerClientExtensions);

        NeoForge.EVENT_BUS.addListener(FrozenRenderHandler::onRenderHand);
        NeoForge.EVENT_BUS.addListener(AbilityClientEventHandler::onRenderTick);

        container.registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_CONFIG);
    }

    private void init(FMLLoadCompleteEvent event) {
        ItemPropertyFunction pulling = ItemProperties.getProperty(Items.BOW.getDefaultInstance(), ResourceLocation.withDefaultNamespace("pulling"));
        ItemProperties.register(ItemHandler.BLOWGUN.get().asItem(), ResourceLocation.withDefaultNamespace("pulling"), pulling);
    }

    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(ItemWroughtHelm.ArmorRender.INSTANCE, ItemHandler.WROUGHT_HELMET);
        event.registerItem(new ItemSolVisage.ClientExtensions(), ItemHandler.SOL_VISAGE);
        event.registerItem(new ItemEarthrendGauntlet.ClientExtensions(), ItemHandler.EARTHREND_GAUNTLET);
        event.registerItem(new ItemGeomancerArmor.ClientExtensions(), ItemHandler.GEOMANCER_BEADS, ItemHandler.GEOMANCER_BELT, ItemHandler.GEOMANCER_ROBE, ItemHandler.GEOMANCER_SANDALS);
        event.registerItem(new ItemUmvuthanaMask.ClientExtensions(), ItemHandler.UMVUTHANA_MASK_FURY, ItemHandler.UMVUTHANA_MASK_FEAR, ItemHandler.UMVUTHANA_MASK_RAGE, ItemHandler.UMVUTHANA_MASK_BLISS, ItemHandler.UMVUTHANA_MASK_MISERY, ItemHandler.UMVUTHANA_MASK_FAITH);
        event.registerItem(new ItemSculptorStaff.ClientExtensions(), ItemHandler.SCULPTOR_STAFF);
    }
}
