package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.client.render.item.RenderGeomancerArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class ItemGeomancerArmor extends ArmorItem implements GeoItem {
    public String controllerName = "controller";
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ItemGeomancerArmor(Type slot, Properties builderIn) {
        super(MaterialHandler.GEOMANCER_ARMOR_MATERIAL, slot, builderIn);
    }

    private PlayState predicate(AnimationState<ItemGeomancerArmor> state) {
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, controllerName, 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable(getDescriptionId() + ".text.0").setStyle(ItemHandler.TOOLTIP_STYLE));
    }

    public static class ClientExtensions implements IClientItemExtensions {
        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot
        equipmentSlot, HumanoidModel<?> original) {
            if (this.armorRenderer == null)
                this.armorRenderer = new RenderGeomancerArmor();
            armorRenderer.prepForRender(entityLiving, itemStack, equipmentSlot, original);
            return armorRenderer;
        }
        private GeoArmorRenderer<?> armorRenderer;
    }
}
