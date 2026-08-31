package com.ussr.pvz.controller;

import com.ussr.pvz.service.GlobalService;

public class GlobalController {

    private final GlobalService globalService;

    public GlobalController() {
        this.globalService = new GlobalService();
    }

    public void handMenuQuit() {
        globalService.handleQuit();
    }

    public String logout() {
        return globalService.handleLogout();
    }

    public String exitCurrentMenu() {
        return globalService.menuExit();
    }

}
