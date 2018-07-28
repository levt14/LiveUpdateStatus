package com.liveupdatestatus.model;

/**
 * Created by LEV on 28/07/2018.
 */

public class User {

    private String displayName;
    private String email;
    private String photoUrl;
    private String userId;

    public User() {
        this.displayName = "none";
        this.email = "none";
        this.photoUrl = "none";
        this.userId = "none";
    }

    public User(String displayName, String email, String photoUrl, String userId) {
        this.displayName = displayName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
