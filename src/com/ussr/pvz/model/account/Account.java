package com.ussr.pvz.model.account;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private String name;
    private String password;
    private String email;
    private AdventureProgress adventureProgress;
    private ScoreRecord scoreRecord;
    private List<NewsItem> personalNews;
    private Collection collection;

    public Account(AccountState state , String password, Collection collection) {
        this.name = state.username();
        this.password = password;
        this.email = state.email();

        adventureProgress = new AdventureProgress(state.currentLvl() , state.coin() , state.gem() , state.plantLvl());
        scoreRecord = new ScoreRecord(state.score());

        personalNews = new ArrayList<>(state.personalNews());
        this.collection = collection;
    }

    public AccountState toState() {
        return new AccountState(this.name ,
                this.password ,
                this.email,
                adventureProgress.getCurrentLvl() ,
                adventureProgress.getCoin() ,
                adventureProgress.getGem() ,
                scoreRecord.getScore() ,
                adventureProgress.getPlantLvls() ,
                personalNews);
    }

    public List<NewsItem> getPersonalNews() {
        return personalNews;
    }
}

