package com.rfid.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for mind map creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMindMapResponse {

    private boolean success;
    private String message;
    private String mindMapId;
}
