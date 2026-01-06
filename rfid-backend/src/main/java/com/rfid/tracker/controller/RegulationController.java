package com.rfid.tracker.controller;

import com.rfid.tracker.entity.Regulation;
import com.rfid.tracker.repository.RegulationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/regulations")
@CrossOrigin(origins = "http://localhost:5173")
public class RegulationController {

    @Autowired
    private RegulationRepository regulationRepository;

    // Get all regulations (for dropdown)
    @GetMapping
    public ResponseEntity<List<Regulation>> getAllRegulations() {
        return ResponseEntity.ok(regulationRepository.findAll());
    }

    // Get regulation by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRegulationById(@PathVariable Long id) {
        return regulationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
