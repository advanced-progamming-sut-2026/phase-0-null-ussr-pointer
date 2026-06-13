package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.controller.command.maincommand.gamecommand.ChoosePlantCommand;
import com.ussr.pvz.model.dto.PlantTypeRequest;

import java.util.regex.Matcher;

public class ChoosePlantController {

    public ChoosePlantController() {
    }

    public String handleCommand(String command) {
        for (ChoosePlantCommand cmd : ChoosePlantCommand.values()) {
            Matcher matcher = cmd.getMatcher(command);
            if (matcher.matches()) {
                return switch (cmd) {
                    case SHOW_ALL_PLANTS -> handleShowAllPlants();
                    case SHOW_AVAILABLE_PLANTS -> handleShowAvailablePlants();
                    case ADD_PLANT -> handleAddPlant(matcher);
                    case REMOVE_PLANT -> handleRemovePlant(matcher);
                    case BOOST_PLANT -> handleBoostPlant(matcher);
                    case START_GAME -> handleStartGame();
                };
            }
        }
        return "";
    }

    private String handleShowAllPlants() {
        // TODO: call choosePlantService.showAllPlants() and return its message
        return "";
    }

    private String handleShowAvailablePlants() {
        // TODO: call choosePlantService.showAvailablePlants() and return its message
        return "";
    }

    private String handleAddPlant(Matcher matcher) {
        PlantTypeRequest request = new PlantTypeRequest(matcher.group("type"));
        // TODO: call choosePlantService.addPlant(request) and return its message
        return "";
    }

    private String handleRemovePlant(Matcher matcher) {
        PlantTypeRequest request = new PlantTypeRequest(matcher.group("type"));
        // TODO: call choosePlantService.removePlant(request) and return its message
        return "";
    }

    private String handleBoostPlant(Matcher matcher) {
        PlantTypeRequest request = new PlantTypeRequest(matcher.group("type"));
        // TODO: call choosePlantService.boostPlant(request) and return its message
        return "";
    }

    private String handleStartGame() {
        // TODO: call choosePlantService.startGame() and return its message
        return "";
    }
}