package com.rfid.tracker.dto;

public class TimetableRequest {
    
    // ✅ CHANGED: sectionId is now String (format: 22ECE1A)
    private String sectionId;
    
    private String dayOfWeek;
    private String timeSlot;
    private String subject;
    private String staffId;
    private String room;

    // Getters and Setters
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
}
