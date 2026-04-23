package com.bobmowzie.mowziesmobs.server.ability;

import com.bobmowzie.mowziesmobs.server.capability.AbilityData;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

public class AbilityClientEventHandler {
    public static void onRenderTick(RenderFrameEvent.Post event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
            for (Ability<?> ability : data.getAbilities()) {
                ability.onRenderTick(event);
            }
        }
    }
}