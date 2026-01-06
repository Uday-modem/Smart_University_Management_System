package com.rfid.tracker.controller;

import com.rfid.tracker.entity.SemesterConfig;
import com.rfid.tracker.repository.SemesterConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/semester-config")
@CrossOrigin(origins = "http://localhost:5173")
public class SemesterConfigController {

    @Autowired
    private SemesterConfigRepository semesterConfigRepository;

    // Get all semester configurations
    @GetMapping
    public ResponseEntity<?> getAllSemesterConfigs() {
        try {
            List<SemesterConfig> configs = semesterConfigRepository.findAll();
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch semester configurations");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get semester config for specific parameters
    @GetMapping("/{academicYear}/{regulationId}/{year}/{semester}")
    public ResponseEntity<?> getSemesterConfig(@PathVariable String academicYear,
                                                @PathVariable Long regulationId,
                                                @PathVariable Integer year,
                                                @PathVariable Integer semester) {
        try {
            Optional<SemesterConfig> config = semesterConfigRepository
                    .findByAcademicYearAndRegulationIdAndYearAndSemester(academicYear, regulationId, year, semester);
            
            if (config.isPresent()) {
                return ResponseEntity.ok(config.get());
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Semester configuration not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch semester configuration");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Add or update semester configuration
    @PostMapping
    public ResponseEntity<?> saveSemesterConfig(@RequestBody SemesterConfigRequest request) {
        try {
            System.out.println("📌 Saving semester config: " + request.getAcademicYear() + 
                             ", Reg: " + request.getRegulationId() + 
                             ", Year: " + request.getYear() + 
                             ", Sem: " + request.getSemester());

            // Check if config already exists
            Optional<SemesterConfig> existingConfig = semesterConfigRepository
                    .findByAcademicYearAndRegulationIdAndYearAndSemester(
                            request.getAcademicYear(), 
                            request.getRegulationId(), 
                            request.getYear(), 
                            request.getSemester());

            SemesterConfig config;
            if (existingConfig.isPresent()) {
                // Update existing
                config = existingConfig.get();
                config.setStartDate(request.getStartDate());
                config.setEndDate(request.getEndDate());
                System.out.println("🔄 Updating existing config");
            } else {
                // Create new
                config = new SemesterConfig();
                config.setAcademicYear(request.getAcademicYear());
                config.setRegulationId(request.getRegulationId());
                config.setYear(request.getYear());
                config.setSemester(request.getSemester());
                config.setStartDate(request.getStartDate());
                config.setEndDate(request.getEndDate());
                System.out.println("✨ Creating new config");
            }

            semesterConfigRepository.save(config);
            System.out.println("✅ Semester config saved successfully");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Semester configuration saved successfully");
            response.put("config", config);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error saving semester config: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to save semester configuration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Delete semester configuration
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSemesterConfig(@PathVariable Long id) {
        try {
            semesterConfigRepository.deleteById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Semester configuration deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete semester configuration");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Request DTO
    public static class SemesterConfigRequest {
        private String academicYear;
        private Long regulationId;
        private Integer year;
        private Integer semester;
        private LocalDate startDate;
        private LocalDate endDate;

        // Getters & Setters
        public String getAcademicYear() { return academicYear; }
        public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

        public Long getRegulationId() { return regulationId; }
        public void setRegulationId(Long regulationId) { this.regulationId = regulationId; }

        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }

        public Integer getSemester() { return semester; }
        public void setSemester(Integer semester) { this.semester = semester; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }
}
