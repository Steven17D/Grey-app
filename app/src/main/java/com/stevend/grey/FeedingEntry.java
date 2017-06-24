package com.stevend.grey;

/**
 * Created by Steven on 6/23/2017.
 */

public class FeedingEntry {
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

    public class Feeder {
        String name;
        String email;
        String photo;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public Feeder(String name, String email, String photo) {
            this.name = name;
            this.email = email;
            this.photo = photo;
        }
    }
}
