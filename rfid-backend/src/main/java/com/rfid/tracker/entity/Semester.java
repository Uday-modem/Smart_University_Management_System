package com.rfid.tracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "semesters")
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "semester_name", nullable = false)
    private String name;

    @Column(name = "semester_number", nullable = false)
    private Integer number;

    @Column(name = "year", nullable = false)
    private Integer year;

    // ✅ NO createdAt field - frontend doesn't need it!

    public Semester() {}

    // Getters/Setters (frontend only needs these 3)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}
