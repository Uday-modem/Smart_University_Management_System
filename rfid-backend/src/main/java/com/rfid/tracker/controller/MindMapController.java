package com.rfid.tracker.controller;

import com.rfid.tracker.dto.CreateMindMapRequest;
import com.rfid.tracker.dto.CreateMindMapResponse;
import com.rfid.tracker.dto.GenerateMindMapFromYouTubeRequest;
import com.rfid.tracker.dto.MindMapDTO;
import com.rfid.tracker.dto.MindMapMermaidResponse;
import com.rfid.tracker.service.StudyAssistantService;
import com.rfid.tracker.service.YouTubeTranscriptService;
import com.rfid.tracker.service.MermaidGraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MIND MAP ORCHESTRATION CONTROLLER - COMPLETE WITH REAL YOUTUBE INTEGRATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * This controller orchestrates the 3-Pipeline system:
 * Pipeline 1: INGESTION (YouTube Transcript Extraction via YouTubeTranscriptService)
 * Pipeline 2: SYNTHESIS (Mermaid Graph Generation via Ollama)
 * Pipeline 3: RENDERING (JSON Response for Frontend)
 *
 * Architecture inspired by NotebookLM and Gemini's study assistant
 *
 * ENDPOINTS:
 * ✅ OLD ENDPOINTS (Backward compatibility) - KEPT AS IS
 * ✅ NEW ENDPOINT: /api/mindmap/generate-from-youtube (Real YouTube integration)
 * ✅ TEST ENDPOINTS: /api/mindmap/test/* (Diagnostic)
 */

@RestController
@Slf4j
@CrossOrigin(origins = "*")
public class MindMapController {

    @Autowired
    private StudyAssistantService studyAssistantService;

    @Autowired
    private YouTubeTranscriptService youtubeTranscriptService;

    @Autowired
    private MermaidGraphService mermaidGraphService;

    // ══════════════════════════════════════════════════════════════════════════════
    // EXISTING ENDPOINTS (Keep for backward compatibility) - ALL ORIGINAL METHODS
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Get all mind maps for a student
     * Original endpoint - maintained for backward compatibility
     */
    @GetMapping("/api/study-assistant/student/{studentId}/mind-maps")
    public ResponseEntity<List<MindMapDTO>> getStudentMindMaps(@PathVariable Long studentId) {
        log.info("📚 Fetching mind maps for student: {}", studentId);
        try {
            List<MindMapDTO> mindMaps = studyAssistantService.getStudentMindMaps(studentId);
            return ResponseEntity.ok(mindMaps);
        } catch (Exception e) {
            log.error("❌ Error fetching mind maps: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get a specific mind map by ID
     * Original endpoint - maintained for backward compatibility
     */
    @GetMapping("/api/study-assistant/mind-maps/{mindMapId}")
    public ResponseEntity<MindMapDTO> getMindMapById(@PathVariable String mindMapId) {
        log.info("🔍 Fetching mind map: {}", mindMapId);
        try {
            MindMapDTO mindMap = studyAssistantService.getMindMapById(mindMapId);
            if (mindMap != null) {
                return ResponseEntity.ok(mindMap);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Error fetching mind map: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Toggle favorite status for a mind map
     * Original endpoint - maintained for backward compatibility
     */
    @PutMapping("/api/study-assistant/mind-maps/{mindMapId}/toggle-favorite")
    public ResponseEntity<Map<String, Object>> toggleFavorite(@PathVariable String mindMapId) {
        log.info("⭐ Toggling favorite status for mind map: {}", mindMapId);
        try {
            boolean success = studyAssistantService.toggleFavorite(mindMapId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("mindMapId", mindMapId);
            response.put("message", success ? "✅ Favorite status toggled" : "❌ Failed to toggle favorite");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error toggling favorite: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete a mind map by ID
     * Original endpoint - maintained for backward compatibility
     */
    @DeleteMapping("/api/study-assistant/mind-maps/{mindMapId}")
    public ResponseEntity<Map<String, Object>> deleteMindMap(@PathVariable String mindMapId) {
        log.info("🗑️ Deleting mind map: {}", mindMapId);
        try {
            boolean success = studyAssistantService.deleteMindMap(mindMapId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("mindMapId", mindMapId);
            response.put("message", success ? "✅ Mind map deleted successfully" : "❌ Mind map not found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error deleting mind map: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // NEW 3-PIPELINE ENDPOINTS (YouTube Integration with Real Transcripts)
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * ✨ MAIN NEW ENDPOINT: Generate Mind Map from YouTube Video
     *
     * This endpoint orchestrates the entire 3-Pipeline system:
     * - Pipeline 1: INGESTION - Extract REAL transcript from YouTube
     * - Pipeline 2: SYNTHESIS - Generate Mermaid code using Ollama
     * - Pipeline 3: RENDERING - Package response for frontend rendering
     *
     * KEY CHANGE: This endpoint uses REAL YouTube transcripts, NOT mock data
     *
     * @param request Contains youtubeUrl and subject
     * @return MindMapMermaidResponse with Mermaid.js code ready for rendering
     */
    @PostMapping("/api/mindmap/generate-from-youtube")
    public ResponseEntity<MindMapMermaidResponse> generateMindMapFromYouTube(
            @RequestBody GenerateMindMapFromYouTubeRequest request) {

        log.info("╔════════════════════════════════════════════════════════════════════════╗");
        log.info("║ 🎬 MIND MAP GENERATION REQUEST - 3 PIPELINE ORCHESTRATION           ║");
        log.info("║ 📊 Real YouTube Transcripts - NotebookLM Architecture                 ║");
        log.info("╚════════════════════════════════════════════════════════════════════════╝");
        log.info("📥 YouTube URL: {}", request.getYoutubeUrl());
        log.info("📚 Subject: {}", request.getSubject());

        try {
            // ══════════════════════════════════════════════════════════════════════════════
            // INPUT VALIDATION
            // ══════════════════════════════════════════════════════════════════════════════

            if (!request.isValid()) {
                log.error("❌ Invalid request: YouTube URL and subject are required");
                return createErrorResponse(request, "Invalid input",
                        "YouTube URL and subject cannot be empty");
            }

            // ══════════════════════════════════════════════════════════════════════════════
            // PIPELINE 1: INGESTION
            // Task: YouTube URL → Extract Video ID → Fetch REAL Transcript
            // ══════════════════════════════════════════════════════════════════════════════

            log.info("\n┌─ PIPELINE 1: INGESTION ──────────────────────────────────────────┐");
            log.info("│ Task: YouTube URL → Extract Video ID → Fetch REAL Transcript │");
            log.info("└──────────────────────────────────────────────────────────────────┘");

            // Step 1: Extract video ID from URL
            log.info("🔍 Step 1: Extracting video ID from YouTube URL...");
            String videoId = youtubeTranscriptService.extractVideoId(request.getYoutubeUrl());
            log.info("✅ Video ID extracted: {}", videoId);

            // Step 2: Fetch REAL transcript from YouTube
            log.info("📡 Step 2: Fetching REAL transcript from YouTube...");
            String transcript = youtubeTranscriptService.extractTranscript(request.getYoutubeUrl());

            // Validate transcript
            if (transcript == null || transcript.trim().isEmpty()) {
                log.error("❌ Pipeline 1 Failed: Could not fetch transcript");
                return createErrorResponse(request, "Transcript fetch failed",
                        "Pipeline 1: Could not extract transcript from YouTube. " +
                        "Possible reasons: Invalid URL, video removed, or no captions available.");
            }

            // Check minimum transcript length
            if (transcript.length() < 100) {
                log.error("❌ Pipeline 1 Failed: Transcript too short ({} chars)", transcript.length());
                return createErrorResponse(request, "Transcript too short",
                        "Pipeline 1: Transcript must be at least 100 characters. Got: " + transcript.length());
            }

            log.info("✅ Pipeline 1 Complete:");
            log.info("   ├─ Video ID: {}", videoId);
            log.info("   ├─ Transcript length: {} characters", transcript.length());
            log.info("   └─ Status: ✅ REAL data extracted from YouTube");

            // ══════════════════════════════════════════════════════════════════════════════
            // PIPELINE 2: SYNTHESIS
            // Task: Transcript → Ollama/LLM → Mermaid.js Graph Code
            // ══════════════════════════════════════════════════════════════════════════════

            log.info("\n┌─ PIPELINE 2: SYNTHESIS ───────────────────────────────────────────┐");
            log.info("│ Task: Transcript → Ollama/LLM → Mermaid.js Graph Code      │");
            log.info("└──────────────────────────────────────────────────────────────────┘");

            log.info("🧠 Step 3: Generating Mermaid graph with Ollama...");
            String mermaidCode = mermaidGraphService.generateMermaidGraph(transcript, request.getSubject());

            // Validate Mermaid code
            if (mermaidCode == null || mermaidCode.isEmpty()) {
                log.error("❌ Pipeline 2 Failed: No Mermaid code generated");
                return createErrorResponse(request, "Mermaid generation failed",
                        "Pipeline 2: Ollama did not generate valid Mermaid code. Try again or check Ollama service.");
            }

            log.info("✅ Pipeline 2 Complete:");
            log.info("   ├─ Mermaid code length: {} characters", mermaidCode.length());
            log.info("   ├─ Contains 'graph TD': {}", mermaidCode.contains("graph TD"));
            log.info("   ├─ Contains connections: {}", mermaidCode.contains("-->"));
            log.info("   └─ Status: ✅ REAL mind map generated from actual video content");

            // ══════════════════════════════════════════════════════════════════════════════
            // PIPELINE 3: RENDERING
            // Task: Package Mermaid Code → JSON Response for Frontend
            // ══════════════════════════════════════════════════════════════════════════════

            log.info("\n┌─ PIPELINE 3: RENDERING ──────────────────────────────────────────┐");
            log.info("│ Task: Package Mermaid Code → JSON Response for Frontend    │");
            log.info("└──────────────────────────────────────────────────────────────────┘");

            log.info("📦 Step 4: Packaging response for frontend rendering...");

            MindMapMermaidResponse response = new MindMapMermaidResponse();
            response.setSuccess(true);
            response.setYoutubeUrl(request.getYoutubeUrl());
            response.setVideoId(videoId);
            response.setSubject(request.getSubject());
            response.setTranscriptLength(transcript.length());
            response.setMermaidCode(mermaidCode);
            response.setMessage("✅ Mind map generated successfully! REAL YouTube data. Ready for rendering.");
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            response.setErrorDescription(null);

            log.info("✅ Pipeline 3 Complete: Response packaged successfully");

            log.info("\n╔════════════════════════════════════════════════════════════════════════╗");
            log.info("║ ✅ ALL 3 PIPELINES COMPLETE - READY FOR FRONTEND RENDERING          ║");
            log.info("║ 🎯 Result: UNIQUE mind map from REAL YouTube transcript             ║");
            log.info("╚════════════════════════════════════════════════════════════════════════╝");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("❌ VALIDATION ERROR: {}", e.getMessage());
            return createErrorResponse(request, "Invalid input", e.getMessage());

        } catch (RuntimeException e) {
            log.error("❌ RUNTIME ERROR in 3-Pipeline: {}", e.getMessage(), e);
            return createErrorResponse(request, "Processing failed", e.getMessage());

        } catch (Exception e) {
            log.error("❌ UNEXPECTED ERROR: {}", e.getMessage(), e);
            return createErrorResponse(request, "Unexpected error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Helper method: Create error response for failed pipeline
     */
    private ResponseEntity<MindMapMermaidResponse> createErrorResponse(
            GenerateMindMapFromYouTubeRequest request,
            String message,
            String errorDescription) {

        MindMapMermaidResponse errorResponse = new MindMapMermaidResponse();
        errorResponse.setSuccess(false);
        errorResponse.setYoutubeUrl(request.getYoutubeUrl());
        errorResponse.setSubject(request.getSubject());
        errorResponse.setMessage("❌ " + message);
        errorResponse.setErrorDescription(errorDescription);
        errorResponse.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        errorResponse.setMermaidCode(null);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // TEST & DIAGNOSTIC ENDPOINTS (Keep these for debugging)
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Health check endpoint
     */
    @GetMapping("/api/mindmap/health")
    public ResponseEntity<String> healthCheck() {
        log.info("🏥 Health check requested");
        return ResponseEntity.ok("✅ Mind Map Service is running (3-Pipeline System Active - Real YouTube Integration)");
    }

    /**
     * Test video ID extraction
     */
    @PostMapping("/api/mindmap/test/extract-id")
    public ResponseEntity<String> testExtractId(@RequestParam String url) {
        log.info("🧪 Testing video ID extraction for: {}", url);
        try {
            String videoId = youtubeTranscriptService.extractVideoId(url);
            return ResponseEntity.ok("✅ Video ID extracted successfully\n\nVideo ID: " + videoId);
        } catch (Exception e) {
            log.error("❌ Error extracting video ID: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Test transcript fetching
     */
    @PostMapping("/api/mindmap/test/fetch-transcript")
    public ResponseEntity<String> testFetchTranscript(@RequestParam String url) {
        log.info("🧪 Testing transcript fetch for: {}", url);
        try {
            String transcript = youtubeTranscriptService.extractTranscript(url);

            if (transcript == null || transcript.length() < 100) {
                return ResponseEntity.badRequest().body("❌ Transcript too short or invalid (minimum 100 characters)");
            }

            String preview = transcript.substring(0, Math.min(500, transcript.length()));
            return ResponseEntity.ok("✅ Transcript fetched successfully\n\n" +
                    "Length: " + transcript.length() + " characters\n\n" +
                    "Preview:\n" + preview + "...");

        } catch (Exception e) {
            log.error("❌ Error fetching transcript: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Test Mermaid generation
     */
    @PostMapping("/api/mindmap/test/generate-mermaid")
    public ResponseEntity<String> testGenerateMermaid(
            @RequestParam String transcript,
            @RequestParam String subject) {

        log.info("🧪 Testing Mermaid generation for subject: {}", subject);
        try {
            String mermaidCode = mermaidGraphService.generateMermaidGraph(transcript, subject);

            if (mermaidCode == null || mermaidCode.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Failed to generate Mermaid code");
            }

            return ResponseEntity.ok("✅ Mermaid code generated successfully\n\n" + mermaidCode);

        } catch (Exception e) {
            log.error("❌ Error generating Mermaid code: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * System status endpoint
     */
    @GetMapping("/api/mindmap/test/status")
    public ResponseEntity<Map<String, Object>> testStatus() {
        log.info("📊 System status check requested");
        Map<String, Object> status = new HashMap<>();
        status.put("service", "Mind Map Generation System");
        status.put("version", "3.0.0-NotebookLM-RealYouTube");
        status.put("status", "RUNNING");
        status.put("architecture", "3-Pipeline (Ingestion → Synthesis → Rendering)");
        status.put("dataSource", "✅ REAL YouTube Transcripts (NO mock data in normal operation)");
        status.put("components", new String[]{
                "✅ StudyAssistantService (JSON Mind Maps)",
                "✅ YouTubeTranscriptService (REAL Transcript Fetching)",
                "✅ MermaidGraphService (Mermaid Generation)",
                "✅ MindMapController (Orchestration)"
        });
        status.put("endpoints", new String[]{
                "GET /api/mindmap/health - Health check",
                "POST /api/mindmap/generate-from-youtube - Main endpoint",
                "POST /api/mindmap/test/extract-id - Test video ID extraction",
                "POST /api/mindmap/test/fetch-transcript - Test transcript fetching",
                "POST /api/mindmap/test/generate-mermaid - Test Mermaid generation"
        });
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return ResponseEntity.ok(status);
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Extract student ID from authorization token
     * For now, returns a default value (implement JWT parsing as needed)
     */
    private Long extractStudentIdFromToken(String token) {
        if (token != null && !token.isEmpty()) {
            log.debug("Token provided: {}", token.substring(0, Math.min(20, token.length())) + "...");
        }
        return 1L; // Default student ID for now
    }
}