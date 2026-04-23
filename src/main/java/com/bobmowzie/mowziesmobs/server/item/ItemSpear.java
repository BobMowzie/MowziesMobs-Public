package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.MMCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ItemSpear extends DiggerItem {
    public static final ResourceLocation SPEAR_REACH_ID = ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "base_attack_damage");

    public ItemSpear(Item.Properties properties) {
        super(Tiers.STONE, BlockTags.MINEABLE_WITH_HOE, properties);
    }

    public boolean canAttackBlock(BlockState p_43291_, Level p_43292_, BlockPos p_43293_, Player p_43294_) {
        return !p_43294_.isCreative();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable(getDescriptionId() + ".text.0").setStyle(ItemHandler.TOOLTIP_STYLE));
        tooltip.add(Component.translatable(getDescriptionId() + ".text.1").setStyle(ItemHandler.TOOLTIP_STYLE));
    }
}
