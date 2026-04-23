package com.bobmowzie.mowziesmobs.server.ability;

import com.bobmowzie.mowziesmobs.server.capability.AbilityData;
import com.bobmowzie.mowziesmobs.server.capability.DataHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class AbilityCommonEventHandler {
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
        for (Ability<?> ability : data.getAbilities()) {
            if (ability instanceof PlayerAbility) {
                ((PlayerAbility) ability).onRightClickEmpty(event);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
        for (Ability<?> ability : data.getAbilities()) {
            if (ability instanceof PlayerAbility) {
                ((PlayerAbility) ability).onRightClickBlock(event);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
        for (Ability<?> ability : data.getAbilities()) {
            if (ability instanceof PlayerAbility) {
                ((PlayerAbility) ability).onRightClickWithItem(event);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
        for (Ability<?> ability : data.getAbilities()) {
            if (ability instanceof PlayerAbility) {
                ((PlayerAbility) ability).onRightClickEntity(event);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
        for (Ability<?> ability : data.getAbilities()) {
            if (ability instanceof PlayerAbility) {
                ((PlayerAbility) ability).onLeftClickEmpty(event);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
        for (Ability<?> ability : data.getAbilities()) {
            if (ability instanceof PlayerAbility) {
                ((PlayerAbility) ability).onLeftClickBlock(event);
            }
        }
    }

    @SubscribeEvent
    public void onLeftClickEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
        for (Ability<?> ability : data.getAbilities()) {
            if (ability instanceof PlayerAbility) {
                ((PlayerAbility) ability).onLeftClickEntity(event);
            }
        }
    }

    @SubscribeEvent
    public void onTakeDamage(LivingDamageEvent.Post event) {
        LivingEntity player = event.getEntity();
        AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
        for (Ability<?> ability : data.getAbilities()) {
            ability.onTakeDamage(event);
        }
    }

    @SubscribeEvent
    public void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity player = event.getEntity();
        AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
        for (Ability<?> ability : data.getAbilities()) {
            if (ability instanceof PlayerAbility) {
                ((PlayerAbility) ability).onJump(event);
            }
        }
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        LivingEntity player = event.getEntity();
        AbilityData data = DataHandler.getData(player, DataHandler.ABILITY_DATA);
        for (Ability<?> ability : data.getAbilities()) {
            if (ability instanceof PlayerAbility) {
                ((PlayerAbility) ability).onFall(event);
            }
        }
    }
}
