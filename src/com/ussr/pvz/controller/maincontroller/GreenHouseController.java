package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.model.dto.GreenhousePotRequest;
import com.ussr.pvz.service.GreenHouseService;

public class GreenHouseController {

    private final GreenHouseService greenHouseService;

    public GreenHouseController() {
        this.greenHouseService = new GreenHouseService();
    }

    public String handleUnlock(int x, int y) {
        try {
            GreenhousePotRequest request = new GreenhousePotRequest(String.valueOf(x), String.valueOf(y));
            return greenHouseService.unlock(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String handlePlant(int x, int y) {
        try {
            GreenhousePotRequest request = new GreenhousePotRequest(String.valueOf(x), String.valueOf(y));
            return greenHouseService.plant(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String handleCollect(int x, int y) {
        try {
            GreenhousePotRequest request = new GreenhousePotRequest(String.valueOf(x), String.valueOf(y));
            return greenHouseService.collect(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String handleGrow(int x, int y) {
        try {
            GreenhousePotRequest request = new GreenhousePotRequest(String.valueOf(x), String.valueOf(y));
            return greenHouseService.grow(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String handleShowGreenHouse() {
        return greenHouseService.showGreenHouse();

    }
}