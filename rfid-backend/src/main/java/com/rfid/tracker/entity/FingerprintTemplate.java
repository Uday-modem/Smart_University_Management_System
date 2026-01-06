package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fingerprint_templates")
public class FingerprintTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fingerprint_id", nullable = false, unique = true)
    private Integer fingerprintId;  // 1-999 (R307 sensor range)

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "user_identifier", nullable = false, length = 50)
    private String userIdentifier;  // registration_number or staff_id

    @Column(name = "template_data", columnDefinition = "TEXT")
    private String templateData;

    @Column(name = "enrolled_date")
    private LocalDateTime enrolledDate;

    @Column(name = "enrolled_by", length = 100)
    private String enrolledBy;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum UserType {
        STUDENT, STAFF
    }

    // Constructors
    public FingerprintTemplate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.enrolledDate = LocalDateTime.now();
        this.isActive = true;
    }

    public FingerprintTemplate(Integer fingerprintId, UserType userType, String userIdentifier) {
        this();
        this.fingerprintId = fingerprintId;
        this.userType = userType;
        this.userIdentifier = userIdentifier;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFingerprintId() {
        return fingerprintId;
    }

    public void setFingerprintId(Integer fingerprintId) {
        this.fingerprintId = fingerprintId;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public String getTemplateData() {
        return templateData;
    }

    public void setTemplateData(String templateData) {
        this.templateData = templateData;
    }

    public LocalDateTime getEnrolledDate() {
        return enrolledDate;
    }

    public void setEnrolledDate(LocalDateTime enrolledDate) {
        this.enrolledDate = enrolledDate;
    }

    public String getEnrolledBy() {
        return enrolledBy;
    }

    public void setEnrolledBy(String enrolledBy) {
        this.enrolledBy = enrolledBy;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
