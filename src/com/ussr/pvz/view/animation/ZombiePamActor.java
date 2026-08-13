package com.ussr.pvz.view.animation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.ussr.pvz.model.entities.zombies.armor.Armor;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ZombiePamActor extends PamActor {

    private final HashMap<String, Boolean> armorVisibility = new HashMap<>();
    private boolean armorPartsResolved;
    private String returnToClip;
    private boolean playingSpecial;
    private List<String> sequenceQueue;
    private int sequenceIndex;
    private String sequenceRestClip;
    private boolean sequenceRestOneShot;
    private boolean playingSequence;
    private boolean deathPlaybackStarted;
    private final java.util.Set<String> detachableBodyParts = new java.util.HashSet<>();
    private boolean hasDetachedHeadPart;
    private boolean detachedDeathPlaying;
    private float detachedDeathTime;
    private String bodyDeathClip;

    private static final float DETACHED_PART_DURATION = 1.30f;
    private static final float HEAD_IMPACT_TIME = 0.84f;
    private static final float ARM_IMPACT_TIME = 0.82f;

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
                || "newspaper_defeat".equals(preferredClip)
                || "intro".equals(preferredClip)
                || "Pre_Intro".equals(preferredClip);
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

        if (detachedDeathPlaying) {
            detachedDeathTime += delta;
            if (detachedDeathTime >= DETACHED_PART_DURATION) {
                detachedDeathPlaying = false;
                currentClipName = null;
                setClip(bodyDeathClip, true);
            }
        }

        if (playingSequence && !playing) {
            sequenceIndex++;
            if (sequenceQueue != null && sequenceIndex < sequenceQueue.size()) {
                advanceSequenceClip();
            } else {
                String restClip = sequenceRestClip; boolean restOneShot = sequenceRestOneShot;
                playingSequence = false; playingSpecial = false; sequenceQueue = null;
                if (restClip != null) { currentClipName = null; setClip(restClip, restOneShot); }
            }
            return;
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

        if (detachedDeathPlaying) {
            drawDetachedPart(batch, "particle_head", true);
            drawDetachedPart(batch, "particle_arm", false);
        }

        // --- 3. Red danger flicker (rendered ON TOP of the sprite) ---
        if (dangerAlpha > 0f) {
            Color prev = batch.getColor().cpy();
            batch.setColor(1f, 0f, 0f, dangerAlpha * parentAlpha);
            super.draw(batch, parentAlpha);
            batch.setColor(prev);
        }
    }

    private void drawDetachedPart(Batch batch, String partName, boolean head) {
        float t = detachedDeathTime;
        float impactTime = head ? HEAD_IMPACT_TIME : ARM_IMPACT_TIME;
        float fallingTime = Math.min(t, impactTime);
        float dx = head ? -45f * fallingTime : 28f * fallingTime;
        float floorY = head
                ? 55f * impactTime - 265f * impactTime * impactTime
                : 35f * impactTime - 245f * impactTime * impactTime;
        float dy;
        if (t < impactTime) {
            dy = head
                    ? 55f * t - 265f * t * t
                    : 35f * t - 245f * t * t;
        } else {
            float timeAfterImpact = t - impactTime;
            float bounceVelocity = head ? 85f : 65f;
            float bounceHeight = bounceVelocity * timeAfterImpact
                    - 310f * timeAfterImpact * timeAfterImpact;
            // Once the bounce returns to the lawn, keep the part resting there.
            dy = floorY + Math.max(0f, bounceHeight);
        }

        float centerX = getX() + getWidth() / 2f + offsetX;
        float centerY = getY() + getHeight() / 2f + offsetY;
        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = oldTransform.cpy();
        transform.translate(centerX, centerY, 0f);
        transform.scale(pamScale, pamScale, 1f);
        transform.translate(dx, dy, 0f);
        batch.setTransformMatrix(transform);
        try {
            player.drawPart(batch, pamPath, "particles", 0f, 0f, 0f, partName);
        } catch (RuntimeException ignored) {
            // Zombies without this optional detached part keep their PAM death clip.
        }
        batch.setTransformMatrix(oldTransform);
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

    public void playSequence(java.util.List<String> clips, String restClip, boolean restOneShot) {
        if (playingSequence || clips == null || clips.isEmpty()) return;
        java.util.List<String> valid = new java.util.ArrayList<>();
        for (String c : clips) if (hasClip(c)) valid.add(c);
        if (valid.isEmpty()) return;
        sequenceQueue = valid; sequenceIndex = 0; sequenceRestClip = restClip;
        sequenceRestOneShot = restOneShot; playingSequence = true; playingSpecial = true;
        advanceSequenceClip();
    }

    /**
     * Plays the complete zombie death. Regular zombie PAMs only provide static
     * head and arm particle artwork, so their falling motion is supplied here.
     * Zombies such as King, whose head fall is already baked into "die", keep
     * using that native clip unchanged.
     */
    public void playDeath(String deathClip) {
        if (deathPlaybackStarted) {
            return;
        }
        resolveArmorParts();
        deathPlaybackStarted = true;
        if (hasDetachedHeadPart && hasClip(deathClip) && hasClip("particles")) {
            bodyDeathClip = deathClip;
            detachedDeathTime = 0f;
            detachedDeathPlaying = true;
            playingSpecial = true;
            for (String partName : detachableBodyParts) {
                armorVisibility.put(partName, false);
            }
            partVisibility = armorVisibility;
            return;
        }
        setClip(deathClip, true);
    }

    public void playDeathSequence(List<String> clips) {
        if (deathPlaybackStarted) {
            return;
        }
        deathPlaybackStarted = true;
        playSequence(clips, null, true);
    }

    private void advanceSequenceClip() {
        String clip = sequenceQueue.get(sequenceIndex);
        clipRef = player.getClip(pamPath, clip);
        returnToClip = null; currentClipName = clip; stateTime = 0f; playing = true;
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
        if (lowerName.equals("particle_head")) {
            hasDetachedHeadPart = true;
        }
        if (lowerName.equals("zombie_skull")
                || lowerName.equals("zombie_jaw")
                || lowerName.contains("hand_outer")
                || lowerName.contains("arm_outer")
                || lowerName.contains("arms_outer")) {
            detachableBodyParts.add(part.name);
        }
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
        boolean isOneShot = "die".equals(clipName)
                || "newspaper_defeat".equals(clipName)
                || "intro".equals(clipName)
                || "Pre_Intro".equals(clipName);
        setClip(clipName, isOneShot);
    }

    /**
     * Sets the clip with an explicit one-shot flag, instead of relying on the
     * hardcoded "die"/"newspaper_defeat" name check. Used for boss death clips
     * (e.g. "die_idle") that don't match those literal names but should still
     * freeze on the last frame rather than loop.
     */
    public void setClip(String clipName, boolean oneShot) {
        if (playingSpecial && !oneShot) {
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
        setLooping(!oneShot);
    }
}
