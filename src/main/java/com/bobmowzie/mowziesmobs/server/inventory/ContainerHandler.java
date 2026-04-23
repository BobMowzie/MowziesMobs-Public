package com.bobmowzie.mowziesmobs.server.inventory;

import com.bobmowzie.mowziesmobs.MMCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ContainerHandler {
    public static final DeferredRegister<MenuType<?>> REG = DeferredRegister.create(Registries.MENU, MMCommon.MODID);
    
    public static final MenuType<ContainerUmvuthanaTrade> UMVUTHANA_TRADE = new MenuType<>(ContainerUmvuthanaTrade::new, FeatureFlags.VANILLA_SET);
    public static final MenuType<ContainerUmvuthiTrade> UMVUTHI_TRADE = new MenuType<>(ContainerUmvuthiTrade::new, FeatureFlags.VANILLA_SET);
    public static final MenuType<ContainerSculptorTrade> SCULPTOR_TRADE = new MenuType<>(ContainerSculptorTrade::new, FeatureFlags.VANILLA_SET);
    
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerUmvuthanaTrade>> CONTAINER_UMVUTHANA_TRADE = REG.register("umvuthana_trade", () -> UMVUTHANA_TRADE);
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerUmvuthiTrade>> CONTAINER_UMVUTHI_TRADE = REG.register("umvuthi_trade", () -> UMVUTHI_TRADE);
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerSculptorTrade>> CONTAINER_SCULPTOR_TRADE = REG.register("sculptor_trade", () -> SCULPTOR_TRADE);
}
