package com.example.cannabisitterapp;

/**
 * Created by noamg on 15/09/2017.
 */

public class PlantsPerUserItem  {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("PlantID")
    private int mPlantId;

    @com.google.gson.annotations.SerializedName("UserID")
    private int mUserId;

    @com.google.gson.annotations.SerializedName("UserPlantID")
    private int mUserPlantId;

    public PlantsPerUserItem(String id, int plantId, int userId, int userPlantId) {
        //super(id);
        mId = id;
        mPlantId = plantId;
        mUserId = userId;
        mUserPlantId = userPlantId;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public int getPlantId() {
        return mPlantId;
    }

    public void setPlantId(int plantId) {
        mPlantId = plantId;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        mUserId = userId;
    }

    public int getUserPlantId() {
        return mUserPlantId;
    }

    public void setUserPlantId(int userPlantId) {
        mUserPlantId = userPlantId;
    }
}
