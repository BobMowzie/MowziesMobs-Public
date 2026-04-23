package com.bobmowzie.mowziesmobs.server.loot;

import com.bobmowzie.mowziesmobs.server.entity.grottol.EntityGrottol;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LootFunctionGrottolDeathType extends LootItemConditionalFunction {
    public static final MapCodec<LootFunctionGrottolDeathType> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).apply(instance, LootFunctionGrottolDeathType::new));

    protected LootFunctionGrottolDeathType(List<LootItemCondition> predicates) {
        super(predicates);
    }

    @Override
    protected @NotNull ItemStack run(@NotNull ItemStack stack, LootContext context) {
        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof EntityGrottol grottol) {
            EntityGrottol.EnumDeathType deathType = grottol.getDeathType();

            if (deathType == EntityGrottol.EnumDeathType.NORMAL) {
                stack.setCount(0);
            } else if (deathType == EntityGrottol.EnumDeathType.FORTUNE_PICKAXE) {
                stack.setCount(stack.getCount() + 1);
            }
        }

        return stack;
    }

    @Override
    public @NotNull LootItemFunctionType<LootFunctionGrottolDeathType> getType() {
        return LootTableHandler.GROTTOL_DEATH_TYPE.get();
    }
}
