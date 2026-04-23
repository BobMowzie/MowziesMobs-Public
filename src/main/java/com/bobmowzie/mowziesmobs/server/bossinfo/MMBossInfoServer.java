package com.bobmowzie.mowziesmobs.server.bossinfo;

import com.bobmowzie.mowziesmobs.server.entity.MowzieEntity;
import com.bobmowzie.mowziesmobs.server.message.MessageUpdateBossBar;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MMBossInfoServer extends ServerBossEvent {
    protected final MowzieEntity entity;

    private final Set<ServerPlayer> unseen = new HashSet<>();

    public MMBossInfoServer(MowzieEntity entity) {
        super(entity.getDisplayName(), entity.bossBarColor(), BossBarOverlay.PROGRESS);
        this.setVisible(entity.hasBossBar());
        this.entity = entity;
    }

    public void update() {
        this.setProgress(this.entity.getHealth() / this.entity.getMaxHealth());
        Iterator<ServerPlayer> it = this.unseen.iterator();
        while (it.hasNext()) {
            ServerPlayer player = it.next();
            if (this.entity.getSensing().hasLineOfSight(player)) {
                super.addPlayer(player);
                it.remove();
            }
        }
    }

    @Override
    public void addPlayer(@NotNull ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, MessageUpdateBossBar.fromEntity(getId(), entity));

        if (this.entity.getSensing().hasLineOfSight(player)) {
            super.addPlayer(player);
        } else {
            this.unseen.add(player);
        }
    }

    @Override
    public void removePlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, MessageUpdateBossBar.fromEntity(getId(), null));

        super.removePlayer(player);
        this.unseen.remove(player);
    }
}
