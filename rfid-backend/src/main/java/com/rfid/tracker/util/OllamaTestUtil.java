package com.rfid.tracker.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to test Ollama connectivity and functionality
 * Run this as a standalone app or unit test to verify Ollama is working
 */
@Slf4j
public class OllamaTestUtil {

    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";

    public static void main(String[] args) {
        testOllamaConnection();
        testMindMapGeneration();
    }

    /**
     * Test if Ollama server is running and responding
     */
    public static boolean testOllamaConnection() {
        try {
            log.info("Testing Ollama connection...");
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();

            // Create a simple test prompt
            String testPrompt = "Create a simple JSON object with keys 'title' and 'branches' for a mind map about programming. Return ONLY valid JSON.";
            String requestBody = String.format(
                "{\"model\": \"llama2\", \"prompt\": %s, \"stream\": false}",
                objectMapper.writeValueAsString(testPrompt)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.info("Sending request to Ollama at: {}", OLLAMA_API_URL);

            ResponseEntity<String> response = restTemplate.postForEntity(
                OLLAMA_API_URL,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Ollama connection successful!");
                log.info("Response: {}", response.getBody());
                return true;
            } else {
                log.error("❌ Ollama returned error status: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("❌ Ollama connection failed: {}", e.getMessage());
            log.error("Make sure Ollama is running at localhost:11434");
            return false;
        }
    }

    /**
     * Test mind map generation with sample transcript
     */
    public static void testMindMapGeneration() {
        try {
            log.info("Testing mind map generation...");
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();

            String testTranscript = "In this video we discuss photosynthesis. Photosynthesis is the process by which plants convert light energy " +
                    "into chemical energy. It occurs in two main stages: light reactions and the Calvin cycle. Light reactions happen in the thylakoid " +
                    "and produce ATP and NADPH. The Calvin cycle happens in the stroma and produces glucose.";

            String prompt = String.format(
                "You are an expert educational content analyzer. Create a mind map JSON for this Biology transcript:\n\n" +
                "%s\n\n" +
                "Return ONLY a valid JSON object with 'title' and 'branches' array. No markdown, no code blocks, pure JSON only.",
                testTranscript
            );

            String requestBody = String.format(
                "{\"model\": \"llama2\", \"prompt\": %s, \"stream\": false}",
                objectMapper.writeValueAsString(prompt)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.info("Generating mind map...");
            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.postForEntity(
                OLLAMA_API_URL,
                entity,
                String.class
            );

            long duration = System.currentTimeMillis() - startTime;

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                int previewLength = Math.min(200, responseBody != null ? responseBody.length() : 0);
                log.info("✅ Mind map generation successful! ({}ms)", duration);
                log.info("Response preview: {}", responseBody != null ? responseBody.substring(0, previewLength) : "null");
            } else {
                log.error("❌ Mind map generation failed: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Mind map generation error: {}", e.getMessage(), e);
        }
    }
}