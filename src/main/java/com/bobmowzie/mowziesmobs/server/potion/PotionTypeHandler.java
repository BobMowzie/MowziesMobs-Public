package com.bobmowzie.mowziesmobs.server.potion;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Created by BobMowzie on 1/10/2019.
 */
public final class PotionTypeHandler {
    private PotionTypeHandler() {}
    
	public static final DeferredRegister<Potion> REG = DeferredRegister.create(Registries.POTION, MMCommon.MODID);

    public static final DeferredHolder<Potion, Potion> POISON_RESIST = REG.register("poison_resist", () -> new Potion("poison_resist", new MobEffectInstance(EffectHandler.POISON_RESIST, 3600)));
    public static final DeferredHolder<Potion, Potion> LONG_POISON_RESIST = REG.register("long_poison_resist", () -> new Potion("long_poison_resist", new MobEffectInstance(EffectHandler.POISON_RESIST, 9600)));
    
    public static void addMixes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.AWKWARD, ItemHandler.NAGA_FANG.get(), POISON_RESIST);
        event.getBuilder().addMix(POISON_RESIST, Items.REDSTONE, LONG_POISON_RESIST);
    }
}
