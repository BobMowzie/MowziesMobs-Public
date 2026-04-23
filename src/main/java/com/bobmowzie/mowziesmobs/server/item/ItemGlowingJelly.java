package com.bobmowzie.mowziesmobs.server.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Created by BobMowzie on 7/30/2018.
 */

public class ItemGlowingJelly extends Item {
    public static FoodProperties GLOWING_JELLY_FOOD = (new FoodProperties.Builder().nutrition(1).saturationModifier(0.1f).effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0), 1.0f)).build();

    public ItemGlowingJelly(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable(getDescriptionId() + ".text.0").setStyle(ItemHandler.TOOLTIP_STYLE));
    }
}
