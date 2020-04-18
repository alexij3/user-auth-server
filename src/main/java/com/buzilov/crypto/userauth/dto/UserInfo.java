package com.buzilov.crypto.userauth.dto;

public class UserInfo {

    private String login;
    private String password;

    public UserInfo(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
