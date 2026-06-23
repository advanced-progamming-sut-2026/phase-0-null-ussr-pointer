package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.MenuState;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;

import java.util.List;

public class AccountService {

    public String logoutAccount() {
        if (App.getAccount() == null) {
            return "you are not logged in";
        }
        List<AccountState> updatedStates = App.getAccounts().stream()
                .map(Account::toState)
                .toList();
        SaveService.saveAccounts(updatedStates);

        App.login(null);
        App.setMenuState(MenuState.LOGIN);
        return "logged out successfully";
    }
}
