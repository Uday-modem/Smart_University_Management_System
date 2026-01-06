package com.rfid.tracker.controller;

import com.rfid.tracker.entity.Staff;
import com.rfid.tracker.entity.StaffExpertise;
import com.rfid.tracker.repository.StaffExpertiseRepository;
import com.rfid.tracker.repository.StaffRepository;
import com.rfid.tracker.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "http://localhost:5173")
public class StaffController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StaffExpertiseRepository expertiseRepository;

    @Autowired
    private EmailService emailService;

    // ✅ ADDED THIS MISSING METHOD for Testing Late Staff Logic
    @PostMapping("/test/trigger-late-check")
    public ResponseEntity<?> testStaffLateCheck() {
        try {
            System.out.println("🔥 MANUALLY TRIGGERING STAFF LATE CHECK...");
            System.out.println("Current Time: " + java.time.LocalDateTime.now());
            
            // Calls the scheduled task method manually
            emailService.checkAndSendStaffAbsenceAlerts(); 
            
            return ResponseEntity.ok("Late check triggered successfully! Check console logs.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/expertise/branch/{branch}")
    public ResponseEntity<List<Map<String, Object>>> getStaffExpertiseByBranch(@PathVariable String branch) {
        try {
            List<Staff> staffInBranch = staffRepository.findByBranch(branch);
            List<String> staffIds = staffInBranch.stream()
                    .map(Staff::getStaffId)
                    .collect(Collectors.toList());

            List<StaffExpertise> expertiseList = staffIds.isEmpty() ? new ArrayList<>() :
                    expertiseRepository.findByStaffIdIn(staffIds);

            List<Map<String, Object>> result = new ArrayList<>();
            for (StaffExpertise exp : expertiseList) {
                Optional<Staff> staffOpt = staffRepository.findByStaffId(exp.getStaffId());
                if (staffOpt.isPresent()) {
                    Staff staff = staffOpt.get();
                    Map<String, Object> expertiseData = new HashMap<>();
                    expertiseData.put("id", exp.getId());
                    expertiseData.put("subject", exp.getSubject());
                    
                    Map<String, Object> staffData = new HashMap<>();
                    staffData.put("id", staff.getId());
                    staffData.put("name", staff.getName());
                    staffData.put("email", staff.getEmail());
                    staffData.put("phone", staff.getPhone());
                    staffData.put("staffId", staff.getStaffId());
                    staffData.put("branch", staff.getBranch());
                    
                    expertiseData.put("staff", staffData);
                    result.add(expertiseData);
                }
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/by-branch/{branch}")
    public ResponseEntity<List<Staff>> getStaffByBranch(@PathVariable String branch) {
        return ResponseEntity.ok(staffRepository.findByBranch(branch));
    }

    @GetMapping
    public ResponseEntity<List<Staff>> getAllStaff() {
        return ResponseEntity.ok(staffRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getStaffById(@PathVariable Long id) {
        return staffRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rfid/{staffId}")
    public ResponseEntity<?> getStaffByStaffId(@PathVariable String staffId) {
        Optional<Staff> staffOpt = staffRepository.findByStaffId(staffId);
        if (staffOpt.isPresent()) {
            return ResponseEntity.ok(staffOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Staff ID not found: " + staffId);
        }
    }

    @PostMapping
    public ResponseEntity<?> createStaff(@RequestBody Staff staff) {
        try {
            if (staff.getStaffId() != null) {
                if (staffRepository.existsByStaffId(staff.getStaffId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Staff ID already exists: " + staff.getStaffId());
                }
            }
            Staff savedStaff = staffRepository.save(staff);
            return new ResponseEntity<>(savedStaff, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating staff: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @RequestBody Staff updatedStaff) {
        try {
            Optional<Staff> existingStaffOpt = staffRepository.findById(id);
            if (existingStaffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Staff existingStaff = existingStaffOpt.get();
            
            // Clean input - TRIM ALL INPUTS
            String newStaffId = (updatedStaff.getStaffId() != null) ? updatedStaff.getStaffId().trim() : null;
            String currentStaffId = existingStaff.getStaffId();
            
            System.out.println("🔍 UPDATE DEBUG: Current ID='" + currentStaffId + "' | New ID='" + newStaffId + "'");

            // Update basic fields
            existingStaff.setName(updatedStaff.getName() != null ? updatedStaff.getName().trim() : "");
            existingStaff.setEmail(updatedStaff.getEmail() != null ? updatedStaff.getEmail().trim() : "");
            existingStaff.setPhone(updatedStaff.getPhone() != null ? updatedStaff.getPhone().trim() : "");
            existingStaff.setBranch(updatedStaff.getBranch());

            // Check if Staff ID is being changed
            if (newStaffId != null && !newStaffId.equalsIgnoreCase(currentStaffId)) {
                System.out.println("🔍 Staff ID is CHANGING from '" + currentStaffId + "' to '" + newStaffId + "'");
                
                // Check if this new ID is taken by someone ELSE
                Optional<Staff> existingWithNewId = staffRepository.findByStaffId(newStaffId);
                
                if (existingWithNewId.isPresent()) {
                    Long existingOwnerId = existingWithNewId.get().getId();
                    Long currentUpdateId = id;
                    
                    System.out.println("🔍 ID Owner ID=" + existingOwnerId + " | Current ID=" + currentUpdateId);
                    
                    if (!existingOwnerId.equals(currentUpdateId)) {
                        // CONFLICT: Someone ELSE owns this ID
                        String ownerName = existingWithNewId.get().getName();
                        String warningMessage = "⚠️ Staff ID '" + newStaffId + "' is already in use by: " + ownerName;
                        System.out.println("❌ CONFLICT: " + warningMessage);
                        
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(warningMessage);
                    }
                }
                // No conflict - proceed with update
                existingStaff.setStaffId(newStaffId);
            }

            Staff saved = staffRepository.save(existingStaff);
            System.out.println("✅ Staff updated successfully. New ID: " + saved.getStaffId());
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("❌ Error updating staff: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        try {
            if (!staffRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            Optional<Staff> staffOpt = staffRepository.findById(id);
            if (staffOpt.isPresent()) {
                String staffIdStr = staffOpt.get().getStaffId();
                List<StaffExpertise> toDelete = expertiseRepository.findByStaffId(staffIdStr);
                expertiseRepository.deleteAll(toDelete);
            }
            staffRepository.deleteById(id);
            return ResponseEntity.ok("Staff and related expertise records deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error deleting staff: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/expertise")
    public ResponseEntity<?> addExpertise(@RequestBody StaffExpertiseRequest request) {
        try {
            Optional<Staff> staffOpt = staffRepository.findById(request.getStaffId());
            if (staffOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Staff not found with ID: " + request.getStaffId());
            }

            Staff staff = staffOpt.get();
            StaffExpertise expertise = new StaffExpertise(staff.getStaffId(), request.getSubject());
            StaffExpertise savedExpertise = expertiseRepository.save(expertise);
            return new ResponseEntity<>(savedExpertise, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding expertise: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/expertise/{id}")
    public ResponseEntity<?> deleteExpertise(@PathVariable Long id) {
        try {
            if (!expertiseRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            expertiseRepository.deleteById(id);
            return ResponseEntity.ok("Expertise deleted successfully");
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting expertise: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/send-absence-alert")
    public ResponseEntity<String> sendManualAbsenceAlert(@RequestBody ManualAlertRequest request) {
        try {
            Optional<Staff> staffOpt = staffRepository.findById(request.getStaffId());
            if (staffOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Staff not found");
            }

            Staff staff = staffOpt.get();
            emailService.sendStaffAbsenceAlert(
                    staff.getName(),
                    staff.getStaffId(),
                    staff.getEmail(),
                    request.getClassName(),
                    request.getMarkTime(),
                    staff.getPhone(),
                    staff.getBranch()
            );
            return ResponseEntity.ok("Absence alert sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending alert: " + e.getMessage());
        }
    }

    // Helper classes
    public static class StaffExpertiseRequest {
        private Long staffId;
        private String subject;
        public Long getStaffId() { return staffId; }
        public void setStaffId(Long staffId) { this.staffId = staffId; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
    }

    public static class ManualAlertRequest {
        private Long staffId;
        private String className;
        private LocalTime markTime;
        public Long getStaffId() { return staffId; }
        public void setStaffId(Long staffId) { this.staffId = staffId; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public LocalTime getMarkTime() { return markTime; }
        public void setMarkTime(LocalTime markTime) { this.markTime = markTime; }
    }
}
