package com.bobmowzie.mowziesmobs.client;

import com.bobmowzie.mowziesmobs.MMCommon;
import com.bobmowzie.mowziesmobs.server.entity.umvuthana.MaskType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.List;
import java.util.Map;

public class MMModels {
    public static final String[] HAND_MODEL_ITEMS = new String[] {"wrought_axe", "spear", "earthrend_gauntlet", "sculptor_staff"};
    // Can only register 'standalone' models which do not have this prefix
    public static final String PREFIX = "item/";

    @SubscribeEvent
    public static void onModelBakeEvent(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();

        for (String item : HAND_MODEL_ITEMS) {
            ModelResourceLocation modelInventory = ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, item));
            ModelResourceLocation modelHand = prefixed(item + "_in_hand");

            BakedModel bakedModelDefault = models.get(modelInventory);
            BakedModel bakedModelHand = models.get(modelHand);
            BakedModel modelWrapper = new BakedModel() {
                @Override
                public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
                    return bakedModelDefault.getQuads(state, side, rand);
                }

                @Override
                public boolean useAmbientOcclusion() {
                    return bakedModelDefault.useAmbientOcclusion();
                }

                @Override
                public boolean isGui3d() {
                    return bakedModelDefault.isGui3d();
                }

                @Override
                public boolean usesBlockLight() {
                    return false;
                }

                @Override
                public boolean isCustomRenderer() {
                    return bakedModelDefault.isCustomRenderer();
                }

                @Override
                public TextureAtlasSprite getParticleIcon() {
                    return bakedModelDefault.getParticleIcon();
                }

                @Override
                public ItemOverrides getOverrides() {
                    return bakedModelDefault.getOverrides();
                }

                @Override
                public BakedModel applyTransform(ItemDisplayContext cameraTransformType, PoseStack mat, boolean applyLeftHandTransform) {
                    BakedModel modelToUse = bakedModelDefault;
                    if (cameraTransformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || cameraTransformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                            || cameraTransformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || cameraTransformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                        modelToUse = bakedModelHand;
                    }
                    return ClientHooks.handleCameraTransforms(mat, modelToUse, cameraTransformType, applyLeftHandTransform);
                }
            };
            models.put(modelInventory, modelWrapper);
        }

        for (MaskType type : MaskType.values()) {
            ModelResourceLocation maskModelInventory = ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID,"umvuthana_mask_" + type.name));
            ModelResourceLocation maskModelFrame = prefixed("umvuthana_mask_" + type.name + "_frame");
            bakeMask(models, maskModelInventory, maskModelFrame);
        }
        ModelResourceLocation maskModelInventory = ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, "sol_visage"));
        ModelResourceLocation maskModelFrame = prefixed("sol_visage_frame");
        bakeMask(models, maskModelInventory, maskModelFrame);
    }

    private static void bakeMask(Map<ModelResourceLocation, BakedModel> map, ModelResourceLocation maskModelInventory, ModelResourceLocation maskModelFrame) {
        BakedModel maskBakedModelDefault = map.get(maskModelInventory);
        BakedModel maskBakedModelFrame = map.get(maskModelFrame);
        BakedModel maskModelWrapper = new BakedModel() {
            @Override
            public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
                return maskBakedModelDefault.getQuads(state, side, rand);
            }

            @Override
            public boolean useAmbientOcclusion() {
                return maskBakedModelDefault.useAmbientOcclusion();
            }

            @Override
            public boolean isGui3d() {
                return maskBakedModelDefault.isGui3d();
            }

            @Override
            public boolean usesBlockLight() {
                return false;
            }

            @Override
            public boolean isCustomRenderer() {
                return maskBakedModelDefault.isCustomRenderer();
            }

            @Override
            public TextureAtlasSprite getParticleIcon() {
                return maskBakedModelDefault.getParticleIcon();
            }

            @Override
            public ItemOverrides getOverrides() {
                return maskBakedModelDefault.getOverrides();
            }

            @Override
            public BakedModel applyTransform(ItemDisplayContext cameraTransformType, PoseStack mat, boolean applyLeftHandTransform) {
                BakedModel modelToUse = maskBakedModelDefault;
                if (cameraTransformType == ItemDisplayContext.FIXED) {
                    modelToUse = maskBakedModelFrame;
                }
                return ClientHooks.handleCameraTransforms(mat, modelToUse, cameraTransformType, applyLeftHandTransform);
            }
        };

        map.put(maskModelInventory, maskModelWrapper);
    }

    public static ModelResourceLocation prefixed(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(MMCommon.MODID, PREFIX + path));
    }
}