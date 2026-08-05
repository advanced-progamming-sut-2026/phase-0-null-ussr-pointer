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
        setLooping(!"die".equals(preferredClip));
    }

    @Override
    public void setClip(String clipName) {
        if (java.util.Objects.equals(
                currentClipName,
                clipName
        )) {
            return;
        }

        super.setClip(clipName);
        setLooping(!"die".equals(currentClipName));
    }
}
