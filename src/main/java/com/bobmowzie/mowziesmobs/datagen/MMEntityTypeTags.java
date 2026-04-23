package com.bobmowzie.mowziesmobs.datagen;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MMEntityTypeTags extends EntityTypeTagsProvider {
    public MMEntityTypeTags(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, MMCommon.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        addToVanillaTags();
        addToCommonTags();

        tag(key("umvuthana"))
                .add(EntityHandler.UMVUTHI.value())
                .add(EntityHandler.UMVUTHANA_MINION.value())
                .add(EntityHandler.UMVUTHANA_RAPTOR.value())
                .add(EntityHandler.UMVUTHANA_CRANE.value())
                .add(EntityHandler.UMVUTHANA_FOLLOWER_TO_RAPTOR.value())
                .add(EntityHandler.UMVUTHANA_CRANE_TO_PLAYER.value())
                .add(EntityHandler.UMVUTHANA_FOLLOWER_TO_PLAYER.value());

        tag(key("umvuthana_umvuthi_aligned"))
                .add(EntityHandler.UMVUTHI.value())
                .add(EntityHandler.UMVUTHANA_MINION.value())
                .add(EntityHandler.UMVUTHANA_RAPTOR.value())
                .add(EntityHandler.UMVUTHANA_CRANE.value())
                .add(EntityHandler.UMVUTHANA_FOLLOWER_TO_RAPTOR.value());
    }

    private void addToVanillaTags() {
        tag(EntityTypeTags.CAN_BREATHE_UNDER_WATER).add(EntityHandler.GROTTOL.get());
        tag(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES).add(EntityHandler.FROSTMAW.get());
        tag(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES).add(EntityHandler.FROSTMAW.get());
        tag(EntityTypeTags.IMPACT_PROJECTILES)
                .add(EntityHandler.DART.get())
                .add(EntityHandler.BOULDER_PROJECTILE.get())
                .add(EntityHandler.POISON_BALL.get());
    }

    private void addToCommonTags() {
        tag(Tags.EntityTypes.BOSSES)
                .add(EntityHandler.FROSTMAW.get())
                .add(EntityHandler.WROUGHTNAUT.get())
                .add(EntityHandler.UMVUTHI.get())
                .add(EntityHandler.SCULPTOR.get());
        tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED)
                .add(EntityHandler.FROSTMAW.get())
                .add(EntityHandler.WROUGHTNAUT.get())
                .add(EntityHandler.UMVUTHI.get())
                .add(EntityHandler.SCULPTOR.get());
        tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED)
                .add(EntityHandler.UMVUTHI.get())
                .add(EntityHandler.SCULPTOR.get());
    }

    private static TagKey<EntityType<?>> key(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, path));
    }
}
