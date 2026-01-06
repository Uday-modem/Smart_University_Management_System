package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "branches")
public class Branch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "branch_code", nullable = false, unique = true, length = 10)
    private String branchCode;
    
    @Column(name = "branch_name", nullable = false, length = 255)
    private String branchName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // ===== CONSTRUCTORS =====
    
    public Branch() {
    }
    
    public Branch(String branchCode, String branchName) {
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.createdAt = LocalDateTime.now();
    }
    
    public Branch(String branchCode, String branchName, LocalDateTime createdAt) {
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.createdAt = createdAt;
    }
    
    // ===== GETTERS AND SETTERS =====
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBranchCode() {
        return branchCode;
    }
    
    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }
    
    public String getBranchName() {
        return branchName;
    }
    
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // ===== TOSTRING =====
    
    @Override
    public String toString() {
        return "Branch{" +
                "id=" + id +
                ", branchCode='" + branchCode + '\'' +
                ", branchName='" + branchName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}