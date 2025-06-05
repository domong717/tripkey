package com.example.tripkey;

import android.util.Log;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;

public class GptPlan {
    private String date; // 전체 여행 일정의 시작 날짜
    private List<Place> places;

    public String getDate() {
        return date;
    }

    public List<Place> getPlaces() {
        return places;
    }
    public void setDateFromStartDate(String startDate, int dayIndex) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(startDate)); // 시작 날짜 설정
            calendar.add(Calendar.DATE, dayIndex);  // DAY(i+1)에 맞게 날짜 계산
            this.date = sdf.format(calendar.getTime());

            // 각 장소에도 같은 날짜 설정
            if (places != null) {
                for (Place place : places) {
                    place.setDate(this.date);  // 각 장소에도 날짜 설정
                }
            }
            Log.d("GptPlan", "Date for Day " + (dayIndex + 1) + ": " + this.date);  // 로그로 확인
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class Place {
        private String placeId;
        private String date;
        private String place;
        private String coord;
        private double latitude;
        private double longitude;
        private String category;
        private String transport;
        private String time;
        private String supply;

        public Place() {}

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getPlace() {
            return place;
        }

        public String getCoord() {
            return coord;
        }
        public void setCoord(String coord) { this.coord = coord; }


        public String getCategory() { return category; }

        public String getTransport() {
            return transport;
        }

        public String getTime() {
            return time;
        }

        public String getSupply() { return supply; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public String getPlaceId() { return placeId; }
        public void setPlaceId(String placeId) { this.placeId = placeId; }

    }
}
