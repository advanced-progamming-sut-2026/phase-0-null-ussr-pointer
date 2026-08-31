package com.ussr.pvz.controller.maincontroller;

import com.ussr.pvz.model.dto.LeaderBoardSortRequest;
import com.ussr.pvz.model.leaderboard.LeaderboardColumn;
import com.ussr.pvz.model.leaderboard.LeaderboardEntry;
import com.ussr.pvz.service.LeaderBoardService;

import java.util.List;
import java.util.regex.Matcher;

public class LeaderBoardController {
    private final LeaderBoardService leaderBoardService;

    public LeaderBoardController() {
        this.leaderBoardService = new LeaderBoardService();
    }


    public List<LeaderboardEntry> getEntries(
            LeaderboardColumn column,
            boolean ascending
    ) {
        return leaderBoardService.getEntries(
                column,
                ascending
        );
    }
}