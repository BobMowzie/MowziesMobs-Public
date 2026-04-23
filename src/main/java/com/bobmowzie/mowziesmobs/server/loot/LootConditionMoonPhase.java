package com.bobmowzie.mowziesmobs.server.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;


public record LootConditionMoonPhase(Integer value) implements LootItemCondition {
    public static final MapCodec<LootConditionMoonPhase> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.INT.fieldOf("value").forGetter(LootConditionMoonPhase::value)
                    )
                    .apply(instance, LootConditionMoonPhase::new)
    );

    @Override
    public @NotNull LootItemConditionType getType() {
        return LootTableHandler.MOON_PHASE.get();
    }

    public boolean test(LootContext context) {
        ServerLevel serverlevel = context.getLevel();
        int i = serverlevel.getMoonPhase();
        return this.value == i;
    }

    public static LootConditionMoonPhase.Builder time(Integer moonPhase) {
        return new LootConditionMoonPhase.Builder(moonPhase);
    }

    public static class Builder implements LootItemCondition.Builder {
        private final Integer value;

        public Builder(Integer moonPhase) {
            this.value = moonPhase;
        }

        public LootConditionMoonPhase build() {
            return new LootConditionMoonPhase(this.value);
        }
    }
}
