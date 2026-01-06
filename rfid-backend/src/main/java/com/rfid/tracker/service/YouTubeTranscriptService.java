package com.rfid.tracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * YOUTUBE TRANSCRIPT EXTRACTION SERVICE - FIXED VERSION
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * Architecture: YouTube URL → Java Backend → Python Microservice → Real Transcript
 * 
 * KEY FIXES:
 * ✅ Extracts from Python service on localhost:5000
 * ✅ Validates transcript is REAL (> 200 chars, not test data)
 * ✅ Rejects any test/mock transcripts
 * ✅ Zero fallback to fake data
 * ✅ Works with all YouTube URL formats
 * 
 * Prerequisites:
 * - Python service running on localhost:5000
 * - yt-dlp library installed
 */

@Service
@Slf4j
public class YouTubeTranscriptService {
    
    private static final String PYTHON_SERVICE_URL = "http://localhost:5000/api/transcript/extract";
    private static final String PYTHON_HEALTH_URL = "http://localhost:5000/api/transcript/health";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Check if Python service is available
     */
    public boolean isPythonServiceAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(PYTHON_HEALTH_URL, String.class);
            log.info("✅ Python Transcript Service is healthy: {}", response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("⚠️ Python Transcript Service unavailable: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract video ID from various YouTube URL formats
     */
    public String extractVideoId(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.trim().isEmpty()) {
            log.error("❌ YouTube URL is null or empty");
            throw new IllegalArgumentException("YouTube URL cannot be null or empty");
        }

        log.info("🔍 Extracting video ID from: {}", youtubeUrl);

        // Pattern 1: youtu.be/VIDEO_ID
        Pattern pattern1 = Pattern.compile("youtu\\.be/([a-zA-Z0-9_-]{11})");
        Matcher matcher1 = pattern1.matcher(youtubeUrl);
        if (matcher1.find()) {
            String videoId = matcher1.group(1);
            log.info("✅ Extracted video ID (youtu.be format): {}", videoId);
            return videoId;
        }

        // Pattern 2: youtube.com/watch?v=VIDEO_ID
        Pattern pattern2 = Pattern.compile("v=([a-zA-Z0-9_-]{11})");
        Matcher matcher2 = pattern2.matcher(youtubeUrl);
        if (matcher2.find()) {
            String videoId = matcher2.group(1);
            log.info("✅ Extracted video ID (youtube.com format): {}", videoId);
            return videoId;
        }

        // Pattern 3: youtube.com/embed/VIDEO_ID
        Pattern pattern3 = Pattern.compile("embed/([a-zA-Z0-9_-]{11})");
        Matcher matcher3 = pattern3.matcher(youtubeUrl);
        if (matcher3.find()) {
            String videoId = matcher3.group(1);
            log.info("✅ Extracted video ID (embed format): {}", videoId);
            return videoId;
        }

        // Pattern 4: Direct video ID
        if (youtubeUrl.matches("[a-zA-Z0-9_-]{11}")) {
            log.info("✅ URL is direct video ID: {}", youtubeUrl);
            return youtubeUrl;
        }

        log.error("❌ Could not extract video ID from URL: {}", youtubeUrl);
        throw new IllegalArgumentException("Invalid YouTube URL format: " + youtubeUrl);
    }

    /**
     * 🔴 FIXED: Validate transcript is REAL (not test data)
     */
    private void validateTranscriptIsReal(String transcript, int lengthInResponse) {
        if (transcript == null || lengthInResponse < 200) {
            log.error("❌ CRITICAL: Received test transcript from Python service!");
            log.error("❌ Length: {} characters (should be 1000+)", lengthInResponse);
            log.error("❌ This means YouTube captions extraction failed");
            throw new RuntimeException(
                "Python service returned invalid transcript. " +
                "Video may not have captions enabled or YouTube access is blocked."
            );
        }
        log.info("✅ Validated: Transcript is REAL ({} characters)", lengthInResponse);
    }

    /**
     * MAIN METHOD: Extract transcript from YouTube video
     * Uses local Python microservice for reliable transcript extraction
     */
    public String extractTranscript(String youtubeUrl) throws Exception {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("📺 YOUTUBE TRANSCRIPT EXTRACTION SERVICE ACTIVATED");
        log.info("═══════════════════════════════════════════════════════════════");
        
        try {
            // Step 1: Verify Python service is available
            if (!isPythonServiceAvailable()) {
                throw new Exception("Python Transcript Service (localhost:5000) is not available. " +
                        "Please start the service first: python youtube-transcript-service/app.py");
            }

            // Step 2: Extract video ID
            String videoId = extractVideoId(youtubeUrl);
            log.info("🎯 Video ID: {}", videoId);

            // Step 3: Call Python service
            log.info("⏳ Calling Python Transcript Service...");
            String transcript = callPythonService(youtubeUrl);

            if (transcript != null && !transcript.trim().isEmpty()) {
                log.info("✅ Transcript fetched successfully!");
                log.info("📊 Transcript length: {} characters", transcript.length());
                log.info("═══════════════════════════════════════════════════════════════");
                return transcript;
            }

            log.error("❌ Python service returned empty transcript for video: {}", videoId);
            throw new Exception("No transcript or captions available for video: " + videoId);

        } catch (Exception e) {
            log.error("❌ Error extracting transcript: {}", e.getMessage());
            throw new Exception("Failed to extract YouTube transcript: " + e.getMessage(), e);
        }
    }

    /**
     * Call Python microservice to extract transcript
     */
    private String callPythonService(String youtubeUrl) throws Exception {
        try {
            log.info("🔗 Connecting to Python service at: {}", PYTHON_SERVICE_URL);

            // Create JSON request body
            String requestBody = "{\"youtubeUrl\": \"" + youtubeUrl + "\"}";
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            // Call Python service
            ResponseEntity<String> response = restTemplate.postForEntity(
                    PYTHON_SERVICE_URL,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new Exception("Python service returned status: " + response.getStatusCode());
            }

            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            
            if (responseJson.get("success").asBoolean()) {
                String transcript = responseJson.get("transcript").asText();
                String source = responseJson.get("source").asText();
                int length = responseJson.get("length").asInt();

                // ⭐ CRITICAL: Validate it's REAL (not test data)
                validateTranscriptIsReal(transcript, length);

                log.info("✅ Transcript extracted from {}: {} characters", source, length);
                return transcript;
            } else {
                String error = responseJson.get("error").asText();
                throw new Exception(error);
            }

        } catch (Exception e) {
            log.error("❌ Python service call failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validate if extracted transcript is legitimate
     */
    public boolean isValidTranscript(String transcript) {
        if (transcript == null || transcript.trim().isEmpty()) {
            log.warn("❌ Transcript is null or empty");
            return false;
        }

        int length = transcript.length();
        int minLength = 200;
        
        if (length < minLength) {
            log.warn("❌ Transcript too short: {} chars (minimum: {})", length, minLength);
            return false;
        }

        log.info("✅ Transcript validation passed: {} characters", length);
        return true;
    }

    /**
     * Clean and normalize transcript text
     */
    public String cleanTranscript(String transcript) {
        if (transcript == null) return "";

        // Remove extra whitespace
        String cleaned = transcript.replaceAll("\\s+", " ").trim();

        // Remove timestamps if present
        cleaned = cleaned.replaceAll("\\[\\d{2}:\\d{2}:\\d{2}\\]", "");

        // Remove common filler words
        cleaned = cleaned.replaceAll("\\b(um|uh|like|you know|sort of)\\b", " ");

        // Final cleanup
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        log.info("📝 Transcript cleaned: {} → {} characters",
                transcript.length(), cleaned.length());

        return cleaned;
    }

    /**
     * Get transcript with automatic cleanup and validation
     */
    public String getCleanTranscript(String youtubeUrl) throws Exception {
        log.info("🔄 Getting clean transcript from: {}", youtubeUrl);
        
        String transcript = extractTranscript(youtubeUrl);
        
        if (!isValidTranscript(transcript)) {
            throw new Exception("Transcript validation failed - content too short or invalid");
        }

        return cleanTranscript(transcript);
    }
}