package com.example.cannabisitterapp;

import com.microsoft.windowsazure.mobileservices.table.DateTimeOffset;

import java.util.Date;

/**
 * Created by noamg on 17/09/2017.
 */

public class LogsPerUserItem {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("MessageID")
    private int messageId;

    @com.google.gson.annotations.SerializedName("ReportingDeviceID")
    private String reportingDeviceId;

    @com.google.gson.annotations.SerializedName("UserID")
    private int userId;

    @com.google.gson.annotations.SerializedName("PlantID")
    private int plantId;

    @com.google.gson.annotations.SerializedName("MessageDate")
    private Date messageDate;

    @com.google.gson.annotations.SerializedName("Humidity")
    private float humidity;

    @com.google.gson.annotations.SerializedName("Tempreture")
    private float temperature;

    @com.google.gson.annotations.SerializedName("didWater")
    private float didWater;

    LogsPerUserItem(){}

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getReportingDeviceId() {
        return reportingDeviceId;
    }

    public void setReportingDeviceId(String reportingDeviceId) {
        this.reportingDeviceId = reportingDeviceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPlantId() {
        return plantId;
    }

    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getDidWater() {
        return didWater;
    }

    public void setDidWater(float didWater) {
        this.didWater = didWater;
    }
}
