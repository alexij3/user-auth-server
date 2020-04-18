package com.buzilov.crypto.userauth.dto;

import java.util.Objects;

public class UserInfo {

    private String login;
    private String password;

    public UserInfo() {
    }

    public UserInfo(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(login, userInfo.login) &&
                Objects.equals(password, userInfo.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password);
    }
}
