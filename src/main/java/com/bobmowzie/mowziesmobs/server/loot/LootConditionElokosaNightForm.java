package com.bobmowzie.mowziesmobs.server.loot;

import com.bobmowzie.mowziesmobs.server.entity.elokosa.EntityElokosa;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

public class LootConditionElokosaNightForm implements LootItemCondition {
    private static final LootConditionElokosaNightForm INSTANCE = new LootConditionElokosaNightForm();

    private LootConditionElokosaNightForm() {
    }

    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity instanceof EntityElokosa elokosa) {
            return elokosa.getNightForm();
        }
        return false;
    }

    @Override
    public @NotNull LootItemConditionType getType() {
        return LootTableHandler.ELOKOSA_NIGHT_FORM.get();
    }

    public static Builder builder() {
        return () -> INSTANCE;
    }

    public static class ConditionSerializer implements Serializer<LootConditionElokosaNightForm> {
        @Override
        public void serialize(JsonObject json, LootConditionElokosaNightForm value, JsonSerializationContext context) {
        }

        @Override
        public LootConditionElokosaNightForm deserialize(JsonObject json, JsonDeserializationContext context) {
            return LootConditionElokosaNightForm.INSTANCE;
        }
    }
}
