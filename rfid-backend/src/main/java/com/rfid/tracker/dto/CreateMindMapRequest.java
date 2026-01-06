package com.rfid.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMindMapRequest {
    
    @JsonProperty("youtubeUrl")
    private String youtubeUrl;  // ✅ FIXED: Was 'videoUrl', now 'youtubeUrl' to match frontend
    
    @JsonProperty("subject")
    private String subject;  // Subject/topic of the video
}