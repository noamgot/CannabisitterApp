package com.example.cannabisitterapp;

/**
 * Created by noamg on 10/09/2017.
 */

public class PlantTableItem {



    /**
     * Item Id
     */
    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    /**
     * Item plant ID
     */
    @com.google.gson.annotations.SerializedName("plantId")
    private int mPlantId;


    @com.google.gson.annotations.SerializedName("name")
    private String mName;



    /**
     * ToDoItem constructor
     */
    public PlantTableItem() {

    }

    public PlantTableItem(int plantId, String id, String name) {
        this.setPlantId(plantId);
        this.setName(name);
        this.setId(id);
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

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlantTableItem)) return false;

        PlantTableItem that = (PlantTableItem) o;

        return mId != null ? mId.equals(that.mId) : that.mId == null;

    }

    @Override
    public int hashCode() {
        return mId != null ? mId.hashCode() : 0;
    }
}
