package com.rfid.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for generating mind maps from YouTube videos
 * This is the entry point for the 3-Pipeline system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateMindMapFromYouTubeRequest {

    @NotBlank(message = "YouTube URL cannot be blank")
    private String youtubeUrl;

    @NotBlank(message = "Subject cannot be blank")
    private String subject;

    // Validation helper
    public boolean isValid() {
        return youtubeUrl != null && !youtubeUrl.trim().isEmpty()
                && subject != null && !subject.trim().isEmpty();
    }
}