package com.ussr.pvz.view.animation;

import pvz.libpvz.pam.PamPlayer;

public class ZombiePamActor extends PamActor {

    public ZombiePamActor(PamPlayer player, String pamPath) {
        super(player, pamPath, "walk"); // Zombies default to walk animations
        this.pamScale = 0.65f; // Zombies are generally taller, so scale is slightly smaller than plants

        // Offset the zombie so its feet touch the floor
        this.offsetY = -40f;
    }
}