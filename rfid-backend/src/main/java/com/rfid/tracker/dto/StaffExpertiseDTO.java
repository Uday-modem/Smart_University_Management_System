package com.rfid.tracker.dto;

public class StaffExpertiseDTO {
    private Long id;
    private String subject;
    private StaffDTO staff; // This uses the StaffDTO we just created

    // Constructors
    public StaffExpertiseDTO() {}

    public StaffExpertiseDTO(Long id, String subject, StaffDTO staff) {
        this.id = id;
        this.subject = subject;
        this.staff = staff;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public StaffDTO getStaff() { return staff; }
    public void setStaff(StaffDTO staff) { this.staff = staff; }
}
