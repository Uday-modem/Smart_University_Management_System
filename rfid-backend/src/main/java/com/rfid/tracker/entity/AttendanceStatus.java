package com.rfid.tracker.entity;

public enum AttendanceStatus {
    PRESENT("Present", "P"),
    LATE("Late", "L"),
    ABSENT("Absent", "A"),
    HALF_DAY("Half Day", "H");  // Changed semicolon after ABSENT to comma, semicolon only at the end
    
    private final String displayName;
    private final String shortCode;

    AttendanceStatus(String displayName, String shortCode) {
        this.displayName = displayName;
        this.shortCode = shortCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortCode() {
        return shortCode;
    }

    public static AttendanceStatus fromShortCode(String shortCode) {
        for (AttendanceStatus status : values()) {
            if (status.shortCode.equalsIgnoreCase(shortCode)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown short code: " + shortCode);
    }
    
    public static AttendanceStatus fromDisplayName(String displayName) {
        for (AttendanceStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown display name: " + displayName);
    }
}
