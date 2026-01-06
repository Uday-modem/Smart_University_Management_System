package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "morning_fingerprint_logs")
public class MorningFingerprintLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ CHANGED: Made nullable because ESP32 doesn't send this immediately
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = true) 
    private UserType userType;

    // ✅ CHANGED: Made nullable because ESP32 only sends ID
    @Column(name = "user_identifier", nullable = true, length = 50)
    private String userIdentifier;

    @Column(name = "fingerprint_id", nullable = false)
    private Integer fingerprintId;

    @Column(name = "scan_date", nullable = false)
    private LocalDate scanDate;

    @Column(name = "scan_time", nullable = false)
    private LocalTime scanTime;

    @Column(name = "scan_datetime")
    private LocalDateTime scanDateTime;

    // ✅ ADDED: Required by ESP32 payload
    @Column(name = "device_id", length = 50)
    private String deviceId;

    // ✅ ADDED: Required by ESP32 payload
    @Column(name = "confidence")
    private Integer confidence;

    // ✅ CHANGED: Renamed Enum and Type to match Controller (Status: PENDING/PROCESSED)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status; // Was AttendanceStatus

    @Column(name = "processed")
    private Boolean processed = false;

    // NEW: Logout fingerprint tracking
    @Column(name = "logout_fingerprint_id")
    private Integer logoutFingerprintId;

    @Column(name = "logout_scan_time")
    private LocalTime logoutScanTime;

    @Column(name = "logout_scan_datetime")
    private LocalDateTime logoutScanDateTime;

    @Column(name = "section_id", length = 20)
    private String sectionId;

    @Column(name = "branch")
    private String branch;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum UserType {
        STUDENT, STAFF
    }

    // ✅ RENAMED/UPDATED: This fixes the "cannot find symbol: variable Status" error
    public enum Status {
        PENDING,
        PROCESSED,
        FAILED,
        IGNORED,  // For duplicates
        ON_TIME,  // Kept for backward compatibility if needed
        LATE      // Kept for backward compatibility if needed
    }

    public MorningFingerprintLog() {
        this.scanDateTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.processed = false;
        this.status = Status.PENDING; // Default to PENDING
    }

    // Updated Constructor
    public MorningFingerprintLog(UserType userType, String userIdentifier, Integer fingerprintId,
                                 LocalDate scanDate, LocalTime scanTime, Status status) {
        this();
        this.userType = userType;
        this.userIdentifier = userIdentifier;
        this.fingerprintId = fingerprintId;
        this.scanDate = scanDate;
        this.scanTime = scanTime;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public String getUserIdentifier() { return userIdentifier; }
    public void setUserIdentifier(String userIdentifier) { this.userIdentifier = userIdentifier; }

    public Integer getFingerprintId() { return fingerprintId; }
    public void setFingerprintId(Integer fingerprintId) { this.fingerprintId = fingerprintId; }

    public LocalDate getScanDate() { return scanDate; }
    public void setScanDate(LocalDate scanDate) { this.scanDate = scanDate; }

    public LocalTime getScanTime() { return scanTime; }
    public void setScanTime(LocalTime scanTime) { this.scanTime = scanTime; }

    public LocalDateTime getScanDateTime() { return scanDateTime; }
    public void setScanDateTime(LocalDateTime scanDateTime) { this.scanDateTime = scanDateTime; }

    // ✅ UPDATED GETTER/SETTER for new Enum
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Boolean getProcessed() { return processed; }
    public void setProcessed(Boolean processed) { this.processed = processed; }

    // ✅ ADDED GETTERS/SETTERS FOR NEW FIELDS
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Integer getConfidence() { return confidence; }
    public void setConfidence(Integer confidence) { this.confidence = confidence; }

    // Logout getters and setters
    public Integer getLogoutFingerprintId() { return logoutFingerprintId; }
    public void setLogoutFingerprintId(Integer logoutFingerprintId) { this.logoutFingerprintId = logoutFingerprintId; }

    public LocalTime getLogoutScanTime() { return logoutScanTime; }
    public void setLogoutScanTime(LocalTime logoutScanTime) { this.logoutScanTime = logoutScanTime; }

    public LocalDateTime getLogoutScanDateTime() { return logoutScanDateTime; }
    public void setLogoutScanDateTime(LocalDateTime logoutScanDateTime) { this.logoutScanDateTime = logoutScanDateTime; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
