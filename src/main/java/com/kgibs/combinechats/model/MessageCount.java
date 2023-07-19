package com.kgibs.combinechats.model;

public class MessageCount {
    private int day;
    private int count;

    public MessageCount(int day, int count) {
        this.day = day;
        this.count = count;
    }


    public int getDay() {
        return day;
    }

    public int getCount() {
        return count;
    }
}
