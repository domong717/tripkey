package com.example.tripkey;

import java.util.List;
public class GptPlan {
    private String date;
    private List<Place> places;

    public String getDate() {
        return date;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public static class Place {
        private String place;
        private String coord;
        private String category;
        private String transport;
        private String time;

        public String getPlace() {
            return place;
        }

        public String getCoord() {
            return coord;
        }

        public String getCategory() {
            return category;
        }

        public String getTransport() {
            return transport;
        }

        public String getTime() {
            return time;
        }
    }
}
