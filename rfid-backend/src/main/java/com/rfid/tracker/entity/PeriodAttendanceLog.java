package com.rfid.tracker.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "period_attendance_logs")
public class PeriodAttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_registration_number", nullable = false)
    private String studentRegistrationNumber;

    @Column(name = "section_id", nullable = false)
    private String sectionId;

    // FIXED: Integer → Long to match BIGINT database column
    @Column(name = "timetable_id")
    private Long timetableId; // ← CHANGED FROM Integer

    @Column(name = "time_slot", nullable = false)
    private String timeSlot;

    @Column(name = "scan_date", nullable = false)
    private LocalDate scanDate;

    @Column(name = "scan_time", nullable = false)
    private LocalTime scanTime;

    @Column(name = "scan_datetime")
    private LocalDateTime scanDateTime;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    // ✅ NEW: Verification code tracking
    @Column(name = "verification_code")
    private String verificationCode;

    // ✅ NEW: When code was verified
    @Column(name = "code_verification_timestamp")
    private LocalDateTime codeVerificationTimestamp;

    // ✅ NEW: How verification was done (RFID or CODE)
    @Column(name = "verified_via", length = 10)
    private String verifiedVia = "RFID"; // Default to RFID

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PeriodAttendanceLog() {
        this.scanDateTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public PeriodAttendanceLog(String studentRegistrationNumber, String sectionId,
                              LocalDate scanDate, LocalTime scanTime, String timeSlot) {
        this.studentRegistrationNumber = studentRegistrationNumber;
        this.sectionId = sectionId;
        this.scanDate = scanDate;
        this.scanTime = scanTime;
        this.timeSlot = timeSlot;
        this.scanDateTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.verifiedVia = "RFID";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentRegistrationNumber() { return studentRegistrationNumber; }
    public void setStudentRegistrationNumber(String studentRegistrationNumber) { this.studentRegistrationNumber = studentRegistrationNumber; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    // FIXED: Integer → Long
    public Long getTimetableId() { return timetableId; }
    public void setTimetableId(Long timetableId) { this.timetableId = timetableId; } // ← CHANGED FROM Integer

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public LocalDate getScanDate() { return scanDate; }
    public void setScanDate(LocalDate scanDate) { this.scanDate = scanDate; }

    public LocalTime getScanTime() { return scanTime; }
    public void setScanTime(LocalTime scanTime) { this.scanTime = scanTime; }

    public LocalDateTime getScanDateTime() { return scanDateTime; }
    public void setScanDateTime(LocalDateTime scanDateTime) { this.scanDateTime = scanDateTime; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    // ✅ NEW: Verification code getters/setters
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public LocalDateTime getCodeVerificationTimestamp() { return codeVerificationTimestamp; }
    public void setCodeVerificationTimestamp(LocalDateTime codeVerificationTimestamp) { this.codeVerificationTimestamp = codeVerificationTimestamp; }

    public String getVerifiedVia() { return verifiedVia; }
    public void setVerifiedVia(String verifiedVia) { this.verifiedVia = verifiedVia; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "PeriodAttendanceLog{" +
                "id=" + id +
                ", studentRegistrationNumber='" + studentRegistrationNumber + '\'' +
                ", sectionId='" + sectionId + '\'' +
                ", timeSlot='" + timeSlot + '\'' +
                ", scanDate=" + scanDate +
                ", scanTime=" + scanTime +
                ", verificationCode='" + verificationCode + '\'' +
                ", verifiedVia='" + verifiedVia + '\'' +
                '}';
    }
}