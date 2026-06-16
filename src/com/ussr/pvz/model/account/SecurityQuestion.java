package com.ussr.pvz.model.account;

public enum SecurityQuestion {
    Q1("What is the name of your favorite TA?"),
    Q2("What is your favorite food?"),
    Q3("What city were you born in?");

    private final String text;

    SecurityQuestion(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}