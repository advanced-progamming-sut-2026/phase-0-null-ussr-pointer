package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.controller.command.maincommand.gamecommand.CollectionCommand;
import com.ussr.pvz.model.dto.CollectionShowPlantRequest;
import com.ussr.pvz.model.dto.CollectionShowZombieRequest;
import com.ussr.pvz.model.dto.PlantTypeRequest;
import com.ussr.pvz.service.CollectionService;

import java.util.regex.Matcher;

public class CollectionController {
    private final CollectionService collectionService = new CollectionService();
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
        return collectionService.showPlants();
    }

    private String handleShowAllPlants() {
        return collectionService.showAllPlants();
    }

    private String handleShowZombies() {
        return collectionService.showZombies();
    }

    private String handleShowAllZombies() {
        return collectionService.showAllZombies();
    }

    private String handleShowPlant(Matcher matcher) {
        CollectionShowPlantRequest request = new CollectionShowPlantRequest(matcher.group("plantName"));
        return collectionService.showPlant(request.plantName());
    }

    private String handleShowZombie(Matcher matcher) {
        CollectionShowZombieRequest request = new CollectionShowZombieRequest(matcher.group("zombieName"));
        return collectionService.showZombie(request);
    }

    private String handleUpgradePlant(Matcher matcher) {
        PlantTypeRequest request = new PlantTypeRequest(matcher.group("plantName"));
        return collectionService.upgradePlant(request);
    }

    private String handlePurchasePlant(Matcher matcher) {
        PlantTypeRequest request = new PlantTypeRequest(matcher.group("plantName"));
        return collectionService.purchasePlant(request);
    }
}