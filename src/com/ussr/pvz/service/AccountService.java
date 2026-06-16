package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;

public class AccountService {

    public String logoutAccount() {
        if (App.getAccount() == null) {
            return "you are not logged in";
        }
        App.login(null);
        App.setMenuState(MenuState.LOGIN);
        return "logged out successfully";
    }
}
