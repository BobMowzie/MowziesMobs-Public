package com.bobmowzie.mowziesmobs.server.advancement;

import net.minecraft.advancements.CriteriaTriggers;

public class AdvancementHandler {
    public static final StealIceCrystalTrigger STEAL_ICE_CRYSTAL_TRIGGER = CriteriaTriggers.register(new StealIceCrystalTrigger());
    public static final GrottolKillFortuneTrigger GROTTOL_KILL_FORTUNE_TRIGGER = CriteriaTriggers.register(new GrottolKillFortuneTrigger());
    public static final GrottolKillSilkTouchTrigger GROTTOL_KILL_SILK_TOUCH_TRIGGER = CriteriaTriggers.register(new GrottolKillSilkTouchTrigger());
    public static final SneakGroveTrigger SNEAK_VILLAGE_TRIGGER = CriteriaTriggers.register(new SneakGroveTrigger());
    public static final SculptorChallengeTrigger SCULPTOR_CHALLENGE_TRIGGER = CriteriaTriggers.register(new SculptorChallengeTrigger());
    public static final SculptorFailureTrigger SCULPTOR_FAILURE_TRIGGER = CriteriaTriggers.register(new SculptorFailureTrigger());

    public static void preInit() { }
}