package com.rfid.tracker.controller;

import com.rfid.tracker.dto.CreateMindMapRequest;
import com.rfid.tracker.dto.CreateMindMapResponse;
import com.rfid.tracker.dto.MindMapDTO;
import com.rfid.tracker.service.StudyAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study-assistant")
@Slf4j
public class StudyAssistantController {

    @Autowired
    private StudyAssistantService studyAssistantService;

    /**
     * Create a new mind map from a video URL
     */
    @PostMapping("/create-mind-map")
    public ResponseEntity<?> createMindMap(
            @RequestBody CreateMindMapRequest request,
            @RequestParam Long studentId) {
        try {
            log.info("Creating mind map for student: {}", studentId);
            log.info("YouTube URL: {}, Subject: {}", request.getYoutubeUrl(), request.getSubject());
            
            // ✅ FIX: Validate request body
            if (request.getYoutubeUrl() == null || request.getYoutubeUrl().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new CreateMindMapResponse(false, "YouTube URL is required", null)
                );
            }
            
            if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new CreateMindMapResponse(false, "Subject is required", null)
                );
            }
            
            CreateMindMapResponse response = studyAssistantService.createMindMap(request, studentId);
            
            // ✅ FIX: Return proper response format
            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("success", response.isSuccess());
            apiResponse.put("message", response.getMessage());
            apiResponse.put("data", new HashMap<String, Object>() {{
                put("mindMapId", response.getMindMapId());
                put("status", "PENDING");
            }});
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            log.error("Error creating mind map: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error: " + e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get all mind maps for a student
     */
    @GetMapping("/my-mind-maps")
    public ResponseEntity<?> getMyMindMaps(@RequestParam Long studentId) {
        try {
            log.info("Fetching mind maps for student: {}", studentId);
            List<MindMapDTO> mindMaps = studyAssistantService.getStudentMindMaps(studentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mind maps retrieved successfully");
            response.put("data", mindMaps);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching mind maps: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error: " + e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get a specific mind map by ID
     */
    @GetMapping("/{mindMapId}")
    public ResponseEntity<?> getMindMap(
            @PathVariable String mindMapId,
            @RequestParam Long studentId) {
        try {
            log.info("Fetching mind map: {} for student: {}", mindMapId, studentId);
            MindMapDTO mindMap = studyAssistantService.getMindMapById(mindMapId);
            
            if (mindMap == null || !mindMap.getStudentId().equals(studentId)) {
                return ResponseEntity.status(404).body(
                    new HashMap<String, Object>() {{
                        put("success", false);
                        put("message", "Mind map not found");
                        put("data", null);
                    }}
                );
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mind map retrieved successfully");
            response.put("data", mindMap);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching mind map: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                new HashMap<String, Object>() {{
                    put("success", false);
                    put("message", "Error: " + e.getMessage());
                    put("data", null);
                }}
            );
        }
    }

    /**
     * Delete a mind map
     */
    @DeleteMapping("/{mindMapId}")
    public ResponseEntity<?> deleteMindMap(
            @PathVariable String mindMapId,
            @RequestParam Long studentId) {
        try {
            log.info("Deleting mind map: {} for student: {}", mindMapId, studentId);
            MindMapDTO mindMap = studyAssistantService.getMindMapById(mindMapId);
            
            if (mindMap == null || !mindMap.getStudentId().equals(studentId)) {
                return ResponseEntity.status(403).body(
                    new HashMap<String, Object>() {{
                        put("success", false);
                        put("message", "Access denied");
                        put("data", null);
                    }}
                );
            }
            
            boolean success = studyAssistantService.deleteMindMap(mindMapId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Mind map deleted successfully" : "Mind map not found");
            response.put("data", null);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting mind map: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                new HashMap<String, Object>() {{
                    put("success", false);
                    put("message", "Error: " + e.getMessage());
                    put("data", null);
                }}
            );
        }
    }

    /**
     * Toggle favorite status of a mind map
     */
    @PutMapping("/{mindMapId}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable String mindMapId,
            @RequestParam Long studentId) {
        try {
            log.info("Toggling favorite for mind map: {} (student: {})", mindMapId, studentId);
            MindMapDTO mindMap = studyAssistantService.getMindMapById(mindMapId);
            
            if (mindMap == null || !mindMap.getStudentId().equals(studentId)) {
                return ResponseEntity.status(403).body(
                    new HashMap<String, Object>() {{
                        put("success", false);
                        put("message", "Access denied");
                        put("data", null);
                    }}
                );
            }
            
            boolean success = studyAssistantService.toggleFavorite(mindMapId);
            MindMapDTO updatedMindMap = studyAssistantService.getMindMapById(mindMapId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Favorite status toggled" : "Mind map not found");
            response.put("data", updatedMindMap);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error toggling favorite: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                new HashMap<String, Object>() {{
                    put("success", false);
                    put("message", "Error: " + e.getMessage());
                    put("data", null);
                }}
            );
        }
    }
}