package com.example.tripkey;

public class FriendItem {
    private String name;
    private String id;
    //private int heartCount; // 좋아요 수
    private String profileImageUrl; // 프로필 이미지 URL

    public FriendItem(String name, String id, String profileImageUrl) {
        this.name = name;
        this.id = id;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
