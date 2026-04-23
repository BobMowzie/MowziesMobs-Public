package com.bobmowzie.mowziesmobs.server.potion;

import com.bobmowzie.mowziesmobs.server.block.ICopiedBlockProperties;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import com.bobmowzie.mowziesmobs.server.tag.TagHandler;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EffectGeomancy extends MowzieEffect {
    public EffectGeomancy() {
        super(MobEffectCategory.BENEFICIAL, 0xCDFF78);
    }

    public static boolean isBlockUseable(BlockState blockState) {
        return checkBlock(blockState, TagHandler.GEOMANCY_USEABLE);
    }

    public static boolean checkBlock(BlockState blockState, TagKey<Block> blockTag) {
        if (blockState.is(blockTag)) return true;

        ICopiedBlockProperties properties = (ICopiedBlockProperties) blockState.getBlock().properties;
        Block baseBlock = properties.getBaseBlock();
        if (baseBlock != null) {
            return baseBlock.defaultBlockState().is(blockTag);
        }

        return false;
    }

    public static boolean canUse(LivingEntity entity) {
        return (entity.getMainHandItem().is(ItemHandler.EARTHREND_GAUNTLET.get()) ||
                entity.getMainHandItem().isEmpty() ||
                entity.getOffhandItem().is(ItemHandler.EARTHREND_GAUNTLET.get()))
                && entity.hasEffect(EffectHandler.GEOMANCY.get());
    }
}
