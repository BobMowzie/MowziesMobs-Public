package com.bobmowzie.mowziesmobs.server.capability;

import com.bobmowzie.mowziesmobs.MMCommon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

public final class DataHandler {
    // Capabilities are used when these data containers are expected to be accessed by other mods as well
    // (through the capability provider you define which type of object (e.g. entity type) returns null or a data container (data attachment))
    public static final DeferredRegister<AttachmentType<?>> MM_ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES.key(), MMCommon.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FrozenData>> FROZEN_DATA = MM_ATTACHMENT_TYPES.register("frozen_data", () -> AttachmentType.serializable(FrozenData::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LivingData>> LIVING_DATA = MM_ATTACHMENT_TYPES.register("living_data", () -> AttachmentType.serializable(LivingData::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerData>> PLAYER_DATA = MM_ATTACHMENT_TYPES.register("player_data", () -> AttachmentType.serializable(PlayerData::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AbilityData>> ABILITY_DATA = MM_ATTACHMENT_TYPES.register("ability_data", () -> AttachmentType.serializable(AbilityData::new).build());

    // Single point of usage in case additional checks are needed etc.
    public static <T> @NotNull T getData(@NotNull Entity entity, @NotNull DeferredHolder<AttachmentType<?>, AttachmentType<T>> type) {
        if (PLAYER_DATA.equals(type) && !(entity instanceof Player)) {
            // It's basically choosing between checking which entity gets passed into here vs. having a null check for every call of this method
            throw new IllegalArgumentException("Cannot fetch player data for non-player entity of type [" + entity.getType() + "]");
        }

        return entity.getData(type);
    }
}
