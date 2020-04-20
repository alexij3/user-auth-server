package com.buzilov.crypto.userauth;

import com.buzilov.crypto.userauth.aes.EncryptionDecryptionManager;
import com.buzilov.crypto.userauth.audit.service.AuditLogService;
import com.buzilov.crypto.userauth.dto.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class UserAuthManager {

    private final AuditLogService auditLogService = new AuditLogService();
    private final static Logger LOGGER = LoggerFactory.getLogger(UserAuthManager.class);

    public Optional<UserInfo> authenticate(String login, String password) throws Exception {

        final EncryptionDecryptionManager manager = new EncryptionDecryptionManager();

        String encryptedLogin = manager.encrypt(login);
        String encryptedPassword = manager.encrypt(password);

        final UserInfo userInfo = new UserInfo(encryptedLogin, encryptedPassword);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Path file = Paths.get("users.json");

            List<UserInfo> users = objectMapper.readValue(file.toFile(), new TypeReference<List<UserInfo>>() {});

            Optional<UserInfo> authenticatedUser = users.stream()
                    .filter(user -> user.equals(userInfo))
                    .peek(user -> user.setLogin(manager.decrypt(user.getLogin())))
                    .findFirst();

            if (authenticatedUser.isPresent()) {
                LOGGER.info("User {} has logged in successfully.", authenticatedUser.get().getLogin());
                auditLogService.writeLog(authenticatedUser.get().getLogin(), "User has successfully logged in.");
            } else {
                LOGGER.info("Bad credentials for user with login {}", login);
                auditLogService.writeLog("", String.format("Unsuccessful attempt to login as %s", login));
            }
            return authenticatedUser;
        } catch (Exception e) {
            throw new Exception("Something went wrong during authentication", e);
        }


    }

}
