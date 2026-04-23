package com.bobmowzie.mowziesmobs.client;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.client.gui.CustomBossBar;
import com.bobmowzie.mowziesmobs.client.model.entity.ModelGeckoPlayerFirstPerson;
import com.bobmowzie.mowziesmobs.client.model.entity.ModelGeckoPlayerThirdPerson;
import com.bobmowzie.mowziesmobs.client.render.MMRenderType;
import com.bobmowzie.mowziesmobs.client.render.block.SculptorBlockMarking;
import com.bobmowzie.mowziesmobs.client.render.entity.player.GeckoFirstPersonRenderer;
import com.bobmowzie.mowziesmobs.client.render.entity.player.GeckoPlayer;
import com.bobmowzie.mowziesmobs.client.render.entity.player.GeckoRenderPlayer;
import com.bobmowzie.mowziesmobs.client.sound.BossMusicPlayer;
import com.bobmowzie.mowziesmobs.server.capability.AbilityData;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import com.bobmowzie.mowziesmobs.server.capability.FrozenData;
import com.bobmowzie.mowziesmobs.server.capability.PlayerData;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntityCameraShake;
import com.bobmowzie.mowziesmobs.server.entity.frostmaw.EntityFrozenController;
import com.bobmowzie.mowziesmobs.server.item.ItemBlowgun;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEventHandler {
    private static final ResourceLocation FROZEN_BLUR = ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHandRender(RenderHandEvent event) {
        if (!ConfigHandler.CLIENT.customPlayerAnims.get()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        boolean shouldAnimate = false;
        AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
        shouldAnimate = data.getActiveAbility() != null;
//        shouldAnimate = (player.ticksExisted / 20) % 2 == 0;
        if (shouldAnimate) {
            GeckoPlayer.GeckoPlayerFirstPerson geckoPlayer = GeckoFirstPersonRenderer.GECKO_PLAYER_FIRST_PERSON;

            if (geckoPlayer != null) {
                ModelGeckoPlayerFirstPerson geckoFirstPersonModel = (ModelGeckoPlayerFirstPerson) geckoPlayer.getModel();
                GeckoFirstPersonRenderer firstPersonRenderer = (GeckoFirstPersonRenderer) geckoPlayer.getPlayerRenderer();

                if (geckoFirstPersonModel != null && firstPersonRenderer != null) {
                    if (!geckoFirstPersonModel.isUsingSmallArms() && player.getSkin().model().name().equals("slim")) {
                        firstPersonRenderer.setSmallArms();
                    }
                    event.setCanceled(true);

                    if (event.isCanceled()) {
                        float delta = event.getPartialTick();
                        float f1 = Mth.lerp(delta, player.xRotO, player.getXRot());
                        firstPersonRenderer.renderItemInFirstPerson(player, f1, delta, event.getHand(), event.getSwingProgress(), event.getItemStack(), event.getEquipProgress(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), geckoPlayer);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void renderLivingEvent(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
        if (event.getEntity() instanceof Player player) {
            if (!ConfigHandler.CLIENT.customPlayerAnims.get()) return;
            if (ConfigHandler.CLIENT.hidePlayerAnimsInFirstPerson.get() && player == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) return;
            float delta = event.getPartialTick();
            AbilityData abilityData = DataHandler.getData(player, DataHandler.ABILITY_DATA);
//            if ((player.tickCount / 20) % 2 == 0) {
            if (abilityData.getActiveAbility() != null) {
                GeckoPlayer.GeckoPlayerThirdPerson geckoPlayer = DataHandler.getData(player, DataHandler.PLAYER_DATA).getGeckoPlayer();

                if (geckoPlayer != null) {
                    ModelGeckoPlayerThirdPerson geckoPlayerModel = (ModelGeckoPlayerThirdPerson) geckoPlayer.getModel();
                    GeckoRenderPlayer animatedPlayerRenderer = (GeckoRenderPlayer) geckoPlayer.getPlayerRenderer();

                    if (geckoPlayerModel != null && animatedPlayerRenderer != null) {
                        event.setCanceled(true);

                        if (event.isCanceled()) {
                            animatedPlayerRenderer.render((AbstractClientPlayer) event.getEntity(), event.getEntity().getYRot(), delta, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), geckoPlayer);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            GeckoPlayer geckoPlayer = DataHandler.getData(player, DataHandler.PLAYER_DATA).getGeckoPlayer();
            if (geckoPlayer != null) geckoPlayer.tick();
            if (player == Minecraft.getInstance().player) GeckoFirstPersonRenderer.GECKO_PLAYER_FIRST_PERSON.tick();
        }
//        if(player.getInventory().getArmor(3).is(ItemHandler.SOL_VISAGE.asItem())){
//            int tick = player.tickCount;
//            double orbitSpeed = 50;
//            double orbitSize = 0.6;
//            double xOffset = (Math.sin(tick * orbitSpeed) * orbitSize);
//            double zOffset= (Math.cos(tick * orbitSpeed) * orbitSize);
//            Vec3 particleVec = Vec3.ZERO.add(xOffset, 2.2f, zOffset).yRot((float)Math.toRadians(-player.getYHeadRot())).xRot((float) Math.toRadians(0f)).add(player.position());
//            Vec3 particleVec2 = Vec3.ZERO.add(-xOffset, 2.2f, -zOffset).yRot((float)Math.toRadians(-player.getYHeadRot())).xRot((float) Math.toRadians(0f)).add(player.position());
//
//            player.level.addParticle(ParticleTypes.SMALL_FLAME, particleVec.x, particleVec.y, particleVec.z, 0d, 0d, 0d);
//            player.level.addParticle(ParticleTypes.SMALL_FLAME, particleVec2.x, particleVec2.y, particleVec2.z, 0d, 0d, 0d);
//
//        }
    }

    @SubscribeEvent
    public static void onRenderTick(RenderFrameEvent.Pre event) {
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

//        if (player != null) {
//            PlayerCapability.Capability playerCapability = CapabilityHandler.getCapability(player, CapabilityHandler.PLAYER_CAPABILITY);
//            if (playerCapability != null && playerCapability.getGeomancy().canUse(player) && playerCapability.getGeomancy().isSpawningBoulder() && playerCapability.getGeomancy().getSpawnBoulderCharge() > 2) {
//                Vector3d lookPos = playerCapability.getGeomancy().getLookPos();
//                Vector3d playerEyes = player.getEyePosition(Minecraft.getInstance().getRenderPartialTicks());
//                Vector3d vec = playerEyes.subtract(lookPos).normalize();
//                float yaw = (float) Math.atan2(vec.z, vec.x);
//                float pitch = (float) Math.asin(vec.y);
//                player.rotationYaw = (float) (yaw * 180f/Math.PI + 90);
//                player.rotationPitch = (float) (pitch * 180f/Math.PI);
//                player.rotationYawHead = player.rotationYaw;
//                player.prevRotationYaw = player.rotationYaw;
//                player.prevRotationPitch = player.rotationPitch;
//                player.prevRotationYawHead = player.rotationYawHead;
//            }
        FrozenData data = DataHandler.getData(player, DataHandler.FROZEN_DATA);
        if (data.getFrozen() && data.getPrevFrozen()) {
            player.setYRot(data.getFrozenYaw());
            player.setXRot(data.getFrozenPitch());
            player.yHeadRot = data.getFrozenYawHead();
            player.yRotO = player.getYRot();
            player.xRotO = player.getXRot();
            player.yHeadRotO = player.yHeadRot;
        }
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        FrozenData data = DataHandler.getData(entity, DataHandler.FROZEN_DATA);
        if (data.getFrozen() && data.getPrevFrozen()) {
            entity.setYRot(entity.yRotO = data.getFrozenYaw());
            entity.setXRot(entity.xRotO = data.getFrozenPitch());
            entity.yHeadRot = entity.yHeadRotO = data.getFrozenYawHead();
            entity.yBodyRot = entity.yBodyRotO = data.getFrozenRenderYawOffset();
            entity.attackAnim = entity.oAttackAnim = data.getFrozenSwingProgress();
            entity.walkAnimation.setSpeed(0);
            entity.walkAnimation.update(0, 0);
            entity.setShiftKeyDown(false);
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        if (event.getName() == VanillaGuiLayers.CAMERA_OVERLAYS) {
            if (Minecraft.getInstance().player != null) {
                FrozenData data = DataHandler.getData(Minecraft.getInstance().player, DataHandler.FROZEN_DATA);

                if (data.getFrozen() && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
                    GuiGraphics graphics = event.getGuiGraphics();
                    graphics.blit(FROZEN_BLUR, 0, 0, 0, 0, graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight());
                }
            }
        }
    }

    // Remove frozen overlay
    @SubscribeEvent
    public static void onRenderHUD(RenderGuiLayerEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.isPassenger()) {
            if (player.getVehicle() instanceof EntityFrozenController) {
                if (event.getName() == VanillaGuiLayers.VEHICLE_HEALTH) {
                    event.setCanceled(true);
                }
                Minecraft.getInstance().gui.setOverlayMessage(Component.empty(), false);
            }
        }
    }

    @SubscribeEvent
    public static void updateFOV(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof ItemBlowgun) {
            int i = player.getTicksUsingItem();
            float f1 = (float)i / 5.0F;
            if (f1 > 1.0F) {
                f1 = 1.0F;
            } else {
                f1 = f1 * f1;
            }

            event.setNewFovModifier(1.0F - f1 * 0.15F);
        }
    }

    @SubscribeEvent
    public static void onSetupCamera(ViewportEvent.ComputeCameraAngles event) {
        Player player = Minecraft.getInstance().player;
        float delta = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        float ticksExistedDelta = player.tickCount + delta;

        if (ConfigHandler.CLIENT.doCameraShakes.get() && !Minecraft.getInstance().isPaused()) {
            float shakeAmplitude = 0;
            for (EntityCameraShake cameraShake : player.level().getEntitiesOfClass(EntityCameraShake.class, player.getBoundingBox().inflate(20, 20, 20))) {
                if (cameraShake.distanceTo(player) < cameraShake.getRadius()) {
                    shakeAmplitude += cameraShake.getShakeAmount(player, delta);
                }
            }
            if (shakeAmplitude > 1.0f) shakeAmplitude = 1.0f;
            event.setPitch((float) (event.getPitch() + shakeAmplitude * Math.cos(ticksExistedDelta * 3 + 2) * 25));
            event.setYaw((float) (event.getYaw() + shakeAmplitude * Math.cos(ticksExistedDelta * 5 + 1) * 25));
            event.setRoll((float) (event.getRoll() + shakeAmplitude * Math.cos(ticksExistedDelta * 4) * 25));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderBossBar(CustomizeGuiOverlayEvent.BossEventProgress event){
        if (!ConfigHandler.CLIENT.customBossBars.get()) return;
        ResourceLocation bossRegistryName = ClientProxy.bossBarRegistryNames.getOrDefault(event.getBossEvent().getId(), null);
        if (bossRegistryName == null) return;
        CustomBossBar customBossBar = CustomBossBar.customBossBars.getOrDefault(bossRegistryName, null);
        if (customBossBar == null) return;

        event.setCanceled(true);
        customBossBar.renderBossBar(event);
    }

    private static ResourceLocation SCULPTOR_BLOCK_GLOW = ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "textures/entity/sculptor_highlight.png");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            ClientLevel level = Minecraft.getInstance().level;
            if (Minecraft.getInstance().player != null && level != null) {
                Vec3 cameraPos = event.getCamera().getPosition();
                double d0 = cameraPos.x();
                double d1 = cameraPos.y();
                double d2 = cameraPos.z();
                for (Long2ObjectMap.Entry<SculptorBlockMarking> entry : ClientProxy.sculptorMarkedBlocks.long2ObjectEntrySet()) {
                    BlockPos blockpos2 = BlockPos.of(entry.getLongKey());
                    event.getPoseStack().pushPose();
                    event.getPoseStack().translate((double) blockpos2.getX() - d0, (double) blockpos2.getY() - d1, (double) blockpos2.getZ() - d2);
                    PoseStack.Pose posestack$pose1 = event.getPoseStack().last();
                    float tick = event.getRenderTick();
                    float blockOffset = (blockpos2.getX() + blockpos2.getY() + blockpos2.getZ()) * 0.25f;
                    VertexConsumer vertexconsumer1 = new SheetedDecalTextureGenerator(Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(MMRenderType.highlight(SCULPTOR_BLOCK_GLOW, tick * 0.02f + blockOffset, tick * 0.01f + blockOffset)), posestack$pose1, 0.25F);
                    ModelData modelData = level.getModelDataManager().getAt(blockpos2);
                    renderBreakingTexture(level.getBlockState(blockpos2), blockpos2, level, event.getPoseStack(), level.random, vertexconsumer1, modelData);

                    event.getPoseStack().popPose();
                }
            }
        }
    }

    private static void renderBreakingTexture(BlockState state, BlockPos pos, BlockAndTintGetter blockAndTintGetter, PoseStack poseStack, RandomSource random, VertexConsumer vertexConsumer, ModelData modelData) {
        if (state.getRenderShape() == RenderShape.MODEL) {
            BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
            BakedModel bakedmodel = blockRenderDispatcher.getBlockModel(state);
            long i = state.getSeed(pos);
            blockRenderDispatcher.getModelRenderer().tesselateBlock(blockAndTintGetter, bakedmodel, state, pos, poseStack, vertexConsumer, true, random, i, OverlayTexture.NO_OVERLAY, modelData, null);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) {
            MMCommon.PROXY.updateMarkedBlocks();
            BossMusicPlayer.tick();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (event.getKeyMapping() == Minecraft.getInstance().options.keyAttack) {
            PlayerData data = DataHandler.getData(player, DataHandler.PLAYER_DATA);
            if (data != null) {
                data.pressedAttackKey(player);
            }
        } else if (event.getKeyMapping() == Minecraft.getInstance().options.keyUse) {
            PlayerData data = DataHandler.getData(player, DataHandler.PLAYER_DATA);
            if (data != null) {
                data.pressedUseKey(player);
            }
        }
    }
}
