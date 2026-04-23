package com.bobmowzie.mowziesmobs.server.entity.umvuthana.trade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record Trade(ItemStack input, ItemStack output, int weight) {
    public static final Codec<Trade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("input").forGetter(Trade::input),
            ItemStack.CODEC.fieldOf("output").forGetter(Trade::output),
            Codec.INT.fieldOf("weight").forGetter(Trade::weight)
    ).apply(instance, Trade::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Trade> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, Trade::input,
            ItemStack.STREAM_CODEC, Trade::output,
            ByteBufCodecs.VAR_INT, Trade::weight,
            Trade::new
    );

    public ItemStack getInput() {
        return input.copy();
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Trade otherTrade) {
            return weight == otherTrade.weight && ItemStack.matches(input, otherTrade.input) && ItemStack.matches(output, otherTrade.output);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 961 * input.hashCode() + 31 * output.hashCode() + weight;
    }

    public CompoundTag serialize(RegistryAccess access) {
        CompoundTag compound = new CompoundTag();
        compound.put("input", input.save(access, new CompoundTag()));
        compound.put("output", output.save(access, new CompoundTag()));
        compound.putInt("weight", weight);
        return compound;
    }

    public static Trade deserialize(RegistryAccess access, CompoundTag compound) {
        ItemStack input = ItemStack.parseOptional(access, compound.getCompound("input"));
        ItemStack output = ItemStack.parseOptional(access, compound.getCompound("output"));
        int weight = compound.getInt("weight");
        if (input.isEmpty() || output.isEmpty() || weight < 1) {
            return null;
        }
        return new Trade(input, output, weight);
    }
}
