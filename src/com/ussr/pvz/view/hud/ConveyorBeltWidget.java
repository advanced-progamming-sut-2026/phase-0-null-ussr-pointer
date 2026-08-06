package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.delivery.ConveyorDeliveryStrategy;
import com.ussr.pvz.service.ChoosePlantService;
import com.ussr.pvz.view.components.PlantCard;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.List;

public class ConveyorBeltWidget extends Table {
    private static final int SLOT_W = 62;
    private static final int SLOT_H = 78;
    private static final int MAX_VISIBLE = 6;

    // Vertical offset applied to a selected packet, mirroring SeedPacketWidget's lift.
    private static final float SELECTED_LIFT = 10f;

    private final TextureBank textures;
    private final GameplayController controller;

    private final Table slotColumn = new Table();
    private final List<String> lastBelt = new ArrayList<>();
    private final List<Stack> slotWidgets = new ArrayList<>();
    private String selectedKey = null;

    public ConveyorBeltWidget(Skin skin, TextureBank textures, GameplayController controller) {
        this.textures = textures;
        this.controller = controller;
        top();

        TextureRegion beltTex = textures.region("IMAGE_UI_HUD_INGAME_BACKGROUND_3SLICE");
        if (beltTex != null) setBackground(new TextureRegionDrawable(beltTex));
        pad(6f);

        slotColumn.top();
        add(slotColumn).width(SLOT_W);

        setTouchable(Touchable.childrenOnly);
        setVisible(false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        GameSession session = App.getGameSession();
        Level level = session != null ? session.getLevel() : null;
        if (!(level != null && level.getDeliveryStrategy() instanceof ConveyorDeliveryStrategy conveyor)) {
            setVisible(false);
            return;
        }
        setVisible(true);

        List<String> belt = conveyor.getConveyorBelt();
        if (!belt.equals(lastBelt)) {
            rebuild(belt);
        }
    }

    private void rebuild(List<String> belt) {
        boolean isNewArrival = !belt.isEmpty()
                && (lastBelt.isEmpty() || !belt.get(belt.size() - 1).equals(lastBelt.get(lastBelt.size() - 1)));

        lastBelt.clear();
        lastBelt.addAll(belt);

        if (selectedKey != null && belt.stream().noneMatch(b -> matches(b, selectedKey))) {
            selectedKey = null;
            controller.setSelectedSeed(null);
        }

        // Full teardown: only happens when the belt's contents actually changed
        // (a packet arrived or was consumed), never on a bare selection toggle.
        slotColumn.clearChildren();
        slotWidgets.clear();
        int shown = 0;
        for (String plantName : belt) {
            if (shown >= MAX_VISIBLE) break;
            boolean animateIn = isNewArrival && shown == belt.size() - 1;
            Stack slot = buildSlot(plantName, animateIn);
            slotWidgets.add(slot);
            slotColumn.add(slot).size(SLOT_W, SLOT_H).padBottom(6f).row();
            shown++;
        }
    }

    private Stack buildSlot(String plantName, boolean animateIn) {
        Stack slot = new Stack();
        slot.setTouchable(Touchable.enabled);

        TextureRegion bgRegion = textures.region("IMAGE_UI_PACKETS_EGYPT");
        Image bg = bgRegion != null ? new Image(bgRegion) : new Image();
        bg.setScaling(Scaling.fit);
        bg.setTouchable(Touchable.disabled);
        slot.add(bg);

        String packetKey = PlantCard.resolvePacketKey(plantName);
        TextureRegion iconRegion = textures.region("IMAGE_UI_PACKETS_" + packetKey);
        Image icon = iconRegion != null ? new Image(iconRegion) : new Image();
        icon.setScaling(Scaling.fit);
        icon.setTouchable(Touchable.disabled);
        Table iconLayer = new Table();
        iconLayer.setTouchable(Touchable.disabled);
        iconLayer.add(icon).grow().pad(3f);
        slot.add(iconLayer);

        applySelectionStyle(slot, matches(plantName, selectedKey));

        slot.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectPacket(plantName);
            }
        });

        if (animateIn) {
            // Rolling entrance: the freshly-delivered packet slides down into place.
            slot.addAction(Actions.sequence(
                    Actions.moveBy(0f, 30f),
                    Actions.moveBy(0f, -30f, 0.3f)
            ));
        }

        return slot;
    }

    private void selectPacket(String plantName) {
        String key = ChoosePlantService.normalizePlantKey(plantName);
        selectedKey = key.equals(selectedKey) ? null : key;
        controller.setSelectedSeed(selectedKey);

        // Update the existing slot actors in place instead of calling rebuild().
        // Rebuilding would clear/re-add children on slotColumn, which invalidates
        // the Table's layout and makes it re-run on the next frame -- overwriting
        // any manual setY() lift we apply here and snapping the packet back down.
        refreshSelectionVisuals();
    }

    private void refreshSelectionVisuals() {
        for (int i = 0; i < slotWidgets.size() && i < lastBelt.size(); i++) {
            applySelectionStyle(slotWidgets.get(i), matches(lastBelt.get(i), selectedKey));
        }
    }

    private void applySelectionStyle(Stack slot, boolean selected) {
        slot.setColor(selected ? Color.GOLD : Color.WHITE);
        slot.setY(selected ? SELECTED_LIFT : 0f);
    }

    private boolean matches(String rawPlantName, String normalizedKey) {
        return normalizedKey != null && ChoosePlantService.normalizePlantKey(rawPlantName).equals(normalizedKey);
    }
}