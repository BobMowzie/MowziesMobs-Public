package com.bobmowzie.mowziesmobs.server.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

public class LootConditionMoonPhase implements LootItemCondition {
    private final int value;

    public LootConditionMoonPhase(int value) {
        this.value = value;
    }

    @Override
    public @NotNull LootItemConditionType getType() {
        return LootTableHandler.MOON_PHASE.get();
    }

    public boolean test(LootContext context) {
        ServerLevel serverlevel = context.getLevel();
        int i = serverlevel.getMoonPhase();
        return this.value == i;
    }

    public static Builder time(int moonPhase) {
        return new Builder(moonPhase);
    }

    public static class Builder implements LootItemCondition.Builder {
        private final int value;

        public Builder(int moonPhase) {
            this.value = moonPhase;
        }

        public LootConditionMoonPhase build() {
            return new LootConditionMoonPhase(this.value);
        }
    }

    public static class ConditionSerializer implements Serializer<LootConditionMoonPhase> {
        @Override
        public void serialize(JsonObject json, LootConditionMoonPhase value, JsonSerializationContext context) {
            json.addProperty("value", value.value);
        }

        @Override
        public LootConditionMoonPhase deserialize(JsonObject json, JsonDeserializationContext context) {
            int moonPhase = GsonHelper.getAsInt(json, "value");
            return new LootConditionMoonPhase(moonPhase);
        }
    }
}
