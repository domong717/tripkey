package com.example.tripkey;

public class FriendItem {
    private String name;
    private String id;
    private int heartCount; // 좋아요 수
    private String profileImageUrl; // 프로필 이미지 URL

//    public FriendItem(String name, String id, int heartCount, String profileImageUrl) {
//        this.name = name;
//        this.id = id;
//        this.heartCount = heartCount;
//        this.profileImageUrl = profileImageUrl;
//    }

    public FriendItem(String name, String id){
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getHeartCount() {
        return heartCount;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
