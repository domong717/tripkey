package com.example.tripkey;

// hihihi
public class ChecklistItem {
    private String id;
    private String text;      // 항목 이름
    private boolean isChecked; // 체크 여부

    public ChecklistItem(String id, String text, boolean isChecked) {
        this.id = id;
        this.text = text;
        this.isChecked = isChecked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}