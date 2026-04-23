package com.bobmowzie.mowziesmobs.server.advancement;

import com.bobmowzie.mowziesmobs.MMCommon;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AdvancementHandler {
    public static final DeferredRegister<CriterionTrigger<?>> MM_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, MMCommon.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, StealIceCrystalTrigger> STEAL_ICE_CRYSTAL_TRIGGER = MM_TRIGGERS.register("steal_ice_crystal", StealIceCrystalTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, GrottolKillFortuneTrigger> GROTTOL_KILL_FORTUNE_TRIGGER = MM_TRIGGERS.register("kill_grottol_fortune", GrottolKillFortuneTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, GrottolKillSilkTouchTrigger> GROTTOL_KILL_SILK_TOUCH_TRIGGER = MM_TRIGGERS.register("kill_grottol_silk_touch", GrottolKillSilkTouchTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SneakGroveTrigger> SNEAK_VILLAGE_TRIGGER = MM_TRIGGERS.register("sneak_grove", SneakGroveTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SculptorChallengeTrigger> SCULPTOR_CHALLENGE_TRIGGER = MM_TRIGGERS.register("sculptor_challenge", SculptorChallengeTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SculptorFailureTrigger> SCULPTOR_FAILURE_TRIGGER = MM_TRIGGERS.register("sculptor_failure", SculptorFailureTrigger::new);
}