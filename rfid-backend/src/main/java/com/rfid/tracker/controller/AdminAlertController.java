package com.rfid.tracker.controller;

import com.rfid.tracker.entity.StaffLateAlert;
import com.rfid.tracker.repository.StaffLateAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Admin Alert Management
 * Handles fetching and acknowledging staff late alerts for admin dashboard popup
 */
@RestController
@RequestMapping("/api/admin/alerts")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminAlertController {

    @Autowired
    private StaffLateAlertRepository staffLateAlertRepository;

    /**
     * GET endpoint to fetch all unacknowledged staff late alerts
     * Returns alerts ordered by creation time (newest first)
     *
     * @return List of StaffLateAlert entities that admin hasn't acknowledged yet
     */
    @GetMapping("/staff-late")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffLateAlert>> getUnacknowledgedStaffAlerts() {
        try {
            List<StaffLateAlert> alerts = staffLateAlertRepository.findByAdminAcknowledgedFalseOrderByCreatedAtDesc();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * POST endpoint to acknowledge/dismiss a specific alert
     * Sets adminAcknowledged flag to true, which removes it from unacknowledged list
     *
     * @param id - Alert ID to acknowledge
     * @return 200 OK if successful, 404 if alert not found
     */
    @PostMapping("/staff-late/{id}/acknowledge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity acknowledgeAlert(@PathVariable Long id) {
        try {
            java.util.Optional<StaffLateAlert> alertOpt = staffLateAlertRepository.findById(id);
            if (alertOpt.isPresent()) {
                StaffLateAlert alert = alertOpt.get();
                alert.setAdminAcknowledged(true);
                staffLateAlertRepository.save(alert);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET endpoint to fetch all staff late alerts (for admin viewing/history)
     *
     * @return List of all StaffLateAlert entities
     */
    @GetMapping("/staff-late/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffLateAlert>> getAllStaffAlerts() {
        try {
            List<StaffLateAlert> alerts = staffLateAlertRepository.findAll();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}