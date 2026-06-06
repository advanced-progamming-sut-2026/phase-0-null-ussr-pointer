package model.entities.plants;

import model.engine.GameEntity;

import javax.swing.text.html.HTML;
import java.util.ArrayList;

public abstract class BasePlant extends GameEntity implements Plant {
    private final int recharge;
    private double actionInterval;
    private final int cost;
    private final ArrayList<Tag> tags = new ArrayList<>();
    private final PlantType type;
    private BasePlant bottom = null;
}
