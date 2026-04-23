package com.bobmowzie.mowziesmobs.server.creativetab;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTabHandler {
    public static final DeferredRegister<CreativeModeTab> REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MMCommon.MODID);

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = REG.register("mowziesmobs_tab", () -> CreativeModeTab.builder()
            .icon(() -> ItemHandler.LOGO.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.mowziesmobs.creativeTab"))
            .displayItems((displayParams, output) -> {
                for (DeferredHolder<Item, ? extends Item> item : ItemHandler.REG.getEntries()) {
                    if (item == ItemHandler.LOGO) continue;
                    output.accept(item.get());
                }
            })
            .build());

    public static void register(IEventBus eventBus) {
        REG.register(eventBus);
    }
}
