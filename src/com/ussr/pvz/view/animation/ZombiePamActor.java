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
    // Gameplay Constructor
    public ZombiePamActor(PamPlayer player, String pamPath, String preferredClip) {
        super(player, pamPath, preferredClip);
        this.pamScale = 0.65f;
        this.offsetY = -40f;
        boolean isOneShot = "die".equals(preferredClip)
                || "newspaper_defeat".equals(preferredClip);
        setLooping(!isOneShot);
    }// In ZombiePamActor:
    private String returnToClip = null; // clip to restore after one-shot finishes

    public void playOnce(String clipName, String returnTo) {
        if (java.util.Objects.equals(currentClipName, clipName)) return;
        this.returnToClip = returnTo;
        super.setClip(clipName);
        setLooping(false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // When one-shot finishes, snap back
        if (returnToClip != null && !playing) {
            returnToClip = null;
            // EntityRenderLayer will call setClip() with the correct state next frame
            // Just reset so the state-based clip can take over
            currentClipName = null; // force setClip() to accept the next state clip
        }
    }

    // In ZombiePamActor.setClip():
    @Override
    public void setClip(String clipName) {
        if (java.util.Objects.equals(currentClipName, clipName)) return;
        super.setClip(clipName);
        boolean isOneShot = "die".equals(currentClipName)
                || "newspaper_defeat".equals(currentClipName);
        setLooping(!isOneShot);
    }
}
