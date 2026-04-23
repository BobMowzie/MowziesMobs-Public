package com.bobmowzie.mowziesmobs.server;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.client.sound.IGeomancyRumbler;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySolarBeam;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySunstrike;
import com.bobmowzie.mowziesmobs.server.entity.naga.EntityNaga;
import com.bobmowzie.mowziesmobs.server.message.*;
import com.bobmowzie.mowziesmobs.server.message.mouse.MessageLeftMouseDown;
import com.bobmowzie.mowziesmobs.server.message.mouse.MessageLeftMouseUp;
import com.bobmowzie.mowziesmobs.server.message.mouse.MessageRightMouseDown;
import com.bobmowzie.mowziesmobs.server.message.mouse.MessageRightMouseUp;
import com.ilexiconn.llibrary.server.network.AnimationMessage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ServerProxy {
    private int nextMessageId;

    public void init(final IEventBus modbus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_CONFIG);
    }

    public void onLateInit(final IEventBus modbus) {
    }

    public void playSunstrikeSound(EntitySunstrike strike) {
    }

    public void playIceBreathSound(Entity entity) {
    }

    public void playBoulderChargeSound(LivingEntity player) {
    }

    public void playNagaSwoopSound(EntityNaga naga) {
    }

    public void playBlackPinkSound(AbstractMinecart entity) {
    }

    public void playSunblockSound(LivingEntity entity) {
    }

    public void playSolarBeamSound(EntitySolarBeam entity) {
    }

    public void playGeomancyRumbleSound(IGeomancyRumbler rumbler) {
    }

    public void minecartParticles(ClientLevel world, AbstractMinecart minecart, float scale, double x, double y, double z, BlockState state, BlockPos pos) {
    }

    public Player getPlayer() {
        return null;
    }

    public void initNetwork() {
        final String version = "1";
        MowziesMobs.NETWORK = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(MowziesMobs.MODID, "net"))
                .networkProtocolVersion(() -> version)
                .clientAcceptedVersions(version::equals)
                .serverAcceptedVersions(version::equals)
                .simpleChannel();
        this.registerMessage(AnimationMessage.class, AnimationMessage::serialize, AnimationMessage::deserialize, new AnimationMessage.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        this.registerMessage(MessageLeftMouseDown.class, MessageLeftMouseDown::serialize, MessageLeftMouseDown::deserialize, new MessageLeftMouseDown.Handler(), NetworkDirection.PLAY_TO_SERVER);
        this.registerMessage(MessageLeftMouseUp.class, MessageLeftMouseUp::serialize, MessageLeftMouseUp::deserialize, new MessageLeftMouseUp.Handler(), NetworkDirection.PLAY_TO_SERVER);
        this.registerMessage(MessageRightMouseDown.class, MessageRightMouseDown::serialize, MessageRightMouseDown::deserialize, new MessageRightMouseDown.Handler(), NetworkDirection.PLAY_TO_SERVER);
        this.registerMessage(MessageRightMouseUp.class, MessageRightMouseUp::serialize, MessageRightMouseUp::deserialize, new MessageRightMouseUp.Handler(), NetworkDirection.PLAY_TO_SERVER);
        this.registerMessage(MessageFreezeEffect.class, MessageFreezeEffect::serialize, MessageFreezeEffect::deserialize, new MessageFreezeEffect.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        this.registerMessage(MessageUmvuthiTrade.class, MessageUmvuthiTrade::serialize, MessageUmvuthiTrade::deserialize, new MessageUmvuthiTrade.Handler(), NetworkDirection.PLAY_TO_SERVER);
        this.registerMessage(MessageBlackPinkInYourArea.class, MessageBlackPinkInYourArea::serialize, MessageBlackPinkInYourArea::deserialize, new MessageBlackPinkInYourArea.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        this.registerMessage(MessageSunblockEffect.class, MessageSunblockEffect::serialize, MessageSunblockEffect::deserialize, new MessageSunblockEffect.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        this.registerMessage(MessageUseAbility.class, MessageUseAbility::serialize, MessageUseAbility::deserialize, new MessageUseAbility.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        this.registerMessage(MessagePlayerUseAbility.class, MessagePlayerUseAbility::serialize, MessagePlayerUseAbility::deserialize, new MessagePlayerUseAbility.Handler(), NetworkDirection.PLAY_TO_SERVER);
        this.registerMessage(MessageInterruptAbility.class, MessageInterruptAbility::serialize, MessageInterruptAbility::deserialize, new MessageInterruptAbility.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        this.registerMessage(MessageJumpToAbilitySectionServerToClient.class, MessageJumpToAbilitySectionServerToClient::serialize, MessageJumpToAbilitySectionServerToClient::deserialize, new MessageJumpToAbilitySectionServerToClient.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        this.registerMessage(MessageSculptorTrade.class, MessageSculptorTrade::serialize, MessageSculptorTrade::deserialize, new MessageSculptorTrade.Handler(), NetworkDirection.PLAY_TO_SERVER);
        this.registerMessage(MessageLinkEntities.class, MessageLinkEntities::serialize, MessageLinkEntities::deserialize, new MessageLinkEntities.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        this.registerMessage(MessageUpdateBossBar.class, MessageUpdateBossBar::serialize, MessageUpdateBossBar::deserialize, new MessageUpdateBossBar.Handler(), NetworkDirection.PLAY_TO_CLIENT);
        this.registerMessage(MessageAddInProgressCooldown.class, MessageAddInProgressCooldown::serialize, MessageAddInProgressCooldown::deserialize, new MessageAddInProgressCooldown.Handler(), NetworkDirection.PLAY_TO_CLIENT);
    }

    private <MSG> void registerMessage(final Class<MSG> clazz, final BiConsumer<MSG, FriendlyByteBuf> encoder, final Function<FriendlyByteBuf, MSG> decoder, final BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer, final NetworkDirection networkDirection) {
        MowziesMobs.NETWORK.registerMessage(this.nextMessageId++, clazz, encoder, decoder, consumer, Optional.of(networkDirection));
    }

    public void setTPS(float tickRate) {
    }


    public Entity getReferencedMob() {
        return null;
    }

    public void setReferencedMob(Entity referencedMob) {
    }

    public void sculptorMarkBlock(int id, BlockPos pos) {
    }

    public void updateMarkedBlocks() {
    }
}
