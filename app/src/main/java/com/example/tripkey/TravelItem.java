package com.example.tripkey;

public class TravelItem {
    private String travelName;
    private String location;
    private String startDate;
    private String endDate;
    private String travelId;

    public TravelItem(String travelName, String location, String startDate, String endDate, String travelId) {
        this.travelName = travelName;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.travelId = travelId;
    }

    public String getTravelName() { return travelName; }
    public String getLocation() { return location; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getTravelId() { return travelId; }
}

