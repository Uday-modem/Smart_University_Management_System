package com.rfid.tracker.controller;

import com.rfid.tracker.service.AdminAttendanceOverrideService;
import com.rfid.tracker.dto.HardwareResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/attendance")
@CrossOrigin(origins = "*")
public class AdminAttendanceController {
    @Autowired
    private AdminAttendanceOverrideService adminAttendanceOverrideService;

    /**
     * Override attendance status
     * Only accessible by ADMIN role
     */
    @PostMapping("/override/{attendanceId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<HardwareResponseDTO> overrideAttendance(
            @PathVariable Long attendanceId,
            @RequestParam String newStatus,
            @RequestParam String adminUsername,
            @RequestParam String reason) {
        
        HardwareResponseDTO response = adminAttendanceOverrideService.overrideAttendance(
                attendanceId, newStatus, adminUsername, reason);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Bulk override for section on a specific date
     */
    @PostMapping("/bulk-override")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<HardwareResponseDTO> bulkOverride(
            @RequestParam String date,
            @RequestParam String sectionId,
            @RequestParam String newStatus,
            @RequestParam String adminUsername,
            @RequestParam String reason) {
        
        LocalDate overrideDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        
        HardwareResponseDTO response = adminAttendanceOverrideService.bulkOverrideAttendance(
                overrideDate, sectionId, newStatus, adminUsername, reason);
        
        return ResponseEntity.ok(response);
    }
}
