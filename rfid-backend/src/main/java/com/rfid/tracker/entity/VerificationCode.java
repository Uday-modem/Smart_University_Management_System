package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_codes")
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_id", nullable = false)
    private String staffId;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "section_id", nullable = false)
    private String sectionId;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "generated_time", nullable = false)
    private LocalDateTime generatedTime;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "is_used", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isUsed = false;

    @Column(name = "code_entered_count", columnDefinition = "INT DEFAULT 0")
    private Integer codeEnteredCount = 0;

    @Column(name = "timetable_id")
    private Long timetableId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ========== CONSTRUCTORS ==========

    public VerificationCode() {
        this.createdAt = LocalDateTime.now();
    }

    public VerificationCode(String staffId, String className, String sectionId, String code, LocalDateTime validUntil, Long timetableId) {
        this.staffId = staffId;
        this.className = className;
        this.sectionId = sectionId;
        this.code = code;
        this.generatedTime = LocalDateTime.now();
        this.validUntil = validUntil;
        this.isUsed = false;
        this.codeEnteredCount = 0;
        this.timetableId = timetableId;
        this.createdAt = LocalDateTime.now();
    }

    // ========== GETTERS & SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getGeneratedTime() {
        return generatedTime;
    }

    public void setGeneratedTime(LocalDateTime generatedTime) {
        this.generatedTime = generatedTime;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public Integer getCodeEnteredCount() {
        return codeEnteredCount;
    }

    public void setCodeEnteredCount(Integer codeEnteredCount) {
        this.codeEnteredCount = codeEnteredCount;
    }

    public Long getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(Long timetableId) {
        this.timetableId = timetableId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ========== UTILITY METHODS ==========

    public boolean isValid() {
        return LocalDateTime.now().isBefore(this.validUntil);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.validUntil);
    }

    @Override
    public String toString() {
        return "VerificationCode{" +
                "id=" + id +
                ", staffId='" + staffId + '\'' +
                ", className='" + className + '\'' +
                ", sectionId='" + sectionId + '\'' +
                ", code='" + code + '\'' +
                ", generatedTime=" + generatedTime +
                ", validUntil=" + validUntil +
                ", isUsed=" + isUsed +
                ", codeEnteredCount=" + codeEnteredCount +
                '}';
    }
}
