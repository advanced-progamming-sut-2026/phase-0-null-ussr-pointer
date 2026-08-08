package com.ussr.pvz.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.math.Interpolation;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.plants.PlantFactory;
import com.ussr.pvz.controller.maincontroller.gamecontroller.GameplayController;
import com.ussr.pvz.model.App;
import com.ussr.pvz.model.engine.session.GameSession;
import com.ussr.pvz.model.level.Level;
import com.ussr.pvz.model.level.delivery.ConveyorDeliveryStrategy;
import com.ussr.pvz.service.ChoosePlantService;
import com.ussr.pvz.service.CollectionService.PlantData;
import com.ussr.pvz.view.components.PlantCard;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.List;

public class ConveyorBeltWidget extends Table {
    private static final int SLOT_W = 110;
    private static final int SLOT_H = 68;
    private static final int MAX_VISIBLE = 6;
    private static final int FRAME_SIDE_W = 13;
    private static final int FRAME_CAP_H = 10;
    private static final int BELT_SURFACE_W = 127;
    private static final int BELT_W = BELT_SURFACE_W + FRAME_SIDE_W * 2;
    private static final int BELT_H = SLOT_H * MAX_VISIBLE;

    private final TextureBank textures;
    private final Skin skin;
    private final GameplayController controller;

    private final Table slotColumn = new Table();
    private final List<String> lastBelt = new ArrayList<>();
    private final List<PlantCard> slotWidgets = new ArrayList<>();
    private String selectedKey = null;
    private int selectedSlot = -1;

    public ConveyorBeltWidget(Skin skin, TextureBank textures, GameplayController controller) {
        this.skin = skin;
        this.textures = textures;
        this.controller = controller;
        top();

        slotColumn.top();
        Stack beltStack = new Stack();
        beltStack.add(buildBeltSurfaceLayer());

        Table packetLayer = new Table();
        packetLayer.top();
        packetLayer.add(slotColumn).width(SLOT_W).top();
        beltStack.add(packetLayer);
        beltStack.add(buildBeltFrame());

        add(beltStack).size(BELT_W, BELT_H);

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
        boolean isNewArrival = belt.size() > lastBelt.size();

        lastBelt.clear();
        lastBelt.addAll(belt);

        if (selectedKey != null && belt.stream().noneMatch(b -> matches(b, selectedKey))) {
            selectedKey = null;
            selectedSlot = -1;
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
            Group holder = buildSlot(plantName, animateIn, shown);
            slotColumn.add(holder).size(SLOT_W, SLOT_H).row();
            shown++;
        }
    }

    private Actor buildBeltSurface() {
        TextureRegion region = textures.region("IMAGE_UI_CONVEYOR_CONVEYOR_BELT");
        if (region == null) {
            return new Actor();
        }
        return new ScrollingBeltActor(region);
    }

    private Table buildBeltSurfaceLayer() {
        Table layer = new Table();
        layer.setTouchable(Touchable.disabled);
        layer.add(buildBeltSurface()).grow()
                .pad(FRAME_CAP_H, FRAME_SIDE_W, FRAME_CAP_H, FRAME_SIDE_W);
        return layer;
    }

    private Actor buildBeltFrame() {
        TextureRegion side = textures.region("IMAGE_UI_CONVEYOR_CONVEYOR_SIDE");
        TextureRegion top = textures.region("IMAGE_UI_CONVEYOR_CONVEYOR_TOP");
        return new ConveyorFrameActor(side, top);
    }

    private Group buildSlot(String plantName, boolean animateIn, int slotIndex) {
        Group holder = new Group();
        holder.setSize(SLOT_W, SLOT_H);
        Plant blueprint = PlantFactory.createPlantByName(plantName, 1);
        PlantCard packet = new PlantCard(toPlantData(blueprint), skin, textures,
                () -> selectPacket(plantName, slotIndex));
        packet.setSize(SLOT_W, SLOT_H);
        packet.setTransform(true);
        packet.setOrigin(Align.center);
        applySelectionStyle(packet, slotIndex == selectedSlot);
        holder.addActor(packet);
        slotWidgets.add(packet);
        if (animateIn) {
            float travelFromBottom = -(BELT_H - (slotIndex + 1f) * SLOT_H);
            packet.setY(travelFromBottom);
            packet.addAction(Actions.moveTo(0f, 0f, 0.55f, Interpolation.smooth));
        }
        return holder;
    }

    private PlantData toPlantData(Plant plant) {
        PlantData data = new PlantData();
        data.id = ChoosePlantService.normalizePlantKey(plant.getName());
        data.name = plant.getName();
        data.level = plant.getLevel();
        data.cost = plant.getCost();
        data.damage = plant.getDamage();
        data.baseHp = plant.getMaxHp();
        data.recharge = (int) plant.getRecharge();
        data.pamPath = plant.getPamPath();
        data.isBoosted = plant.isBuffed();
        return data;
    }

    private void selectPacket(String plantName, int slotIndex) {
        String key = ChoosePlantService.normalizePlantKey(plantName);
        boolean deselect = slotIndex == selectedSlot;
        selectedKey = deselect ? null : key;
        selectedSlot = deselect ? -1 : slotIndex;
        controller.setSelectedSeed(selectedKey);

        // Update the existing slot actors in place instead of calling rebuild().
        // Rebuilding would clear/re-add children on slotColumn, which invalidates
        // the Table's layout and makes it re-run on the next frame -- overwriting
        // any manual setY() lift we apply here and snapping the packet back down.
        refreshSelectionVisuals();
    }

    private void refreshSelectionVisuals() {
        for (int i = 0; i < slotWidgets.size() && i < lastBelt.size(); i++) {
            applySelectionStyle(slotWidgets.get(i), i == selectedSlot);
        }
    }

    public void clearSelection() {
        if (selectedKey == null) {
            return;
        }
        selectedKey = null;
        selectedSlot = -1;
        controller.setSelectedSeed(null);
        refreshSelectionVisuals();
    }

    private void applySelectionStyle(PlantCard slot, boolean selected) {
        slot.setSelectionVisible(false);
        slot.setVisible(true);
        slot.setColor(selected ? Color.GOLD : Color.WHITE);
        slot.setScale(selected ? 1.05f : 1f);
    }

    private static final class ScrollingBeltActor extends Actor {
        private static final float PIXELS_PER_SECOND = 2.8f;
        private final TextureRegion region;
        private float offset;

        private ScrollingBeltActor(TextureRegion region) {
            this.region = region;
            setTouchable(Touchable.disabled);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            offset = (offset + PIXELS_PER_SECOND * delta) % region.getRegionHeight();
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            float stripHeight = region.getRegionHeight();
            for (float y = -stripHeight + offset; y < getHeight(); y += stripHeight) {
                drawClippedStrip(batch, y, stripHeight);
            }
            batch.setColor(Color.WHITE);
        }

        private void drawClippedStrip(Batch batch, float stripY, float stripHeight) {
            float visibleBottom = Math.max(0f, stripY);
            float visibleTop = Math.min(getHeight(), stripY + stripHeight);
            float visibleHeight = visibleTop - visibleBottom;
            if (visibleHeight <= 0f) return;

            int clippedFromBottom = Math.round(visibleBottom - stripY);
            int clippedFromTop = Math.round(stripY + stripHeight - visibleTop);
            int sourceHeight = region.getRegionHeight() - clippedFromBottom - clippedFromTop;
            if (sourceHeight <= 0) return;

            TextureRegion visibleRegion = new TextureRegion(
                    region.getTexture(),
                    region.getRegionX(),
                    region.getRegionY() + clippedFromTop,
                    region.getRegionWidth(),
                    sourceHeight
            );
            batch.draw(visibleRegion, getX(), getY() + visibleBottom,
                    getWidth(), visibleHeight);
        }
    }

    private static final class ConveyorFrameActor extends Actor {
        private final TextureRegion leftSide;
        private final TextureRegion rightSide;
        private final TextureRegion top;
        private final TextureRegion bottom;

        private ConveyorFrameActor(TextureRegion side, TextureRegion top) {
            this.rightSide = side;
            this.leftSide = side == null ? null : new TextureRegion(side);
            if (leftSide != null) {
                leftSide.flip(true, false);
            }
            this.top = top;
            this.bottom = top == null ? null : new TextureRegion(top);
            if (bottom != null) {
                bottom.flip(false, true);
            }
            setTouchable(Touchable.disabled);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            if (leftSide != null) {
                batch.draw(leftSide, getX(), getY(),
                        FRAME_SIDE_W, getHeight());
            }
            if (rightSide != null) {
                batch.draw(rightSide, getX() + getWidth() - FRAME_SIDE_W, getY(),
                        FRAME_SIDE_W, getHeight());
            }
            if (top != null) {
                batch.draw(top, getX(), getY() + getHeight() - FRAME_CAP_H,
                        getWidth(), FRAME_CAP_H);
            }
            if (bottom != null) {
                batch.draw(bottom, getX(), getY(), getWidth(), FRAME_CAP_H);
            }
            batch.setColor(Color.WHITE);
        }
    }

    private boolean matches(String rawPlantName, String normalizedKey) {
        return normalizedKey != null && ChoosePlantService.normalizePlantKey(rawPlantName).equals(normalizedKey);
    }
}
