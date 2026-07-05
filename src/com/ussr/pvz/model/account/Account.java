package com.ussr.pvz.model.account;

import com.ussr.pvz.model.greenhouse.Greenhouse;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private String name;
    private String nickname;
    private String password;
    private String email;
    private final Gender gender;
    private final SecurityQuestion securityQuestion;
    private final String securityAnswer;
    private final AdventureProgress adventureProgress;
    private final ScoreRecord scoreRecord;
    private final List<NewsItem> personalNews;
    private Collection collection;
    private Greenhouse greenhouse;
    private int difficultyLvl;
    private SavedBoosts savedBoosts;

    public Account(AccountState state, Collection collection) {
        this.name = state.username();
        this.nickname = state.nickname();
        this.password = state.password();
        this.email = state.email();
        this.gender = state.gender();
        this.securityQuestion = state.securityQuestion();
        this.securityAnswer = state.securityAnswer();
        this.difficultyLvl = state.difficultyLvl();

        this.adventureProgress = new AdventureProgress(
                state.currentChapter(),
                state.currentLvl(),
                state.minigamesWon(),
                state.questsCompleted(),
                state.coin(),
                state.gem(),
                state.plantLvl()
        );

        this.scoreRecord = new ScoreRecord(state.score());
        this.personalNews = new ArrayList<>(state.personalNews());
        this.collection = collection;
        this.greenhouse = state.greenhouse() != null ? Greenhouse.fromMap(state.greenhouse()) : new Greenhouse();

        this.savedBoosts = new SavedBoosts();
        if (state.savedBoosts() != null) {
            for (String boost : state.savedBoosts()) {
                this.savedBoosts.addBoost(boost);
            }
        }
    }

    public AccountState toState() {
        return new AccountState(
                this.name,
                this.nickname,
                this.password,
                this.email,
                this.gender,
                this.difficultyLvl,
                this.securityQuestion,
                this.securityAnswer,
                adventureProgress.getCurrentChapter(),
                adventureProgress.getCurrentLvl(),
                adventureProgress.getMinigamesWon(),
                adventureProgress.getQuestsCompleted(),
                adventureProgress.getCoin(),
                adventureProgress.getGem(),
                scoreRecord.getScore(),
                adventureProgress.getPlantLvls(),
                adventureProgress.getSeenZombies(),
                personalNews,
                this.greenhouse != null ? this.greenhouse.toMap() : null,
                this.savedBoosts.getBoosts(),
                adventureProgress.getPlantFoodCount(),
                adventureProgress.getSeedPackets()
        );
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public Gender getGender() {
        return gender;
    }

    public int getDifficultyLvl() {
        return difficultyLvl;
    }

    public SecurityQuestion getSecurityQuestion() {
        return securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public List<NewsItem> getPersonalNews() {
        return personalNews;
    }

    public Collection getCollection() {
        return collection;
    }

    public Greenhouse getGreenhouse() {
        return greenhouse;
    }

    public AdventureProgress getAdventureProgress() {
        return adventureProgress;
    }

    public ScoreRecord getScoreRecord() {
        return scoreRecord;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDifficultyLvl(int difficultyLvl) {
        this.difficultyLvl = difficultyLvl;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public void setGreenhouse(Greenhouse greenhouse) {
        this.greenhouse = greenhouse;
    }

    public SavedBoosts getSavedBoosts() {
        return savedBoosts;
    }

    public void setSavedBoosts(SavedBoosts savedBoosts) {
        this.savedBoosts = savedBoosts;
    }
}