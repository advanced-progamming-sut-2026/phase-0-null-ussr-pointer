package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.dto.GreenhousePotRequest;

public class GreenHouseService {
    public String plant(GreenhousePotRequest request) {
        int x, y;
        try {
            x = Integer.parseInt(request.x());
            y = Integer.parseInt(request.y());
        } catch (NumberFormatException e) {
            return "number format is wrong";
        }

        if (!App.getAccount().getGreenhouse().isPotUnlocked(x, y)) {
            return "Pot is locked";
        }

        if (App.getAccount().getGreenhouse().isPotOccupied(x, y)) {
            return "Pot is currently in use";
        }

        App.getAccount().getGreenhouse().plant(x, y, App.getAccount().getCollection());
        return "Plant planted in " + x + " " + y + "successfully";
    }
}
