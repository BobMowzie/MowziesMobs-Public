package com.bobmowzie.mowziesmobs.client.model.entity;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieGeoBone;
import com.bobmowzie.mowziesmobs.client.model.tools.geckolib.MowzieGeoModel;
import com.bobmowzie.mowziesmobs.server.entity.elokosa.EntityElokosa;
import com.bobmowzie.mowziesmobs.server.entity.elokosa.EntityElokosaHowler;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.data.EntityModelData;

public class ModelElokosa extends MowzieGeoModel<EntityElokosa> {
    public MowzieGeoBone[] tailOriginal;
    public MowzieGeoBone[] tailDynamic;

    public ModelElokosa() {
        super();
    }

    @Override
    public ResourceLocation getModelResource(EntityElokosa elokosa) {
        return new ResourceLocation(MowziesMobs.MODID, "geo/elokosa.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EntityElokosa elokosa) {
        boolean isElite = elokosa instanceof EntityElokosaHowler;
        return new ResourceLocation(MowziesMobs.MODID,"textures/entity/" + (isElite ? "elokosa_elite.png" : "elokosa.png"));
    }

    @Override
    public ResourceLocation getAnimationResource(EntityElokosa elokosa) {
        return new ResourceLocation(MowziesMobs.MODID,"animations/elokosa.animation.json");
    }

    @Override
    public void setCustomAnimations(EntityElokosa entity, long instanceId, AnimationState<EntityElokosa> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        boolean isElite = entity instanceof EntityElokosaHowler;

        MowzieGeoBone rootNight = getMowzieBone("root");
        MowzieGeoBone rootDay = getMowzieBone("root_day");
        float whichForm = -getControllerValue("whichFormController");
        if (whichForm <= 0.1f) {
            if (entity.getNightForm()) {
                rootDay.setHidden(true);
                rootNight.setHidden(false);
                entity.tailChain.setHidden(false);
            }
            else {
                rootDay.setHidden(false);
                rootNight.setHidden(true);
                entity.tailChain.setHidden(true);
            }
        }
        else if (whichForm >= 1f) {
            rootDay.setHidden(true);
            rootNight.setHidden(false);
            entity.tailChain.setHidden(false);
        }
        else {
            rootDay.setHidden(false);
            rootNight.setHidden(false);
            entity.tailChain.setHidden(false);
        }
        MowzieGeoBone rootShared = getMowzieBone("root_shared");
        MowzieGeoBone scythe = getMowzieBone("scythe");
        MowzieGeoBone beard = getMowzieBone("beard");
        MowzieGeoBone tailTuft = getMowzieBone("tailTuft");
        if (isElite) {
            MowzieGeoBone tuft1 = getMowzieBone("tuft1");
            MowzieGeoBone tuft2 = getMowzieBone("tuft2");
            MowzieGeoBone tuft3 = getMowzieBone("tuft3");
            MowzieGeoBone tuft4 = getMowzieBone("tuft4");
            MowzieGeoBone tail1 = getMowzieBone("tail1");
            rootShared.multiplyScale(1.185f, 1.185f, 1.185f);
            tail1.multiplyScale(1.085f/1.185f, 1.085f/1.185f, 1.085f/1.185f);
            tuft1.setScaleY(tuft1.getScaleY() * 1.18f);
            tuft2.setScaleY(tuft2.getScaleY() * 1.18f);
            tuft3.setScaleY(tuft3.getScaleY() * 1.18f);
            tuft4.setScaleY(tuft4.getScaleY() * 1.18f);
            scythe.setHidden(false);
            beard.setHidden(false);
            tailTuft.setHidden(true);
        }
        else {
            rootShared.multiplyScale(1.085f, 1.085f, 1.085f);
            scythe.setHidden(true);
            beard.setHidden(true);
            tailTuft.setHidden(false);
        }

        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        if (rootNight != null && !entity.onGround() && entity.getClingDirection() == Direction.DOWN && entity.getActiveAbilityType() == EntityElokosa.LEAP_ABILITY) {
            rootNight.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD * 0.2f);
        }
        float frame = entity.frame + animationState.getPartialTick();
        float ticks = entity.tickCount;

        MowzieGeoBone headLook = getMowzieBone("headLook");
        MowzieGeoBone headLookDay = getMowzieBone("head_day");

        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float headYaw = Mth.wrapDegrees(data.netHeadYaw());
        float headPitch = Mth.wrapDegrees(data.headPitch());
        if (headLook != null) {
            headLook.addRotX(headPitch * Math.PI / 180F);
            headLook.addRotY(headYaw * Math.PI / 180F);
            headLookDay.addRotX(headPitch * Math.PI / 180F);
            headLookDay.addRotY(headYaw * Math.PI / 180F);
        }

        MowzieGeoBone rightArm = getMowzieBone("rightArm");
        MowzieGeoBone rightHand = getMowzieBone("rightHand");
        MowzieGeoBone rightArmDay = getMowzieBone("ArmR_Joint_day");
        MowzieGeoBone rightHandDay = getMowzieBone("HandR_day");
        float wallClingController = getControllerValue("wallClingController");
        float upperClingBlockMissing = entity.missingWallClingUpperBlock ? 1f : 0f;
        rightArm.addRotX(-0.75 * wallClingController * upperClingBlockMissing);
        rightArm.addRotY(0.3 * wallClingController * upperClingBlockMissing);
        rightHand.addRotZ(0.4 * wallClingController * upperClingBlockMissing);
        rightHand.addRotX(-0.3 * wallClingController * upperClingBlockMissing);
        rightArmDay.addRotX(-1 * wallClingController * upperClingBlockMissing);
        rightArmDay.addRotY(-0.2 * wallClingController * upperClingBlockMissing);
        rightHandDay.addRotX(-0.8 * wallClingController * upperClingBlockMissing);
        rightHandDay.addRotY(-0.8 * wallClingController * upperClingBlockMissing);

        float animSpeed = 0.65f;
        float limbSwing = animationState.getLimbSwing();
        float limbSwingAmount = animationState.getLimbSwingAmount();

//        limbSwing = 0.5f * (entity.tickCount + animationState.getPartialTick());
//        limbSwingAmount = 1f;
////        float angle = 0.03f * (entity.tickCount + animationState.getPartialTick());
//        Vec3 moveVec = new Vec3(1, 0, 0);
////        moveVec = moveVec.normalize();
////        moveVec = moveVec.yRot(angle);

        double forward = Mth.lerp(animationState.getPartialTick(), entity.prevMoveDirForward, entity.moveDirForward);
        double backward = Mth.lerp(animationState.getPartialTick(), entity.prevMoveDirBackward, entity.moveDirBackward);
        double left = Mth.lerp(animationState.getPartialTick(), entity.prevMoveDirLeft, entity.moveDirLeft);
        double right = Mth.lerp(animationState.getPartialTick(), entity.prevMoveDirRight, entity.moveDirRight);
        limbSwingAmount *= 2;
        limbSwingAmount = Math.min(0.7f, limbSwingAmount);
        double locomotionAnimController = getControllerValue("locomotionAnimController");
        double runAnim = getControllerValue("walkRunSwitchController");
        double walkAnim = 1.0 - runAnim;
        walkForwardAnim(forward * locomotionAnimController * walkAnim, limbSwing, limbSwingAmount, animSpeed);
        walkBackwardAnim(backward * locomotionAnimController * walkAnim, limbSwing, limbSwingAmount, animSpeed);
        walkLeftAnim(left * locomotionAnimController * walkAnim, limbSwing, limbSwingAmount, animSpeed);
        walkRightAnim(right * locomotionAnimController * walkAnim, limbSwing, limbSwingAmount, animSpeed);
        runAnim(locomotionAnimController * runAnim, limbSwing, limbSwingAmount, animSpeed);

        walkAnimDay(locomotionAnimController * walkAnim, limbSwing, limbSwingAmount, animSpeed);
        runAnimDay(locomotionAnimController * runAnim, limbSwing, limbSwingAmount, animSpeed);

        if (!rootNight.isHidden()) {
            if (tailOriginal == null || tailDynamic == null) {
                tailOriginal = new MowzieGeoBone[]{getMowzieBone("tail1"), getMowzieBone("tail2"), getMowzieBone("tail3"), getMowzieBone("tail4"), getMowzieBone("tail5"), getMowzieBone("tail6")};
                tailDynamic = new MowzieGeoBone[tailOriginal.length];
            }
            if (entity.tailChain != null) {
                entity.tailChain.setChainArrays(tailOriginal, tailDynamic);
                double tailAlpha = getControllerValue("tailPhysicsBlendController");
                entity.tailChain.setAlpha((float) (1.0 - tailAlpha));
            }

        }
    }

    private void runAnim(double blend, float limbSwing, float limbSwingAmount, float speed) {
        MowzieGeoBone head = getMowzieBone("head");
        MowzieGeoBone headJoint = getMowzieBone("headJoint");
        MowzieGeoBone coM = getMowzieBone("CoM");
        MowzieGeoBone stomach = getMowzieBone("stomach");
        MowzieGeoBone upperBody = getMowzieBone("upperBody");
        MowzieGeoBone leftThigh = getMowzieBone("leftThigh");
        MowzieGeoBone leftCalf = getMowzieBone("leftCalf");
        MowzieGeoBone leftFoot = getMowzieBone("leftFoot");
        MowzieGeoBone leftToes = getMowzieBone("leftToes");
        MowzieGeoBone rightThigh = getMowzieBone("rightThigh");
        MowzieGeoBone rightCalf = getMowzieBone("rightCalf");
        MowzieGeoBone rightFoot = getMowzieBone("rightFoot");
        MowzieGeoBone rightToes = getMowzieBone("rightToes");
        MowzieGeoBone leftArm = getMowzieBone("leftArm");
        MowzieGeoBone leftForeArm = getMowzieBone("leftForeArm");
        MowzieGeoBone leftHand = getMowzieBone("leftHand");
        MowzieGeoBone rightArm = getMowzieBone("rightArm");
        MowzieGeoBone rightForeArm = getMowzieBone("rightForeArm");
        MowzieGeoBone rightHand = getMowzieBone("rightHand");

        float globalHeight = 1f;
        float globalDegree = 1f;
        speed *= 0.95f;

        coM.addPosY(blend * (Math.cos(limbSwing * speed) * 4f * globalHeight + 4 * globalHeight) * limbSwingAmount);
        coM.addPosZ(blend * (Math.cos(limbSwing * speed + 1.9) * 2f * globalHeight) * limbSwingAmount);
        stomach.addRotX(blend * (Math.cos(limbSwing * speed + 1.5) * -0.3 * globalHeight - 0.3f) * limbSwingAmount);
        upperBody.addRotX(blend * (Math.cos(limbSwing * speed - 0.4) * -0.4f * globalHeight + 0.15f) * limbSwingAmount);
        headJoint.addRotX(blend * (Math.cos(limbSwing * speed + 0.185) * 0.4 * globalHeight + 0.15 * globalHeight) * limbSwingAmount);
        headJoint.addPosY(blend * (Math.cos(limbSwing * speed + 1) * 2 * globalHeight) * limbSwingAmount);
        headJoint.addPosZ(blend * (Math.cos(limbSwing * speed + 1.9) * -1 * globalHeight) * limbSwingAmount);
        headJoint.addRotZ(blend * (Math.cos(limbSwing * speed + 0.185) * 0.16 * globalHeight) * limbSwingAmount);
        headJoint.addPosX(blend * (Math.cos(limbSwing * speed - 0.9) * -0.65 * globalHeight) * limbSwingAmount);

        double legTimingOffset = 0.9;
        leftThigh.addRotX(blend * (-Math.cos(limbSwing * speed + 0 + legTimingOffset) * 1.1f * globalDegree - 0.1 * globalDegree) * limbSwingAmount);
        leftThigh.addPosY(blend * (-Math.cos(limbSwing * speed + 0.5 + legTimingOffset) * 2f * globalDegree) * limbSwingAmount);
        leftThigh.addRotY(blend * (-Math.cos(limbSwing * speed + 0.5 + legTimingOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        leftCalf.addRotX(blend * (Math.cos(limbSwing * speed + 1.2 + legTimingOffset) * 0.7f * globalDegree + 0.2 * globalDegree) * limbSwingAmount);
        leftFoot.addRotX(blend * (Math.cos(limbSwing * speed + 1.2 + legTimingOffset) * -0.7f * globalDegree - 0.8f * globalDegree) * limbSwingAmount);
        leftToes.addRotX(blend * (-Math.cos(limbSwing * speed + 1.7 + legTimingOffset) * -1.7f * globalDegree - 1f * globalDegree) * limbSwingAmount);

        double rightLegOffset = 0.7;
        rightThigh.addRotX(blend * (-Math.cos(limbSwing * speed + 0 + legTimingOffset + rightLegOffset) * 1.1f * globalDegree - 0.1 * globalDegree) * limbSwingAmount);
        rightThigh.addPosY(blend * (-Math.cos(limbSwing * speed + 0.5 + legTimingOffset + rightLegOffset) * 2f * globalDegree) * limbSwingAmount);
        rightThigh.addRotY(blend * (Math.cos(limbSwing * speed + 0.5 + legTimingOffset + rightLegOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        rightCalf.addRotX(blend * (Math.cos(limbSwing * speed + 1.2 + legTimingOffset + rightLegOffset) * 0.7f * globalDegree + 0.2 * globalDegree) * limbSwingAmount);
        rightFoot.addRotX(blend * (Math.cos(limbSwing * speed + 1.2 + legTimingOffset + rightLegOffset) * -0.7f * globalDegree - 0.8f * globalDegree) * limbSwingAmount);
        rightToes.addRotX(blend * (-Math.cos(limbSwing * speed + 1.7 + legTimingOffset + rightLegOffset) * -1.7f * globalDegree - 1f * globalDegree) * limbSwingAmount);

        leftArm.addRotX(blend * -(Math.cos(limbSwing * speed + -0.8) * 0.5 * globalDegree + 0.5) * limbSwingAmount);
        leftArm.addRotZ(blend * (Math.cos(limbSwing * speed + 0.52) * 0.4 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotX(blend * (Math.cos(limbSwing * speed - 1.65) * -0.5 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotZ(blend * (Math.cos(limbSwing * speed - 1.65) * 0.9 * globalDegree) * limbSwingAmount);
        leftHand.addRotX(blend * (Math.cos(limbSwing * speed + 3) * -0.8 * globalDegree - 0.1) * limbSwingAmount);
        leftHand.addRotZ(blend * (Math.cos(limbSwing * speed- 1.65) * -0.3 * globalDegree) * limbSwingAmount);
        leftHand.addRotY(blend * (Math.cos(limbSwing * speed- 1.65) * -0.3 * globalDegree + 0.3) * limbSwingAmount);

        double rightArmOffset = 1.3;
        rightArm.addRotX(blend * -(Math.cos(limbSwing * speed + -0.8 + rightArmOffset) * 0.5 * globalDegree - 0.5) * limbSwingAmount);
        rightArm.addRotZ(blend * -(Math.cos(limbSwing * speed + 0.52 + rightArmOffset) * 0.4 * globalDegree) * limbSwingAmount);
        rightForeArm.addRotX(blend * (Math.cos(limbSwing * speed - 1.65 + rightArmOffset) * -0.5 * globalDegree - 0.87) * limbSwingAmount);
        rightForeArm.addRotZ(blend * (Math.cos(limbSwing * speed - 0.3 + rightArmOffset) * 0.7 * globalDegree - 0.2) * limbSwingAmount);
        rightHand.addRotX(blend * (Math.cos(limbSwing * speed + 3 + rightArmOffset) * -0.8 * globalDegree - 0.1) * limbSwingAmount);
        rightHand.addRotY(blend * -(Math.cos(limbSwing * speed - 0.3 + rightArmOffset) * -0.3 * globalDegree - 0.3) * limbSwingAmount);

        MowzieGeoBone tail1 = getMowzieBone("tail1");
        MowzieGeoBone tail2 = getMowzieBone("tail2");
        MowzieGeoBone tail3 = getMowzieBone("tail3");
        MowzieGeoBone tail4 = getMowzieBone("tail4");
        MowzieGeoBone tail5 = getMowzieBone("tail5");
        tail1.addRotX(blend * (Math.cos(limbSwing * speed + 1.76) * 0.2f * globalHeight) * limbSwingAmount);
        tail2.addRotX(blend * (Math.cos(limbSwing * speed + 1.76 - 0.5) * 0.2f * globalHeight) * limbSwingAmount);
        tail3.addRotX(blend * (Math.cos(limbSwing * speed + 1.76 - 1) * 0.2f * globalHeight) * limbSwingAmount);
        tail4.addRotX(blend * (Math.cos(limbSwing * speed + 1.76 - 1.5) * 0.2f * globalHeight) * limbSwingAmount);
        tail5.addRotX(blend * (Math.cos(limbSwing * speed + 1.76 - 2) * 0.2f * globalHeight) * limbSwingAmount);

        MowzieGeoBone tuft1 = getMowzieBone("tuft1");
        MowzieGeoBone tuft2 = getMowzieBone("tuft2");
        MowzieGeoBone tuft3 = getMowzieBone("tuft3");
        MowzieGeoBone tuft4 = getMowzieBone("tuft4");
        tuft1.addRotX(blend * (Math.cos(limbSwing * speed + 1.76) * 0.1f * globalHeight) * limbSwingAmount);
        tuft2.addRotX(blend * (Math.cos(limbSwing * speed + 1.76 - 0.25) * 0.02f * globalHeight) * limbSwingAmount);
        tuft2.addRotX(blend * (Math.cos(limbSwing * speed + 1.76 - 0.5) * 0.1f * globalHeight) * limbSwingAmount);
        tuft4.addRotX(blend * (Math.cos(limbSwing * speed + 1.76 - 0.5) * 0.1f * globalHeight) * limbSwingAmount);
    }

    private void walkForwardAnim(double blend, float limbSwing, float limbSwingAmount, float speed) {
        MowzieGeoBone head = getMowzieBone("head");
        MowzieGeoBone headJoint = getMowzieBone("headJoint");
        MowzieGeoBone coM = getMowzieBone("CoM");
        MowzieGeoBone legs = getMowzieBone("legs");
        MowzieGeoBone stomach = getMowzieBone("stomach");
        MowzieGeoBone upperBody = getMowzieBone("upperBody");
        MowzieGeoBone leftThigh = getMowzieBone("leftThigh");
        MowzieGeoBone leftCalf = getMowzieBone("leftCalf");
        MowzieGeoBone leftFoot = getMowzieBone("leftFoot");
        MowzieGeoBone leftToes = getMowzieBone("leftToes");
        MowzieGeoBone rightThigh = getMowzieBone("rightThigh");
        MowzieGeoBone rightCalf = getMowzieBone("rightCalf");
        MowzieGeoBone rightFoot = getMowzieBone("rightFoot");
        MowzieGeoBone rightToes = getMowzieBone("rightToes");
        MowzieGeoBone leftArm = getMowzieBone("leftArm");
        MowzieGeoBone leftForeArm = getMowzieBone("leftForeArm");
        MowzieGeoBone leftHand = getMowzieBone("leftHand");
        MowzieGeoBone rightArm = getMowzieBone("rightArm");
        MowzieGeoBone rightForeArm = getMowzieBone("rightForeArm");
        MowzieGeoBone rightHand = getMowzieBone("rightHand");

        speed *= 2.1f;
        float globalHeight = 1.1f;
        float globalDegree = 0.9f;

        coM.addPosY(blend * (Math.cos(limbSwing * speed) * 0.3f * globalHeight + 0.1 * globalHeight) * limbSwingAmount);
        coM.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.2f * globalHeight) * limbSwingAmount);
        coM.addRotX(blend * (poweredWave(limbSwing, speed, -1.5f, 3f) * -0.12f * globalHeight - 0.075f * globalHeight) * limbSwingAmount);
        stomach.addRotX( blend * -0.1f * globalHeight * limbSwingAmount);
        headJoint.addRotX( blend * 0.1f * globalHeight * limbSwingAmount);
        headJoint.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        headJoint.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.2f * globalHeight) * limbSwingAmount);
        legs.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        leftArm.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        rightArm.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        upperBody.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.3f * globalHeight) * limbSwingAmount);
        upperBody.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.3f * globalHeight) * limbSwingAmount);
        legs.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.15f * globalHeight) * limbSwingAmount);
        legs.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.15f * globalHeight) * limbSwingAmount);

        upperBody.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        upperBody.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        headJoint.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        headJoint.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        leftArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.2f * globalHeight) * limbSwingAmount);
        rightArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.2f * globalHeight) * limbSwingAmount);
        leftArm.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, 2f, 3f) * -3f * globalHeight * limbSwingAmount);
        leftArm.addPosY(blend * 1f * globalHeight * limbSwingAmount);
        rightArm.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, Mth.PI+2f, 3f) * -3f * globalHeight * limbSwingAmount);
        rightArm.addPosY(blend * 1f * globalHeight * limbSwingAmount);
        leftArm.addPosY(blend * (Math.cos(limbSwing * speed * 0.5f - 0f) * 1f * globalHeight + 0.9f * globalHeight) * limbSwingAmount);
        rightArm.addPosY(blend * (Math.cos(limbSwing * speed * 0.5f - 0f) * -1f * globalHeight + 0.9f * globalHeight) * limbSwingAmount);

        leftArm.addRotX(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.5 * globalDegree + 0 * globalDegree) * limbSwingAmount);
        leftArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0.52) * 0.4 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 - 1.65) * -0.5 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 - 1.65) * 0.9 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotY(blend * (Math.cos(limbSwing * speed * 0.5) * -0.7 * globalDegree) * limbSwingAmount);
        leftHand.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 3) * -0.8 * globalDegree - 0.1) * limbSwingAmount);
        leftHand.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5- 1.65) * -0.3 * globalDegree + -0.4 * globalDegree) * limbSwingAmount);
        leftHand.addRotY(blend * (Math.cos(limbSwing * speed * 0.5- 1.65) * -0.3 * globalDegree + 0.3) * limbSwingAmount);
        leftHand.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, 1f, 2.5f) * -0.9f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);

        double rightArmOffset = (Math.PI);
        rightArm.addRotX(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8 + rightArmOffset) * 0.5 * globalDegree - 1 * globalDegree) * limbSwingAmount);
        rightArm.addRotZ(blend * -(Math.cos(limbSwing * speed * 0.5 + 0.52 + rightArmOffset) * 0.4 * globalDegree) * limbSwingAmount);
        rightForeArm.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 - 1.65 + rightArmOffset) * -0.25 * globalDegree - 0.87 * globalDegree) * limbSwingAmount);
        rightForeArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 - 0.3 + rightArmOffset) * 0.7 * globalDegree - 0.2 * globalDegree) * limbSwingAmount);
        rightForeArm.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + rightArmOffset) * 0.4 * globalDegree) * limbSwingAmount);
        rightHand.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 3 + rightArmOffset) * -0.8 * globalDegree + 0.1 * globalDegree) * limbSwingAmount);
        rightHand.addRotZ(blend * (poweredWave(limbSwing, speed * 0.5f,- 0 + rightArmOffset, 2f) * -0.8 * globalDegree + 0.3 * globalDegree) * limbSwingAmount);
        rightHand.addRotY(blend * -(Math.cos(limbSwing * speed * 0.5 - 0.52 + rightArmOffset) * -0.6 * globalDegree + 0.3 * globalDegree) * limbSwingAmount);
        rightHand.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, Mth.PI+1f, 2.5f) * -0.9f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);

        double legTimingOffset = 1.5;
        leftThigh.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.15f * globalHeight) * limbSwingAmount);
        leftThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.35f * globalHeight) * limbSwingAmount);
        leftThigh.addRotX(blend * (-Math.cos(limbSwing * speed * 0.5 + 0 + legTimingOffset) * 0.8f * globalDegree + 0.1 * globalDegree) * limbSwingAmount);
        leftThigh.addPosY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset) * 0.8f * globalHeight + 1 * globalHeight) * limbSwingAmount);
        leftThigh.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, -1.5f, 3f) * -1f * globalHeight * limbSwingAmount);
        leftThigh.addPosZ(blend * poweredWave(limbSwing, speed * 0.5f, -1.5f, 3f) * -1f * globalHeight * limbSwingAmount);
        leftThigh.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        leftCalf.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 2.2 + legTimingOffset) * 0.7f * globalDegree + 0.2 * globalDegree) * limbSwingAmount);
        leftFoot.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 2.2 + legTimingOffset) * -0.7f * globalDegree - 0.8f * globalDegree) * limbSwingAmount);
        leftToes.addRotX(blend * (-Math.cos(limbSwing * speed * 0.5 + 2.5 + legTimingOffset) * -1.7f * globalDegree - 1f * globalDegree) * limbSwingAmount);
        leftToes.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, -2f, 4f) * -1f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);

        double rightLegOffset = (Math.PI);
        rightThigh.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.15f * globalHeight) * limbSwingAmount);
        rightThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.35f * globalHeight) * limbSwingAmount);
        rightThigh.addRotX(blend * (-Math.cos(limbSwing * speed * 0.5 + 0 + legTimingOffset + rightLegOffset) * 0.8f * globalDegree + 0.4 * globalDegree) * limbSwingAmount);
        rightThigh.addPosY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset + rightLegOffset) * 0.8f * globalHeight + 1 * globalHeight) * limbSwingAmount);
        rightThigh.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, -1.5f + Mth.PI, 3f) * -0.5f * globalHeight * limbSwingAmount);
        rightThigh.addPosZ(blend * poweredWave(limbSwing, speed * 0.5f, -1.5f + Mth.PI, 3f) * -0.5f * globalHeight * limbSwingAmount);
        rightThigh.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 0.5 + legTimingOffset + rightLegOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        rightCalf.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 2.2 + legTimingOffset + rightLegOffset) * 0.7f * globalDegree + 0.2 * globalDegree) * limbSwingAmount);
        rightFoot.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 2.2 + legTimingOffset + rightLegOffset) * -0.7f * globalDegree - 0.8f * globalDegree) * limbSwingAmount);
        rightToes.addRotX(blend * (-Math.cos(limbSwing * speed * 0.5 + 2.5 + legTimingOffset + rightLegOffset) * -1.7f * globalDegree - 1f * globalDegree) * limbSwingAmount);
        rightToes.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, -2f + Mth.PI, 4f) * -1f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);

        MowzieGeoBone tail1 = getMowzieBone("tail1");
        MowzieGeoBone tail2 = getMowzieBone("tail2");
        MowzieGeoBone tail3 = getMowzieBone("tail3");
        MowzieGeoBone tail4 = getMowzieBone("tail4");
        MowzieGeoBone tail5 = getMowzieBone("tail5");
        tail1.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 3.46) * -0.13f * globalHeight) * limbSwingAmount);
        tail1.addRotX(blend * (Math.cos(limbSwing * speed + 3.46) * -0.13f * globalHeight) * limbSwingAmount);
        tail2.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 3.46 - 0.5) * 0.13f * globalHeight) * limbSwingAmount);
        tail2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.13f * globalHeight) * limbSwingAmount);
        tail3.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 1) * -0.13f * globalHeight) * limbSwingAmount);
        tail4.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 1.5) * -0.13f * globalHeight) * limbSwingAmount);
        tail5.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 2) * -0.13f * globalHeight) * limbSwingAmount);

        MowzieGeoBone tuft1 = getMowzieBone("tuft1");
        MowzieGeoBone tuft2 = getMowzieBone("tuft2");
        MowzieGeoBone tuft3 = getMowzieBone("tuft3");
        MowzieGeoBone tuft4 = getMowzieBone("tuft4");
        tuft1.addRotX(blend * (Math.cos(limbSwing * speed + 3.46) * -0.065f * globalHeight) * limbSwingAmount);
        tuft2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.25) * -0.02f * globalHeight) * limbSwingAmount);
        tuft2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.065f * globalHeight) * limbSwingAmount);
        tuft4.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.065f * globalHeight) * limbSwingAmount);
    }

    private void walkBackwardAnim(double blend, float limbSwing, float limbSwingAmount, float speed) {
        MowzieGeoBone head = getMowzieBone("head");
        MowzieGeoBone headJoint = getMowzieBone("headJoint");
        MowzieGeoBone coM = getMowzieBone("CoM");
        MowzieGeoBone legs = getMowzieBone("legs");
        MowzieGeoBone stomach = getMowzieBone("stomach");
        MowzieGeoBone upperBody = getMowzieBone("upperBody");
        MowzieGeoBone leftThigh = getMowzieBone("leftThigh");
        MowzieGeoBone leftCalf = getMowzieBone("leftCalf");
        MowzieGeoBone leftFoot = getMowzieBone("leftFoot");
        MowzieGeoBone leftToes = getMowzieBone("leftToes");
        MowzieGeoBone rightThigh = getMowzieBone("rightThigh");
        MowzieGeoBone rightCalf = getMowzieBone("rightCalf");
        MowzieGeoBone rightFoot = getMowzieBone("rightFoot");
        MowzieGeoBone rightToes = getMowzieBone("rightToes");
        MowzieGeoBone leftArm = getMowzieBone("leftArm");
        MowzieGeoBone leftForeArm = getMowzieBone("leftForeArm");
        MowzieGeoBone leftHand = getMowzieBone("leftHand");
        MowzieGeoBone rightArm = getMowzieBone("rightArm");
        MowzieGeoBone rightForeArm = getMowzieBone("rightForeArm");
        MowzieGeoBone rightHand = getMowzieBone("rightHand");

        speed *= 2.1f;
        float globalHeight = 1.1f;
        float globalDegree = 0.9f;
        double sideFlipTimingOffset = 0;//Mth.PI/2f;
        coM.addPosY(blend * (Math.cos(-limbSwing * speed + sideFlipTimingOffset) * 0.3f * globalHeight + 0.1 * globalHeight) * limbSwingAmount);
        coM.addRotY(blend * (Math.cos(-limbSwing * speed * 0.5 + -0.8 + sideFlipTimingOffset) * -0.2f * globalHeight) * limbSwingAmount);
        coM.addRotX(blend * (poweredWave(-limbSwing, speed, -1.5f + sideFlipTimingOffset, 3f) * -0.15f * globalHeight - 0.075f * globalHeight) * limbSwingAmount);
        stomach.addRotX( blend * -0.1f * globalHeight * limbSwingAmount);
        headJoint.addRotX( blend * 0.1f * globalHeight * limbSwingAmount);
        headJoint.addRotX(blend * poweredWave(-limbSwing, speed, -1.5f + sideFlipTimingOffset, 3f) * 0.15f * globalHeight * limbSwingAmount);
        headJoint.addRotY(blend * (Math.cos(-limbSwing * speed * 0.5 + -0.8 + sideFlipTimingOffset) * -0.2f * globalHeight) * limbSwingAmount);
        legs.addRotX(blend * poweredWave(-limbSwing, speed, - + sideFlipTimingOffset, 3f) * 0.15f * globalHeight * limbSwingAmount);
        leftArm.addRotX(blend * poweredWave(-limbSwing, speed, -1.5f + sideFlipTimingOffset, 3f) * 0.15f * globalHeight * limbSwingAmount);
        rightArm.addRotX(blend * poweredWave(-limbSwing, speed, -1.5f + sideFlipTimingOffset, 3f) * 0.15f * globalHeight * limbSwingAmount);
        upperBody.addRotY(blend * (Math.cos(-limbSwing * speed * 0.5 + -0.8 + sideFlipTimingOffset) * 0.3f * globalHeight) * limbSwingAmount);
        upperBody.addRotZ(blend * (Math.cos(-limbSwing * speed * 0.5 + -0.8 + sideFlipTimingOffset) * 0.3f * globalHeight) * limbSwingAmount);
        legs.addRotY(blend * (Math.cos(-limbSwing * speed * 0.5 + -0.8 + sideFlipTimingOffset) * -0.15f * globalHeight) * limbSwingAmount);
        legs.addRotZ(blend * (Math.cos(-limbSwing * speed * 0.5 + -0.8 + sideFlipTimingOffset) * -0.15f * globalHeight) * limbSwingAmount);

        upperBody.addRotY(blend * (Math.cos(-limbSwing * speed * 0.5 + sideFlipTimingOffset) * 0.1f * globalHeight) * limbSwingAmount);
        upperBody.addRotZ(blend * (-Math.cos(-limbSwing * speed * 0.5 + sideFlipTimingOffset) * 0.1f * globalHeight) * limbSwingAmount);
        headJoint.addRotY(blend * (-Math.cos(-limbSwing * speed * 0.5 + sideFlipTimingOffset) * 0.1f * globalHeight) * limbSwingAmount);
        headJoint.addRotZ(blend * (Math.cos(-limbSwing * speed * 0.5 + sideFlipTimingOffset) * 0.1f * globalHeight) * limbSwingAmount);
        leftArm.addRotZ(blend * (Math.cos(-limbSwing * speed * 0.5 + sideFlipTimingOffset) * 0.2f * globalHeight) * limbSwingAmount);
        rightArm.addRotZ(blend * (Math.cos(-limbSwing * speed * 0.5 + sideFlipTimingOffset) * 0.2f * globalHeight) * limbSwingAmount);
        leftArm.addPosY(blend * poweredWave(-limbSwing, speed * 0.5f, 2f + sideFlipTimingOffset, 3f) * -3f * globalHeight * limbSwingAmount);
        leftArm.addPosY(blend * 1f * globalHeight * limbSwingAmount);
        rightArm.addPosY(blend * poweredWave(-limbSwing, speed * 0.5f, Mth.PI+2f + sideFlipTimingOffset, 3f) * -3f * globalHeight * limbSwingAmount);
        rightArm.addPosY(blend * 1f * globalHeight * limbSwingAmount);
        leftArm.addPosY(blend * (Math.cos(-limbSwing * speed * 0.5f + sideFlipTimingOffset) * 1f * globalHeight + 0.9f * globalHeight) * limbSwingAmount);
        rightArm.addPosY(blend * (Math.cos(-limbSwing * speed * 0.5f + sideFlipTimingOffset) * -1f * globalHeight + 0.9f * globalHeight) * limbSwingAmount);

        leftArm.addRotX(blend * -(Math.cos(-limbSwing * speed * 0.5 + -0.8 + sideFlipTimingOffset) * 0.5 * globalDegree + 0 * globalDegree) * limbSwingAmount);
        leftArm.addRotZ(blend * (Math.cos(-limbSwing * speed * 0.5 + 0.52 + sideFlipTimingOffset) * 0.4 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotX(blend * (Math.cos(-limbSwing * speed * 0.5 - 1.65 + sideFlipTimingOffset) * -0.5 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotZ(blend * (Math.cos(-limbSwing * speed * 0.5 - 1.65 + sideFlipTimingOffset) * 0.9 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotY(blend * (Math.cos(-limbSwing * speed * 0.5 + sideFlipTimingOffset) * -0.7 * globalDegree) * limbSwingAmount);
        leftHand.addRotX(blend * (Math.cos(-limbSwing * speed * 0.5 + 3 + sideFlipTimingOffset) * -0.8 * globalDegree - 0.1) * limbSwingAmount);
        leftHand.addRotZ(blend * (Math.cos(-limbSwing * speed * 0.5- 1.65 + sideFlipTimingOffset) * -0.3 * globalDegree + -0.4 * globalDegree) * limbSwingAmount);
        leftHand.addRotY(blend * (Math.cos(-limbSwing * speed * 0.5- 1.65 + sideFlipTimingOffset) * -0.3 * globalDegree + 0.3) * limbSwingAmount);
        leftHand.addRotX(blend * (poweredWave(-limbSwing, speed * 0.5f, 1f + sideFlipTimingOffset, 2.5f) * -0.9f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);

        double rightArmOffset = (Math.PI);
        rightArm.addRotX(blend * -(Math.cos(-limbSwing * speed * 0.5 + -0.8 + rightArmOffset + sideFlipTimingOffset) * 0.5 * globalDegree - 1 * globalDegree) * limbSwingAmount);
        rightArm.addRotZ(blend * -(Math.cos(-limbSwing * speed * 0.5 + 0.52 + rightArmOffset + sideFlipTimingOffset) * 0.4 * globalDegree) * limbSwingAmount);
        rightForeArm.addRotX(blend * (Math.cos(-limbSwing * speed * 0.5 - 1.65 + rightArmOffset + sideFlipTimingOffset) * -0.25 * globalDegree - 0.87 * globalDegree) * limbSwingAmount);
        rightForeArm.addRotZ(blend * (Math.cos(-limbSwing * speed * 0.5 - 0.3 + rightArmOffset + sideFlipTimingOffset) * 0.7 * globalDegree - 0.2 * globalDegree) * limbSwingAmount);
        rightForeArm.addRotY(blend * (Math.cos(-limbSwing * speed * 0.5 + rightArmOffset + sideFlipTimingOffset) * 0.4 * globalDegree) * limbSwingAmount);
        rightHand.addRotX(blend * (Math.cos(-limbSwing * speed * 0.5 + 3 + rightArmOffset + sideFlipTimingOffset) * -0.8 * globalDegree + 0.1 * globalDegree) * limbSwingAmount);
        rightHand.addRotZ(blend * (poweredWave(-limbSwing, speed * 0.5f,- 0 + rightArmOffset + sideFlipTimingOffset, 2f) * -0.8 * globalDegree + 0.3 * globalDegree) * limbSwingAmount);
        rightHand.addRotY(blend * -(Math.cos(-limbSwing * speed * 0.5 - 0.52 + rightArmOffset + sideFlipTimingOffset) * -0.6 * globalDegree + 0.3 * globalDegree) * limbSwingAmount);
        rightHand.addRotX(blend * (poweredWave(-limbSwing, speed * 0.5f, Mth.PI+1f + sideFlipTimingOffset, 2.5f) * -0.9f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);

        leftArm.setHidden(false);
        rightArm.setHidden(false);
        double legTimingOffset = 1.5;
        leftThigh.addRotY(blend * (Math.cos(-limbSwing * speed * 0.5 + -0.8 + sideFlipTimingOffset) * 0.15f * globalHeight) * limbSwingAmount);
        leftThigh.addRotZ(blend * (Math.cos(-limbSwing * speed * 0.5 + sideFlipTimingOffset) * 0.35f * globalHeight) * limbSwingAmount);
        leftThigh.addRotX(blend * (-Math.cos(-limbSwing * speed * 0.5 + 0 + legTimingOffset + sideFlipTimingOffset) * 0.8f * globalDegree + 0.1 * globalDegree) * limbSwingAmount);
        leftThigh.addPosY(blend * (-Math.cos(-limbSwing * speed * 0.5 + 1.5 + legTimingOffset + sideFlipTimingOffset) * 0.8f * globalHeight + 1 * globalHeight) * limbSwingAmount);
        leftThigh.addPosY(blend * poweredWave(-limbSwing, speed * 0.5f, -1.5f + Mth.PI + sideFlipTimingOffset, 3f) * -1f * globalHeight * limbSwingAmount);
        leftThigh.addPosZ(blend * poweredWave(-limbSwing, speed * 0.5f, -1.5f + Mth.PI + sideFlipTimingOffset, 3f) * -1f * globalHeight * limbSwingAmount);
        leftThigh.addRotY(blend * (-Math.cos(-limbSwing * speed * 0.5 + 1.5 + legTimingOffset + sideFlipTimingOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        leftCalf.addRotX(blend * (Math.cos(-limbSwing * speed * 0.5 + 1 + legTimingOffset + sideFlipTimingOffset) * 0.7f * globalDegree + 0.2 * globalDegree) * limbSwingAmount);
        leftFoot.addRotX(blend * (Math.cos(-limbSwing * speed * 0.5 + 1 + legTimingOffset + sideFlipTimingOffset) * -0.7f * globalDegree - 0.8f * globalDegree) * limbSwingAmount);
        leftToes.addRotX(blend * (-Math.cos(-limbSwing * speed * 0.5 + 2 + legTimingOffset + sideFlipTimingOffset) * -1.2f * globalDegree - 1f * globalDegree) * limbSwingAmount);
        leftToes.addRotX(blend * (poweredWave(-limbSwing, speed * 0.5f, -5f + sideFlipTimingOffset, 4f) * 2.5f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);

        double rightLegOffset = (Math.PI);
        rightThigh.addRotY(blend * (Math.cos(-limbSwing * speed * 0.5 + -0.8 + sideFlipTimingOffset) * 0.15f * globalHeight) * limbSwingAmount);
        rightThigh.addRotZ(blend * (Math.cos(-limbSwing * speed * 0.5 + sideFlipTimingOffset) * 0.35f * globalHeight) * limbSwingAmount);
        rightThigh.addRotX(blend * (-Math.cos(-limbSwing * speed * 0.5 + 0 + legTimingOffset + rightLegOffset + sideFlipTimingOffset) * 0.8f * globalDegree + 0.4 * globalDegree) * limbSwingAmount);
        rightThigh.addPosY(blend * (-Math.cos(-limbSwing * speed * 0.5 + 1.5 + legTimingOffset + rightLegOffset + sideFlipTimingOffset) * 0.8f * globalHeight + 1 * globalHeight) * limbSwingAmount);
        rightThigh.addPosY(blend * poweredWave(-limbSwing, speed * 0.5f, -1.5f + Mth.PI + sideFlipTimingOffset, 3f) * -0.5f * globalHeight * limbSwingAmount);
        rightThigh.addPosZ(blend * poweredWave(-limbSwing, speed * 0.5f, -1.5f + Mth.PI + sideFlipTimingOffset, 3f) * -0.5f * globalHeight * limbSwingAmount);
        rightThigh.addRotY(blend * (Math.cos(-limbSwing * speed * 0.5 + 0.5 + legTimingOffset + rightLegOffset + sideFlipTimingOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        rightCalf.addRotX(blend * (Math.cos(-limbSwing * speed * 0.5 + 2.2 + legTimingOffset + rightLegOffset + sideFlipTimingOffset) * 0.7f * globalDegree + 0.2 * globalDegree) * limbSwingAmount);
        rightFoot.addRotX(blend * (Math.cos(-limbSwing * speed * 0.5 + 2.2 + legTimingOffset + rightLegOffset + sideFlipTimingOffset) * -0.7f * globalDegree - 0.8f * globalDegree) * limbSwingAmount);
        rightToes.addRotX(blend * (-Math.cos(-limbSwing * speed * 0.5 + 2.5 + legTimingOffset + rightLegOffset + sideFlipTimingOffset) * -1.7f * globalDegree - 1f * globalDegree) * limbSwingAmount);
        rightToes.addRotX(blend * (poweredWave(-limbSwing, speed * 0.5f, -2f + Mth.PI + sideFlipTimingOffset, 4f) * -1f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);

        MowzieGeoBone tail1 = getMowzieBone("tail1");
        MowzieGeoBone tail2 = getMowzieBone("tail2");
        MowzieGeoBone tail3 = getMowzieBone("tail3");
        MowzieGeoBone tail4 = getMowzieBone("tail4");
        MowzieGeoBone tail5 = getMowzieBone("tail5");
        tail1.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 3.46) * -0.13f * globalHeight) * limbSwingAmount);
        tail1.addRotX(blend * (Math.cos(limbSwing * speed + 3.46) * -0.13f * globalHeight) * limbSwingAmount);
        tail2.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 3.46 - 0.5) * 0.13f * globalHeight) * limbSwingAmount);
        tail2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.13f * globalHeight) * limbSwingAmount);
        tail3.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 1) * -0.13f * globalHeight) * limbSwingAmount);
        tail4.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 1.5) * -0.13f * globalHeight) * limbSwingAmount);
        tail5.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 2) * -0.13f * globalHeight) * limbSwingAmount);

        MowzieGeoBone tuft1 = getMowzieBone("tuft1");
        MowzieGeoBone tuft2 = getMowzieBone("tuft2");
        MowzieGeoBone tuft3 = getMowzieBone("tuft3");
        MowzieGeoBone tuft4 = getMowzieBone("tuft4");
        tuft1.addRotX(blend * (Math.cos(limbSwing * speed + 3.46) * -0.065f * globalHeight) * limbSwingAmount);
        tuft2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.25) * -0.02f * globalHeight) * limbSwingAmount);
        tuft2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.065f * globalHeight) * limbSwingAmount);
        tuft4.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.065f * globalHeight) * limbSwingAmount);
    }

    private void walkLeftAnim(double blend, float limbSwing, float limbSwingAmount, float speed) {
        MowzieGeoBone head = getMowzieBone("head");
        MowzieGeoBone headJoint = getMowzieBone("headJoint");
        MowzieGeoBone coM = getMowzieBone("CoM");
        MowzieGeoBone legs = getMowzieBone("legs");
        MowzieGeoBone stomach = getMowzieBone("stomach");
        MowzieGeoBone upperBody = getMowzieBone("upperBody");
        MowzieGeoBone leftThigh = getMowzieBone("leftThigh");
        MowzieGeoBone leftCalf = getMowzieBone("leftCalf");
        MowzieGeoBone leftFoot = getMowzieBone("leftFoot");
        MowzieGeoBone leftToes = getMowzieBone("leftToes");
        MowzieGeoBone rightThigh = getMowzieBone("rightThigh");
        MowzieGeoBone rightCalf = getMowzieBone("rightCalf");
        MowzieGeoBone rightFoot = getMowzieBone("rightFoot");
        MowzieGeoBone rightToes = getMowzieBone("rightToes");
        MowzieGeoBone leftArm = getMowzieBone("leftArm");
        MowzieGeoBone leftForeArm = getMowzieBone("leftForeArm");
        MowzieGeoBone leftHand = getMowzieBone("leftHand");
        MowzieGeoBone rightArm = getMowzieBone("rightArm");
        MowzieGeoBone rightForeArm = getMowzieBone("rightForeArm");
        MowzieGeoBone rightHand = getMowzieBone("rightHand");

        speed *= 2.1f;
        float globalHeight = 1.1f;
        float globalDegree = 0.9f;

        coM.addPosY(blend * (Math.cos(limbSwing * speed) * 0.3f * globalHeight + 0.1 * globalHeight) * limbSwingAmount);
        coM.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.2f * globalHeight) * limbSwingAmount);
        coM.addRotX(blend * (poweredWave(limbSwing, speed, -1.5f, 3f) * -0.15f * globalHeight - 0.075f * globalHeight) * limbSwingAmount);
        stomach.addRotX( blend * -0.1f * globalHeight * limbSwingAmount);
        headJoint.addRotX( blend * 0.1f * globalHeight * limbSwingAmount);
        headJoint.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        headJoint.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.15f * globalHeight) * limbSwingAmount);
        legs.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        leftArm.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        rightArm.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        upperBody.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.3f * globalHeight) * limbSwingAmount);
        upperBody.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.3f * globalHeight) * limbSwingAmount);
        legs.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.15f * globalHeight) * limbSwingAmount);
        legs.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.15f * globalHeight) * limbSwingAmount);

        double rightArmOffset = (Math.PI) - 0.5;
        upperBody.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        upperBody.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        headJoint.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        headJoint.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        leftArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.2f * globalHeight) * limbSwingAmount);
        rightArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.2f * globalHeight) * limbSwingAmount);
        leftArm.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, 2f, 3f) * -3f * globalHeight * limbSwingAmount);
        leftArm.addPosY(blend * 1f * globalHeight * limbSwingAmount);
        rightArm.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, 3f + rightArmOffset, 6f) * -2f * globalHeight * limbSwingAmount);
        rightArm.addPosY(blend * 1f * globalHeight * limbSwingAmount);
        leftArm.addPosY(blend * (Math.cos(limbSwing * speed * 0.5f - 0f) * 2f * globalHeight + 0.9f * globalHeight) * limbSwingAmount);
        rightArm.addPosY(blend * (Math.cos(limbSwing * speed * 0.5f - 0.8f + rightArmOffset) * -2f * globalHeight + 0.9f * globalHeight) * limbSwingAmount);

        leftArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.5 * globalDegree + 0 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 - 2) * -0.9 * globalDegree + 0.2 * globalDegree) * limbSwingAmount);
        leftHand.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 - 1) * 1.2 * globalDegree - 0.1 * globalDegree) * limbSwingAmount);

        rightArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8 + rightArmOffset) * 0.5 * globalDegree - 0 * globalDegree) * limbSwingAmount);
        rightForeArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 - 2 + rightArmOffset) * -0.9 * globalDegree - 0.0 * globalDegree) * limbSwingAmount);
        rightForeArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 - 0.8 + rightArmOffset) * 0.6 * globalDegree - 0.0 * globalDegree) * limbSwingAmount);
        rightHand.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 - 3.2 + rightArmOffset) * 0.5 * globalDegree) * limbSwingAmount);
        rightHand.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 - 3.2 + rightArmOffset) * 0.8 * globalDegree) * limbSwingAmount);


//        leftArm.setHidden(false);
//        rightArm.setHidden(false);
        double legTimingOffset = 3.1;
        leftThigh.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.15f * globalHeight) * limbSwingAmount);
//        leftThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.35f * globalHeight) * limbSwingAmount);
        leftThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0 + legTimingOffset) * 0.8f * globalDegree + 0.1 * globalDegree) * limbSwingAmount);
        leftThigh.addPosY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset) * 0.5f * globalHeight + 1 * globalHeight) * limbSwingAmount);
        leftThigh.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, -1.5f, 3f) * -0.5f * globalHeight * limbSwingAmount);
        leftThigh.addPosZ(blend * poweredWave(limbSwing, speed * 0.5f, -1.5f, 3f) * -0.5f * globalHeight * limbSwingAmount);
        leftThigh.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        leftCalf.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 1.7 + legTimingOffset) * 0.9f * globalDegree + 0.2 * globalDegree) * limbSwingAmount);
        leftFoot.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 1.7 + legTimingOffset) * -0.9f * globalDegree - 0.8f * globalDegree) * limbSwingAmount);
        leftToes.addRotX(blend * (-Math.cos(limbSwing * speed * 0.5 + 1 + legTimingOffset) * -1.7f * globalDegree - 1f * globalDegree) * limbSwingAmount);
        leftToes.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, -3.5f, 4f) * -1f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);
//
        double rightLegOffset = (Math.PI);
        rightThigh.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.15f * globalHeight) * limbSwingAmount);
//        rightThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.35f * globalHeight) * limbSwingAmount);
        rightThigh.addRotX(blend * 0.2f * globalDegree * limbSwingAmount);
        rightThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0 + legTimingOffset + rightLegOffset) * 0.8f * globalDegree) * limbSwingAmount);
        rightThigh.addPosY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset + rightLegOffset) * 0.5f * globalHeight + 1 * globalHeight) * limbSwingAmount);
        rightThigh.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, -2f + Mth.PI, 3f) * -1f * globalHeight * limbSwingAmount);
        rightThigh.addPosZ(blend * poweredWave(limbSwing, speed * 0.5f, -2f + Mth.PI, 3f) * -1f * globalHeight * limbSwingAmount);
        rightThigh.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset + rightLegOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        rightCalf.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 1.2 + legTimingOffset + rightLegOffset) * 0.9f * globalDegree + 0.2 * globalDegree) * limbSwingAmount);
        rightFoot.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 1.2 + legTimingOffset + rightLegOffset) * -0.9f * globalDegree - 0.8f * globalDegree) * limbSwingAmount);
        rightToes.addRotX(blend * (-Math.cos(limbSwing * speed * 0.5 + 1 + legTimingOffset + rightLegOffset) * -1.7f * globalDegree - 1f * globalDegree) * limbSwingAmount);
        rightToes.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, -3.5f + Mth.PI, 4f) * -1f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);
//
        MowzieGeoBone tail1 = getMowzieBone("tail1");
        MowzieGeoBone tail2 = getMowzieBone("tail2");
        MowzieGeoBone tail3 = getMowzieBone("tail3");
        MowzieGeoBone tail4 = getMowzieBone("tail4");
        MowzieGeoBone tail5 = getMowzieBone("tail5");
        tail1.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 3.46) * 0.13f * globalHeight) * limbSwingAmount);
        tail1.addRotX(blend * (Math.cos(limbSwing * speed + 3.46) * -0.13f * globalHeight) * limbSwingAmount);
        tail2.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 3.46 - 0.5) * -0.13f * globalHeight) * limbSwingAmount);
        tail2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.13f * globalHeight) * limbSwingAmount);
        tail3.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 1) * -0.13f * globalHeight) * limbSwingAmount);
        tail4.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 1.5) * -0.13f * globalHeight) * limbSwingAmount);
        tail5.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 2) * -0.13f * globalHeight) * limbSwingAmount);

        MowzieGeoBone tuft1 = getMowzieBone("tuft1");
        MowzieGeoBone tuft2 = getMowzieBone("tuft2");
        MowzieGeoBone tuft3 = getMowzieBone("tuft3");
        MowzieGeoBone tuft4 = getMowzieBone("tuft4");
        tuft1.addRotX(blend * (Math.cos(limbSwing * speed + 3.46) * -0.065f * globalHeight) * limbSwingAmount);
        tuft2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.25) * -0.02f * globalHeight) * limbSwingAmount);
        tuft2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.065f * globalHeight) * limbSwingAmount);
        tuft4.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.065f * globalHeight) * limbSwingAmount);
    }

    private void walkRightAnim(double blend, float limbSwing, float limbSwingAmount, float speed) {
//        limbSwing = - limbSwing;

        MowzieGeoBone head = getMowzieBone("head");
        MowzieGeoBone headJoint = getMowzieBone("headJoint");
        MowzieGeoBone coM = getMowzieBone("CoM");
        MowzieGeoBone legs = getMowzieBone("legs");
        MowzieGeoBone stomach = getMowzieBone("stomach");
        MowzieGeoBone upperBody = getMowzieBone("upperBody");
        MowzieGeoBone leftThigh = getMowzieBone("leftThigh");
        MowzieGeoBone leftCalf = getMowzieBone("leftCalf");
        MowzieGeoBone leftFoot = getMowzieBone("leftFoot");
        MowzieGeoBone leftToes = getMowzieBone("leftToes");
        MowzieGeoBone rightThigh = getMowzieBone("rightThigh");
        MowzieGeoBone rightCalf = getMowzieBone("rightCalf");
        MowzieGeoBone rightFoot = getMowzieBone("rightFoot");
        MowzieGeoBone rightToes = getMowzieBone("rightToes");
        MowzieGeoBone leftArm = getMowzieBone("leftArm");
        MowzieGeoBone leftForeArm = getMowzieBone("leftForeArm");
        MowzieGeoBone leftHand = getMowzieBone("leftHand");
        MowzieGeoBone rightArm = getMowzieBone("rightArm");
        MowzieGeoBone rightForeArm = getMowzieBone("rightForeArm");
        MowzieGeoBone rightHand = getMowzieBone("rightHand");

        speed *= 2.1f;
        float globalHeight = 1.1f;
        float globalDegree = 0.9f;

        coM.addPosY(blend * (Math.cos(limbSwing * speed) * 0.3f * globalHeight + 0.1 * globalHeight) * limbSwingAmount);
        coM.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.2f * globalHeight) * limbSwingAmount);
        coM.addRotX(blend * (poweredWave(limbSwing, speed, -1.5f, 3f) * -0.15f * globalHeight - 0.075f * globalHeight) * limbSwingAmount);
        stomach.addRotX( blend * -0.1f * globalHeight * limbSwingAmount);
        headJoint.addRotX( blend * 0.1f * globalHeight * limbSwingAmount);
        headJoint.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        headJoint.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.15f * globalHeight) * limbSwingAmount);
        legs.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        leftArm.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        rightArm.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        upperBody.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.3f * globalHeight) * limbSwingAmount);
        upperBody.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.3f * globalHeight) * limbSwingAmount);
        legs.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.15f * globalHeight) * limbSwingAmount);
        legs.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.15f * globalHeight) * limbSwingAmount);

        double leftArmOffset = Math.PI - 0.5;
        upperBody.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        upperBody.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        headJoint.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        headJoint.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        rightArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5) * 0.2f * globalHeight) * limbSwingAmount);
        leftArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5) * 0.2f * globalHeight) * limbSwingAmount);
        rightArm.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, 3f + Mth.PI/2f, 4f) * -1f * globalHeight * limbSwingAmount);
//        rightArm.addPosY(blend * 1f * globalHeight * limbSwingAmount);
        leftArm.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, 3f + leftArmOffset + Mth.PI/2f, 6f) * 2f * globalHeight * limbSwingAmount);
        leftArm.addPosY(blend * 1f * globalHeight * limbSwingAmount);
        rightArm.addPosY(blend * (Math.cos(limbSwing * speed * 0.5f) * -2f * globalHeight) * limbSwingAmount);
        leftArm.addPosY(blend * (Math.cos(limbSwing * speed * 0.5f - 0.8f + leftArmOffset) * -2f * globalHeight + 0.9f * globalHeight) * limbSwingAmount);

        rightArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.5 * globalDegree + 0 * globalDegree) * limbSwingAmount);
        rightForeArm.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 - 2) * -0.9 * globalDegree - 0.2 * globalDegree) * limbSwingAmount);
        rightHand.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 - 1) * 1.2 * globalDegree + 0.1 * globalDegree) * limbSwingAmount);

        leftArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8 + leftArmOffset) * 0.5 * globalDegree - 0 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 - 2 + leftArmOffset) * -0.9 * globalDegree - 0.0 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 - 0.8 + leftArmOffset) * 0.6 * globalDegree - 0.0 * globalDegree) * limbSwingAmount);
        leftHand.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 - 3.2 + leftArmOffset) * 0.5 * globalDegree) * limbSwingAmount);
        leftHand.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 - 3.2 + leftArmOffset) * 0.8 * globalDegree) * limbSwingAmount);


//        leftArm.setHidden(false);
//        rightArm.setHidden(false);
        double legTimingOffset = 3.1;
        double sideFlipTimingOffset = Mth.PI;
        leftThigh.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.15f * globalHeight) * limbSwingAmount);
//        leftThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.35f * globalHeight) * limbSwingAmount);
        leftThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0 + legTimingOffset + sideFlipTimingOffset) * 0.8f * globalDegree + 0.25 * globalDegree) * limbSwingAmount);
        leftThigh.addPosY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset) * 0.5f * globalHeight + 1 * globalHeight) * limbSwingAmount);
        leftThigh.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, -1.5f, 3f) * -0.5f * globalHeight * limbSwingAmount);
        leftThigh.addPosZ(blend * poweredWave(limbSwing, speed * 0.5f, -1.5f, 3f) * -0.5f * globalHeight * limbSwingAmount);
        leftThigh.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        leftCalf.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 1.7 + legTimingOffset) * 0.9f * globalDegree + 0.2 * globalDegree) * limbSwingAmount);
        leftFoot.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 1.7 + legTimingOffset) * -0.9f * globalDegree - 0.8f * globalDegree) * limbSwingAmount);
        leftToes.addRotX(blend * (-Math.cos(limbSwing * speed * 0.5 + 1 + legTimingOffset) * -1.7f * globalDegree - 1f * globalDegree) * limbSwingAmount);
        leftToes.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, -3.5f, 4f) * -1f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);
//
        double rightLegOffset = (Math.PI);
        rightThigh.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.15f * globalHeight) * limbSwingAmount);
//        rightThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.35f * globalHeight) * limbSwingAmount);
        rightThigh.addRotX(blend * 0.2f * globalDegree * limbSwingAmount);
        rightThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0 + legTimingOffset + rightLegOffset + sideFlipTimingOffset) * 0.8f * globalDegree + 0.15 * globalDegree) * limbSwingAmount);
        rightThigh.addPosY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset + rightLegOffset) * 0.5f * globalHeight + 1 * globalHeight) * limbSwingAmount);
        rightThigh.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, -2f + Mth.PI, 3f) * -1f * globalHeight * limbSwingAmount);
        rightThigh.addPosZ(blend * poweredWave(limbSwing, speed * 0.5f, -2f + Mth.PI, 3f) * -1f * globalHeight * limbSwingAmount);
        rightThigh.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset + rightLegOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        rightCalf.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 1.2 + legTimingOffset + rightLegOffset) * 0.9f * globalDegree + 0.2 * globalDegree) * limbSwingAmount);
        rightFoot.addRotX(blend * (Math.cos(limbSwing * speed * 0.5 + 1.2 + legTimingOffset + rightLegOffset) * -0.9f * globalDegree - 0.8f * globalDegree) * limbSwingAmount);
        rightToes.addRotX(blend * (-Math.cos(limbSwing * speed * 0.5 + 1 + legTimingOffset + rightLegOffset) * -1.7f * globalDegree - 1f * globalDegree) * limbSwingAmount);
        rightToes.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, -3.5f + Mth.PI, 4f) * -1f * globalDegree + 0.3 * globalDegree) * limbSwingAmount);
//
        MowzieGeoBone tail1 = getMowzieBone("tail1");
        MowzieGeoBone tail2 = getMowzieBone("tail2");
        MowzieGeoBone tail3 = getMowzieBone("tail3");
        MowzieGeoBone tail4 = getMowzieBone("tail4");
        MowzieGeoBone tail5 = getMowzieBone("tail5");
        tail1.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 3.46) * 0.13f * globalHeight) * limbSwingAmount);
        tail1.addRotX(blend * (Math.cos(limbSwing * speed + 3.46) * -0.13f * globalHeight) * limbSwingAmount);
        tail2.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 3.46 - 0.5) * -0.13f * globalHeight) * limbSwingAmount);
        tail2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.13f * globalHeight) * limbSwingAmount);
        tail3.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 1) * -0.13f * globalHeight) * limbSwingAmount);
        tail4.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 1.5) * -0.13f * globalHeight) * limbSwingAmount);
        tail5.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 2) * -0.13f * globalHeight) * limbSwingAmount);

        MowzieGeoBone tuft1 = getMowzieBone("tuft1");
        MowzieGeoBone tuft2 = getMowzieBone("tuft2");
        MowzieGeoBone tuft3 = getMowzieBone("tuft3");
        MowzieGeoBone tuft4 = getMowzieBone("tuft4");
        tuft1.addRotX(blend * (Math.cos(limbSwing * speed + 3.46) * -0.065f * globalHeight) * limbSwingAmount);
        tuft2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.25) * -0.02f * globalHeight) * limbSwingAmount);
        tuft2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.065f * globalHeight) * limbSwingAmount);
        tuft4.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.065f * globalHeight) * limbSwingAmount);
    }

    private void runAnimDay(double blend, float limbSwing, float limbSwingAmount, float speed) {
        limbSwingAmount = limbSwingAmount * limbSwingAmount;

        MowzieGeoBone head = getMowzieBone("head_day");
        MowzieGeoBone headJoint = getMowzieBone("headJoint_day");
        MowzieGeoBone coM = getMowzieBone("CoM_day");
        MowzieGeoBone stomach = getMowzieBone("stomach_day");
        MowzieGeoBone leftThighJoint = getMowzieBone("LegL_Joint_day");
        MowzieGeoBone leftThigh = getMowzieBone("LegL_day");
        MowzieGeoBone leftCalf = getMowzieBone("LowerLegL_day");
        MowzieGeoBone rightThighJoint = getMowzieBone("LegR_Joint_day");
        MowzieGeoBone rightThigh = getMowzieBone("LegR_day");
        MowzieGeoBone rightCalf = getMowzieBone("LowerLegL_day");
        MowzieGeoBone leftArmJoint = getMowzieBone("ArmL_Joint_day");
        MowzieGeoBone leftArm = getMowzieBone("UpperArmL_day");
        MowzieGeoBone leftForeArm = getMowzieBone("LowerArmL_day");
        MowzieGeoBone leftHand = getMowzieBone("HandL_day");
        MowzieGeoBone rightArmJoint = getMowzieBone("ArmR_Joint_day");
        MowzieGeoBone rightArm = getMowzieBone("UpperArmR_day");
        MowzieGeoBone rightForeArm = getMowzieBone("LowerArmR_day");
        MowzieGeoBone rightHand = getMowzieBone("HandR_day");

        float globalHeight = 1.15f;
        float globalDegree = 1.8f;
        speed *= 1f;

        coM.addPosY(blend * (Math.cos(limbSwing * speed) * 2.7f * globalHeight + 4 * globalHeight) * limbSwingAmount);
        coM.addPosZ(blend * (Math.cos(limbSwing * speed + 1.9) * 2f * globalHeight) * limbSwingAmount);
        stomach.addRotX(blend * (Math.cos(limbSwing * speed + 1.5) * -0.3 * globalHeight - 0.5f) * limbSwingAmount);
        headJoint.addRotX(blend * (Math.cos(limbSwing * speed - 0.8) * -0.4 * globalHeight + 0.45 * globalHeight) * limbSwingAmount);

        double legTimingOffset = 1.3;

        leftThighJoint.addRotX(blend * (-Math.cos(limbSwing * speed + 0 + legTimingOffset) * 1.1f * globalDegree - 0.1 * globalDegree) * limbSwingAmount);
        leftThigh.addRotY(blend * (-Math.cos(limbSwing * speed + 0.5 + legTimingOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        leftThigh.addPosY(blend * (-Math.cos(limbSwing * speed + 0.5 + legTimingOffset) * 1f * globalDegree) * limbSwingAmount);

        double rightLegOffset = 0.7;
        rightThighJoint.addRotX(blend * (-Math.cos(limbSwing * speed + 0 + legTimingOffset + rightLegOffset) * 1.1f * globalDegree - 0.1 * globalDegree) * limbSwingAmount);
        rightThigh.addRotY(blend * (Math.cos(limbSwing * speed + 0.5 + legTimingOffset + rightLegOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);
        rightThigh.addPosY(blend * (-Math.cos(limbSwing * speed + 0.5 + legTimingOffset + rightLegOffset) * 1f * globalDegree) * limbSwingAmount);

        double rightArmOffset = 1.3;
        double leftArmOffset = 0.4;
        leftArm.addPosY(blend * poweredWave(limbSwing, speed, 0f + leftArmOffset, 4f) * -1.5f * globalHeight * limbSwingAmount);
        leftArmJoint.addPosY(blend * poweredWave(limbSwing, speed, 2.5f + leftArmOffset, 4f) * 1.5f * globalHeight * limbSwingAmount);
        leftArmJoint.addRotX(blend * -(Math.cos(limbSwing * speed + -0.8 + leftArmOffset) * 0.7 * globalDegree - 1.4) * limbSwingAmount);
        leftArmJoint.addRotY(blend * -(Math.cos(limbSwing * speed + -0.8 + leftArmOffset) * -0.4 * globalDegree - 0.5) * limbSwingAmount);
        leftArmJoint.addRotZ(blend * -(Math.cos(limbSwing * speed + -0.8 + leftArmOffset) * 0.6 * globalDegree + 0.2f) * limbSwingAmount);
        leftForeArm.addRotX(blend * (Math.cos(limbSwing * speed - 1.65 + leftArmOffset) * 0.5 * globalDegree - 1.1) * limbSwingAmount);
//        leftForeArm.addRotX(blend * poweredWave(limbSwing, speed, -2.1f + Mth.PI + leftArmOffset, 3f) * 0.5f * globalHeight * limbSwingAmount);
        leftForeArm.addRotZ(blend * (Math.cos(limbSwing * speed - 0.3 + leftArmOffset) * 0.7 * globalDegree + 0.2) * limbSwingAmount);
        leftHand.addRotX(blend * (Math.cos(limbSwing * speed + 4.5 + leftArmOffset) * -1 * globalDegree - 0.7) * limbSwingAmount);
        leftHand.addRotY(blend * -(Math.cos(limbSwing * speed + 4.5 + leftArmOffset) * -1 * globalDegree - 0.8) * limbSwingAmount);

        rightArm.addPosY(blend * poweredWave(limbSwing, speed, 0.5f, 4f) * -1.5f * globalHeight * limbSwingAmount);
        rightArmJoint.addPosY(blend * poweredWave(limbSwing, speed, -1f, 4f) * 1.5f * globalHeight * limbSwingAmount);
        rightArmJoint.addRotX(blend * -(Math.cos(limbSwing * speed + -0.8 + rightArmOffset) * 0.7 * globalDegree - 1) * limbSwingAmount);
        rightArmJoint.addRotY(blend * -(Math.cos(limbSwing * speed + -0.8 + rightArmOffset) * 0.4 * globalDegree + 0.5) * limbSwingAmount);
        rightArmJoint.addRotZ(blend * -(Math.cos(limbSwing * speed + -0.8 + rightArmOffset) * -0.6 * globalDegree - 0.2f) * limbSwingAmount);
        rightForeArm.addRotX(blend * (Math.cos(limbSwing * speed - 1.65 + rightArmOffset) * 0.5 * globalDegree - 0.87) * limbSwingAmount);
        rightForeArm.addRotX(blend * poweredWave(limbSwing, speed, -0.8f + Mth.PI, 3f) * 0.5f * globalHeight * limbSwingAmount);
        rightForeArm.addRotZ(blend * (Math.cos(limbSwing * speed - 0.3 + rightArmOffset) * -0.7 * globalDegree - 0.2) * limbSwingAmount);
        rightHand.addRotX(blend * (Math.cos(limbSwing * speed + 4.5 + rightArmOffset) * -0.8 * globalDegree - 0.8) * limbSwingAmount);
        rightHand.addRotY(blend * -(Math.cos(limbSwing * speed + 4.5 + rightArmOffset) * 1 * globalDegree + 0.5) * limbSwingAmount);

        MowzieGeoBone tail1 = getMowzieBone("tail1_day");
        MowzieGeoBone tail2 = getMowzieBone("tail2_day");
        MowzieGeoBone tail3 = getMowzieBone("tail3_day");

        tail1.addRotX(blend * (Math.cos(limbSwing * speed + 0.9) * 0.3f * globalHeight) * limbSwingAmount);
        tail2.addRotX(blend * (Math.cos(limbSwing * speed + 0.9 - 1) * 0.2f * globalHeight) * limbSwingAmount);
        tail3.addRotX(blend * (Math.cos(limbSwing * speed + 0.9 - 2) * 0.2f * globalHeight) * limbSwingAmount);
    }

    private void walkAnimDay(double blend, float limbSwing, float limbSwingAmount, float speed) {
        limbSwingAmount = Math.min(limbSwingAmount, 0.5f);

        MowzieGeoBone head = getMowzieBone("head_day");
        MowzieGeoBone headJoint = getMowzieBone("headJoint_day");
        MowzieGeoBone coM = getMowzieBone("CoM_day");
        MowzieGeoBone stomach = getMowzieBone("stomach_day");
        MowzieGeoBone leftThigh = getMowzieBone("LegL_day");
        MowzieGeoBone leftCalf = getMowzieBone("LowerLegL_day");
        MowzieGeoBone rightThigh = getMowzieBone("LegR_day");
        MowzieGeoBone rightCalf = getMowzieBone("LowerLegL_day");
        MowzieGeoBone leftArmJoint = getMowzieBone("ArmL_Joint_day");
        MowzieGeoBone leftArm = getMowzieBone("UpperArmL_day");
        MowzieGeoBone leftForeArm = getMowzieBone("LowerArmL_day");
        MowzieGeoBone leftHand = getMowzieBone("HandL_day");
        MowzieGeoBone rightArmJoint = getMowzieBone("ArmR_Joint_day");
        MowzieGeoBone rightArm = getMowzieBone("UpperArmR_day");
        MowzieGeoBone rightForeArm = getMowzieBone("LowerArmR_day");
        MowzieGeoBone rightHand = getMowzieBone("HandR_day");

        speed *= 2.7f;
        float globalHeight = 1.3f;
        float globalDegree = 1.3f;

        coM.addPosY(blend * (Math.cos(limbSwing * speed) * 0.3f * globalHeight + 0.2 * globalHeight) * limbSwingAmount);
        coM.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.2f * globalHeight) * limbSwingAmount);
        coM.addRotX(blend * (poweredWave(limbSwing, speed, -1.5f, 3f) * -0.15f * globalHeight - 0.175f * globalHeight) * limbSwingAmount);
        stomach.addRotX( blend * -0.1f * globalHeight * limbSwingAmount);
        headJoint.addRotX( blend * 0.4f * globalHeight * limbSwingAmount);
        headJoint.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        headJoint.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * -0.1f * globalHeight) * limbSwingAmount);
        leftArm.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        rightArm.addRotX(blend * poweredWave(limbSwing, speed, -1.5f, 3f) * 0.15f * globalHeight * limbSwingAmount);
        stomach.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.3f * globalHeight) * limbSwingAmount);
        stomach.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.3f * globalHeight) * limbSwingAmount);

        stomach.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        stomach.addRotZ(blend * (-Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        headJoint.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        headJoint.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.1f * globalHeight) * limbSwingAmount);
        headJoint.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 - 1) * -0.4f * globalHeight) * limbSwingAmount);

        leftArm.addPosY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 1 * globalHeight - 0 * globalHeight) * limbSwingAmount);
        leftArm.addRotX(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.5 * globalDegree + 0.1 * globalDegree) * limbSwingAmount);
        leftArm.addRotY(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.3 * globalDegree + 0.5 * globalDegree) * limbSwingAmount);
        leftArm.addRotZ(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.4 * globalDegree + 0.65 * globalDegree) * limbSwingAmount);
        leftArm.addRotZ(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8 - 1.57) * 0.4 * globalDegree + 0.4 * globalDegree) * limbSwingAmount);
        leftArm.addRotZ(blend * (poweredWave(limbSwing, speed * 0.5f, -0.8f -3.14f, 5f) * 0.9f * globalHeight) * limbSwingAmount);
        leftForeArm.addRotX(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.4 * globalDegree + 0 * globalDegree) * limbSwingAmount);
        leftForeArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.55 * globalDegree - 0.2 * globalDegree) * limbSwingAmount);
        leftHand.addRotY(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.3 * globalDegree - 0.5 * globalDegree) * limbSwingAmount);
        leftHand.addRotX(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8 - 1.57) * 1 * globalDegree - 0.2 * globalDegree) * limbSwingAmount);
        leftHand.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, -0.8f -2.5f, 10f) * 1.7f * globalHeight) * limbSwingAmount);
        leftHand.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, -0.8f, 8f) * 1.1f * globalHeight) * limbSwingAmount);

        double rightArmTimingOffset = Mth.PI;
        rightArm.addPosY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8 + rightArmTimingOffset) * 1 * globalHeight - 0 * globalHeight) * limbSwingAmount);
        rightArm.addRotX(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8 + rightArmTimingOffset) * 0.5 * globalDegree + 0.1 * globalDegree) * limbSwingAmount);
        rightArm.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8 + rightArmTimingOffset) * 0.3 * globalDegree + 0.5 * globalDegree) * limbSwingAmount);
        rightArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8 + rightArmTimingOffset) * 0.4 * globalDegree + 0.65 * globalDegree) * limbSwingAmount);
        rightArm.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8 - 1.57 + rightArmTimingOffset) * 0.4 * globalDegree + 0.4 * globalDegree) * limbSwingAmount);
        rightArm.addRotZ(blend * -(poweredWave(limbSwing, speed * 0.5f, -0.8f -3.14f + rightArmTimingOffset, 5f) * 0.9f * globalHeight) * limbSwingAmount);
        rightForeArm.addRotX(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8 + rightArmTimingOffset) * 0.4 * globalDegree + 0 * globalDegree) * limbSwingAmount);
        rightForeArm.addRotZ(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8 + rightArmTimingOffset) * 0.55 * globalDegree - 0.2 * globalDegree) * limbSwingAmount);
        rightHand.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8 + rightArmTimingOffset) * 0.3 * globalDegree - 0.5 * globalDegree) * limbSwingAmount);
        rightHand.addRotX(blend * -(Math.cos(limbSwing * speed * 0.5 + -0.8 - 1.57 + rightArmTimingOffset) * 1 * globalDegree - 0.2 * globalDegree) * limbSwingAmount);
        rightHand.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, -0.8f -2.5f + rightArmTimingOffset, 10f) * 1.7f * globalHeight) * limbSwingAmount);
        rightHand.addRotX(blend * (poweredWave(limbSwing, speed * 0.5f, -0.8f + rightArmTimingOffset, 8f) * 1.1f * globalHeight) * limbSwingAmount);

//
        double legTimingOffset = 1.5;
        leftThigh.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8) * 0.15f * globalHeight) * limbSwingAmount);
        leftThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0) * 0.35f * globalHeight) * limbSwingAmount);
        leftThigh.addRotX(blend * (-Math.cos(limbSwing * speed * 0.5 + 0 + legTimingOffset) * 0.8f * globalDegree + 0.1 * globalDegree) * limbSwingAmount);
        leftThigh.addPosY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset) * 0.8f * globalHeight - 0.2 * globalHeight) * limbSwingAmount);
        leftThigh.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, -1.7f, 5f) * -2f * globalHeight * limbSwingAmount);
        leftThigh.addPosZ(blend * poweredWave(limbSwing, speed * 0.5f, -1.57f, 3f) * -1f * globalHeight * limbSwingAmount);
        leftThigh.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset) * 0.4f * globalDegree + 0.4) * limbSwingAmount);

        double rightLegOffset = (Math.PI);
        rightThigh.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + -0.8 + rightLegOffset) * -0.15f * globalHeight) * limbSwingAmount);
        rightThigh.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 0 + rightLegOffset) * -0.35f * globalHeight) * limbSwingAmount);
        rightThigh.addRotX(blend * (-Math.cos(limbSwing * speed * 0.5 + 0 + legTimingOffset + rightLegOffset) * 0.8f * globalDegree + 0.1 * globalDegree) * limbSwingAmount);
        rightThigh.addPosY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset + rightLegOffset) * 0.8f * globalHeight - 0.2 * globalHeight) * limbSwingAmount);
        rightThigh.addPosY(blend * poweredWave(limbSwing, speed * 0.5f, -1.7f + rightLegOffset, 5f) * -2f * globalHeight * limbSwingAmount);
        rightThigh.addPosZ(blend * poweredWave(limbSwing, speed * 0.5f, -1.5f + rightLegOffset, 3f) * -1f * globalHeight * limbSwingAmount);
        rightThigh.addRotY(blend * (-Math.cos(limbSwing * speed * 0.5 + 1.5 + legTimingOffset + rightLegOffset) * -0.4f * globalDegree - 0.4) * limbSwingAmount);

        MowzieGeoBone tail1 = getMowzieBone("tail1_day");
        MowzieGeoBone tail2 = getMowzieBone("tail2_day");
        MowzieGeoBone tail3 = getMowzieBone("tail3_day");
        tail1.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 3.46) * 0.6f * globalHeight) * limbSwingAmount);
        tail1.addRotX(blend * (Math.cos(limbSwing * speed + 3.46) * -0.2f * globalHeight) * limbSwingAmount);
        tail2.addRotY(blend * (Math.cos(limbSwing * speed * 0.5 + 3.46 - 0.5) * 0.6f * globalHeight) * limbSwingAmount);
        tail2.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 0.5) * -0.13f * globalHeight) * limbSwingAmount);
        tail3.addRotX(blend * (Math.cos(limbSwing * speed + 3.46 - 1) * -0.13f * globalHeight) * limbSwingAmount);
        tail3.addRotZ(blend * (Math.cos(limbSwing * speed * 0.5 + 3.46 - 1) * 0.6f * globalHeight) * limbSwingAmount);
    }
}