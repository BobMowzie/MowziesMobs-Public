package com.bobmowzie.mowziesmobs.mixin.client;

import com.bobmowzie.mowziesmobs.server.entity.MowzieEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

@Mixin(value = GeoModel.class, remap = false)
public abstract class GeoModelMixin {
    @Shadow private long lastRenderedInstance;

    /* FIXME 1.21
        Unsure what the exact problem is - maybe the manager falsely thinks the inventory-rendered entity is a re-render because it already rendered the in-world variant?
        Either way it seems that inventory rendering doesn't properly work with the current rendering setup
        - The temporary changes to the rotation seem to have lasting effects on the in-world entity
        - The entity may diverge into two java objects with their tick count diverging (causing the inventory entity to flicker)
        - The inventory partial tick is always 1 but for normal rendering 'Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)' is used
        This mixin makes things sort of work
    */
    @WrapOperation(method = "handleAnimations", at = @At(value = "FIELD", target = "Lsoftware/bernie/geckolib/model/GeoModel;lastRenderedInstance:J", ordinal = 0))
    private <T extends GeoAnimatable> long test(GeoModel<?> instance, Operation<Long> original, @Local(argsOnly = true) T animatable) {
        if (animatable instanceof MowzieEntity entity && entity.renderingInGUI) {
            // Don't return early
            return -lastRenderedInstance;
        }

        return original.call(instance);
    }
}
