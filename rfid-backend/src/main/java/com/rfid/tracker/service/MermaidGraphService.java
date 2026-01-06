package com.rfid.tracker.service;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MERMAID GRAPH SERVICE - THE "NOTEBOOKLM" BRAIN
 * ═══════════════════════════════════════════════════════════════════════════
 * * Purpose: Convert RAW transcript text into structured knowledge graphs.
 * * CORE PIPELINE:
 * 1. RECEIVE: Accepts raw transcript text.
 * 2. TRUNCATE: Limits to 25,000 chars.
 * 3. ANALYZE: Sends to Ollama (Smart Switching: Localhost -> Docker Host).
 * 4. SYNTHESIZE: Converts JSON response into Mermaid.js graph syntax.
 */
@Service
@Slf4j
public class MermaidGraphService {
    
    // API CONFIGURATIONS
    private static final String URL_LOCAL = "http://localhost:11434/api/generate";
    private static final String URL_DOCKER = "http://host.docker.internal:11434/api/generate";
    
    private static final String AI_MODEL = "llama2"; // Ensure you have: 'ollama pull llama2'
    private static final int MAX_TRANSCRIPT_CHARS = 25000;  

    /**
     * MAIN METHOD: Generate Mermaid mind map from Transcript Text
     */
    public String generateMermaidGraph(String transcriptBody, String subject) {
        log.info("═════════════════════════════════════════════════════════════");
        log.info("🧠 MIND MAP GENERATION STARTING for: {}", subject);
        log.info("═════════════════════════════════════════════════════════════");

        try {
            // VALIDATION: Ensure we have text
            if (transcriptBody == null || transcriptBody.trim().isEmpty()) {
                log.error("❌ Transcript is empty! Cannot generate map.");
                return generateFallbackMermaidDiagram(subject);
            }

            // STEP 1: TRUNCATE (Prevent Context Overflow)
            String processedTranscript = truncateTranscript(transcriptBody);
            
            // STEP 2: Extract Key Context
            String[] keyPhrases = extractKeyPhrases(processedTranscript);

            // STEP 3: Send to Ollama (With Smart Failover)
            String analysisJson = askOllamaWithFailover(processedTranscript, subject, keyPhrases);
            
            if (analysisJson == null) {
                return generateFallbackMermaidDiagram(subject);
            }

            // STEP 4: Build the Mermaid Code from JSON
            String mermaidCode = buildMermaidFromAnalysis(analysisJson, processedTranscript.length());
            
            if (mermaidCode == null) {
                return generateFallbackMermaidDiagram(subject);
            }

            return mermaidCode;

        } catch (Exception e) {
            log.error("🔥 PIPELINE FAILED: {}", e.getMessage());
            e.printStackTrace();
            return generateFallbackMermaidDiagram(subject);
        }
    }

    private String truncateTranscript(String transcript) {
        if (transcript.length() > MAX_TRANSCRIPT_CHARS) {
            log.info("✂️ Truncating transcript to {} chars.", MAX_TRANSCRIPT_CHARS);
            return transcript.substring(0, MAX_TRANSCRIPT_CHARS) + "... [Truncated]";
        }
        return transcript;
    }

    private String[] extractKeyPhrases(String transcript) {
        String[] sentences = transcript.split("[.!?]");
        List<String> phrases = new ArrayList<>();
        for (String s : sentences) {
            s = s.trim();
            if (s.length() > 20 && s.length() < 150) {
                phrases.add(s.replaceAll("\\s+", " "));
                if (phrases.size() >= 15) break;
            }
        }
        return phrases.toArray(new String[0]);
    }

    /**
     * SMART OLLAMA CONNECTION
     * Tries localhost first. If that fails (Docker environment), tries host.docker.internal.
     */
    private String askOllamaWithFailover(String transcript, String subject, String[] keyPhrases) {
        // First try: Localhost
        String response = askOllama(URL_LOCAL, transcript, subject, keyPhrases, false);
        
        if (response != null) return response;

        // Second try: Docker Host
        log.warn("⚠️ Localhost connection failed. Switching to Docker Host networking...");
        response = askOllama(URL_DOCKER, transcript, subject, keyPhrases, true);

        if (response == null) {
             log.error("❌ ❌ ALL OLLAMA CONNECTIONS FAILED ❌ ❌");
             log.error("👉 Please run: 'ollama serve' in a terminal.");
        }
        return response;
    }

    private String askOllama(String apiUrl, String transcript, String subject, String[] keyPhrases, boolean isRetry) {
        log.info("📡 Connecting to Ollama at: {} (Model: {})", apiUrl, AI_MODEL);
        
        try {
            // 1. Build Prompt
            StringBuilder context = new StringBuilder();
            for(String p : keyPhrases) context.append("- ").append(p).append("\n");

            String prompt = "You are an expert Knowledge Graph Engineer.\n" +
                    "Analyze the following VIDEO TRANSCRIPT and extract the hierarchical structure.\n\n" +
                    "VIDEO TITLE: " + subject + "\n\n" +
                    "TRANSCRIPT EXCERPT:\n" + transcript + "\n\n" +
                    "--- INSTRUCTIONS ---\n" +
                    "1. Identify the MAIN TOPIC (Root Node).\n" +
                    "2. Extract ALL key chapters/concepts as Subtopics.\n" +
                    "3. For EACH subtopic, extract 3-5 specific details (leaf nodes).\n" +
                    "4. IGNORE conversational filler ('Welcome back', 'Subscribe').\n" +
                    "5. OUTPUT FORMAT: Return ONLY valid JSON. No markdown.\n\n" +
                    "JSON STRUCTURE:\n" +
                    "{\n" +
                    "  \"mainTopic\": \"Title of Video\",\n" +
                    "  \"subtopics\": [\n" +
                    "    { \"name\": \"Concept 1\", \"details\": [\"Detail A\", \"Detail B\"] },\n" +
                    "    { \"name\": \"Concept 2\", \"details\": [\"Detail C\", \"Detail D\"] }\n" +
                    "  ]\n" +
                    "}";

            // 2. Configure Timeouts
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(3000); // Fast fail on connection (3s)
            factory.setReadTimeout(120000);  // Long read for AI thinking (2m)

            RestTemplate restTemplate = new RestTemplate(factory);
            
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model", AI_MODEL);
            requestMap.put("prompt", prompt);
            requestMap.put("stream", false);
            requestMap.put("temperature", 0.2); 
            requestMap.put("num_predict", 2048); 

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);

            // 3. Send Request
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            
            if (response.getBody() == null) return null;

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response.getBody(), Map.class);
            return extractJsonString((String) responseMap.get("response"));

        } catch (ResourceAccessException e) {
            log.warn("⚠️ Connection refused to {}", apiUrl);
            return null; // Triggers failover
        } catch (Exception e) {
            log.error("❌ Ollama Error: {}", e.getMessage());
            return null;
        }
    }

    private String extractJsonString(String text) {
        if (text == null) return null;
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    private String buildMermaidFromAnalysis(String json, int transcriptLength) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(json, Map.class);

            String rootTopic = (String) data.get("mainTopic");
            List<Map<String, Object>> subtopics = (List<Map<String, Object>>) data.get("subtopics");
            
            int maxTopics = calculateMaxTopics(transcriptLength);
            int topicsToShow = Math.min(subtopics.size(), maxTopics);
            
            log.info("📊 GENERATING GRAPH: Showing {}/{} topics", topicsToShow, subtopics.size());

            StringBuilder sb = new StringBuilder();
            sb.append("graph TD\n");
            
            rootTopic = sanitize(rootTopic);
            sb.append(String.format("  Root[\"%s\"]\n", rootTopic));

            int branchIndex = 1;
            for (int i = 0; i < topicsToShow; i++) {
                Map<String, Object> topic = subtopics.get(i);
                String topicName = sanitize((String) topic.get("name"));
                String branchId = "B" + branchIndex;
                sb.append(String.format("  Root --> %s(\"%s\")\n", branchId, topicName));

                List<String> details = (List<String>) topic.get("details");
                if (details != null) {
                    for (int j = 0; j < Math.min(details.size(), 5); j++) {
                        String detail = sanitize(details.get(j));
                        String leafId = branchId + "_L" + (j + 1);
                        sb.append(String.format("  %s --> %s[\"%s\"]\n", branchId, leafId, detail));
                    }
                }
                branchIndex++;
            }

            sb.append("\n  classDef root fill:#ff6b6b,stroke:#c92a2a,color:#fff,stroke-width:2px\n");
            sb.append("  classDef branch fill:#4ecdc4,stroke:#1ba0a0,color:#fff,stroke-width:2px\n");
            sb.append("  classDef leaf fill:#45b7d1,stroke:#2c7a99,color:#fff\n");
            sb.append("  class Root root\n");
            
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ Error parsing JSON to Mermaid: {}", e.getMessage());
            return null;
        }
    }

    private int calculateMaxTopics(int len) {
        if (len < 5000) return 4;
        if (len < 12000) return 8;
        if (len < 20000) return 15;
        return Integer.MAX_VALUE;
    }

    private String sanitize(String text) {
        if (text == null) return "Unknown";
        return text.replaceAll("[\"'\\[\\]\\(\\)]", "").trim();
    }

    private String generateFallbackMermaidDiagram(String subject) {
        log.warn("⚠️  Generating fallback diagram for: {}", subject);
        return "graph TD\n" +
                "  A[\"" + subject + "\"]\n" +
                "  A --> B(\"Concept 1\")\n" +
                "  A --> C(\"Concept 2\")\n" +
                "  B --> B1[\"Detail A\"]\n" +
                "  C --> C1[\"Detail B\"]\n" +
                "  classDef root fill:#ff6b6b,stroke:#c92a2a,color:#fff\n" +
                "  class A root";
    }
}