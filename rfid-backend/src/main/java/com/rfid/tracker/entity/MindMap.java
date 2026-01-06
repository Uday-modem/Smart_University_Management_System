package com.rfid.tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "mind_maps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MindMap {

    @Id
    @Column(name = "mind_map_id")
    private String mindMapId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "video_url", nullable = false, columnDefinition = "LONGTEXT")
    private String videoUrl;

    @Column(name = "video_title", nullable = false)
    private String videoTitle;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(name = "mind_map_json", columnDefinition = "LONGTEXT")
    private String mindMapJson;

    @Column(name = "key_points", columnDefinition = "LONGTEXT")
    private String keyPoints;

    @Column(name = "summary", columnDefinition = "LONGTEXT")
    private String summary;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}