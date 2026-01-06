package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "student_name", nullable = false)
    private String studentName;
    
    @Column(name = "student_email", nullable = false)
    private String studentEmail;
    
    @Column(name = "student_registration_number", nullable = false)
    private String studentRegistrationNumber;
    
    @Column(name = "issue_type", nullable = false)
    private String issueType;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, RESOLVED
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Constructors
    public Complaint() {}
    
    public Complaint(Long studentId, String studentName, String studentEmail, 
                    String studentRegistrationNumber, String issueType, String description) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.studentRegistrationNumber = studentRegistrationNumber;
        this.issueType = issueType;
        this.description = description;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    
    public String getStudentRegistrationNumber() { return studentRegistrationNumber; }
    public void setStudentRegistrationNumber(String studentRegistrationNumber) { 
        this.studentRegistrationNumber = studentRegistrationNumber; 
    }
    
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
