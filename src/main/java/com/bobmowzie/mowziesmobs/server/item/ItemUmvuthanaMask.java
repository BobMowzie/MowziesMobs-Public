package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.client.render.item.RenderUmvuthanaMaskArmor;
import com.bobmowzie.mowziesmobs.client.render.item.RenderUmvuthanaMaskItem;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthana;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthanaCraneToPlayer;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.EntityUmvuthanaFollowerToPlayer;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.MaskType;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;

public class ItemUmvuthanaMask extends ArmorItem implements UmvuthanaMask, GeoItem {
    private final MaskType type;

    public String controllerName = "controller";
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ItemUmvuthanaMask(MaskType type, Item.Properties properties) {
        super(MaterialHandler.UMVUTHANA_MASK_MATERIAL, Type.HELMET, properties);
        this.type = type;
    }

    public Holder<MobEffect> getPotion() {
        return type.potion;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack materialItemStack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack headStack = player.getInventory().armor.get(3);
        if (headStack.getItem() instanceof ItemSolVisage) {
            if (ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SOL_VISAGE.breakable.get() && !player.isCreative()) headStack.hurtAndBreak(2, player, LivingEntity.getSlotForHand(hand));
            boolean didSpawn = spawnUmvuthana(type, stack, player,(float)stack.getDamageValue() / (float)stack.getMaxDamage());
            if (didSpawn) {
                if (!player.isCreative()) stack.shrink(1);
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            }
        }
        return super.use(world, player, hand);
    }

    private boolean spawnUmvuthana(MaskType mask, ItemStack stack, Player player, float durability) {
        if (DataHandler.getData(player, DataHandler.PLAYER_DATA).getPackSize() < ConfigHandler.COMMON.TOOLS_AND_ABILITIES.SOL_VISAGE.maxFollowers.get()) {
            player.playSound(MMSounds.ENTITY_UMVUTHI_BELLY.get(), 1.5f, 1);
            player.playSound(MMSounds.ENTITY_UMVUTHANA_BLOWDART.get(), 1.5f, 0.5f);
            double angle = player.getYHeadRot();
            if (angle < 0) {
                angle = angle + 360;
            }
            EntityUmvuthanaFollowerToPlayer umvuthana;
            if (mask == MaskType.FAITH) umvuthana = new EntityUmvuthanaCraneToPlayer(EntityHandler.UMVUTHANA_CRANE_TO_PLAYER.get(), player.level(), player);
            else umvuthana = new EntityUmvuthanaFollowerToPlayer(EntityHandler.UMVUTHANA_FOLLOWER_TO_PLAYER.get(), player.level(), player);
//            property.addPackMember(umvuthana);
            if (!player.level().isClientSide) {
                if (mask != MaskType.FAITH) {
                    int weapon;
                    if (mask != MaskType.FURY) weapon = umvuthana.randomizeWeapon();
                    else weapon = 0;
                    umvuthana.setWeapon(weapon);
                }
                umvuthana.absMoveTo(player.getX() + 1 * Math.sin(-angle * (Math.PI / 180)), player.getY() + 1.5, player.getZ() + 1 * Math.cos(-angle * (Math.PI / 180)), (float) angle, 0);
                umvuthana.setActive(false);
                umvuthana.active = false;
                player.level().addFreshEntity(umvuthana);
                double vx = 0.5 * Math.sin(-angle * Math.PI / 180);
                double vy = 0.5;
                double vz = 0.5 * Math.cos(-angle * Math.PI / 180);
                umvuthana.setDeltaMovement(vx, vy, vz);
                umvuthana.setHealth((1.0f - durability) * umvuthana.getMaxHealth());
                umvuthana.setMask(mask);
                umvuthana.setStoredMask(stack.copy());
                if (stack.has(DataComponents.CUSTOM_NAME))
                    umvuthana.setCustomName(stack.getHoverName());
            }
            return true;
        }
        return false;
    }

    public MaskType getMaskType() {
        return type;
    }

    @Override
    public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "textures/item/umvuthana_mask_" + this.type.name + ".png");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable(getDescriptionId() + ".text.0").setStyle(ItemHandler.TOOLTIP_STYLE));
        tooltip.add(Component.translatable(getDescriptionId() + ".text.1").setStyle(ItemHandler.TOOLTIP_STYLE));
    }

    private static final RawAnimation UMVUTHANA_ANIM = RawAnimation.begin().thenLoop("umvuthana");
    private static final RawAnimation PLAYER_ANIM = RawAnimation.begin().thenLoop("player");
    public <P extends Item & GeoItem> PlayState predicate(AnimationState<P> event) {
        Entity entity = event.getData(DataTickets.ENTITY);
        if (entity instanceof EntityUmvuthana) {
            event.getController().setAnimation(UMVUTHANA_ANIM);
        }
        else {
            event.getController().setAnimation(PLAYER_ANIM);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, controllerName, 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static class ClientExtensions implements IClientItemExtensions {
        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
            if (this.armorRenderer == null)
                this.armorRenderer = new RenderUmvuthanaMaskArmor();
            if (equipmentSlot == EquipmentSlot.HEAD) armorRenderer.prepForRender(entityLiving, itemStack, equipmentSlot, original);
            return armorRenderer;
        }

        private final BlockEntityWithoutLevelRenderer itemRenderer = new RenderUmvuthanaMaskItem();
        private GeoArmorRenderer<?> armorRenderer;

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return itemRenderer;
        }
    }
}
