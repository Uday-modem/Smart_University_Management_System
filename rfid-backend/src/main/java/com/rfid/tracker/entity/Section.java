package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "section")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_code", nullable = false, unique = true, length = 20)
    private String sectionCode;

    @Column(nullable = false)
    private String branch;

    @Column(nullable = false)
    private Integer year;

    // No semester here!

    @Column(name = "regulation_id")
    private Long regulationId;

    @Column(nullable = false)
    private Integer capacity = 50;

    @Column(name = "current_count")
    private Integer currentCount = 0;

    @Column(name = "section_letter", length = 1)
    private String sectionLetter;

    @Column(name = "display_name", length = 10)
    private String displayName;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public Section() {}

    public Section(String sectionCode, String branch, Integer year,
                   Long regulationId, String sectionLetter) {
        this.sectionCode = sectionCode;
        this.branch = branch;
        this.year = year;
        this.regulationId = regulationId;
        this.sectionLetter = sectionLetter;
        this.displayName = branch.toUpperCase() + "-" + sectionLetter;
        this.capacity = 50;
        this.currentCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Long getRegulationId() { return regulationId; }
    public void setRegulationId(Long regulationId) { this.regulationId = regulationId; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getCurrentCount() { return currentCount; }
    public void setCurrentCount(Integer currentCount) { this.currentCount = currentCount; }

    public String getSectionLetter() { return sectionLetter; }
    public void setSectionLetter(String sectionLetter) { this.sectionLetter = sectionLetter; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Section{" +
                "id=" + id +
                ", sectionCode='" + sectionCode + '\'' +
                ", branch='" + branch + '\'' +
                ", year=" + year +
                ", regulationId=" + regulationId +
                ", displayName='" + displayName + '\'' +
                ", currentCount=" + currentCount +
                ", capacity=" + capacity +
                '}';
    }
}
