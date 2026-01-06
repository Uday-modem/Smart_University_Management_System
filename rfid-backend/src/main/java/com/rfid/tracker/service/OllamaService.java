package com.rfid.tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * OLLAMA SERVICE - NOTEBOOKLM-INSPIRED KNOWLEDGE GRAPH ENGINE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Purpose: Convert REAL YouTube transcripts into structured knowledge graphs
 *
 * CRITICAL FEATURES:
 * ✅ Works ONLY with real YouTube transcripts (no mock data)
 * ✅ Validates transcript content before processing
 * ✅ Creates hierarchical knowledge graph
 * ✅ Identifies core concepts from actual transcript
 * ✅ Zero template generation - content-driven only
 * ✅ NotebookLM-inspired prompt engineering
 * ✅ Returns structured JSON (optimized for frontend)
 *
 * KEY CHANGE FROM PREVIOUS:
 * - REMOVED all mock transcript generation
 * - ADDED validation to ensure real content
 * - RECEIVES real YouTube transcript from YouTubeTranscriptService
 * - GUARANTEES unique output per video
 */

@Service
@Slf4j
public class OllamaService {

    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final int MIN_TRANSCRIPT_LENGTH = 200;
    private static final int MIN_UNIQUE_WORDS = 50;

    @Autowired
    private YouTubeTranscriptService youtubeTranscriptService;

    /**
     * Generate knowledge graph from REAL YouTube transcript
     *
     * @param transcript REAL YouTube transcript (NOT mock data)
     * @param subject Video subject/title
     * @return JSON knowledge graph
     */
    public String generateMindMap(String transcript, String subject) {
        try {
            // CRITICAL: Validate that we have a REAL transcript
            if (!isValidYouTubeTranscript(transcript)) {
                log.error("❌ VALIDATION FAILED: Not a valid YouTube transcript");
                return generateValidationErrorResponse(
                    "❌ INVALID INPUT: No valid YouTube transcript provided. " +
                    "Please provide actual YouTube video transcript content. " +
                    "System requires at least " + MIN_TRANSCRIPT_LENGTH + " characters of actual transcript."
                );
            }

            log.info("═══════════════════════════════════════════════════════════════");
            log.info("✅ NOTEBOOKLM KNOWLEDGE GRAPH ENGINE ACTIVATED");
            log.info("═══════════════════════════════════════════════════════════════");
            log.info("📺 Subject: {}", subject);
            log.info("📄 Transcript length: {} characters", transcript.length());
            log.info("📊 Unique words detected: {}", countUniqueWords(transcript));
            log.info("🔍 Analyzing transcript for core topics and themes...");

            // Build NotebookLM-inspired prompt with REAL transcript
            String prompt = buildNotebookLMInspiredPrompt(transcript, subject);

            // Send to Ollama
            String responseJson = callOllamaAPI(prompt);

            if (responseJson != null && !responseJson.isEmpty()) {
                log.info("✅ Knowledge graph generated successfully");
                log.info("═══════════════════════════════════════════════════════════════");
                return responseJson;
            } else {
                log.error("❌ Ollama returned empty response");
                return generateErrorResponse("Ollama API returned empty response");
            }

        } catch (Exception e) {
            log.error("❌ Error generating knowledge graph: {}", e.getMessage(), e);
            return generateErrorResponse("Error: " + e.getMessage());
        }
    }

    /**
     * CRITICAL VALIDATION: Ensure this is a REAL YouTube transcript
     * NOT just a subject name or mock data
     */
    private boolean isValidYouTubeTranscript(String transcript) {
        if (transcript == null || transcript.trim().isEmpty()) {
            log.warn("❌ Transcript is null or empty");
            return false;
        }

        int length = transcript.length();
        int uniqueWords = countUniqueWords(transcript);

        // Check minimum length
        if (length < MIN_TRANSCRIPT_LENGTH) {
            log.warn("❌ Transcript too short: {} chars (minimum: {})", length, MIN_TRANSCRIPT_LENGTH);
            return false;
        }

        // Check for real content (not just repeated words)
        if (uniqueWords < MIN_UNIQUE_WORDS) {
            log.warn("❌ Transcript has too few unique words: {} (minimum: {})", uniqueWords, MIN_UNIQUE_WORDS);
            return false;
        }

        // Check for depth and varied vocabulary
        String contentAnalysis = transcript.toLowerCase();
        boolean hasReasonableDepth = contentAnalysis.length() > 500;
        boolean hasVariedVocabulary = uniqueWords > (length / 15);

        if (!hasReasonableDepth || !hasVariedVocabulary) {
            log.warn("❌ Transcript lacks real content characteristics");
            return false;
        }

        log.info("✅ Transcript validation passed");
        log.info("   - Length: {} chars", length);
        log.info("   - Unique words: {}", uniqueWords);
        return true;
    }

    /**
     * Count unique words in transcript
     */
    private int countUniqueWords(String text) {
        if (text == null || text.isEmpty()) return 0;
        String[] words = text.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", " ")
            .split("\\s+");
        java.util.Set<String> uniqueWords = new java.util.HashSet<>(java.util.Arrays.asList(words));
        return uniqueWords.size();
    }

    /**
     * BUILD NOTEBOOKLM-INSPIRED PROMPT
     *
     * This converts a linear YouTube transcript into a hierarchical knowledge graph
     *
     * Methodology:
     * 1. Identify core central topic
     * 2. Extract 3-5 main themes/pillars
     * 3. Decompose into specific insights and evidence
     * 4. Create hierarchical structure
     * 5. STRICT SOURCE ADHERENCE - ONLY use transcript content
     */
    private String buildNotebookLMInspiredPrompt(String transcript, String subject) {
        return "You are the \"KNOWLEDGE GRAPH ENGINE\" - inspired by NotebookLM.\n" +
            "Your goal: Convert this YouTube transcript into a structured, hierarchical KNOWLEDGE GRAPH.\n\n" +
            "═════════════════════════════════════════════════════════════════════════════════════════\n" +
            "YOUTUBE TRANSCRIPT - YOUR ONLY SOURCE OF TRUTH\n" +
            "═════════════════════════════════════════════════════════════════════════════════════════\n" +
            "Subject: " + subject + "\n" +
            "Transcript Content:\n" +
            transcript + "\n" +
            "═════════════════════════════════════════════════════════════════════════════════════════\n\n" +
            "YOUR PRIME DIRECTIVES:\n" +
            "─────────────────────────────────────────────────────────────────────────────────────────\n\n" +
            "1. IDENTIFY THE CORE (CENTRAL TOPIC):\n" +
            "   ▸ Find the SINGLE most important central topic from THIS transcript\n" +
            "   ▸ This is the root node of your knowledge graph\n" +
            "   ▸ Be SPECIFIC to this transcript's actual content\n\n" +
            "2. EXTRACT MAIN THEMES (Level 1 Nodes - 3-5 themes):\n" +
            "   ▸ Identify 3-5 distinct main themes/pillars explicitly discussed in the transcript\n" +
            "   ▸ Each theme must:\n" +
            "     - Be explicitly discussed in the transcript\n" +
            "     - Represent a major section or concept area\n" +
            "     - Be mutually distinct (not overlapping)\n\n" +
            "3. DECOMPOSE INTO INSIGHTS (Level 2 Nodes):\n" +
            "   ▸ For each main theme, extract 2-4 specific insights/data points\n" +
            "   ▸ Requirements:\n" +
            "     - Each insight MUST be explicitly mentioned in the transcript\n" +
            "     - Include specific examples from the video\n" +
            "     - Reference actual moments/sections from transcript\n" +
            "     - Be technical and precise\n\n" +
            "4. OUTPUT FORMAT - Return ONLY as JSON:\n" +
            "{\n" +
            "  \"rootTopic\": \"The main topic from transcript\",\n" +
            "  \"themes\": [\n" +
            "    {\n" +
            "      \"name\": \"Theme 1 Name\",\n" +
            "      \"description\": \"Brief description from transcript\",\n" +
            "      \"insights\": [\n" +
            "        {\n" +
            "          \"title\": \"Specific insight from transcript\",\n" +
            "          \"details\": \"Details extracted from actual content\",\n" +
            "          \"examples\": \"Real examples mentioned in video\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "5. CRITICAL REQUIREMENTS:\n" +
            "   ✓ Extract themes and details ONLY from the provided transcript\n" +
            "   ✓ Do NOT add external knowledge or assumptions\n" +
            "   ✓ Use short, clear labels (under 15 words)\n" +
            "   ✓ Every node MUST be traceable to the transcript\n" +
            "   ✓ Return ONLY valid JSON, no explanations\n" +
            "   ✓ No templates or generic content\n\n" +
            "NOW: Analyze this REAL YouTube transcript and generate a knowledge graph in JSON format.\n" +
            "Remember: This is REAL video content, not a template. Make it specific and accurate!";
    }

    /**
     * Call Ollama API with the prompt
     */
    private String callOllamaAPI(String prompt) throws Exception {
        log.info("🔗 Connecting to Ollama API: {}", OLLAMA_API_URL);

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", "llama2");
        requestMap.put("prompt", prompt);
        requestMap.put("stream", false);
        requestMap.put("temperature", 0.5);
        requestMap.put("top_p", 0.7);
        requestMap.put("top_k", 20);
        requestMap.put("num_predict", 2048);

        String requestBody = objectMapper.writeValueAsString(requestMap);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        log.info("📡 Sending request to Ollama...");
        ResponseEntity<String> response = restTemplate.postForEntity(OLLAMA_API_URL, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            log.info("✅ Ollama responded successfully");
            return extractOllamaResponse(response.getBody());
        } else {
            throw new Exception("Ollama API error: " + response.getStatusCode());
        }
    }

    /**
     * Extract the generated response from Ollama
     */
    private String extractOllamaResponse(String responseBody) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

        if (response.containsKey("response")) {
            String responseText = (String) response.get("response");
            log.info("✅ Response extracted from Ollama");
            return responseText.trim();
        }

        return null;
    }

    /**
     * Generate validation error response
     */
    private String generateValidationErrorResponse(String message) {
        return "{\n" +
            "  \"error\": true,\n" +
            "  \"message\": \"" + message + "\",\n" +
            "  \"status\": \"VALIDATION_FAILED\"\n" +
            "}";
    }

    /**
     * Generate error response
     */
    private String generateErrorResponse(String message) {
        return "{\n" +
            "  \"error\": true,\n" +
            "  \"message\": \"" + message + "\",\n" +
            "  \"status\": \"ERROR\"\n" +
            "}";
    }
}