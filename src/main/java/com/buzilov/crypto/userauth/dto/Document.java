package com.buzilov.crypto.userauth.dto;

import com.buzilov.crypto.userauth.mac.ConfidentialityLevel;

public class Document {

    private int id;
    private String name;
    private String content;
    private ConfidentialityLevel confidentialityLevel;

    public Document() {
    }

    public Document(int id, String name, String content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }

    public Document(String name, String content, ConfidentialityLevel confidentialityLevel) {
        this.name = name;
        this.content = content;
        this.confidentialityLevel = confidentialityLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ConfidentialityLevel getConfidentialityLevel() {
        return confidentialityLevel;
    }

    public void setConfidentialityLevel(ConfidentialityLevel confidentialityLevel) {
        this.confidentialityLevel = confidentialityLevel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
