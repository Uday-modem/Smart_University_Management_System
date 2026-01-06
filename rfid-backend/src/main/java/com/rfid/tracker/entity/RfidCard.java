package com.rfid.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rfid_cards")
public class RfidCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_uid", nullable = false, unique = true, length = 20)
    private String cardUid;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "user_id", nullable = false)
    private Long userId; // This links to the Student's primary ID

    @Column(name = "issued_date")
    private LocalDateTime issuedDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum UserType {
        STUDENT, STAFF
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- Minimal Getters and Setters for the Controller ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardUid() { return cardUid; }
    public void setCardUid(String cardUid) { this.cardUid = cardUid; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
