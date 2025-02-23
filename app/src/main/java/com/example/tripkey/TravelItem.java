package com.example.tripkey;

public class TravelItem {
    private String travelName;
    private String location;
    private String startDate;
    private String endDate;

    public TravelItem(String travelName, String location, String startDate, String endDate) {
        this.travelName = travelName;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getTravelName() { return travelName; }
    public String getLocation() { return location; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
}

