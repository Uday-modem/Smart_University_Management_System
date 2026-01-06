package com.rfid.tracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private boolean success;
    private String message;
    private Object user;
    private String token; // <-- FIX 1: Add the token field

    public LoginResponse() {}

    // FIX 2: Update the constructor to include the token
    public LoginResponse(boolean success, String message, Object user, String token) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    // FIX 3: Add a getter and setter for the new token field
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        // FIX 4: Update toString for better debugging (optional but good practice)
        return "LoginResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", user=" + user +
                ", token='" + (token != null ? "present" : "null") + '\'' + // Avoid logging the full token
                '}';
    }
}
