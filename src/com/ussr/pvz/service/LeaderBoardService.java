package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import java.util.List;

public class LeaderBoardService {
    public LeaderBoardService() {
    }

    public String show() {
        List<Account> accounts = App.getAccounts();
        if(accounts.isEmpty()) {
            return "No accounts found";
        }

        accounts.getFirst().getAdventureProgress();
        //todo first handle chapters
        return "";
    }
}
