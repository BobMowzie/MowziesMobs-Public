package com.bobmowzie.mowziesmobs.client.render.item;

import com.bobmowzie.mowziesmobs.client.model.armor.ModelGeomancerArmor;
import com.bobmowzie.mowziesmobs.client.render.entity.MowzieGeoArmorRenderer;
import com.bobmowzie.mowziesmobs.server.item.ItemGeomancerArmor;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public class RenderGeomancerArmor extends MowzieGeoArmorRenderer<ItemGeomancerArmor> {
    protected GeoBone beads = null;
    protected GeoBone belt = null;
    protected GeoBone robe = null;

    public RenderGeomancerArmor() {
        super(new ModelGeomancerArmor());
    }

    @Override
    protected void grabRelevantBones(BakedGeoModel bakedModel) {
        super.grabRelevantBones(bakedModel);
        this.beads = this.model.getBone("prayer_beads").orElse(null);
        this.belt = this.model.getBone("belt").orElse(null);
        this.robe = this.model.getBone("robes").orElse(null);
    }

    @Override
    public void setAllVisible(boolean pVisible) {
        super.setAllVisible(pVisible);
        setBoneVisible(this.beads, false);
        setBoneVisible(this.belt, false);
        setBoneVisible(this.robe, false);
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        setAllVisible(false);

        switch (currentSlot) {
            case HEAD -> {
                setBoneVisible(this.getBodyBone(), true);
                setBoneVisible(this.beads, true);
                setBoneVisible(this.getHeadBone(), true);
            }
            case CHEST -> {
                setBoneVisible(this.getBodyBone(), true);
                setBoneVisible(this.robe, true);
                setBoneVisible(this.getRightArmBone(), true);
                setBoneVisible(this.getLeftArmBone(), true);
            }
            case LEGS -> {
                setBoneVisible(this.getBodyBone(), true);
                setBoneVisible(this.belt, true);
                setBoneVisible(this.getRightLegBone(), true);
                setBoneVisible(this.getLeftLegBone(), true);
            }
            case FEET -> {
                setBoneVisible(this.getRightBootBone(), true);
                setBoneVisible(this.getLeftBootBone(), true);
            }
            default -> {
            }
        }
    }
}
