package com.rfid.tracker.dto;

public class HardwareResponseDTO {
    private String status;
    private String message;
    private Object data;
    private Long timestamp;

    public HardwareResponseDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public HardwareResponseDTO(String status, String message) {
        this();
        this.status = status;
        this.message = message;
    }

    public HardwareResponseDTO(String status, String message, Object data) {
        this();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
