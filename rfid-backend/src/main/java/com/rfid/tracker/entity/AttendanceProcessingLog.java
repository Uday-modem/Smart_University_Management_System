package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_processing_logs")
public class AttendanceProcessingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "user_identifier", nullable = false, length = 50)
    private String userIdentifier;

    @Column(name = "process_date", nullable = false)
    private LocalDate processDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "morning_fingerprint_status", nullable = false)
    private MorningFingerprintStatus morningFingerprintStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "logout_fingerprint_status")
    private LogoutFingerprintStatus logoutFingerprintStatus = LogoutFingerprintStatus.MISSING;

    @Column(name = "total_periods_scanned", nullable = false)
    private Integer totalPeriodsScanned;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_status", nullable = false)
    private FinalStatus finalStatus;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "auto_processed")
    private Boolean autoProcessed = true;

    @Column(name = "processing_notes", columnDefinition = "TEXT")
    private String processingNotes;

    public enum UserType {
        STUDENT, STAFF
    }

    public enum MorningFingerprintStatus {
        ON_TIME, LATE, MISSING
    }

    public enum LogoutFingerprintStatus {
        COMPLETED, MISSING
    }

    public enum FinalStatus {
        PRESENT, ABSENT, HALF_DAY
    }

    public AttendanceProcessingLog() {
        this.processedAt = LocalDateTime.now();
        this.autoProcessed = true;
        this.logoutFingerprintStatus = LogoutFingerprintStatus.MISSING;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public String getUserIdentifier() { return userIdentifier; }
    public void setUserIdentifier(String userIdentifier) { this.userIdentifier = userIdentifier; }

    public LocalDate getProcessDate() { return processDate; }
    public void setProcessDate(LocalDate processDate) { this.processDate = processDate; }

    public MorningFingerprintStatus getMorningFingerprintStatus() { return morningFingerprintStatus; }
    public void setMorningFingerprintStatus(MorningFingerprintStatus status) { this.morningFingerprintStatus = status; }

    public LogoutFingerprintStatus getLogoutFingerprintStatus() { return logoutFingerprintStatus; }
    public void setLogoutFingerprintStatus(LogoutFingerprintStatus status) { this.logoutFingerprintStatus = status; }

    public Integer getTotalPeriodsScanned() { return totalPeriodsScanned; }
    public void setTotalPeriodsScanned(Integer total) { this.totalPeriodsScanned = total; }

    public FinalStatus getFinalStatus() { return finalStatus; }
    public void setFinalStatus(FinalStatus status) { this.finalStatus = status; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime time) { this.processedAt = time; }

    public Boolean getAutoProcessed() { return autoProcessed; }
    public void setAutoProcessed(Boolean auto) { this.autoProcessed = auto; }

    public String getProcessingNotes() { return processingNotes; }
    public void setProcessingNotes(String notes) { this.processingNotes = notes; }
}
