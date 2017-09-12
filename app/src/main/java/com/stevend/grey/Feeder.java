package com.stevend.grey;

import com.google.firebase.auth.FirebaseUser;

public class Feeder implements Cloneable{
    private String name;
    private String email;
    private String photoUrl;

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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Feeder() {}

    public Feeder(String name, String email, String photoUrl) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public Feeder(FirebaseUser firebaseUser) {
        this.name = firebaseUser.getDisplayName();
        this.email = firebaseUser.getEmail();
        this.photoUrl = String.valueOf(firebaseUser.getPhotoUrl());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
