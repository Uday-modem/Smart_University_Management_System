package com.rfid.tracker.controller;

import com.rfid.tracker.dto.*;
import com.rfid.tracker.service.HardwareIntegrationService;
import com.rfid.tracker.entity.HardwareDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class HardwareController {
    
    @Autowired
    private HardwareIntegrationService hardwareIntegrationService;

    @PostMapping("/api/attendance/log/fingerprint")
    public ResponseEntity<HardwareResponseDTO> fingerprintScan(@RequestBody FingerprintScanRequest request) {
        System.out.println(">>> ESP32 FINGERPRINT REQUEST RECEIVED");
        System.out.println("Fingerprint ID: " + request.getFingerprintId());
        System.out.println("Device ID: " + request.getDeviceId());
        System.out.println("Scan Time: " + request.getScanTime());
        System.out.println("Scan Date: " + request.getScanDate());
        
        HardwareResponseDTO response = hardwareIntegrationService.processFingerprint(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/attendance/log/rfid")
    public ResponseEntity<HardwareResponseDTO> rfidScan(@RequestBody RFIDScanRequest request) {
        System.out.println(">>> ESP32 RFID REQUEST RECEIVED");
        System.out.println("Card UID: " + request.getCardUid());
        System.out.println("Device ID: " + request.getDeviceId());
        System.out.println("Scan Time: " + request.getScanTime());
        System.out.println("Scan Date: " + request.getScanDate());
        System.out.println("Room Number: " + request.getRoomNumber());
        
        HardwareResponseDTO response = hardwareIntegrationService.processRFIDScan(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/hardware/register-device")
    public ResponseEntity<HardwareResponseDTO> registerDevice(@RequestBody HardwareDevice device) {
        System.out.println(">>> DEVICE REGISTRATION REQUEST");
        System.out.println("Device ID: " + device.getDeviceId());
        
        HardwareResponseDTO response = hardwareIntegrationService.registerDevice(device);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/hardware/device-ping/{deviceId}")
    public ResponseEntity<HardwareResponseDTO> devicePing(@PathVariable String deviceId) {
        System.out.println(">>> DEVICE PING RECEIVED: " + deviceId);
        
        HardwareResponseDTO response = hardwareIntegrationService.updateDevicePing(deviceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/hardware/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"Hardware API is running\"}");
    }

    @GetMapping("/api/attendance/log/health")
    public ResponseEntity<String> logHealth() {
        return ResponseEntity.ok("{\"status\": \"Attendance Log API is running\"}");
    }
}
