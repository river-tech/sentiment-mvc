package com.team.app.controller;

public class SentimentSummary {
    public final int positive;
    public final int negative;
    public final int neutral;

    public SentimentSummary(int positive, int negative, int neutral) {
        this.positive = positive;
        this.negative = negative;
        this.neutral = neutral;
    }

    public int getPositive() { return positive; }
    public int getNegative() { return negative; }
    public int getNeutral() { return neutral; }
}


