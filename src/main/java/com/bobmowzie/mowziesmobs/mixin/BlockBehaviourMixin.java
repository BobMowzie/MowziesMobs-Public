package com.bobmowzie.mowziesmobs.mixin;

import com.bobmowzie.mowziesmobs.server.block.ICopiedBlockProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.Properties.class)
public class BlockBehaviourMixin implements ICopiedBlockProperties {
    @Unique public @Nullable Block mowziesMobs$baseBlock;

    @Inject(method = "ofLegacyCopy", at = @At("RETURN"))
    private static void onCopy(BlockBehaviour blockBehaviour, CallbackInfoReturnable<BlockBehaviour.Properties> cir) {
        if (blockBehaviour instanceof Block block) {
            ((ICopiedBlockProperties) cir.getReturnValue()).mowziesMobs$setBaseBlock(block);
        }
    }

    @Override
    public @Nullable Block mowziesMobs$getBaseBlock() {
        return mowziesMobs$baseBlock;
    }

    @Override
    public void mowziesMobs$setBaseBlock(Block block) {
        mowziesMobs$baseBlock = block;
    }
}
