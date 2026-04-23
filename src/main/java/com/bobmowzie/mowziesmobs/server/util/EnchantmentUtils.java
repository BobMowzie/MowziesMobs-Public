package com.bobmowzie.mowziesmobs.server.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnchantmentUtils {
    public static int getLevel(@NotNull final ResourceKey<Enchantment> enchantment, @NotNull final LivingEntity entity) {
        return entity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(enchantment).map(reference -> EnchantmentHelper.getEnchantmentLevel(reference, entity)).orElse(0);
    }

    public static int getLevel(@NotNull final ResourceKey<Enchantment> enchantment, @NotNull final Level level, @NotNull final ItemStack stack) {
        return level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(enchantment).map(stack::getEnchantmentLevel).orElse(0);
    }

    public static @Nullable Holder.Reference<Enchantment> getHolder(final ResourceKey<Enchantment> enchantment) {
        var lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
        return lookup == null ? null : lookup.getOrThrow(enchantment);
    }
}
