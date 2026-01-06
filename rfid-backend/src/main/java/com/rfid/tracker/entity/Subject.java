package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subjects", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"subject_code", "branch", "year", "semester", "regulation_id"})
})
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_code", nullable = false)
    private String subjectCode;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name = "branch", nullable = false)
    private String branch;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "regulation_id", nullable = false)
    private Regulation regulation;

    @Column(name = "credits")
    private Integer credits = 3;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public Subject() {}

    public Subject(String subjectCode, String subjectName, String branch, Integer year, 
                   Integer semester, Regulation regulation) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.branch = branch;
        this.year = year;
        this.semester = semester;
        this.regulation = regulation;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public Regulation getRegulation() { return regulation; }
    public void setRegulation(Regulation regulation) { this.regulation = regulation; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
