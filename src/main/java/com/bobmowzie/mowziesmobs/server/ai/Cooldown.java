package com.bobmowzie.mowziesmobs.server.ai;

import net.minecraft.world.entity.LivingEntity;

public class Cooldown {
    private int timer;
    private int durationMin;
    private int durationMax;
    private LivingEntity entity;
    private boolean isPaused = false;
    private final String name;

    public Cooldown(LivingEntity entity, int duration, String name) {
        this.durationMin = duration;
        this.durationMax = duration;
        this.entity = entity;
        this.name = name;
    }

    public Cooldown(LivingEntity entity, int durationMin, int durationMax, String name) {
        this.durationMin = durationMin;
        this.durationMax = durationMax;
        this.entity = entity;
        this.name = name;
    }

    public boolean isFinished() {
        return timer <= 0;
    }

    public void finish() {
        this.timer = 0;
    }

    public void tick() {
        if (!isPaused) {
            timer = Math.max(0, timer - 1);
        }
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void startCooldown() {
        this.timer = entity.getRandom().nextInt(durationMin, durationMax);
    }

    public void startCooldown(int duration) {
        this.timer = duration;
    }

    public void startCooldown(int durationMin, int durationMax) {
        this.timer = entity.getRandom().nextInt(durationMin, durationMax);
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void paused() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public String getName() {
        return name;
    }
}
