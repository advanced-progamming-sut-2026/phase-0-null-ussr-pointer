package com.ussr.pvz.view.animation;

import pvz.libpvz.pam.PamPlayer;

public class ZombiePamActor extends PamActor {

    // Collection Constructor (Defaults to "walk")
    public ZombiePamActor(PamPlayer player, String pamPath) {
        super(player, pamPath, "walk");
        this.pamScale = 0.65f;
        this.offsetY = -40f;
    }

    // Gameplay Constructor (Takes dynamic clips)
    public ZombiePamActor(PamPlayer player, String pamPath, String preferredClip) {
        super(player, pamPath, preferredClip);
        this.pamScale = 0.65f;
        this.offsetY = -40f;
    }

    @Override
    public void setClip(String clipName) {
        super.setClip(clipName);
        if ("die".equals(clipName)) {
            this.looping = false;
            this.stateTime = 0f; // restart from beginning exactly once
        } else {
            this.looping = true;
        }
    }
}