package com.liveupdatestatus.model;

/**
 * Created by LEV on 29/07/2018.
 */

public class Status {

    private String userId;
    private String userStatus;

    public Status() {
        this.userId = "none";
        this.userStatus = "none";
    }

    public Status(String userId, String userStatus) {
        this.userId = userId;
        this.userStatus = userStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}
