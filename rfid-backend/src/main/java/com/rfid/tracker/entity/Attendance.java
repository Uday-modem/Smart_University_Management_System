package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ FIXED: Added missing field causing SQL Error
    @Column(name = "user_identifier", nullable = false)
    private String userIdentifier; // Stores Student Reg No or Staff ID

    // ✅ NEW: To distinguish between STUDENT and STAFF records
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "student_id", columnDefinition = "BIGINT")
    private Long studentId; // Can be null if it's a staff member

    @Column(name = "staff_id")
    private String staffId; // Can be null if it's a student

    @Column(name = "section_id")
    private String sectionId;

    @Column(name = "branch")
    private String branch;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @Column(name = "mark_time")
    private LocalTime markTime;

    @Column(name = "remarks")
    private String remarks;

    // Admin override fields
    @Column(name = "override_by")
    private String overrideBy;

    @Column(name = "override_reason", columnDefinition = "TEXT")
    private String overrideReason;

    @Column(name = "override_datetime")
    private LocalDateTime overrideDatetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "original_status")
    private AttendanceStatus originalStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Attendance() {
        this.createdAt = LocalDateTime.now();
        this.markTime = LocalTime.now(); // Default mark time
    }

    public enum UserType {
        STUDENT, STAFF
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserIdentifier() { return userIdentifier; }
    public void setUserIdentifier(String userIdentifier) { this.userIdentifier = userIdentifier; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public LocalTime getMarkTime() { return markTime; }
    public void setMarkTime(LocalTime markTime) { this.markTime = markTime; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getOverrideBy() { return overrideBy; }
    public void setOverrideBy(String overrideBy) { this.overrideBy = overrideBy; }

    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }

    public LocalDateTime getOverrideDatetime() { return overrideDatetime; }
    public void setOverrideDatetime(LocalDateTime overrideDatetime) { this.overrideDatetime = overrideDatetime; }

    public AttendanceStatus getOriginalStatus() { return originalStatus; }
    public void setOriginalStatus(AttendanceStatus originalStatus) { this.originalStatus = originalStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
