package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_entry_logs")
public class StaffEntryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ NEW: Raw RFID Tag from ESP32
    @Column(name = "rfid_tag", length = 50)
    private String rfidTag;

    // ✅ CHANGED: Made nullable (we might not know the staff ID immediately upon scan)
    @Column(name = "staff_id_number", nullable = true)
    private String staffIdNumber;

    @Column(name = "staff_db_id")
    private Integer staffDbId;

    @Column(name = "timetable_id")
    private Long timetableId;

    // ✅ CHANGED: Made nullable (mapped from DeviceID during processing)
    @Column(name = "room_number", nullable = true)
    private String roomNumber;

    // ✅ NEW: Device ID from ESP32
    @Column(name = "device_id", length = 50)
    private String deviceId;

    @Column(name = "section_id")
    private String sectionId;

    @Column(name = "time_slot")
    private String timeSlot;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "entry_time", nullable = false)
    private LocalTime entryTime;

    @Column(name = "entry_datetime")
    private LocalDateTime entryDateTime;

    @Column(name = "expected_time")
    private LocalTime expectedTime;

    // ✅ CHANGED: Renamed Enum type to 'Status' and added PENDING/PROCESSED
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.PENDING;

    @Column(name = "notification_sent")
    private Boolean notificationSent = false;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ✅ UPDATED ENUM: Merged old statuses (ON_TIME) with new staging statuses (PENDING)
    public enum Status {
        PENDING,
        PROCESSED,
        FAILED,
        IGNORED, // For duplicates
        ON_TIME,
        LATE,
        ABSENT
    }

    public StaffEntryLog() {
        this.entryDateTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.notificationSent = false;
        this.status = Status.PENDING; // Default for new logs
    }

    public StaffEntryLog(String staffIdNumber, String roomNumber, LocalDate entryDate, LocalTime entryTime) {
        this();
        this.staffIdNumber = staffIdNumber;
        this.roomNumber = roomNumber;
        this.entryDate = entryDate;
        this.entryTime = entryTime;
        this.status = Status.ON_TIME;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRfidTag() { return rfidTag; }
    public void setRfidTag(String rfidTag) { this.rfidTag = rfidTag; }

    public String getStaffIdNumber() { return staffIdNumber; }
    public void setStaffIdNumber(String staffIdNumber) { this.staffIdNumber = staffIdNumber; }

    public Integer getStaffDbId() { return staffDbId; }
    public void setStaffDbId(Integer staffDbId) { this.staffDbId = staffDbId; }

    public Long getTimetableId() { return timetableId; }
    public void setTimetableId(Long timetableId) { this.timetableId = timetableId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public LocalTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getEntryDateTime() { return entryDateTime; }
    public void setEntryDateTime(LocalDateTime entryDateTime) { this.entryDateTime = entryDateTime; }

    public LocalTime getExpectedTime() { return expectedTime; }
    public void setExpectedTime(LocalTime expectedTime) { this.expectedTime = expectedTime; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Boolean getNotificationSent() { return notificationSent; }
    public void setNotificationSent(Boolean notificationSent) { this.notificationSent = notificationSent; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
