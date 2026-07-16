package com.ussr.pvz.model.engine;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.NewsItem;
import com.ussr.pvz.model.entities.plants.Plant;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.level.Level;

import java.util.List;

public class NewsObserver {
    public static void triggerNewPlant(List<String> plants) {
        int currentTimestamp = (int) (System.currentTimeMillis() / 1000);
        plants.forEach(plant -> {
            NewsItem newsItem = new NewsItem("New plant unlocked",
                    plant+" unlocked for you",currentTimestamp);
            App.getAccount().getPersonalNews().add(newsItem);
        });
    }

    public static void triggerNewZombie(Zombie zombie) {
        int currentTimestamp = (int) (System.currentTimeMillis() / 1000);
        NewsItem newsItem = new NewsItem("New zombie unlocked",
                zombie.getAlias()+" unlocked for you",currentTimestamp);
        App.getAccount().getPersonalNews().add(newsItem);
    }

    public static void triggerNewLevel(Level level) {
        int currentTimestamp = (int) (System.currentTimeMillis() / 1000);
        NewsItem newsItem = new NewsItem("New Level unlocked",
                level.getId()+" unlocked for you",currentTimestamp);
        App.getAccount().getPersonalNews().add(newsItem);
    }
    //todo use this somewhere and check the above usages and fix them if needed
    public static void triggerNewMiniGame(Level level) {
        int currentTimestamp = (int) (System.currentTimeMillis() / 1000);
        NewsItem newsItem = new NewsItem("New minigame unlocked",
                level.getId()+" unlocked for you",currentTimestamp);
        App.getAccount().getPersonalNews().add(newsItem);
    }
}
