package com.example.cannabisitterapp;

/**
 * Created by noamg on 17/09/2017.
 */

public class UserItem {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("UserId")
    private int mUserId;

    @com.google.gson.annotations.SerializedName("User")
    private String mUser;

    @com.google.gson.annotations.SerializedName("Name")
    private String mName;

    @com.google.gson.annotations.SerializedName("Password")
    private String mPassword;

    public UserItem(){

    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        mUserId = userId;
    }

    public String getUser() {
        return mUser;
    }

    public void setUser(String user) {
        mUser = user;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }
}
