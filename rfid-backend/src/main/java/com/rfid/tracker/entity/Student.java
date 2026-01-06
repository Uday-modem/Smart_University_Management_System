package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "branch", nullable = false)
    private String branch;
    
    @Column(name = "year")
    private Integer year;
    
    @Column(name = "semester")
    private Integer semester;
    
    // ✅ CHANGED: sectionId is now STRING (format: 22ECE1A)
    @Column(name = "section_id", length = 20)
    private String sectionId;
    
    // ✅ NEW: section display name (format: ECE-A)
    @Column(name = "section", length = 10)
    private String section;
    
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "regulation_id")
    private Long regulationId;

    @Column(name = "attendance_status")
    private String attendanceStatus = "ACTIVE";

    @Column(name = "entry_type")
    private String entryType = "REGULAR";

    // Constructors
    public Student() {}

    public Student(String registrationNumber, String name, String email, String password, 
                   String branch, Integer year, Integer semester) {
        this.registrationNumber = registrationNumber;
        this.name = name;
        this.email = email;
        this.password = password;
        this.branch = branch;
        this.year = year;
        this.semester = semester;
        this.createdAt = LocalDateTime.now();
        this.attendanceStatus = "ACTIVE";
        this.entryType = "REGULAR";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    // ✅ CHANGED: sectionId is now String
    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    // ✅ NEW: section getter/setter
    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getRegulationId() {
        return regulationId;
    }

    public void setRegulationId(Long regulationId) {
        this.regulationId = regulationId;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", branch='" + branch + '\'' +
                ", year=" + year +
                ", semester=" + semester +
                ", sectionId='" + sectionId + '\'' +
                ", section='" + section + '\'' +
                ", entryType='" + entryType + '\'' +
                ", attendanceStatus='" + attendanceStatus + '\'' +
                '}';
    }
}
