package com.bobmowzie.mowziesmobs.server.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GrottolKillSilkTouchTrigger extends SimpleCriterionTrigger<GrottolKillSilkTouchTrigger.Instance> {
    public record Instance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player)
        ).apply(instance, Instance::new));
    }

    public void trigger(@NotNull ServerPlayer player) {
        super.trigger(player, instance -> true);
    }

    @Override
    public @NotNull Codec<Instance> codec() {
        return Instance.CODEC;
    }
}
