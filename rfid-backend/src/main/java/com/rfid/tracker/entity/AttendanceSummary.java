package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_summary")
public class AttendanceSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "total_classes")
    private Integer totalClasses = 0;

    @Column(name = "attended_classes")
    private Integer attendedClasses = 0;

    @Column(name = "attendance_percentage", precision = 5, scale = 2)
    private BigDecimal attendancePercentage = BigDecimal.ZERO;

    @Column(name = "status")
    private String status = "NORMAL"; // NORMAL, CONDONATION, DETAINED

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    // Constructors
    public AttendanceSummary() {}

    public AttendanceSummary(Long studentId, Integer semester, String academicYear) {
        this.studentId = studentId;
        this.semester = semester;
        this.academicYear = academicYear;
    }

    // Calculate percentage and status
    public void calculateAttendance() {
        if (totalClasses > 0) {
            double percentage = ((double) attendedClasses / totalClasses) * 100;
            this.attendancePercentage = BigDecimal.valueOf(percentage).setScale(2, BigDecimal.ROUND_HALF_UP);
            
            // Set status based on percentage
            if (percentage < 65) {
                this.status = "DETAINED";
            } else if (percentage >= 65 && percentage < 75) {
                this.status = "CONDONATION";
            } else {
                this.status = "NORMAL";
            }
        }
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getTotalClasses() { return totalClasses; }
    public void setTotalClasses(Integer totalClasses) { this.totalClasses = totalClasses; }

    public Integer getAttendedClasses() { return attendedClasses; }
    public void setAttendedClasses(Integer attendedClasses) { this.attendedClasses = attendedClasses; }

    public BigDecimal getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(BigDecimal attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
