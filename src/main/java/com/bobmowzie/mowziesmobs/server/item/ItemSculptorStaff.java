package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.client.render.item.RenderSculptorStaff;
import com.bobmowzie.mowziesmobs.server.ability.AbilityHandler;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * Created by BobMowzie on 6/6/2017.
 */
public class ItemSculptorStaff extends DiggerItem implements GeoItem {
    public static final String CONTROLLER_NAME = "controller";
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation DISAPPEAR_ANIM = RawAnimation.begin().thenPlayAndHold("disappear");
    public static final String DISAPPEAR_ANIM_NAME = "disappear";

    public ItemSculptorStaff(Properties properties) {
        super(Tiers.STONE, BlockTags.MINEABLE_WITH_HOE, properties);

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public boolean isValidRepairItem(ItemStack tool, ItemStack ingredient) {
        return ingredient.is(ItemHandler.BLUFF_ROD.get());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        AbilityHandler.INSTANCE.sendAbilityMessage(player, AbilityHandler.ROCK_SLING);
        player.startUsingItem(hand);
        if (!player.getAbilities().instabuild) player.getItemInHand(hand).hurtAndBreak(2, player, LivingEntity.getSlotForHand(hand));
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable(getDescriptionId() + ".text.0").setStyle(ItemHandler.TOOLTIP_STYLE));
        tooltip.add(Component.translatable(getDescriptionId() + ".text.1").setStyle(ItemHandler.TOOLTIP_STYLE));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER_NAME, 0, state -> PlayState.STOP)
                .triggerableAnim(DISAPPEAR_ANIM_NAME, DISAPPEAR_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static class ClientExtensions implements IClientItemExtensions {
        private final BlockEntityWithoutLevelRenderer renderer = new RenderSculptorStaff();

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return renderer;
        }
    }
}
