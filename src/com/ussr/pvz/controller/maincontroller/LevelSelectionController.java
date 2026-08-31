package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.service.LevelSelectionService;


public class LevelSelectionController {
    private final LevelSelectionService service = new LevelSelectionService();

    public String selectLevel(String levelId) {
        return service.selectLevel(levelId);
    }

}
