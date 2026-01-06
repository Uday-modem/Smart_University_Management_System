package com.rfid.tracker.dto;

import com.rfid.tracker.entity.AttendanceStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceMarkRequest {
    private LocalDate date;
    private LocalTime markTime;
    private AttendanceStatus status;
    private Long studentId;
    private String staffId;
    
    // ✅ CHANGED: sectionId is now String (format: 22ECE1A)
    private String sectionId;
    
    private String branch;
    private String remarks;
    private LocalTime entryTime;
    private Integer lateWindowMinutes;

    // Constructors
    public AttendanceMarkRequest() {}

    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getMarkTime() { return markTime; }
    public void setMarkTime(LocalTime markTime) { this.markTime = markTime; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    // ✅ CHANGED: sectionId getter/setter now uses String
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalTime entryTime) { this.entryTime = entryTime; }

    public Integer getLateWindowMinutes() { return lateWindowMinutes; }
    public void setLateWindowMinutes(Integer lateWindowMinutes) { 
        this.lateWindowMinutes = lateWindowMinutes; 
    }
}
