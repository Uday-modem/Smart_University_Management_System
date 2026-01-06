package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hardware_devices")
public class HardwareDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId;

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "building")
    private String building;

    @Column(name = "floor")
    private String floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    private DeviceType deviceType = DeviceType.ATTENDANCE;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_ping")
    private LocalDateTime lastPing;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DeviceType {
        ATTENDANCE, ENTRY_EXIT, BOTH
    }

    public HardwareDevice() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.deviceType = DeviceType.ATTENDANCE;
    }

    public HardwareDevice(String deviceId, String deviceName, String roomNumber) {
        this();
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.roomNumber = roomNumber;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public DeviceType getDeviceType() { return deviceType; }
    public void setDeviceType(DeviceType deviceType) { this.deviceType = deviceType; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getLastPing() { return lastPing; }
    public void setLastPing(LocalDateTime lastPing) { this.lastPing = lastPing; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }

    public LocalDate getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDate installationDate) { this.installationDate = installationDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
