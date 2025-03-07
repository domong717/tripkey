package com.example.tripkey.ui.trip;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class TripViewModel extends androidx.lifecycle.ViewModel {

    private final MutableLiveData<String> travelName;
    private final MutableLiveData<String> location;
    private final MutableLiveData<String> startDate;
    private final MutableLiveData<String> endDate;
    private final MutableLiveData<String> travelStyle;

    public TripViewModel() {
        travelName = new MutableLiveData<>();
        location = new MutableLiveData<>();
        startDate = new MutableLiveData<>();
        endDate = new MutableLiveData<>();
        travelStyle = new MutableLiveData<>();
    }

    public void setTravelName(String name) {
        travelName.setValue(name);
    }

    public LiveData<String> getTravelName() {
        return travelName;
    }

    public void setLocation(String loc) {
        location.setValue(loc);
    }

    public LiveData<String> getLocation() {
        return location;
    }

    public void setStartDate(String date) {
        startDate.setValue(date);
    }

    public LiveData<String> getStartDate() {
        return startDate;
    }

    public void setEndDate(String date) {
        endDate.setValue(date);
    }

    public LiveData<String> getEndDate() {
        return endDate;
    }

    public void setTravelStyle(String travelStyle) {
        endDate.setValue(travelStyle);
    }

    public LiveData<String> getTravelStyle() {
        return travelStyle;
    }


}