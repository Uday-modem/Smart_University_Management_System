package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "regulations")
public class Regulation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "regulation_code", nullable = false, unique = true)
    private String regulationCode; // R20, R23, R25

    @Column(name = "regulation_name", nullable = false)
    private String regulationName; // Regulation 2020, etc.

    @Column(name = "start_year")
    private String startYear; // 2022

    @Column(name = "end_year")
    private String endYear; // 2026

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public Regulation() {}

    public Regulation(String regulationCode, String regulationName, String startYear, String endYear) {
        this.regulationCode = regulationCode;
        this.regulationName = regulationName;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRegulationCode() { return regulationCode; }
    public void setRegulationCode(String regulationCode) { this.regulationCode = regulationCode; }

    public String getRegulationName() { return regulationName; }
    public void setRegulationName(String regulationName) { this.regulationName = regulationName; }

    public String getStartYear() { return startYear; }
    public void setStartYear(String startYear) { this.startYear = startYear; }

    public String getEndYear() { return endYear; }
    public void setEndYear(String endYear) { this.endYear = endYear; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
