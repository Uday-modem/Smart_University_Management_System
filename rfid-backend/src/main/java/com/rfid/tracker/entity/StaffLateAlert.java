package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "staff_late_alerts")
public class StaffLateAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_id_number", nullable = false)
    private String staffIdNumber;

    @Column(name = "staff_name")
    private String staffName;

    @Column(name = "alert_date", nullable = false)
    private LocalDate alertDate;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    @Column(name = "actual_entry_time")
    private LocalTime actualEntryTime;

    @Column(name = "minutes_late")
    private int minutesLate;

    @Column(name = "time_slot")
    private String timeSlot;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    @Column(name = "entry_log_id")
    private Long entryLogId;

    @Column(name = "notification_sent_to_staff")
    private boolean notificationSentToStaff;

    @Column(name = "notification_sent_to_admin")
    private boolean notificationSentToAdmin;

    @Column(name = "admin_acknowledged")
    private boolean adminAcknowledged;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStaffIdNumber() { return staffIdNumber; }
    public void setStaffIdNumber(String staffIdNumber) { this.staffIdNumber = staffIdNumber; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public LocalDate getAlertDate() { return alertDate; }
    public void setAlertDate(LocalDate alertDate) { this.alertDate = alertDate; }
    public LocalTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public LocalTime getActualEntryTime() { return actualEntryTime; }
    public void setActualEntryTime(LocalTime actualEntryTime) { this.actualEntryTime = actualEntryTime; }
    public int getMinutesLate() { return minutesLate; }
    public void setMinutesLate(int minutesLate) { this.minutesLate = minutesLate; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Long getEntryLogId() { return entryLogId; }
    public void setEntryLogId(Long entryLogId) { this.entryLogId = entryLogId; }
    public boolean isNotificationSentToStaff() { return notificationSentToStaff; }
    public void setNotificationSentToStaff(boolean notificationSentToStaff) { this.notificationSentToStaff = notificationSentToStaff; }
    public boolean isNotificationSentToAdmin() { return notificationSentToAdmin; }
    public void setNotificationSentToAdmin(boolean notificationSentToAdmin) { this.notificationSentToAdmin = notificationSentToAdmin; }
    public boolean isAdminAcknowledged() { return adminAcknowledged; }
    public void setAdminAcknowledged(boolean adminAcknowledged) { this.adminAcknowledged = adminAcknowledged; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
