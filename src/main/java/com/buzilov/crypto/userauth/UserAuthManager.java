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

    public String authenticate(String login, String password) {

        final EncryptionDecryptionManager manager = new EncryptionDecryptionManager();

        String encryptedPassword = manager.encrypt(password);

        final UserInfo userInfo = new UserInfo(login, encryptedPassword);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Path file = Paths.get("users.json");

            List<UserInfo> users = objectMapper.readValue(file.toFile(), new TypeReference<List<UserInfo>>() {});

            Optional<UserInfo> registeredUser = users.stream()
                    .filter(user -> user.equals(userInfo))
                    .findFirst();

            if (registeredUser.isPresent()) {
                return String.format("Welcome, %s.", login);
            } else {
                return "Wrong credentials.";
            }

        } catch (Exception e) {
            return "Something went wrong during authentication";
        }


    }

}
