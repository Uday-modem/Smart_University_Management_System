package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "timetable")
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ KEPT: sectionId is STRING (format: 22ECE1A)
    @Column(name = "section_id", nullable = false, length = 20)
    private String sectionId;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @Column(name = "time_slot", nullable = false)
    private String timeSlot;

    @Column(nullable = false)
    private String subject;

    // ✅ KEPT AS STRING: Stores staff ID string (e.g., "STAFF001")
    @Column(name = "staff_id", nullable = false)
    private String staffId;

    private String room;

    @Column(name = "scheduled_start_time")
    private LocalTime scheduledStartTime;

    @Column(name = "scheduled_end_time")
    private LocalTime scheduledEndTime;

    // Default constructor
    public Timetable() {}

    // Constructor with parameters
    public Timetable(String sectionId, String dayOfWeek, String timeSlot, String subject, String staffId, String room) {
        this.sectionId = sectionId;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.subject = subject;
        this.staffId = staffId;
        this.room = room;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public LocalTime getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(LocalTime scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    public LocalTime getScheduledEndTime() {
        return scheduledEndTime;
    }

    public void setScheduledEndTime(LocalTime scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }

    @Override
    public String toString() {
        return "Timetable{" +
                "id=" + id +
                ", sectionId='" + sectionId + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", timeSlot='" + timeSlot + '\'' +
                ", subject='" + subject + '\'' +
                ", staffId='" + staffId + '\'' +
                ", room='" + room + '\'' +
                ", scheduledStartTime=" + scheduledStartTime +
                ", scheduledEndTime=" + scheduledEndTime +
                '}';
    }
}