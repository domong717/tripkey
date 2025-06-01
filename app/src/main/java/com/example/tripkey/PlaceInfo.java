package com.example.tripkey;

import java.io.Serializable;

public class PlaceInfo implements Serializable {
    private String name;
    private double latitude;
    private double longitude;
    private String category;

    public PlaceInfo(String name, double latitude, double longitude, String category) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCategory() {
        return category;
    }
    @Override
    public String toString() {
        return "PlaceInfo{" +
                "name='" + name + '\'' +
                ", lat=" + latitude +
                ", lng=" + longitude +
                ", category='" + category + '\'' +
                '}';
    }

}
