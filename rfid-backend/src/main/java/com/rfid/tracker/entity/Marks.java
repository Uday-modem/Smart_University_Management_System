package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "marks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "subject_id", "semester"})
})
public class Marks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    // ✅ NEW: Optional regulation tracking (for audit/history)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "regulation_id")
    private Regulation regulation;

    @Column(name = "internal_marks", precision = 5, scale = 2)
    private BigDecimal internalMarks = BigDecimal.ZERO;

    @Column(name = "external_marks", precision = 5, scale = 2)
    private BigDecimal externalMarks = BigDecimal.ZERO;

    @Column(name = "total_marks", precision = 5, scale = 2)
    private BigDecimal totalMarks = BigDecimal.ZERO;

    @Column(name = "grade")
    private String grade;

    @Column(name = "status")
    private String status = "NOT_GRADED";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public Marks() {}

    public Marks(Student student, Subject subject, Integer semester) {
        this.student = student;
        this.subject = subject;
        this.semester = semester;
    }

    // Helper methods
    public void calculateTotalMarks() {
        this.totalMarks = this.internalMarks.add(this.externalMarks);
        calculateGrade();
    }

    public void calculateGrade() {
        double total = this.totalMarks.doubleValue();
        if (total >= 90) this.grade = "O";
        else if (total >= 80) this.grade = "A+";
        else if (total >= 70) this.grade = "A";
        else if (total >= 60) this.grade = "B+";
        else if (total >= 50) this.grade = "B";
        else if (total >= 40) this.grade = "C";
        else this.grade = "F";
        this.status = "GRADED";
    }

    // Getters & Setters (all existing + new regulation getter/setter)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public Regulation getRegulation() { return regulation; }
    public void setRegulation(Regulation regulation) { this.regulation = regulation; }

    public BigDecimal getInternalMarks() { return internalMarks; }
    public void setInternalMarks(BigDecimal internalMarks) { this.internalMarks = internalMarks; }

    public BigDecimal getExternalMarks() { return externalMarks; }
    public void setExternalMarks(BigDecimal externalMarks) { this.externalMarks = externalMarks; }

    public BigDecimal getTotalMarks() { return totalMarks; }
    public void setTotalMarks(BigDecimal totalMarks) { this.totalMarks = totalMarks; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
