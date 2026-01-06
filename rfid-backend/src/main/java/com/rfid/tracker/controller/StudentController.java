package com.rfid.tracker.controller;

import com.rfid.tracker.entity.Student;
import com.rfid.tracker.repository.StudentRepository;
import com.rfid.tracker.repository.PeriodAttendanceLogRepository;
import com.rfid.tracker.service.StudentService;
import com.rfid.tracker.entity.PeriodAttendanceLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private PeriodAttendanceLogRepository periodAttendanceLogRepository;

    // ========== STUDENT PROFILE ENDPOINT ==========

    /**
     * Get current student profile (with section info).
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            System.out.println("📌 Fetching profile for: " + email);

            Optional<Student> studentOpt = studentRepository.findByEmail(email);

            if (studentOpt.isEmpty()) {
                System.out.println("❌ Student not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Student not found"));
            }

            Student student = studentOpt.get();
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", student.getId());
            profile.put("name", student.getName());
            profile.put("email", student.getEmail());
            profile.put("registrationNumber", student.getRegistrationNumber());
            profile.put("branch", student.getBranch());
            profile.put("year", student.getYear());
            profile.put("semester", student.getSemester());
            profile.put("sectionId", student.getSectionId()); // ✅ String: 22ECE1A
            profile.put("section", student.getSection()); // ✅ String: ECE-A
            profile.put("regulationId", student.getRegulationId());
            profile.put("entryType", student.getEntryType());
            profile.put("attendanceStatus", student.getAttendanceStatus());

            System.out.println("✅ Profile fetched successfully");
            System.out.println(" Section ID: " + student.getSectionId());
            System.out.println(" Section: " + student.getSection());

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            System.err.println("❌ Error fetching profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch profile: " + e.getMessage()));
        }
    }

    // ========== SECTION MANAGEMENT ENDPOINTS ==========

    /**
     * ADMIN ENDPOINT: Bulk assign sections to existing students.
     * POST /api/students/bulk-assign-sections
     * Body: { "branch": "ECE", "year": 1, "semester": 1 }
     */
    @PostMapping("/bulk-assign-sections")
    public ResponseEntity<Map<String, Object>> bulkAssignSections(@RequestBody Map<String, Object> request) {
        try {
            String branch = (String) request.get("branch");
            Integer year = (Integer) request.get("year");

            if (branch == null || year == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Missing required fields: branch, year"));
            }

            System.out.println("📋 Admin triggered bulk section assignment: " + branch + " Y" + year);
            String result = studentService.bulkAssignSections(branch, year);

            return ResponseEntity.ok(Map.of(
                    "message", result,
                    "branch", branch,
                    "year", year
            ));
        } catch (Exception e) {
            System.err.println("❌ Error in bulk assignment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed: " + e.getMessage()));
        }
    }

    /**
     * Get students by sectionId (String format: 22ECE1A).
     * GET /api/students/by-section-id/{sectionId}
     * Example: /api/students/by-section-id/22ECE1A
     */
    @GetMapping("/by-section-id/{sectionId}")
    public ResponseEntity<Map<String, Object>> getStudentsBySectionId(@PathVariable String sectionId) {
        try {
            List<Student> students = studentService.getStudentsBySectionId(sectionId);
            System.out.println("📋 Fetched " + students.size() + " students from sectionId: " + sectionId);

            return ResponseEntity.ok(Map.of(
                    "sectionId", sectionId,
                    "count", students.size(),
                    "students", students
            ));
        } catch (Exception e) {
            System.err.println("❌ Error fetching students by sectionId: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed: " + e.getMessage()));
        }
    }

    /**
     * Get students by section display name (ECE-A).
     * GET /api/students/by-section/{section}
     * Example: /api/students/by-section/ECE-A
     */
    @GetMapping("/by-section/{section}")
    public ResponseEntity<Map<String, Object>> getStudentsBySection(@PathVariable String section) {
        try {
            List<Student> students = studentService.getStudentsBySection(section);
            System.out.println("📋 Fetched " + students.size() + " students from section: " + section);

            return ResponseEntity.ok(Map.of(
                    "section", section,
                    "count", students.size(),
                    "students", students
            ));
        } catch (Exception e) {
            System.err.println("❌ Error fetching students by section: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed: " + e.getMessage()));
        }
    }

    /**
     * Get all students (ADMIN).
     */
    @GetMapping("/all")
    public ResponseEntity<List<Student>> getAllStudents() {
        try {
            List<Student> students = studentRepository.findAll();
            System.out.println("📋 Fetched all " + students.size() + " students");
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            System.err.println("❌ Error fetching all students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Get student by ID (ADMIN).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStudentById(@PathVariable Long id) {
        try {
            Optional<Student> studentOpt = studentService.getStudentById(id);

            if (studentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Student not found"));
            }

            return ResponseEntity.ok(Map.of("student", studentOpt.get()));
        } catch (Exception e) {
            System.err.println("❌ Error fetching student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed: " + e.getMessage()));
        }
    }

    /**
     * Update student details (ADMIN).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateStudent(@PathVariable Long id, @RequestBody Student updatedStudent) {
        try {
            Optional<Student> existingOpt = studentService.getStudentById(id);

            if (existingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Student not found"));
            }

            Student existing = existingOpt.get();
            existing.setName(updatedStudent.getName());
            existing.setEmail(updatedStudent.getEmail());
            existing.setBranch(updatedStudent.getBranch());
            existing.setYear(updatedStudent.getYear());
            existing.setSemester(updatedStudent.getSemester());
            existing.setRegistrationNumber(updatedStudent.getRegistrationNumber());

            Student saved = studentService.saveStudent(existing);

            return ResponseEntity.ok(Map.of("message", "Student updated", "student", saved));
        } catch (Exception e) {
            System.err.println("❌ Error updating student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed: " + e.getMessage()));
        }
    }

    /**
     * Delete student (ADMIN).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable Long id) {
        try {
            Optional<Student> studentOpt = studentService.getStudentById(id);

            if (studentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Student not found"));
            }

            studentRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Student deleted successfully"));
        } catch (Exception e) {
            System.err.println("❌ Error deleting student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed: " + e.getMessage()));
        }
    }

    // ========== ✅ NEW CODE VERIFICATION ENDPOINTS ==========

    /**
     * POST /api/students/enter-code
     * Student submits verification code from their dashboard
     */
    @PostMapping("/enter-code")
    public ResponseEntity<Map<String, Object>> studentEnterCode(@RequestBody Map<String, Object> request) {
        try {
            Long studentId = ((Number) request.get("studentId")).longValue();
            String code = (String) request.get("code");
            String sectionId = (String) request.get("sectionId");
            String dateStr = (String) request.get("date");
            String timeSlot = (String) request.get("timeSlot");

            System.out.println("📌 [ENDPOINT] /students/enter-code called");
            System.out.println("   Student ID: " + studentId + " | Code: " + code + " | Section: " + sectionId);

            // Validate student
            Optional<Student> studentOpt = studentRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Student not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Student student = studentOpt.get();
            String studentRegNo = student.getRegistrationNumber();

            LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
            if (timeSlot == null) timeSlot = "GENERAL";

            // Call AttendanceService to verify code and mark attendance
            // Note: You'll need to inject AttendanceService here
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Code entry endpoint - integrate with AttendanceService");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Error in /students/enter-code: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/students/active-code
     * Authenticated student gets active code for their section
     */
    @GetMapping("/active-code")
    public ResponseEntity<Map<String, Object>> getStudentActiveCode() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            System.out.println("📌 [ENDPOINT] /students/active-code called for: " + email);

            Optional<Student> studentOpt = studentRepository.findByEmail(email);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Student not found"));
            }

            Student student = studentOpt.get();
            String sectionId = student.getSectionId();

            Map<String, Object> response = new HashMap<>();
            response.put("studentSectionId", sectionId);
            response.put("codeActive", false);
            response.put("message", "No active verification code at this time");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error in /students/active-code: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * GET /api/students/attendance-details/{date}
     * Show student's attendance with code/RFID verification method
     */
    @GetMapping("/attendance-details/{date}")
    public ResponseEntity<Map<String, Object>> getAttendanceDetails(@PathVariable String date) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            System.out.println("📌 [ENDPOINT] /students/attendance-details called for: " + email);

            Optional<Student> studentOpt = studentRepository.findByEmail(email);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Student not found"));
            }

            Student student = studentOpt.get();
            LocalDate scanDate = LocalDate.parse(date);

            // Get all period attendance logs for this student on this date
            List<PeriodAttendanceLog> logs = periodAttendanceLogRepository
                    .findByStudentRegistrationNumberAndScanDate(
                            student.getRegistrationNumber(), scanDate);

            Map<String, Object> response = new HashMap<>();
            response.put("date", date);
            response.put("totalPeriods", logs.size());
            response.put("attendanceRecords", new java.util.ArrayList<>());

            int rfidCount = 0, codeCount = 0;

            for (PeriodAttendanceLog log : logs) {
                Map<String, Object> record = new HashMap<>();
                record.put("timeSlot", log.getTimeSlot());
                record.put("scanTime", log.getScanTime().toString());
                record.put("verifiedVia", log.getVerifiedVia()); // "RFID" or "CODE"

                if ("RFID".equals(log.getVerifiedVia())) {
                    rfidCount++;
                    record.put("method", "RFID Card Scan");
                } else {
                    codeCount++;
                    record.put("method", "Verification Code");
                }

                ((List<Map<String, Object>>) response.get("attendanceRecords")).add(record);
            }

            response.put("rfidScans", rfidCount);
            response.put("codeEntries", codeCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error in /students/attendance-details: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}