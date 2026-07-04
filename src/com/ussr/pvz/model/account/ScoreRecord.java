package com.ussr.pvz.model.account;

public class ScoreRecord {
    private int score;

    public ScoreRecord(int score) {
        this.score = score;
    }

    //getter
    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
