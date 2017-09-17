package com.example.cannabisitterapp;

/**
 * Created by noamg on 17/09/2017.
 */

public class PlantItem {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("PlantId")
    private int mPlantId;

    @com.google.gson.annotations.SerializedName("PlantName")
    private String mPlantName;

    @com.google.gson.annotations.SerializedName("GroundHumidityThreshold")
    private float mGroundHumidityThreshold;

    @com.google.gson.annotations.SerializedName("TempretureThreshold")
    private float mTempretureThreshold;

    public PlantItem(String id, int plantId, String plantName, float groundHumidityThreshold, float tempretureThreshold) {
        mId = id;
        mPlantId = plantId;
        mPlantName = plantName;
        mGroundHumidityThreshold = groundHumidityThreshold;
        mTempretureThreshold = tempretureThreshold;
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

    public String getPlantName() {
        return mPlantName;
    }

    public void setPlantName(String plantName) {
        mPlantName = plantName;
    }

    public float getGroundHumidityThreshold() {
        return mGroundHumidityThreshold;
    }

    public void setGroundHumidityThreshold(float groundHumidityThreshold) {
        mGroundHumidityThreshold = groundHumidityThreshold;
    }

    public float getTempretureThreshold() {
        return mTempretureThreshold;
    }

    public void setTempretureThreshold(float tempretureThreshold) {
        mTempretureThreshold = tempretureThreshold;
    }
}
