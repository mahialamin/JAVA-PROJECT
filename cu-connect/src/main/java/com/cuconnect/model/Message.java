package com.cuconnect.model;

import java.sql.Timestamp;

/**
 * Represents a single group chat message in a section chat room.
 * Faculty can ONLY view — only students can set sender via ChatService.
 */
public class Message {
    private int id;
    private int senderId;
    private String senderName;   // Populated by JOIN for display
    private String senderRole;   // "STUDENT" or "FACULTY"
    private int sectionId;
    private String content;
    private Timestamp sentAt;

    public Message() {}

    public Message(int id, int senderId, String senderName, String senderRole,
                   int sectionId, String content, Timestamp sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.sectionId = sectionId;
        this.content = content;
        this.sentAt = sentAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }
}
