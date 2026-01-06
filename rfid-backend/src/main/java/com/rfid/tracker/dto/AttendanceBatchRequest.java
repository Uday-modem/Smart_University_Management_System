package com.rfid.tracker.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AttendanceBatchRequest {
    private LocalDate date;
    
    // ✅ CHANGED: sectionId is now String (format: 22ECE1A)
    private String sectionId;
    
    private String branch;
    private LocalTime entryTime;
    private Integer lateWindowMinutes;
    private List<AttendanceMarkRequest> attendances;

    // Constructors
    public AttendanceBatchRequest() {}

    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    // ✅ CHANGED: sectionId getter/setter now uses String
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public LocalTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalTime entryTime) { this.entryTime = entryTime; }

    public Integer getLateWindowMinutes() { return lateWindowMinutes; }
    public void setLateWindowMinutes(Integer lateWindowMinutes) { 
        this.lateWindowMinutes = lateWindowMinutes; 
    }

    public List<AttendanceMarkRequest> getAttendances() { return attendances; }
    public void setAttendances(List<AttendanceMarkRequest> attendances) { 
        this.attendances = attendances; 
    }
}
