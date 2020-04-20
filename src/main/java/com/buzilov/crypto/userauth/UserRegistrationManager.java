package com.buzilov.crypto.userauth;

import com.buzilov.crypto.userauth.aes.EncryptionDecryptionManager;
import com.buzilov.crypto.userauth.audit.service.AuditLogService;
import com.buzilov.crypto.userauth.dto.UserInfo;
import com.buzilov.crypto.userauth.exception.UserAlreadyRegisteredException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class UserRegistrationManager {

    private final AuditLogService auditLogService = new AuditLogService();

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationManager.class);

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
                auditLogService.writeLog("", String.format("Unsuccessful attempt to register a user %s since one exists", login));
                LOGGER.error("User {} could not be registered because he already exists in the system.", login);
                throw new UserAlreadyRegisteredException("User is already registered!");
            } else {
                users.add(userInfo);
                objectMapper.writeValue(file.toFile(), users);
                LOGGER.info("Successfully registered the user {}", login);
                auditLogService.writeLog("", String.format("Registered the user with login %s", login));
            }

        } catch (IOException e) {
            LOGGER.error("Something went wrong during user registration", e);
        }

    }

}
