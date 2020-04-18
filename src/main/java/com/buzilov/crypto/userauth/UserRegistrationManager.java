package com.buzilov.crypto.userauth;

import com.buzilov.crypto.userauth.aes.EncryptionDecryptionManager;
import com.buzilov.crypto.userauth.dto.UserInfo;

public class UserRegistrationManager {

    public void register(String login, String password) {
        final EncryptionDecryptionManager manager = new EncryptionDecryptionManager();

        String encryptedPassword = manager.encrypt(password);

        final UserInfo userInfo = new UserInfo(login, encryptedPassword);


    }

}
