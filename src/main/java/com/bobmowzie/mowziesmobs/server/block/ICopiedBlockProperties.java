package com.bobmowzie.mowziesmobs.server.block;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public interface ICopiedBlockProperties {
    @Nullable Block mowziesMobs$getBaseBlock();
    void mowziesMobs$setBaseBlock(Block block);
}
