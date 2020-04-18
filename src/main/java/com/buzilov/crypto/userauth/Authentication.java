package com.buzilov.crypto.userauth;

import com.buzilov.crypto.userauth.dto.UserInfo;

public class Authentication {

    public static UserInfo currentUserInfo;

    public static UserInfo getCurrentUserInfo() {
        return currentUserInfo;
    }

    public static boolean isUserAuthenticated() {
        return currentUserInfo != null;
    }

}
