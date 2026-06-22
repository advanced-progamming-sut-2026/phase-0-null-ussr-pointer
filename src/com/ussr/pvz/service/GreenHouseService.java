package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.dto.GreenhousePotRequest;
import com.ussr.pvz.model.greenhouse.SproutPlant;

public class GreenHouseService {

    private int[] parseCoordinates(GreenhousePotRequest request) {
        try {
            int x = Integer.parseInt(request.x());
            int y = Integer.parseInt(request.y());
            return new int[]{x, y};
        } catch (NumberFormatException e) {
            throw new NumberFormatException("number format is wrong");
        }
    }

    private void validatePotUnlocked(int x, int y) {
        if (!App.getAccount().getGreenhouse().isPotUnlocked(x, y)) {
            throw new IllegalStateException("Pot is locked");
        }
    }

    private void validatePotOccupied(int x, int y) {
        if (!App.getAccount().getGreenhouse().isPotOccupied(x, y)) {
            throw new IllegalStateException("Pot is currently not in use");
        }
    }

    private void validatePotNotOccupied(int x, int y) {
        if (App.getAccount().getGreenhouse().isPotOccupied(x, y)) {
            throw new IllegalStateException("Pot is currently in use");
        }
    }

    public String plant(GreenhousePotRequest request) {
        int[] coords = parseCoordinates(request);
        int x = coords[0];
        int y = coords[1];

        validatePotUnlocked(x, y);
        validatePotNotOccupied(x, y);

        App.getAccount().getGreenhouse().plant(x, y, App.getAccount().getCollection());
        return "Plant planted in " + x + " " + y + "successfully";
    }

    public String collect(GreenhousePotRequest request) {
        int[] coords = parseCoordinates(request);
        int x = coords[0];
        int y = coords[1];

        validatePotUnlocked(x, y);
        validatePotOccupied(x, y);

        SproutPlant result = null;
        try {
            result = App.getAccount().getGreenhouse().collect(x, y);
        } catch (Exception e) {
            return e.getMessage();
        }

        if (result.isReady()) {
            App.getAccount().getAdventureProgress().addCoin(500);

            return "The plant collected successfully and 500 coin added to your wallet";
        } else {
            //todo implement the a reserved boost for that plant and change the return message
            return "The plant collected successfully and a boost is there for you";
        }
    }

    public String grow(GreenhousePotRequest request) {
        int[] coords = parseCoordinates(request);
        int x = coords[0];
        int y = coords[1];

        validatePotUnlocked(x, y);
        validatePotOccupied(x, y);

        try {
            int cost = App.getAccount().getGreenhouse().speedUp(x, y);
            int currentGem = App.getAccount().getAdventureProgress().getGem();
            if (cost > currentGem ) {
                throw new IllegalStateException("You don't have enough money");
            }else{
                App.getAccount().getGreenhouse().grow(x, y);
                App.getAccount().getAdventureProgress().addGem(-cost);
                return "The plant grew successfully and is ready to collect";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}