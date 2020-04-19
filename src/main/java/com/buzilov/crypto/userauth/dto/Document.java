package com.buzilov.crypto.userauth.dto;

import com.buzilov.crypto.userauth.mac.ConfidentialityLevel;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return id == document.id &&
                Objects.equals(name, document.name) &&
                Objects.equals(content, document.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, content);
    }
}
