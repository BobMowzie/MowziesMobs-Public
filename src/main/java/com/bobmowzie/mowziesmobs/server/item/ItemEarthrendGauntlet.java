package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.client.render.item.RenderEarthrendGauntlet;
import com.bobmowzie.mowziesmobs.server.ability.AbilityHandler;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * Created by BobMowzie on 6/6/2017.
 */
public class ItemEarthrendGauntlet extends DiggerItem implements GeoItem {
    public static final String CONTROLLER_NAME = "controller";
    public static final String CONTROLLER_IDLE_NAME = "controller_idle";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenLoop("open");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    public static final String IDLE_ANIM_NAME = "idle";
    public static final String OPEN_ANIM_NAME = "open";
    public static final String ATTACK_ANIM_NAME = "attack";

    public ItemEarthrendGauntlet(Properties properties) {
        super(Tiers.STONE, BlockTags.MINEABLE_WITH_PICKAXE, properties);

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand handIn) {
        ItemStack stack = player.getItemInHand(handIn);
        player.startUsingItem(handIn);
        if (stack.getDamageValue() + 5 < stack.getMaxDamage() || ConfigHandler.COMMON.TOOLS_AND_ABILITIES.EARTHREND_GAUNTLET.breakable.get()) {
            if (!worldIn.isClientSide()) AbilityHandler.INSTANCE.sendAbilityMessage(player, AbilityHandler.TUNNELING_ABILITY);
            player.startUsingItem(handIn);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(handIn));
        }
        else {
            DataHandler.getData(player, DataHandler.ABILITY_DATA).getAbilityMap().get(AbilityHandler.TUNNELING_ABILITY).end();
        }
        return super.use(worldIn, player, handIn);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 72000;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ConfigHandler.COMMON.TOOLS_AND_ABILITIES.EARTHREND_GAUNTLET.durability.get();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable(getDescriptionId() + ".text.0").setStyle(ItemHandler.TOOLTIP_STYLE));
        if (ConfigHandler.COMMON.TOOLS_AND_ABILITIES.EARTHREND_GAUNTLET.enableTunneling.get()) {
            tooltip.add(Component.translatable(getDescriptionId() + ".text.1").setStyle(ItemHandler.TOOLTIP_STYLE));
        }
        tooltip.add(Component.translatable(getDescriptionId() + ".text.2").setStyle(ItemHandler.TOOLTIP_STYLE));
        if (!ConfigHandler.COMMON.TOOLS_AND_ABILITIES.EARTHREND_GAUNTLET.breakable.get()) {
            tooltip.add(Component.translatable(getDescriptionId() + ".text.3").setStyle(ItemHandler.TOOLTIP_STYLE));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER_IDLE_NAME, 3, this::predicateIdle));
        controllers.add(new AnimationController<>(this, CONTROLLER_NAME, 3, state -> PlayState.STOP)
                .triggerableAnim(IDLE_ANIM_NAME, IDLE_ANIM)
                .triggerableAnim(OPEN_ANIM_NAME, OPEN_ANIM)
                .triggerableAnim(ATTACK_ANIM_NAME, ATTACK_ANIM));
    }

    public <P extends Item & GeoItem> PlayState predicateIdle(AnimationState<P> event) {
        event.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (DataHandler.getData(entity, DataHandler.ABILITY_DATA).getActiveAbility() == null) {
            if (entity.getUseItem() != stack) {
                if (entity.level() instanceof ServerLevel) {
                    triggerAnim(entity, GeoItem.getOrAssignId(stack, (ServerLevel) entity.level()), CONTROLLER_NAME, ATTACK_ANIM_NAME);
                }
            }
        }
        return super.onEntitySwing(stack, entity, hand);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (player.getUseItem() != stack) {
            if (entity.level() instanceof ServerLevel) {
                triggerAnim(entity, GeoItem.getOrAssignId(stack, (ServerLevel) entity.level()), CONTROLLER_NAME, ATTACK_ANIM_NAME);
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static class ClientExtensions implements IClientItemExtensions {
        private final BlockEntityWithoutLevelRenderer renderer = new RenderEarthrendGauntlet();

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return renderer;
        }
    }
}
