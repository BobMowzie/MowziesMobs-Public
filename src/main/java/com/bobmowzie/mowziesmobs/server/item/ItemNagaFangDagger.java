package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ItemNagaFangDagger extends DiggerItem {
    public ItemNagaFangDagger(Item.Properties properties) {
        super(Tiers.STONE, BlockTags.MINEABLE_WITH_HOE, properties);
    }

    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override // Enchantment table
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.SWEEPING_EDGE)) {
            return false;
        }

        return super.isPrimaryItemFor(stack, enchantment);
    }

    @Override // Anvil / Enchant command / Enchant randomly function
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.SWEEPING_EDGE)) {
            return false;
        }

        return super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (super.hurtEnemy(stack, target, attacker)) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, ConfigHandler.COMMON.TOOLS_AND_ABILITIES.NAGA_FANG_DAGGER.poisonDuration.get(), 3, false, true));
            return true;
        }
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        Item item = repair.getItem();
        return item instanceof ItemNagaFang;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable(getDescriptionId() + ".text.0").setStyle(ItemHandler.TOOLTIP_STYLE));
        tooltip.add(Component.translatable(getDescriptionId() + ".text.1").setStyle(ItemHandler.TOOLTIP_STYLE));
    }
}
