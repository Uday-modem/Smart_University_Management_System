package com.rfid.tracker.dto;

public class RFIDScanRequest {
    private String cardUid;
    private String deviceId;
    private String scanTime;
    private String scanDate;
    private String roomNumber;

    public RFIDScanRequest() {}

    public RFIDScanRequest(String cardUid, String deviceId, String scanTime, String scanDate) {
        this.cardUid = cardUid;
        this.deviceId = deviceId;
        this.scanTime = scanTime;
        this.scanDate = scanDate;
    }

    public String getCardUid() { return cardUid; }
    public void setCardUid(String cardUid) { this.cardUid = cardUid; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getScanTime() { return scanTime; }
    public void setScanTime(String scanTime) { this.scanTime = scanTime; }

    public String getScanDate() { return scanDate; }
    public void setScanDate(String scanDate) { this.scanDate = scanDate; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}
