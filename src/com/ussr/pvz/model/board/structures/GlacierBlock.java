package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;

public class GlacierBlock extends InteractableStructure {
    private int hp;
    private final TileType previousTileType;
    private final Runnable onDestroyed;
    private final String spawnZombieAliasOnDestroy;
    private final String pamLocation = "768/FULL/EFFECTS/ZOMBOSS_GLACIER_BLOCK/ZOMBOSS_GLACIER_BLOCK.PAM";
    private boolean damaged = false;

    public GlacierBlock(int hp, TileType previousTileType, Runnable onDestroyed) {
        this(hp, previousTileType, onDestroyed, null);
    }

    public GlacierBlock(int hp, TileType previousTileType, Runnable onDestroyed, String spawnZombieAliasOnDestroy) {
        this.hp = hp;
        this.previousTileType = previousTileType;
        this.onDestroyed = onDestroyed;
        this.spawnZombieAliasOnDestroy = spawnZombieAliasOnDestroy;
        setAlive(true);
    }

    @Override
    public void takeDamage(int damage) {
        if (!isAlive()) return;
        damaged = true;
        hp -= damage;
        if (hp <= 0) {
            setAlive(false);
        }
    }

    @Override
    public void onDestroy(GameSession session) {
        int col = (int) getPosition().x();
        int row = (int) getPosition().y();

        if (session.getLawn().getTile(row, col) != null) {
            session.getLawn().getTile(row, col).setType(previousTileType);
        }

        if (spawnZombieAliasOnDestroy != null && !spawnZombieAliasOnDestroy.isBlank()) {
            try {
                Zombie spawned = ZombieFactory.create(spawnZombieAliasOnDestroy, row, col);
                session.spawnZombie(spawned);
            } catch (Exception ignored) {
            }
        }

        if (onDestroyed != null) {
            onDestroyed.run();
        }
    }

    public boolean isDamaged() {
            return damaged;
        }

    public String getPamLocation() {
        return pamLocation;
    }
}