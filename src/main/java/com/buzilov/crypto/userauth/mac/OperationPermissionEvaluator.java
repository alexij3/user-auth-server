package com.buzilov.crypto.userauth.mac;

import com.buzilov.crypto.userauth.dto.Document;
import com.buzilov.crypto.userauth.dto.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class OperationPermissionEvaluator {

    public List<Operation> evaluateOperations(Document document, UserInfo userInfo) {
        List<Operation> evaluatedOperations = new ArrayList<>();

        if (userInfo.getConfidentialityLevel().getLevelValue() == document.getConfidentialityLevel().getLevelValue()) {
            evaluatedOperations.add(Operation.READ);
            evaluatedOperations.add(Operation.WRITE);
        } else if (userInfo.getConfidentialityLevel().getLevelValue() > document.getConfidentialityLevel().getLevelValue()) {
            evaluatedOperations.add(Operation.READ);
        } else {
            evaluatedOperations.add(Operation.WRITE);
        }

        return evaluatedOperations;
    }

    public enum Operation {
        READ, WRITE
    }

}
