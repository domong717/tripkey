package com.example.tripkey;

import java.util.List;

public class TripPost {
    private String title;
    private String date;
    private String location;
    private int peopleCount;
    private String costPerPerson;
    private List<Place> placeList;
    private String teamMBTI;

    public TripPost(String title, String date, String location, int peopleCount, String costPerPerson, List<Place> placeList, String teamMBTI) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.peopleCount = peopleCount;
        this.costPerPerson = costPerPerson;
        this.placeList = placeList;
        this.teamMBTI = teamMBTI;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public int getPeopleCount() { return peopleCount; }
    public String getCostPerPerson() { return costPerPerson; }
    public List<Place> getPlaceList() { return placeList; }
    public String getTeamMBTI() { return teamMBTI; }
}
