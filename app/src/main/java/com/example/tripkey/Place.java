package com.example.tripkey;

public class Place {
    private String name;
    private String comment;
    private int photoResId;   // 기존 필드
    private String photoUrl;  // 새로 추가한 필드

    // 생성자 - 사진 URL용
    public Place(String name, String comment, String photoUrl) {
        this.name = name;
        this.comment = comment;
        this.photoUrl = photoUrl;
        this.photoResId = 0;  // 기본값으로 0 설정
    }

    // 기존 생성자 유지 (필요하면)
    public Place(String name, String comment, int photoResId) {
        this.name = name;
        this.comment = comment;
        this.photoResId = photoResId;
        this.photoUrl = null;
    }

    public String getName() { return name; }
    public String getComment() { return comment; }
    public int getPhotoResId() { return photoResId; }
    public String getPhotoUrl() { return photoUrl; }  // 새 getter
}
