package com.buzilov.crypto.userauth;

import com.buzilov.crypto.userauth.aes.EncryptionDecryptionManager;
import com.buzilov.crypto.userauth.dto.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class UserAuthManager {

    public Optional<UserInfo> authenticate(String login, String password) throws Exception {

        final EncryptionDecryptionManager manager = new EncryptionDecryptionManager();

        String encryptedLogin = manager.encrypt(login);
        String encryptedPassword = manager.encrypt(password);

        final UserInfo userInfo = new UserInfo(encryptedLogin, encryptedPassword);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Path file = Paths.get("users.json");

            List<UserInfo> users = objectMapper.readValue(file.toFile(), new TypeReference<List<UserInfo>>() {});

            return users.stream()
                    .filter(user -> user.equals(userInfo))
                    .peek(user -> user.setLogin(manager.decrypt(user.getLogin())))
                    .findFirst();

        } catch (Exception e) {
            throw new Exception("Something went wrong during authentication", e);
        }


    }

}
