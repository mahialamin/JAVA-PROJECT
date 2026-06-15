package com.cuconnect.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Represents a Notice posted by a Faculty member.
 * Can be general (sectionId = null) or section-specific.
 * Supports pinning and expiry dates.
 * The readCount and totalStudents fields are populated by the service layer for analytics.
 */
public class Notice {
    private int id;
    private String title;
    private String content;
    private int createdBy;
    private String creatorName;   // Populated by JOIN query for display
    private Integer sectionId;   // NULL = general notice visible to all
    private boolean pinned;
    private Date expiryDate;     // NULL = never expires
    private Timestamp createdAt;

    // Analytics fields (not stored in DB — computed by service)
    private int readCount;
    private int totalStudents;

    public Notice() {}

    public Notice(int id, String title, String content, int createdBy,
                  String creatorName, Integer sectionId, boolean pinned,
                  Date expiryDate, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.creatorName = creatorName;
        this.sectionId = sectionId;
        this.pinned = pinned;
        this.expiryDate = expiryDate;
        this.createdAt = createdAt;
    }

    // Convenience: check if notice is expired
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return expiryDate.before(new Date(System.currentTimeMillis()));
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Integer getSectionId() { return sectionId; }
    public void setSectionId(Integer sectionId) { this.sectionId = sectionId; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getReadCount() { return readCount; }
    public void setReadCount(int readCount) { this.readCount = readCount; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
}
