package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.client.particle.ParticleHandler;
import com.bobmowzie.mowziesmobs.client.particle.util.AdvancedParticleBase;
import com.bobmowzie.mowziesmobs.client.particle.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.elokosa.PawType;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class ItemElokosaPaw extends Item {
    private final PawType pawType;

    public ItemElokosaPaw(PawType pawType, Properties properties) {
        super(properties.stacksTo(1).durability(3));
        this.pawType = pawType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);
        player.playSound(MMSounds.ENTITY_ELOKOSA_PAW.get(), 1, 1.15f - 0.06f * pawType.ordinal());
        int cooldown = ConfigHandler.COMMON.TOOLS_AND_ABILITIES.ELOKOSA_PAW.cooldown.getAsInt();
        if (!player.hasInfiniteMaterials()) {
            for (DeferredHolder<Item, ItemElokosaPaw> item : ItemHandler.ELOKOSA_PAWS) {
                player.getCooldowns().addCooldown(item.get(), cooldown);
            }
        }
        itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(usedHand));

        if (level.isClientSide()) {
            float ringScale = 100f;
            AdvancedParticleBase.spawnParticle(level, ParticleHandler.BURST_MESSY, player.getX(), player.getY() + player.getBbHeight()/2f, player.getZ(), 0, 0, 0, false, 0, Math.PI/2f, 0, 0, 5F, 0.70,0.55,0.99, 1, 1, 20, true, false, new ParticleComponent[]{
                    new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.Expression(t -> (float) Math.pow(t, 0.6) * ringScale * 1.3f), false),
                    new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, new ParticleComponent.Expression(t -> 1f - (float) Math.pow(t, 0.5)), false),
            });
            AdvancedParticleBase.spawnParticle(level, ParticleHandler.RING_BIG, player.getX(), player.getY() + player.getBbHeight()/2f, player.getZ(), 0, 0, 0, false, 0, Math.PI/2f, 0, 0, 5F, 0.85,0.65,0.99, 1, 1, 15, true, false, new ParticleComponent[]{
                    new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.Expression(t -> (float) Math.pow(t, 0.6) * ringScale), false),
                    new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, new ParticleComponent.Expression(t -> 1f - (float) Math.pow(t, 0.5)), false),
            });
            float glowScale = 130f;
            AdvancedParticleBase.spawnParticle(level, ParticleHandler.GLOW, player.getX(), player.getY() + player.getBbHeight()/2f, player.getZ(), 0, 0, 0, true, 0, 0, 0, 0, 5F, 0.65,0.45,0.95, 1, 1, 20, true, false, new ParticleComponent[]{
                    new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.Expression(t -> (float) Math.pow(t, 0.6) * glowScale), false),
                    new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, new ParticleComponent.Expression(t -> 1f - (float) Math.pow(t, 0.5)), false),
            });
            Vec3 moonPos;
            float moonSize;
            if (Minecraft.getInstance().player == player && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
                boolean usingMainHand = usedHand == InteractionHand.MAIN_HAND;
                boolean isRightHanded = player.getMainArm() == HumanoidArm.RIGHT;
                boolean usedRightHand = usingMainHand == isRightHanded;
                float sidewaysOffset = 0.76f;
                Vec3 playerLeftAngle = player.getUpVector(0).cross(player.getLookAngle());
                moonPos = player.getEyePosition().add(player.getLookAngle()).add(player.getUpVector(0).scale(0.2f)).add(playerLeftAngle.scale(usedRightHand ? -sidewaysOffset : sidewaysOffset));
                moonSize = 4.3f;
                AdvancedParticleBase.spawnParticle(level, pawType.getParticleType(), moonPos.x, moonPos.y, moonPos.z, 0, 0, 0, true, 0, 0, 0, 0, 5F, 1, 1, 1, 1, 1, 25, true, false, new ParticleComponent[]{
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, new ParticleComponent.KeyTrack(
                                new float[] {0, 0, 1f, 1f, 0},
                                new float[] {0, 0.05f, 0.15f, 0.5f, 1}
                        ), false),
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.Expression(t -> ((1f - (float) Math.pow(t, 0.6))*((float) Math.pow(t, 0.5))*7f + 1f) * moonSize), false),
                        new ParticleComponent.ScreenSpace(usedRightHand ? sidewaysOffset : -sidewaysOffset, 1.2f, -0.5f)
                });
                AdvancedParticleBase.spawnParticle(level, ParticleHandler.GLOW, moonPos.x, moonPos.y, moonPos.z, 0, 0, 0, true, 0, 0, 0, 0, 5F, 0.4f, 0.576, 1, 1, 1, 25, true, false, new ParticleComponent[]{
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, new ParticleComponent.KeyTrack(
                                new float[] {0, 0, 0.3f, 0.3f, 0},
                                new float[] {0, 0.05f, 0.15f, 0.5f, 1}
                        ), false),
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.Expression(t -> ((1f - (float) Math.pow(t, 0.8))*((float) Math.pow(t, 0.5))*7f + 1f) * moonSize * 0.8f), false),
                        new ParticleComponent.ScreenSpace(usedRightHand ? sidewaysOffset : -sidewaysOffset, 1.2f, -0.5f)
                });
            }
            else {
                moonSize = 5f;
                moonPos = new Vec3(player.getX(), player.getY() + player.getBbHeight() + 0.65f, player.getZ());
                AdvancedParticleBase.spawnParticle(level, pawType.getParticleType(), moonPos.x, moonPos.y, moonPos.z, 0, 0, 0, true, 0, 0, 0, 0, 5F, 1, 1, 1, 1, 1, 25, true, false, new ParticleComponent[]{
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, new ParticleComponent.KeyTrack(
                                new float[] {0, 0, 1f, 1f, 0},
                                new float[] {0, 0.05f, 0.15f, 0.5f, 1}
                        ), false),
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.Expression(t -> ((1f - (float) Math.pow(t, 0.6))*((float) Math.pow(t, 0.5))*7f + 1f) * moonSize), false),
                });
                AdvancedParticleBase.spawnParticle(level, ParticleHandler.GLOW, moonPos.x, moonPos.y, moonPos.z, 0, 0, 0, true, 0, 0, 0, 0, 5F, 0.4f, 0.576, 1, 1, 1, 25, true, false, new ParticleComponent[]{
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, new ParticleComponent.KeyTrack(
                                new float[] {0, 0, 0.3f, 0.3f, 0},
                                new float[] {0, 0.05f, 0.15f, 0.5f, 1}
                        ), false),
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.Expression(t -> ((1f - (float) Math.pow(t, 0.8))*((float) Math.pow(t, 0.5))*7f + 1f) * moonSize * 0.8f), false),
                });
            }
            for (int i = 0; i < 60; i++) {
                Vec3 vel = new Vec3(0.15 + player.getRandom().nextFloat() * 0.4f, 0, 0).yRot(player.getRandom().nextFloat() * Mth.TWO_PI).xRot(player.getRandom().nextFloat() * Mth.TWO_PI);
                AdvancedParticleBase.spawnParticle(level, ParticleHandler.PIXEL, moonPos.x + vel.x, moonPos.y + vel.y, moonPos.z + vel.z, vel.x, vel.y, vel.z, true, 0, 0, 0, 0, 5F, 0.85,0.65,0.99, 1, 0.9, 10 + player.getRandom().nextInt(10), true, true, new ParticleComponent[]{
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.Expression(t -> (1f - (float) Math.pow(t, 2)) * 2.5f), false),
                        new ParticleComponent.ForceOverTime(new Vec3(0, 0.003, 0)),
                        new ParticleComponent.Vortex(new Vec3(0, 1, 0), new Vec3(player.getX(), player.getY() + player.getBbHeight()/2f, player.getZ()), ParticleComponent.KeyTrack.startAndEnd(player.getRandom().nextFloat() * 0.15f, 0f)),
                        new ParticleComponent.CurlNoise(0.025f, 4f),
                });
            }
        }

        float radius = 10;
        List<LivingEntity> entitiesNearby = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius, radius, radius), e -> e.distanceToSqr(player) <= radius * radius && e != player);
        int effectDuration = ConfigHandler.COMMON.TOOLS_AND_ABILITIES.ELOKOSA_PAW.effectDuration.getAsInt();
        for (LivingEntity entity : entitiesNearby) {
            entity.addEffect(new MobEffectInstance(pawType.getPotion(), effectDuration));

            if (level.isClientSide()) {
                for (int i = 0; i < 30; i++) {
                    Vec3 vel = new Vec3(0.05 + entity.getRandom().nextFloat() * 0.1f, 0, 0).yRot(entity.getRandom().nextFloat() * Mth.TWO_PI).xRot(entity.getRandom().nextFloat() * Mth.TWO_PI);
                    AdvancedParticleBase.spawnParticle(level, ParticleHandler.PIXEL, entity.getX(), entity.getY() + entity.getBbHeight()/2f, entity.getZ(), vel.x, vel.y, vel.z, true, 0, 0, 0, 0, 5F, 0.85,0.65,0.99, 1, 0.9, 10 + player.getRandom().nextInt(10), true, true, new ParticleComponent[]{
                            new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.Expression(t -> (1f - (float) Math.pow(t, 2)) * 2.5f), false),
                            new ParticleComponent.ForceOverTime(new Vec3(0, 0.003, 0)),
                            new ParticleComponent.Vortex(new Vec3(0, 1, 0), new Vec3(entity.getX(), entity.getY() + entity.getBbHeight()/2f, entity.getZ()), ParticleComponent.KeyTrack.startAndEnd(entity.getRandom().nextFloat() * 0.1f, 0f)),
                            new ParticleComponent.CurlNoise(0.025f, 4f),
                    });
                }
                AdvancedParticleBase.spawnParticle(level, ParticleHandler.BURST_OUT, entity.getX(), entity.getY() + entity.getBbHeight()/2f, entity.getZ(), 0, 0, 0, true, 0, 0, 0, 0, 4F, 0.65,0.45,0.95, 1, 1, 18, true, false, new ParticleComponent[]{
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.SCALE, new ParticleComponent.Expression(t -> (float) Math.pow(t, 0.6) * 14f), false),
                        new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, new ParticleComponent.Expression(t -> 1f - (float) Math.pow(t, 0.5)), false),
                });
            }
        }
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable(getDescriptionId() + ".text.0").setStyle(ItemHandler.TOOLTIP_STYLE));
        if (pawType == PawType.CRESCENT || pawType == PawType.NEW) {
            tooltip.add(Component.translatable(getDescriptionId() + ".text.1").setStyle(ItemHandler.TOOLTIP_STYLE));
        }
        if (pawType == PawType.NEW) {
            tooltip.add(Component.translatable(getDescriptionId() + ".text.2").setStyle(ItemHandler.TOOLTIP_STYLE));
        }
    }
}
