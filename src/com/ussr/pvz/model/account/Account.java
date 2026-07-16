package com.ussr.pvz.model.account;

import com.ussr.pvz.model.greenhouse.Greenhouse;
import com.ussr.pvz.model.quest.ConfigurableQuest;
import com.ussr.pvz.model.quest.QuestManager;

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
    private QuestManager questManager;
    private long lastLoginTime;


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

        this.questManager = new QuestManager();
        try {
            this.questManager.loadFromJson();
            this.questManager.restoreState(state.completedQuests(), state.activeQuestProgress());
        } catch (Exception e) {
            System.err.println("Failed to load quests: " + e.getMessage());
        }

        this.lastLoginTime = (state.lastLoginTime() > 0) ? state.lastLoginTime() : System.currentTimeMillis();
        checkAndResetDailyQuests();
    }

    public AccountState toState() {
        List<String> completedIds = this.questManager.getCompleted().stream()
                .map(ConfigurableQuest::getId)
                .toList();
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
                adventureProgress.getSeedPackets(),
                completedIds,
                this.questManager.exportProgressMap(),
                this.lastLoginTime
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

    public QuestManager getQuestManager() {
        return questManager;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void updateLoginTime() {
        this.lastLoginTime = System.currentTimeMillis();
        checkAndResetDailyQuests();
    }

    private void checkAndResetDailyQuests() {
        long currentTime = System.currentTimeMillis();
        long timeDiffMillis = currentTime - lastLoginTime;
        long timeDiffHours = timeDiffMillis / (1000 * 60 * 60);

        // Reset daily quests and shop offers if more than 24 hours have passed
        if (timeDiffHours >= 24) {
            questManager.resetDaily();
            resetDailyShopOffers();
            this.lastLoginTime = currentTime;
        }
    }

    private void resetDailyShopOffers() {
        com.ussr.pvz.model.shop.ShopManager shopManager = com.ussr.pvz.model.App.getShopManager();
        if (shopManager != null) {
            // Reinitialize the shop to reset daily offers
            com.ussr.pvz.model.App.initShop();
        }
    }
}