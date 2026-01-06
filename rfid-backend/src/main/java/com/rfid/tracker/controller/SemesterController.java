package com.rfid.tracker.controller;

import com.rfid.tracker.entity.Semester;
import com.rfid.tracker.repository.SemesterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/semesters")
@CrossOrigin(origins = "http://localhost:5173")
public class SemesterController {

    @Autowired
    private SemesterRepository semesterRepository;

    @GetMapping
    public ResponseEntity<List<Semester>> getSemestersByYear(@RequestParam Integer year) {
        try {
            System.out.println("📌 Fetching semesters for year: " + year);
            List<Semester> semesters = semesterRepository.findByYear(year);
            System.out.println("✅ Found " + semesters.size() + " semesters for year " + year);
            if (semesters.isEmpty()) {
                System.out.println("⚠️ Warning: No semesters found for year " + year + ". Table might be empty.");
            }
            return ResponseEntity.ok(semesters != null ? semesters : List.of());
        } catch (Exception e) {
            System.err.println("❌ Error fetching semesters for year " + year + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of()); // Return empty list instead of error
        }
    }

    // (Optional) If you want all semesters:
    @GetMapping("/all")
    public ResponseEntity<List<Semester>> getAllSemesters() {
        return ResponseEntity.ok(semesterRepository.findAll());
    }
}
