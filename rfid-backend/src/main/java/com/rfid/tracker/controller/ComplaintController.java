package com.rfid.tracker.controller;

import com.rfid.tracker.entity.Complaint;
import com.rfid.tracker.entity.Student;
import com.rfid.tracker.repository.ComplaintRepository;
import com.rfid.tracker.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "http://localhost:5173")
public class ComplaintController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private StudentRepository studentRepository;

    // ==================== STUDENT ENDPOINTS ====================

    // Submit a complaint (Student)
    @PostMapping("/submit")
    public ResponseEntity<?> submitComplaint(@RequestBody ComplaintRequest request) {
        try {
            // Get authenticated student
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            System.out.println("📌 Complaint submission from: " + email);

            Optional<Student> studentOpt = studentRepository.findByEmail(email);
            if (studentOpt.isEmpty()) {
                System.out.println("❌ Student not found");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Student not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Student student = studentOpt.get();

            // Create complaint
            Complaint complaint = new Complaint(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getRegistrationNumber(),
                request.getIssueType(),
                request.getDescription()
            );

            complaintRepository.save(complaint);

            System.out.println("✅ Complaint submitted successfully");
            System.out.println("   Issue Type: " + request.getIssueType());
            System.out.println("   Student: " + student.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Complaint submitted successfully");
            response.put("complaintId", complaint.getId());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error submitting complaint: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to submit complaint: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get student's own complaints
    @GetMapping("/my-complaints")
    public ResponseEntity<?> getMyComplaints() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Optional<Student> studentOpt = studentRepository.findByEmail(email);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Student not found"));
            }

            Student student = studentOpt.get();
            List<Complaint> complaints = complaintRepository.findByStudentId(student.getId());

            return ResponseEntity.ok(complaints);

        } catch (Exception e) {
            System.err.println("❌ Error fetching complaints: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch complaints"));
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    // Get all complaints (Admin)
    @GetMapping("/all")
    public ResponseEntity<?> getAllComplaints() {
        try {
            System.out.println("📌 Fetching all complaints for admin");
            
            List<Complaint> complaints = complaintRepository.findAllByOrderByCreatedAtDesc();
            
            System.out.println("✅ Found " + complaints.size() + " complaints");
            
            return ResponseEntity.ok(complaints);

        } catch (Exception e) {
            System.err.println("❌ Error fetching all complaints: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch complaints"));
        }
    }

    // Get complaint statistics (Admin)
    @GetMapping("/stats")
    public ResponseEntity<?> getComplaintStats() {
        try {
            long totalComplaints = complaintRepository.count();
            long pendingComplaints = 0;
            long resolvedComplaints = 0;
            
            try {
                pendingComplaints = complaintRepository.countByStatus("PENDING");
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not count PENDING complaints: " + e.getMessage());
                // Fallback: count manually if repository method fails
                pendingComplaints = complaintRepository.findByStatus("PENDING").size();
            }
            
            try {
                resolvedComplaints = complaintRepository.countByStatus("RESOLVED");
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not count RESOLVED complaints: " + e.getMessage());
                // Fallback: count manually if repository method fails
                resolvedComplaints = complaintRepository.findByStatus("RESOLVED").size();
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", totalComplaints);
            stats.put("pending", pendingComplaints);
            stats.put("resolved", resolvedComplaints);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            System.err.println("❌ Error fetching stats: " + e.getMessage());
            e.printStackTrace();
            // Return default stats instead of error to prevent frontend crash
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("total", 0);
            defaultStats.put("pending", 0);
            defaultStats.put("resolved", 0);
            return ResponseEntity.ok(defaultStats);
        }
    }

    // Mark complaint as resolved (Admin)
    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveComplaint(@PathVariable Long id) {
        try {
            System.out.println("📌 Resolving complaint ID: " + id);

            Optional<Complaint> complaintOpt = complaintRepository.findById(id);
            if (complaintOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Complaint not found"));
            }

            Complaint complaint = complaintOpt.get();
            complaint.setStatus("RESOLVED");
            complaintRepository.save(complaint);

            System.out.println("✅ Complaint resolved");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Complaint marked as resolved");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error resolving complaint: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to resolve complaint"));
        }
    }

    // Delete complaint (Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComplaint(@PathVariable Long id) {
        try {
            System.out.println("📌 Deleting complaint ID: " + id);

            if (!complaintRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Complaint not found"));
            }

            complaintRepository.deleteById(id);

            System.out.println("✅ Complaint deleted");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Complaint deleted successfully");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error deleting complaint: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete complaint"));
        }
    }

    // Request DTO
    public static class ComplaintRequest {
        private String issueType;
        private String description;

        public String getIssueType() { return issueType; }
        public void setIssueType(String issueType) { this.issueType = issueType; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
