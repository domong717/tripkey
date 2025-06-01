package com.example.tripkey;

import java.util.List;

public class KakaoSearchResponse {
    public List<Document> documents;

    public static class Document {
        public String place_name;
        public String address_name;
        public String road_address_name;
        public String phone;
        public String x; // 경도 (longitude)
        public String y; // 위도 (latitude)
        public String place_url;
        public String category_name;
        public String category_group_code;
    }
}
