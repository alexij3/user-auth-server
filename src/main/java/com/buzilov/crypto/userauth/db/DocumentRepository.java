package com.buzilov.crypto.userauth.db;

import com.buzilov.crypto.userauth.Authentication;
import com.buzilov.crypto.userauth.audit.service.AuditLogService;
import com.buzilov.crypto.userauth.dto.Document;
import com.buzilov.crypto.userauth.dto.UserInfo;
import com.buzilov.crypto.userauth.mac.OperationPermissionEvaluator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.bcel.internal.generic.ATHROW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DocumentRepository {

    private final AuditLogService auditLogService = new AuditLogService();
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentRepository.class);

    public List<Document> getAll() {
        auditLogService.writeLog(Authentication.getCurrentUserInfo().getLogin(), "Requested list of all documents.");

        Path filePath = Paths.get("documents.json");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(filePath.toFile(), new TypeReference<List<Document>>() {})
                    .stream()
                    .filter(document -> Authentication.getCurrentUserInfo().getConfidentialityLevel().getLevelValue() >= document.getConfidentialityLevel().getLevelValue())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Something went wrong during extraction of all documents.", e);
            throw new RuntimeException(e);
        }
    }

    public Document update(Document document) {
        auditLogService.writeLog(Authentication.getCurrentUserInfo().getLogin(), "Requested edit of document.");
        LOGGER.info("User {} is trying to edit document with id {}", Authentication.getCurrentUserInfo().getLogin(), document.getId());

        Path filePath = Paths.get("documents.json");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<Document> existingDocuments = objectMapper.readValue(filePath.toFile(), new TypeReference<List<Document>>() {});
            Optional<Document> documentToEdit = existingDocuments.stream()
                                                        .filter(doc -> doc.getId() == document.getId())
                                                        .findFirst();

            if (documentToEdit.isPresent()) {
                auditLogService.writeLog(Authentication.getCurrentUserInfo().getLogin(), String.format("Changing document's content with ID %s from %s to %s", documentToEdit.get().getId(), documentToEdit.get().getContent(),
                        document.getContent()));

                int documentToEditIndex = existingDocuments.indexOf(documentToEdit.get());
                existingDocuments.add(documentToEditIndex, document);
                existingDocuments.remove(documentToEditIndex + 1);

                objectMapper.writeValue(filePath.toFile(), existingDocuments);
                return document;
            } else {
                LOGGER.error("Document with ID {} could not be found.", document.getId());
                throw new RuntimeException(String.format("Could not find document with ID %d in the repository!", document.getId()));
            }

        } catch (IOException e) {
            LOGGER.error("Something went wrong during getting document by id {}", document.getId(), e);
            throw new RuntimeException(e);
        }
    }


}
