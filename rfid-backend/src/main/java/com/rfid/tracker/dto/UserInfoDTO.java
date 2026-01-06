package com.rfid.tracker.dto;

public class UserInfoDTO {
    private String userId;
    private String userIdentifier;
    private String userName;
    private String userType;
    private String sectionId;
    private String branch;
    private String email;

    public UserInfoDTO() {}

    public UserInfoDTO(String userId, String userIdentifier, String userName, String userType) {
        this.userId = userId;
        this.userIdentifier = userIdentifier;
        this.userName = userName;
        this.userType = userType;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserIdentifier() { return userIdentifier; }
    public void setUserIdentifier(String userIdentifier) { this.userIdentifier = userIdentifier; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
