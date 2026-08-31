package com.ussr.pvz.controller.maincontroller.gamecontroller;

import com.ussr.pvz.model.dto.MenuEnterChapterRequest;
import com.ussr.pvz.service.game.GameService;

public class GameController {
    private final GameService gameService = new GameService();

    public GameController() {
    }

    public String enterChapter(String chapterId) {
        return gameService.menuEnterChapter(
                new MenuEnterChapterRequest(chapterId)
        );
    }
}