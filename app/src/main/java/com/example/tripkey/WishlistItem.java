package com.example.tripkey;

public class WishlistItem {
    private String travelId;

    public WishlistItem() {
        // Firestore 역직렬화용 빈 생성자 필요
    }

    public WishlistItem(String travelId) {
        this.travelId = travelId;
    }

    public String getTravelId() {
        return travelId;
    }

    public void setTravelId(String travelId) {
        this.travelId = travelId;
    }
}
