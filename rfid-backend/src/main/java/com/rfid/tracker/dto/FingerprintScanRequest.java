package com.rfid.tracker.dto;

public class FingerprintScanRequest {
    private Integer fingerprintId;
    private String deviceId;
    private String scanTime;
    private String scanDate;
    private Long confidence;

    public FingerprintScanRequest() {}

    public FingerprintScanRequest(Integer fingerprintId, String deviceId, String scanTime, String scanDate) {
        this.fingerprintId = fingerprintId;
        this.deviceId = deviceId;
        this.scanTime = scanTime;
        this.scanDate = scanDate;
    }

    public Integer getFingerprintId() { return fingerprintId; }
    public void setFingerprintId(Integer fingerprintId) { this.fingerprintId = fingerprintId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getScanTime() { return scanTime; }
    public void setScanTime(String scanTime) { this.scanTime = scanTime; }

    public String getScanDate() { return scanDate; }
    public void setScanDate(String scanDate) { this.scanDate = scanDate; }

    public Long getConfidence() { return confidence; }
    public void setConfidence(Long confidence) { this.confidence = confidence; }
}
