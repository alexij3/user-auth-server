package com.buzilov.crypto.userauth.audit.service;

import com.buzilov.crypto.userauth.audit.dto.AuditEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.time.LocalDateTime;

public class AuditLogService {

    private static final String AUDIT_LOG_FILE_PATH = "audit.log";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogService.class);
    private static int auditLogCounter = 0;

    public void writeLog(String username, String message) {

        try {
            Path file = Paths.get(AUDIT_LOG_FILE_PATH);

            if (!file.toFile().exists()) {
                Files.createFile(file);
            }

            AuditEntry entry = new AuditEntry(
                    ++auditLogCounter,
                    username.isEmpty() ? "unidentified user" : username,
                    LocalDateTime.now(),
                    message
            );

            Files.write(file, entry.toString().getBytes(), StandardOpenOption.APPEND);

            LOGGER.info("Created new audit log with id {}", auditLogCounter);

        } catch (Exception e) {
            LOGGER.error("Something went wrong during creation of record in audit log.", e);
        }

    }

}
