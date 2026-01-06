package com.rfid.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for mind map generation
 * Contains the complete Mermaid.js graph code + metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MindMapMermaidResponse {

    private boolean success;
    private String youtubeUrl;
    private String videoId;
    private String subject;
    private int transcriptLength;
    private String mermaidCode;
    private String message;
    private String errorDescription;
    private String timestamp;
}
