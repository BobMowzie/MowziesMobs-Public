package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.server.ability.AbilityHandler;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Created by BobMowzie on 6/6/2017.
 */
public class ItemIceCrystal extends Item {
    public ItemIceCrystal(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand handIn) {
        ItemStack stack = player.getItemInHand(handIn);
        player.startUsingItem(handIn);
        if (stack.getDamageValue() + 5 < stack.getMaxDamage() || ConfigHandler.COMMON.TOOLS_AND_ABILITIES.ICE_CRYSTAL.breakable.get()) {
            if (!worldIn.isClientSide()) AbilityHandler.INSTANCE.sendAbilityMessage(player, AbilityHandler.ICE_BREATH_ABILITY);
            stack.hurtAndBreak(5, player, LivingEntity.getSlotForHand(handIn));
            player.startUsingItem(handIn);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(handIn));
        } else {
            DataHandler.getData(player, DataHandler.ABILITY_DATA).getAbilityMap().get(AbilityHandler.ICE_BREATH_ABILITY).end();
        }
        return super.use(worldIn, player, handIn);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
//        if (entityLiving instanceof Player) {
//            Ability<?>iceBreathAbility = AbilityHandler.INSTANCE.getAbility(entityLiving, AbilityHandler.ICE_BREATH_ABILITY);
//            if (iceBreathAbility != null && iceBreathAbility.isUsing()) {
//                iceBreathAbility.end();
//            }
//        }
        super.releaseUsing(stack, worldIn, entityLiving, timeLeft);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHandler.COMMON.TOOLS_AND_ABILITIES.ICE_CRYSTAL.durability.get();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable(getDescriptionId() + ".text.0").setStyle(ItemHandler.TOOLTIP_STYLE));
        if (!ConfigHandler.COMMON.TOOLS_AND_ABILITIES.ICE_CRYSTAL.breakable.get()) {
            tooltip.add(Component.translatable(getDescriptionId() + ".text.1").setStyle(ItemHandler.TOOLTIP_STYLE));
        }
    }
}
