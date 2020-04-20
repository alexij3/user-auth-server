package com.buzilov.crypto.userauth.audit.dto;

import java.time.LocalDateTime;

public class AuditEntry {

    private int id;
    private String username;
    private LocalDateTime dateTime;
    private String message;

    public AuditEntry(int id, String username, LocalDateTime dateTime, String message) {
        this.id = id;
        this.username = username;
        this.dateTime = dateTime;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "[" + dateTime + "]:\t" + id + "\t" + username + "\t" + message + "\n";
    }
}
