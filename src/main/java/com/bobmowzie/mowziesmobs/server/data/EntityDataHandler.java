package com.bobmowzie.mowziesmobs.server.data;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.trade.Trade;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public class EntityDataHandler {
    public static final DeferredRegister<EntityDataSerializer<?>> REG = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, MowziesMobs.MODID);
    public static final EntityDataSerializer<Optional<Trade>> OPTIONAL_TRADE_SERIALIZER = new EntityDataSerializer<Optional<Trade>>() {
        @Override
        public void write(FriendlyByteBuf buf, Optional<Trade> value) {
            if (value.isPresent()) {
                Trade trade = value.get();
                buf.writeItem(trade.getInput());
                buf.writeItem(trade.getOutput());
                buf.writeInt(trade.getWeight());
            } else {
                buf.writeItem(ItemStack.EMPTY);
            }
        }

        @Override
        public Optional<Trade> read(FriendlyByteBuf buf) {
            ItemStack input = buf.readItem();
            if (input == ItemStack.EMPTY) {
                return Optional.empty();
            }
            return Optional.of(new Trade(input, buf.readItem(), buf.readInt()));
        }

        @Override
        public EntityDataAccessor<Optional<Trade>> createAccessor(int id) {
            return new EntityDataAccessor<>(id, this);
        }

        @Override
        public Optional<Trade> copy(Optional<Trade> value) {
            if (value.isPresent()) {
                return Optional.of(new Trade(value.get()));
            }
            return Optional.empty();
        }
    };
    public static RegistryObject<EntityDataSerializer<Optional<Trade>>> OPTIONAL_TRADE = REG.register("optional_trade", () -> OPTIONAL_TRADE_SERIALIZER);
}
