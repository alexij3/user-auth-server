package com.buzilov.crypto.userauth;

import com.buzilov.crypto.userauth.dto.UserInfo;

public class Authentication {

    private static UserInfo currentUserInfo;

    public static UserInfo getCurrentUserInfo() {
        return currentUserInfo;
    }

    public static void setUserInfo(UserInfo userInfo) {
        Authentication.currentUserInfo = userInfo;
    }

    public static boolean isUserAuthenticated() {
        return currentUserInfo != null;
    }

}
