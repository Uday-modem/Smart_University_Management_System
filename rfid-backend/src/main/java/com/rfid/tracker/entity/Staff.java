package com.rfid.tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "branch", nullable = false)
    private String branch;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "staff_id", unique = true)
    private String staffId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Staff() {
        this.createdAt = LocalDateTime.now();
    }

    public Staff(String name, String email, String phone, String branch) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.branch = branch;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
