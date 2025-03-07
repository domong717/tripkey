package com.example.tripkey;

// hihihi
public class ChecklistItem {
    private String text;      // 항목 이름
    private boolean isChecked; // 체크 여부

    public ChecklistItem(String text, boolean isChecked) {
        this.text = text;
        this.isChecked = isChecked;
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