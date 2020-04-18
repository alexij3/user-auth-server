package com.buzilov.crypto.userauth;

import com.buzilov.crypto.userauth.aes.EncryptionDecryptionManager;
import com.buzilov.crypto.userauth.dto.UserInfo;
import com.buzilov.crypto.userauth.exception.UserAlreadyRegisteredException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserRegistrationManager {

    public void register(String login, String password) throws UserAlreadyRegisteredException, IOException {
        final EncryptionDecryptionManager manager = new EncryptionDecryptionManager();

        String encryptedLogin = manager.encrypt(login);
        String encryptedPassword = manager.encrypt(password);

        final UserInfo userInfo = new UserInfo(encryptedLogin, encryptedPassword);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Path file;
            try {
                file = Files.createFile(Paths.get("users.json"));
                Files.write(file, "[]".getBytes());
            } catch (FileAlreadyExistsException e) {
                file = Paths.get("users.json");
            }

            List<UserInfo> users = objectMapper.readValue(file.toFile(), new TypeReference<List<UserInfo>>() {});

            if (users.contains(userInfo)) {
                throw new UserAlreadyRegisteredException("User is already registered!");
            } else {
                users.add(userInfo);
                objectMapper.writeValue(file.toFile(), users);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
