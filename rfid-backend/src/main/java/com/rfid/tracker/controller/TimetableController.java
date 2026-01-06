package com.rfid.tracker.controller;

import com.rfid.tracker.dto.TimetableRequest;
import com.rfid.tracker.entity.Section;
import com.rfid.tracker.entity.Timetable;
import com.rfid.tracker.entity.Student;
import com.rfid.tracker.entity.Staff;
import com.rfid.tracker.repository.SectionRepository;
import com.rfid.tracker.repository.StaffRepository;
import com.rfid.tracker.repository.TimetableRepository;
import com.rfid.tracker.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional; // ✅ ADDED
import jakarta.persistence.EntityManager; // ✅ ADDED
import jakarta.persistence.PersistenceContext; // ✅ ADDED

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/timetable")
@CrossOrigin(origins = "http://localhost:5173", methods = {
    RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
public class TimetableController {

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StudentRepository studentRepository;

    // ✅ ADDED: EntityManager to handle manual deletion of related records
    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<Timetable>> getTimetableBySection(
            @PathVariable String sectionId) {
        System.out.println("Fetching timetable for section: " + sectionId);
        List<Timetable> timetable = timetableRepository.findBySectionId(sectionId);
        System.out.println("Found " + timetable.size() + " timetable entries");
        return ResponseEntity.ok(timetable);
    }

    @GetMapping("/student")
    public ResponseEntity<List<Timetable>> getTimetableForLoggedInStudent(
            @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Student> studentOpt = studentRepository.findByEmail(userDetails.getUsername());
        if (studentOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Student student = studentOpt.get();
        String sectionId = student.getSectionId();

        if (sectionId == null || sectionId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Timetable> timetable = timetableRepository.findBySectionId(sectionId);
        return ResponseEntity.ok(timetable);
    }

    @PostMapping
    public ResponseEntity<?> createTimetableEntry(@RequestBody TimetableRequest request) {
        try {
            System.out.println("Creating timetable entry for section: " + request.getSectionId());
            
            Object staffIdObj = request.getStaffId();
            System.out.println("Staff ID received: " + staffIdObj + " (type: " + (staffIdObj != null ? staffIdObj.getClass().getSimpleName() : "null") + ")");

            Optional<Section> sectionOpt = sectionRepository.findBySectionCode(request.getSectionId());
            if (sectionOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Error: Section with code " + request.getSectionId() + " does not exist.");
            }

            Long staffIdLong;
            try {
                if (staffIdObj instanceof Number) {
                    staffIdLong = ((Number) staffIdObj).longValue();
                } else if (staffIdObj instanceof String) {
                    staffIdLong = Long.parseLong((String) staffIdObj);
                } else {
                    staffIdLong = Long.parseLong(staffIdObj.toString());
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to parse staffId: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: Invalid staff ID format: " + staffIdObj + ". Must be a number.");
            }

            System.out.println("Parsed Staff ID as Long: " + staffIdLong);

            Optional<Staff> staffOpt = staffRepository.findById(staffIdLong);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Error: Staff with ID " + staffIdLong + " does not exist.");
            }

            Staff staff = staffOpt.get();
            System.out.println("✅ Staff found: " + staff.getName() + " (ID: " + staff.getId() + ", staffId: " + staff.getStaffId() + ")");

            Timetable newEntry = new Timetable();
            newEntry.setSectionId(request.getSectionId());
            newEntry.setDayOfWeek(request.getDayOfWeek());
            newEntry.setTimeSlot(request.getTimeSlot());
            newEntry.setSubject(request.getSubject());
            newEntry.setStaffId(staff.getStaffId());
            newEntry.setRoom(request.getRoom());

            if (request.getTimeSlot() != null) {
                try {
                    String[] times = request.getTimeSlot().split("-");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    newEntry.setScheduledStartTime(LocalTime.parse(times[0].trim(), formatter));
                    newEntry.setScheduledEndTime(LocalTime.parse(times[1].trim(), formatter));
                } catch (Exception e) {
                    System.err.println("Could not parse time from timeSlot: " + request.getTimeSlot());
                }
            }

            Timetable savedEntry = timetableRepository.save(newEntry);
            System.out.println("✅ Timetable entry created successfully with ID: " + savedEntry.getId());
            return new ResponseEntity<>(savedEntry, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error creating timetable entry: " + e.getMessage());
            return new ResponseEntity<>("An unexpected internal server error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ FIXED: Handle staffId conversion for UPDATE (same logic as POST)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTimetableEntry(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updateData) {
        try {
            Optional<Timetable> existingEntryOptional = timetableRepository.findById(id);
            if (existingEntryOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Timetable existingEntry = existingEntryOptional.get();
            
            // Update subject if provided
            if (updateData.containsKey("subject") && updateData.get("subject") != null) {
                existingEntry.setSubject((String) updateData.get("subject"));
            }
            
            // Update room if provided
            if (updateData.containsKey("room") && updateData.get("room") != null) {
                existingEntry.setRoom((String) updateData.get("room"));
            }
            
            // Update staffId if provided (same conversion logic as POST)
            if (updateData.containsKey("staffId") && updateData.get("staffId") != null) {
                Object staffIdObj = updateData.get("staffId");
                Long staffIdLong;
                try {
                    if (staffIdObj instanceof Number) {
                        staffIdLong = ((Number) staffIdObj).longValue();
                    } else {
                        staffIdLong = Long.parseLong(staffIdObj.toString());
                    }
                    
                    Optional<Staff> staffOpt = staffRepository.findById(staffIdLong);
                    if (staffOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Error: Staff with ID " + staffIdLong + " does not exist.");
                    }
                    
                    Staff staff = staffOpt.get();
                    existingEntry.setStaffId(staff.getStaffId());
                    System.out.println("✅ Updated staff to: " + staff.getName() + " (ID: " + staff.getStaffId() + ")");
                    
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Error: Invalid staff ID format: " + staffIdObj);
                }
            }

            Timetable savedEntry = timetableRepository.save(existingEntry);
            System.out.println("✅ Timetable entry updated: " + id);
            return ResponseEntity.ok(savedEntry);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + e.getMessage());
        }
    }

    // ✅ FIXED: Added cleanup for foreign key constraints before deletion
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteTimetableEntry(@PathVariable Long id) {
        try {
            if (!timetableRepository.existsById(id)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            System.out.println("♻️ Starting deletion for Timetable ID: " + id);

            // 1. Delete associated verification codes first (Foreign Key Constraint Fix)
            int deletedCodes = entityManager.createQuery("DELETE FROM VerificationCode v WHERE v.timetableId = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            
            // 2. Delete associated staff entry logs
            int deletedStaffLogs = entityManager.createQuery("DELETE FROM StaffEntryLog s WHERE s.timetableId = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            
            // 3. Delete associated period attendance logs
            int deletedPeriodLogs = entityManager.createQuery("DELETE FROM PeriodAttendanceLog p WHERE p.timetableId = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            System.out.println("♻️ Cleanup complete: " + deletedCodes + " codes, " + deletedStaffLogs + " staff logs, " + deletedPeriodLogs + " period logs deleted.");

            // 4. Finally delete the timetable entry
            timetableRepository.deleteById(id);
            System.out.println("✅ Timetable entry deleted successfully: " + id);
            
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error deleting timetable entry: " + e.getMessage());
            return new ResponseEntity<>("Error deleting timetable: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
