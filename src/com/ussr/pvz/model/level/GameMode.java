package com.ussr.pvz.model.level;

// TODO(score-game-mode): BOUNCE is defined but nothing implements it yet. Needed before it's usable:
//   - 5 MooPoint-earning patterns (spec suggests: multi-kill-with-one-shot, fast-kill,
//     simultaneous-kill; pick 2 more of your own)
//   - deterministic daily zombie-spawn seed (same zombie generation for every player each day)
//   - score accumulation hooks wired into the kill/event pipeline (see GameEventBus / QuestEventTracker
//     for the pattern already used for quests)
//   - persistence into Account.ScoreRecord.highestScore, and LeaderBoardService reading it correctly
//   - a menu command to enter this mode (see GameService.menuEnterChapter for the adventure-mode pattern)
public enum GameMode {
    ADVENTURE,
    MINIGAME,
    BOUNCE
}
