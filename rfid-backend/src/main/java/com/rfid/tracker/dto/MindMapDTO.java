package com.rfid.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MindMapDTO {

    private String mindMapId;
    private Long studentId;
    private String videoUrl;
    private String videoTitle;
    private String subject;
    private String status;  // PENDING, PROCESSING, COMPLETED, FAILED
    private String mindMapJson;
    private String keyPoints;
    private String summary;
    private Boolean isFavorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
