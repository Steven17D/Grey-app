package com.stevend.grey;

/**
 * Created by Steven on 6/23/2017.
 */

public class FeedingEntry implements Cloneable{
    private Feeder feeder;
    private long time;
    private int amount;

    public FeedingEntry(Feeder feeder, long time, int amount) {
        this.feeder = feeder;
        this.time = time;
        this.amount = amount;
    }

    public FeedingEntry(){}

    public Feeder getFeeder() {
        return feeder;
    }

    public void setFeeder(Feeder feeder) {
        this.feeder = feeder;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

