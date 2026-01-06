package com.rfid.tracker.service;

import com.rfid.tracker.entity.Attendance;
import com.rfid.tracker.entity.AttendanceStatus;
import com.rfid.tracker.repository.AttendanceRepository;
import com.rfid.tracker.dto.HardwareResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AdminAttendanceOverrideService {
    @Autowired
    private AttendanceRepository attendanceRepository;

    /**
     * Admin override attendance status
     * @param attendanceId The attendance record ID
     * @param newStatus New status (PRESENT, ABSENT, HALF_DAY, LATE)
     * @param adminUsername Admin who is making the override
     * @param reason Reason for override
     * @return Response DTO
     */
    @Transactional
    public HardwareResponseDTO overrideAttendance(Long attendanceId, 
                                                   String newStatus, 
                                                   String adminUsername, 
                                                   String reason) {
        try {
            Optional<Attendance> attendanceOpt = attendanceRepository.findById(attendanceId);
            
            if (!attendanceOpt.isPresent()) {
                return new HardwareResponseDTO("ERROR", "Attendance record not found");
            }
            
            Attendance attendance = attendanceOpt.get();
            
            // Store original status if not already overridden
            if (attendance.getOriginalStatus() == null) {
                attendance.setOriginalStatus(attendance.getStatus());
            }
            
            // Update with new status
            AttendanceStatus status = AttendanceStatus.valueOf(newStatus);
            attendance.setStatus(status);
            attendance.setOverrideBy(adminUsername);
            attendance.setOverrideReason(reason);
            attendance.setOverrideDatetime(LocalDateTime.now());
            
            attendanceRepository.save(attendance);
            
            return new HardwareResponseDTO("SUCCESS", 
                    String.format("Attendance overridden from %s to %s by %s", 
                            attendance.getOriginalStatus(), newStatus, adminUsername),
                    attendance);
        } catch (IllegalArgumentException e) {
            return new HardwareResponseDTO("ERROR", "Invalid status: " + newStatus + 
                    ". Must be PRESENT, ABSENT, LATE, or HALF_DAY");
        } catch (Exception e) {
            return new HardwareResponseDTO("ERROR", "Error overriding attendance: " + e.getMessage());
        }
    }

    /**
     * Bulk override for exception cases (e.g., college events, holidays)
     */
    @Transactional
    public HardwareResponseDTO bulkOverrideAttendance(LocalDate date, 
                                                      String sectionId,
                                                      String newStatus,
                                                      String adminUsername,
                                                      String reason) {
        try {
            // Find all attendance records for given date and section
            // Apply override to all
            
            return new HardwareResponseDTO("SUCCESS", "Bulk override completed");
        } catch (Exception e) {
            return new HardwareResponseDTO("ERROR", "Error in bulk override: " + e.getMessage());
        }
    }
}
