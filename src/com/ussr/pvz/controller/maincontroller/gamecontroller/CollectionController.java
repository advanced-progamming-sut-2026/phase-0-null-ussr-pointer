package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.controller.command.maincommand.gamecommand.CollectionCommand;
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
                    case UPGRADE_PLANT -> handleUpgradePlant(matcher);
                    case PURCHASE_PLANT -> handlePurchasePlant(matcher);
                    default -> "Command recognized but handled entirely in GUI overlay.";
                };
            }
        }
        return "";
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