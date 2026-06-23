package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.controller.command.maincommand.gamecommand.ChoosePlantCommand;
import com.ussr.pvz.model.dto.PlantTypeRequest;
import com.ussr.pvz.service.ChoosePlantService;

import java.util.regex.Matcher;

public class ChoosePlantController {
    private final ChoosePlantService choosePlantService = new ChoosePlantService();


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
        return choosePlantService.showAllPlants();
    }

    private String handleShowAvailablePlants() {
        return choosePlantService.showAvailablePlants();
    }

    private String handleAddPlant(Matcher matcher) {
        PlantTypeRequest request = new PlantTypeRequest(matcher.group("type"));
        return choosePlantService.addPlant(request);
    }

    private String handleRemovePlant(Matcher matcher) {
        PlantTypeRequest request = new PlantTypeRequest(matcher.group("type"));
        return choosePlantService.removePlant(request);
    }

    private String handleBoostPlant(Matcher matcher) {
        PlantTypeRequest request = new PlantTypeRequest(matcher.group("type"));
        return choosePlantService.boostPlant(request);
    }

    private String handleStartGame() {
        return choosePlantService.startGame();
    }
}