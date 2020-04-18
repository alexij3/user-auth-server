package com.buzilov.crypto.userauth.db;

import com.buzilov.crypto.userauth.Authentication;
import com.buzilov.crypto.userauth.dto.Document;
import com.buzilov.crypto.userauth.dto.UserInfo;
import com.buzilov.crypto.userauth.mac.OperationPermissionEvaluator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentRepository {

    public List<Document> getAll() {
        Path filePath = Paths.get("documents.json");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(filePath.toFile(), new TypeReference<List<Document>>() {})
                    .stream()
                    .filter(document -> Authentication.getCurrentUserInfo().getConfidentialityLevel().getLevelValue() >= document.getConfidentialityLevel().getLevelValue())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
