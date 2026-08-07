package com.ussr.pvz.view.animation;

import com.ussr.pvz.model.entities.zombies.armor.Armor;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.Locale;

public class ZombiePamActor extends PamActor {

    private final HashMap<String, Boolean> armorVisibility = new HashMap<>();
    private boolean armorPartsResolved;
    private String returnToClip;
    private boolean playingSpecial;

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
    }

    public void playOnce(String clipName, String returnTo) {
        if (playingSpecial || !hasClip(clipName)) {
            return;
        }

        ClipRef specialClip = player.getClip(pamPath, clipName);
        this.returnToClip = returnTo;
        this.currentClipName = clipName;
        this.clipRef = specialClip;
        this.stateTime = 0f;
        this.playing = true;
        this.playingSpecial = true;
        setLooping(false);
    }

    public boolean isPlayingSpecial() {
        return playingSpecial;
    }

    private boolean hasClip(String clipName) {
        if (clipName == null || clipName.isBlank()) {
            return false;
        }
        try {
            return player.clips(pamPath).contains(clipName);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    /** Selects the armor artwork already embedded in this zombie's PAM. */
    public void setArmor(Armor armor) {
        resolveArmorParts();
        if (armorVisibility.isEmpty()) {
            return;
        }

        armorVisibility.replaceAll((name, visible) -> false);
        if (armor != null && !armor.isDestroyed()) {
            String wanted = switch (armor.getDamageLayer()) {
                case 0 -> "_norm";
                case 1 -> "_damage_01";
                default -> "_damage_02";
            };
            armorVisibility.replaceAll((name, visible) ->
                    name.toLowerCase(Locale.ROOT).contains(wanted));
        }
        partVisibility = armorVisibility;
    }

    private void resolveArmorParts() {
        if (armorPartsResolved) {
            return;
        }
        armorPartsResolved = true;
        try {
            collectArmorParts(player.getParts(pamPath));
        } catch (RuntimeException ignored) {
            // A PAM without named armor parts simply renders normally.
        }
    }

    private void collectArmorParts(PamPlayer.AnimationPart part) {
        if (part == null) {
            return;
        }
        String lowerName = part.name == null
                ? ""
                : part.name.toLowerCase(Locale.ROOT);
        if (lowerName.contains("armor")
                && (lowerName.endsWith("_norm")
                || lowerName.endsWith("_damage_01")
                || lowerName.endsWith("_damage_02"))) {
            armorVisibility.put(part.name, false);
        }
        for (PamPlayer.AnimationPart child : part.children) {
            collectArmorParts(child);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (playingSpecial && !playing) {
            String nextClip = returnToClip;
            playingSpecial = false;
            returnToClip = null;
            currentClipName = null;
            setClip(nextClip == null ? "idle" : nextClip);
        }
    }

    @Override
    public void setClip(String clipName) {
        if (playingSpecial && !"die".equals(clipName)) {
            return;
        }
        if (playingSpecial) {
            playingSpecial = false;
            returnToClip = null;
        }
        if (java.util.Objects.equals(currentClipName, clipName)) {
            return;
        }
        super.setClip(clipName);
        boolean isOneShot = "die".equals(currentClipName)
                || "newspaper_defeat".equals(currentClipName);
        setLooping(!isOneShot);
    }
}
