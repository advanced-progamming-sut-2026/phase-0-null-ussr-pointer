package com.ussr.pvz.model.board.structures;

import com.ussr.pvz.model.board.terrain.TileType;
import com.ussr.pvz.model.engine.session.GameSession;

public class GlacierBlock extends InteractableStructure {
    private int hp;
    private final TileType previousTileType;
    private final Runnable onDestroyed;
    private final String pamLocation = "768/FULL/EFFECTS/GLACIER_BLOCK/GLACIER_BLOCK.PAM";

    public GlacierBlock(int hp, TileType previousTileType, Runnable onDestroyed) {
        this.hp = hp;
        this.previousTileType = previousTileType;
        this.onDestroyed = onDestroyed;
        setAlive(true);
    }

    @Override
    public void takeDamage(int damage) {
        if (!isAlive()) return;
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
        if (onDestroyed != null) {
            onDestroyed.run();
        }
    }

    public String getPamLocation() {
        return pamLocation;
    }
}