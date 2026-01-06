package com.rfid.tracker.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "staff_expertise")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StaffExpertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_id", nullable = false)
    private String staffId;

    @Column(nullable = false)
    private String subject;

    public StaffExpertise() {}

    public StaffExpertise(String staffId, String subject) {
        this.staffId = staffId;
        this.subject = subject;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}
