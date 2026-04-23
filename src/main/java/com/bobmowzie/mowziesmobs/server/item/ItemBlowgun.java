package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class ItemBlowgun extends BowItem {
    public static final Predicate<ItemStack> DARTS = stack -> stack.getItem() == ItemHandler.DART.get();

    public ItemBlowgun(Item.Properties properties) {
        super(properties);
    }

    @Override // Mostly a copy of the parent class
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player) {
            ItemStack itemstack = player.getProjectile(stack);

            if (!itemstack.isEmpty()) {
                int duration = this.getUseDuration(stack, entityLiving) - timeLeft;
                duration = EventHooks.onArrowLoose(stack, level, player, duration, !itemstack.isEmpty());

                if (duration < 0) {
                    return;
                }

                float power = ItemBlowgun.getPowerForTime(duration);

                if (!((double) power < 0.1)) {
                    List<ItemStack> ammo = draw(stack, itemstack, player);

                    if (level instanceof ServerLevel serverlevel && !ammo.isEmpty()) {
                        this.shoot(serverlevel, player, player.getUsedItemHand(), stack, ammo, power * 3.0F, 1.0F, power == 1.0F, null);
                    }

                    // Custom sound
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), MMSounds.ENTITY_UMVUTHANA_BLOWDART.get(), SoundSource.PLAYERS, 1.0F, 1.0F / (player.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    public static float getPowerForTime(int charge) {
        float power = (float) charge / 5;
        power = (power * power + power * 2) / 3;

        if (power > 1) {
            power = 1;
        }

        return power;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 72000;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable(getDescriptionId() + ".text.0").setStyle(ItemHandler.TOOLTIP_STYLE));
        tooltip.add(Component.translatable(getDescriptionId() + ".text.1").setStyle(ItemHandler.TOOLTIP_STYLE));
        tooltip.add(Component.translatable(getDescriptionId() + ".text.2").setStyle(ItemHandler.TOOLTIP_STYLE));
    }

    @Override
    protected @NotNull Projectile createProjectile(@NotNull Level level, @NotNull LivingEntity shooter, @NotNull ItemStack weapon, ItemStack ammo, boolean isCrit) {
        ArrowItem arrowitem = ammo.getItem() instanceof ItemDart dart ? dart : ItemHandler.DART.get(); // Use dart as default item
        AbstractArrow arrow = arrowitem.createArrow(level, ammo, shooter, weapon);

        if (isCrit) {
            arrow.setCritArrow(true);
        }

        return customArrow(arrow, ammo, weapon);
    }


    @Override
    public @NotNull Predicate<ItemStack> getSupportedHeldProjectiles() {
        return DARTS;
    }

    @Override
    public @NotNull ItemStack getDefaultCreativeAmmo(@Nullable Player player, @NotNull ItemStack projectileWeaponItem) {
        return ItemHandler.DART.get().getDefaultInstance();
    }
}
