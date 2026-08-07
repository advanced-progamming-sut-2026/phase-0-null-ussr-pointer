package com.ussr.pvz.view.animation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
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

    // -------------------------------------------------------------------------
    // Glow & danger state (view-only — driven by EntityRenderLayer each frame)
    // -------------------------------------------------------------------------

    /** When true, a soft green halo is drawn around this zombie. */
    private boolean glowing;

    /**
     * 0 = no danger overlay; 1 = full danger.
     * EntityRenderLayer sets this every frame based on the zombie's X position.
     * The value is already a smooth sine-wave value so we just use it directly.
     */
    private float dangerAlpha;

    // Glow pulse accumulator — advanced in act()
    private float glowTime;

    // Tuning constants
    /** How fast the glow pulses (radians per second). */
    private static final float GLOW_PULSE_SPEED   = 2.5f;
    /** Base alpha of the green glow overlay. */
    private static final float GLOW_BASE_ALPHA     = 0.25f;
    /** Amplitude of the glow pulse on top of the base. */
    private static final float GLOW_PULSE_AMPLITUDE = 0.12f;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    // Collection Constructor (Defaults to "walk")
    public ZombiePamActor(PamPlayer player, String pamPath) {
        super(player, pamPath, "walk");
        this.pamScale = 0.65f;
        this.offsetY = -40f;
    }

    // Gameplay Constructor
    public ZombiePamActor(PamPlayer player, String pamPath, String preferredClip) {
        super(player, pamPath, preferredClip);
        this.pamScale = 0.65f;
        this.offsetY = -40f;
        boolean isOneShot = "die".equals(preferredClip)
                || "newspaper_defeat".equals(preferredClip);
        setLooping(!isOneShot);
    }

    // -------------------------------------------------------------------------
    // Public setters called by EntityRenderLayer each frame
    // -------------------------------------------------------------------------

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    /**
     * Set the danger flicker intensity [0..1].
     * EntityRenderLayer computes a smooth sine value and passes it here.
     */
    public void setDangerAlpha(float dangerAlpha) {
        this.dangerAlpha = Math.max(0f, Math.min(1f, dangerAlpha));
    }

    // -------------------------------------------------------------------------
    // act — advance glow timer
    // -------------------------------------------------------------------------

    @Override
    public void act(float delta) {
        super.act(delta);

        if (glowing) {
            glowTime += delta;
        }

        // Special-clip return logic (unchanged from original)
        if (playingSpecial && !playing) {
            String nextClip = returnToClip;
            playingSpecial = false;
            returnToClip = null;
            currentClipName = null;
            setClip(nextClip == null ? "idle" : nextClip);
        }
    }

    // -------------------------------------------------------------------------
    // draw — overlay passes
    // -------------------------------------------------------------------------

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // --- 1. Green glow (rendered BEFORE the normal sprite so it appears behind) ---
        if (glowing) {
            float pulse = GLOW_BASE_ALPHA
                    + GLOW_PULSE_AMPLITUDE * (float) Math.sin(glowTime * GLOW_PULSE_SPEED);

            Color prev = batch.getColor().cpy();
            // Additive-style green tint: keep red/blue very low, green full
            batch.setColor(0f, 1f, 0f, pulse * parentAlpha);
            super.draw(batch, parentAlpha);
            batch.setColor(prev);
        }

        // --- 2. Normal sprite draw ---
        super.draw(batch, parentAlpha);

        // --- 3. Red danger flicker (rendered ON TOP of the sprite) ---
        if (dangerAlpha > 0f) {
            Color prev = batch.getColor().cpy();
            batch.setColor(1f, 0f, 0f, dangerAlpha * parentAlpha);
            super.draw(batch, parentAlpha);
            batch.setColor(prev);
        }
    }

    // -------------------------------------------------------------------------
    // Original methods — unchanged
    // -------------------------------------------------------------------------

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