package com.example.tripkey;

import java.util.ArrayList;

public class RecordItem {
    private String recordId;
    private String place;
    private String record;
    private ArrayList<String> photoUris;

    public RecordItem(String recordId, String place, String record, ArrayList<String> photoUris) {
        this.recordId = recordId;
        this.place = place;
        this.record = record;
        this.photoUris = photoUris;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getPlace() {
        return place;
    }

    public String getRecord() {
        return record;
    }

    public ArrayList<String> getPhotoUris() {
        return photoUris;
    }
}
