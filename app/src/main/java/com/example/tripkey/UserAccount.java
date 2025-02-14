package com.example.tripkey;

public class UserAccount {
    private String userName;
    private String pwd;

    // Firestore를 위한 기본 생성자
    public UserAccount() {}

    // 매개변수 생성자
    public UserAccount(String userName, String pwd) {
        this.userName = userName;
        this.pwd = pwd;
    }

    // Getter와 Setter
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
