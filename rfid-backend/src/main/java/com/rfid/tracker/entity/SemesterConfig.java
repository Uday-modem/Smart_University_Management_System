package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "semester_config")
public class SemesterConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "regulation_id")
    private Long regulationId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public SemesterConfig() {}

    public SemesterConfig(String academicYear, Long regulationId, Integer year, Integer semester, LocalDate startDate, LocalDate endDate) {
        this.academicYear = academicYear;
        this.regulationId = regulationId;
        this.year = year;
        this.semester = semester;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters & Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getAcademicYear() { 
        return academicYear; 
    }
    
    public void setAcademicYear(String academicYear) { 
        this.academicYear = academicYear; 
    }

    public Long getRegulationId() { 
        return regulationId; 
    }
    
    public void setRegulationId(Long regulationId) { 
        this.regulationId = regulationId; 
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

    public LocalDate getStartDate() { 
        return startDate; 
    }
    
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
    }

    public LocalDate getEndDate() { 
        return endDate; 
    }
    
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; 
    }

    public Boolean getIsActive() { 
        return isActive; 
    }
    
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }

    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; 
    }
}
