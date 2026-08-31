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
    private static final float ARM_IMPACT_TIME  = 0.82f;

    // -------------------------------------------------------------------------
    // Status / glow / danger state  (view-only — driven by EntityRenderLayer)
    // -------------------------------------------------------------------------

    /**
     * Current zombie status — used to choose the tint color drawn on top of the
     * sprite.  EntityRenderLayer sets this every frame from zombie.getStatus().
     */
    private com.ussr.pvz.model.entities.zombies.Zombie.Status zombieStatus =
            com.ussr.pvz.model.entities.zombies.Zombie.Status.NORMAL;

    /** When true, a soft green halo is drawn on top of the sprite. */
    private boolean glowing;

    /**
     * 0 = no danger overlay; 1 = full danger.
     * EntityRenderLayer sets this every frame based on the zombie's X position.
     */
    private float dangerAlpha;

    /**
     * When true the sprite is drawn mirrored horizontally (speed.x > 0, i.e.
     * hypnotized walk or Prospector reverse).  EntityRenderLayer sets this every
     * frame by checking zombie.getSpeed().x() > 0.
     */
    private boolean mirroredHorizontally;

    // Glow-pulse accumulator
    private float glowTime;

    // ── Tint constants ────────────────────────────────────────────────────────
    private static final float GLOW_PULSE_SPEED    = 2.5f;
    private static final float GLOW_BASE_ALPHA      = 0.35f;   // raised so it's visible
    private static final float GLOW_PULSE_AMPLITUDE = 0.15f;

    /** Alpha for all non-glow, non-danger status overlays. */
    private static final float STATUS_OVERLAY_ALPHA = 0.40f;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public ZombiePamActor(PamPlayer player, String pamPath) {
        super(player, pamPath, "walk");
        this.pamScale = 0.65f;
        this.offsetY  = -40f;
    }

    public ZombiePamActor(PamPlayer player, String pamPath, String preferredClip) {
        super(player, pamPath, preferredClip);
        this.pamScale = 0.65f;
        this.offsetY  = -40f;
        boolean isOneShot = "die".equals(preferredClip)
                || "newspaper_defeat".equals(preferredClip)
                || "intro".equals(preferredClip)
                || "Pre_Intro".equals(preferredClip);
        setLooping(!isOneShot);
    }

    // -------------------------------------------------------------------------
    // Public setters called by EntityRenderLayer each frame
    // -------------------------------------------------------------------------

    /** Called every frame with the zombie's current status. */
    public void setZombieStatus(com.ussr.pvz.model.entities.zombies.Zombie.Status status) {
        this.zombieStatus = (status != null)
                ? status
                : com.ussr.pvz.model.entities.zombies.Zombie.Status.NORMAL;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    public void setDangerAlpha(float dangerAlpha) {
        this.dangerAlpha = Math.max(0f, Math.min(1f, dangerAlpha));
    }

    /**
     * Mirrors the sprite horizontally when the zombie is moving right
     * (positive speed.x — hypnotized, Prospector reverse phase, etc.).
     */
    public void setMirroredHorizontally(boolean mirrored) {
        this.mirroredHorizontally = mirrored;
    }

    public void setFrozenSolid(boolean frozenSolid) {
        setPaused(frozenSolid);
    }

    // -------------------------------------------------------------------------
    // act
    // -------------------------------------------------------------------------

    @Override
    public void act(float delta) {
        super.act(delta);

        if (glowing) glowTime += delta;

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
                String restClip = sequenceRestClip;
                boolean restOneShot = sequenceRestOneShot;
                playingSequence = false;
                playingSpecial  = false;
                sequenceQueue   = null;
                if (restClip != null) { currentClipName = null; setClip(restClip, restOneShot); }
            }
            return;
        }

        if (playingSpecial && !playing) {
            String nextClip = returnToClip;
            playingSpecial = false;
            returnToClip   = null;
            currentClipName = null;
            setClip(nextClip == null ? "idle" : nextClip);
        }
    }

    // -------------------------------------------------------------------------
    // draw
    // -------------------------------------------------------------------------

    /**
     * Renders the sprite with the correct horizontal scale.
     * PamActor.draw() uses {@code transform.scale(pamScale, pamScale, 1f)} which
     * would flip both axes if we negated pamScale.  Instead we call
     * {@link PamPlayer#draw(Batch, ClipRef, float, float, float, float, float, boolean, java.util.Map)}
     * directly so we can pass scaleX = ±|pamScale| independently of scaleY.
     */
    private void drawSprite(Batch batch, float parentAlpha) {
        if (player == null || clipRef == null) {
            super.draw(batch, parentAlpha);  // fallback
            return;
        }
        float scaleX = mirroredHorizontally ? -Math.abs(pamScale) : Math.abs(pamScale);
        float scaleY = Math.abs(pamScale);

        // Centre point in screen space (same calculation as PamActor.draw)
        float centerX = getX() + getWidth() / 2f + offsetX;
        float centerY = getY() + getHeight() / 2f + offsetY;

        com.badlogic.gdx.graphics.Color prev = batch.getColor().cpy();
        // Apply parentAlpha to whatever tint is already set
        batch.setColor(prev.r, prev.g, prev.b, prev.a * parentAlpha);

        try {
            if (partVisibility == null || partVisibility.isEmpty()) {
                player.draw(batch, clipRef, stateTime, centerX, centerY,
                        scaleX, scaleY, looping);
            } else {
                player.draw(batch, clipRef, stateTime, centerX, centerY,
                        scaleX, scaleY, looping, partVisibility);
            }
        } catch (Exception ignored) {}

        batch.setColor(prev);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // 1. Normal sprite (always drawn first, at full colour)
        // Mirror: flip X scale only — PamPlayer.draw supports separate scaleX/scaleY.
        // We draw by hand here instead of calling super so we can pass ±scaleX.
        drawSprite(batch, parentAlpha);

        if (detachedDeathPlaying) {
            drawDetachedPart(batch, "particle_head", true);
            drawDetachedPart(batch, "particle_arm",  false);
        }

        // 2. Status colour overlay — drawn ON TOP so it's always visible
        drawStatusOverlay(batch, parentAlpha);

        // 3. Red danger flicker — also on top
        if (dangerAlpha > 0f) {
            Color prev = batch.getColor().cpy();
            batch.setColor(1f, 0f, 0f, dangerAlpha * parentAlpha);
            drawSprite(batch, 1f);  // parentAlpha already baked into dangerAlpha
            batch.setColor(prev);
        }
    }

    /**
     * Draws a semi-transparent colour pass on top of the sprite to communicate
     * the zombie's current status.  Nothing is drawn for NORMAL (and DEAD).
     *
     * <ul>
     *   <li>FREEZE / FROZEN_SOLID → icy blue</li>
     *   <li>FIRED                 → orange-red</li>
     *   <li>POISONED              → purple</li>
     *   <li>BUTTER                → yellow</li>
     *   <li>HYPNOTIZED            → magenta</li>
     *   <li>glowing (plant-food)  → pulsing green</li>
     * </ul>
     */
    private void drawStatusOverlay(Batch batch, float parentAlpha) {
        float r = 0f, g = 0f, b = 0f, a = 0f;

        // Glowing takes priority over any status tint
        if (glowing) {
            float pulse = GLOW_BASE_ALPHA
                    + GLOW_PULSE_AMPLITUDE * (float) Math.sin(glowTime * GLOW_PULSE_SPEED);
            r = 0f; g = 1f; b = 0f;
            a = pulse * parentAlpha;
        } else {
            switch (zombieStatus) {
                case FREEZE, FROZEN_SOLID -> { r = 0.45f; g = 0.75f; b = 1.00f; a = STATUS_OVERLAY_ALPHA * parentAlpha; }
                case FIRED               -> { r = 1.00f; g = 0.45f; b = 0.00f; a = STATUS_OVERLAY_ALPHA * parentAlpha; }
                case POISONED            -> { r = 0.65f; g = 0.00f; b = 0.90f; a = STATUS_OVERLAY_ALPHA * parentAlpha; }
                case BUTTER              -> { r = 1.00f; g = 0.90f; b = 0.00f; a = STATUS_OVERLAY_ALPHA * parentAlpha; }
                case HYPNOTIZED          -> { r = 1.00f; g = 0.00f; b = 1.00f; a = STATUS_OVERLAY_ALPHA * parentAlpha; }
                default -> { return; }  // NORMAL / DEAD — no overlay
            }
        }

        Color prev = batch.getColor().cpy();
        batch.setColor(r, g, b, a);
        drawSprite(batch, 1f);  // parentAlpha already baked into 'a'
        batch.setColor(prev);
    }

    // -------------------------------------------------------------------------
    // Detached-part physics (unchanged)
    // -------------------------------------------------------------------------

    private void drawDetachedPart(Batch batch, String partName, boolean head) {
        float t          = detachedDeathTime;
        float impactTime = head ? HEAD_IMPACT_TIME : ARM_IMPACT_TIME;
        float floorY;
        float dy;

        if (head) {
            floorY = 55f * impactTime - 265f * impactTime * impactTime;
        } else {
            floorY = 35f * impactTime - 245f * impactTime * impactTime;
        }

        if (t < impactTime) {
            dy = head
                    ? 55f * t - 265f * t * t
                    : 35f * t - 245f * t * t;
        } else {
            float afterImpact = t - impactTime;
            float vel = head ? 85f : 65f;
            dy = floorY + Math.max(0f, vel * afterImpact - 310f * afterImpact * afterImpact);
        }

        float dx = head ? -45f * Math.min(t, impactTime) : 28f * Math.min(t, impactTime);

        float centerX = getX() + getWidth() / 2f + offsetX;
        float centerY = getY() + getHeight() / 2f + offsetY;

        Matrix4 old = batch.getTransformMatrix().cpy();
        Matrix4 transform = old.cpy();
        transform.translate(centerX, centerY, 0f);
        transform.scale(pamScale, pamScale, 1f);
        transform.translate(dx, dy, 0f);
        batch.setTransformMatrix(transform);
        try {
            player.drawPart(batch, pamPath, "particles", 0f, 0f, 0f, partName);
        } catch (RuntimeException ignored) {}
        batch.setTransformMatrix(old);
    }

    // -------------------------------------------------------------------------
    // Clip control (unchanged)
    // -------------------------------------------------------------------------

    public void playOnce(String clipName, String returnTo) {
        if (playingSpecial || !hasClip(clipName)) return;
        ClipRef specialClip = player.getClip(pamPath, clipName);
        this.returnToClip   = returnTo;
        this.currentClipName = clipName;
        this.clipRef    = specialClip;
        this.stateTime  = 0f;
        this.playing    = true;
        this.playingSpecial = true;
        setLooping(false);
    }

    public void playSequence(java.util.List<String> clips, String restClip, boolean restOneShot) {
        if (playingSequence || clips == null || clips.isEmpty()) return;
        java.util.List<String> valid = new java.util.ArrayList<>();
        for (String c : clips) if (hasClip(c)) valid.add(c);
        if (valid.isEmpty()) return;
        sequenceQueue     = valid;
        sequenceIndex     = 0;
        sequenceRestClip  = restClip;
        sequenceRestOneShot = restOneShot;
        playingSequence   = true;
        playingSpecial    = true;
        advanceSequenceClip();
    }

    public void playDeath(String deathClip) {
        if (deathPlaybackStarted) return;
        resolveArmorParts();
        deathPlaybackStarted = true;
        if (hasDetachedHeadPart && hasClip(deathClip) && hasClip("particles")) {
            bodyDeathClip        = deathClip;
            detachedDeathTime    = 0f;
            detachedDeathPlaying = true;
            playingSpecial       = true;
            for (String partName : detachableBodyParts) armorVisibility.put(partName, false);
            partVisibility = armorVisibility;
            return;
        }
        setClip(deathClip, true);
    }

    public void playDeathSequence(List<String> clips) {
        if (deathPlaybackStarted) return;
        deathPlaybackStarted = true;
        playSequence(clips, null, true);
    }

    private void advanceSequenceClip() {
        String clip = sequenceQueue.get(sequenceIndex);
        clipRef         = player.getClip(pamPath, clip);
        returnToClip    = null;
        currentClipName = clip;
        stateTime       = 0f;
        playing         = true;
        setLooping(false);
    }

    public boolean isPlayingSpecial() { return playingSpecial; }

    private boolean hasClip(String clipName) {
        if (clipName == null || clipName.isBlank()) return false;
        try { return player.clips(pamPath).contains(clipName); }
        catch (RuntimeException ignored) { return false; }
    }

    public void setArmor(Armor armor) {
        resolveArmorParts();
        if (armorVisibility.isEmpty()) return;
        armorVisibility.replaceAll((name, visible) -> false);
        if (armor != null && !armor.isDestroyed()) {
            String wanted = switch (armor.getDamageLayer()) {
                case 0  -> "_norm";
                case 1  -> "_damage_01";
                default -> "_damage_02";
            };
            armorVisibility.replaceAll((name, visible) ->
                    name.toLowerCase(Locale.ROOT).contains(wanted));
        }
        partVisibility = armorVisibility;
    }

    private void resolveArmorParts() {
        if (armorPartsResolved) return;
        armorPartsResolved = true;
        try { collectArmorParts(player.getParts(pamPath)); }
        catch (RuntimeException ignored) {}
    }

    private void collectArmorParts(PamPlayer.AnimationPart part) {
        if (part == null) return;
        String lowerName = part.name == null ? "" : part.name.toLowerCase(Locale.ROOT);
        if (lowerName.equals("particle_head")) hasDetachedHeadPart = true;
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
        for (PamPlayer.AnimationPart child : part.children) collectArmorParts(child);
    }

    @Override
    public void setClip(String clipName) {
        boolean isOneShot = "die".equals(clipName)
                || "newspaper_defeat".equals(clipName)
                || "intro".equals(clipName)
                || "Pre_Intro".equals(clipName);
        setClip(clipName, isOneShot);
    }

    public void setClip(String clipName, boolean oneShot) {
        if (playingSpecial && !oneShot) return;
        if (playingSpecial) { playingSpecial = false; returnToClip = null; }
        if (java.util.Objects.equals(currentClipName, clipName)) return;
        super.setClip(clipName);
        setLooping(!oneShot);
    }
}