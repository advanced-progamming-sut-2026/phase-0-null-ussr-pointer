package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.controller.command.maincommand.gamecommand.CollectionCommand;
import com.ussr.pvz.model.dto.CollectionShowPlantRequest;
import com.ussr.pvz.model.dto.CollectionShowZombieRequest;
import com.ussr.pvz.model.dto.PlantTypeRequest;

import java.util.regex.Matcher;

public class CollectionController {

    public CollectionController() {
    }

    public String handleCommand(String command) {
        for (CollectionCommand cmd : CollectionCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case SHOW_PLANTS -> handleShowPlants();
                    case SHOW_ALL_PLANTS -> handleShowAllPlants();
                    case SHOW_ZOMBIES -> handleShowZombies();
                    case SHOW_ALL_ZOMBIES -> handleShowAllZombies();
                    case SHOW_PLANT -> handleShowPlant(matcher);
                    case SHOW_ZOMBIE -> handleShowZombie(matcher);
                    case UPGRADE_PLANT -> handleUpgradePlant(matcher);
                    case PURCHASE_PLANT -> handlePurchasePlant(matcher);
                };
            }
        }
        return "";
    }

    private String handleShowPlants() {
        // TODO: call collectionService.showPlants() and return its message
        return "";
    }

    private String handleShowAllPlants() {
        // TODO: call collectionService.showAllPlants() and return its message
        return "";
    }

    private String handleShowZombies() {
        // TODO: call collectionService.showZombies() and return its message
        return "";
    }

    private String handleShowAllZombies() {
        // TODO: call collectionService.showAllZombies() and return its message
        return "";
    }

    private String handleShowPlant(Matcher matcher) {
        CollectionShowPlantRequest request = new CollectionShowPlantRequest(matcher.group("plantName"));
        // TODO: call collectionService.showPlant(request) and return its message
        return "";
    }

    private String handleShowZombie(Matcher matcher) {
        CollectionShowZombieRequest request = new CollectionShowZombieRequest(matcher.group("zombieName"));
        // TODO: call collectionService.showZombie(request) and return its message
        return "";
    }

    private String handleUpgradePlant(Matcher matcher) {
        PlantTypeRequest request = new PlantTypeRequest(matcher.group("plantName"));
        // TODO: call collectionService.upgradePlant(request) and return its message
        return "";
    }

    private String handlePurchasePlant(Matcher matcher) {
        PlantTypeRequest request = new PlantTypeRequest(matcher.group("plantName"));
        // TODO: call collectionService.purchasePlant(request) and return its message
        return "";
    }
}