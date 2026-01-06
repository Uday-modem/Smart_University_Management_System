package com.rfid.tracker.controller;

import com.rfid.tracker.entity.StaffLateAlert;
import com.rfid.tracker.repository.StaffLateAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/alerts")
@CrossOrigin(origins = "http://localhost:5173")
public class StaffLateAlertController {

    @Autowired
    private StaffLateAlertRepository alertRepository;

    /**
     * Fetch all unacknowledged late alerts
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<StaffLateAlert>> getLateAlerts() {
        List<StaffLateAlert> alerts = alertRepository.findByAdminAcknowledgedFalseOrderByCreatedAtDesc();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Delete (dismiss) an alert
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> dismissAlert(@PathVariable Long id) {
        if (alertRepository.existsById(id)) {
            alertRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Mark an alert as acknowledged (optional - instead of deleting)
     */
    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> acknowledgeAlert(@PathVariable Long id) {
        return alertRepository.findById(id).map(alert -> {
            alert.setAdminAcknowledged(true);
            alertRepository.save(alert);
            return ResponseEntity.ok(alert);
        }).orElse(ResponseEntity.notFound().build());
    }
}
