package com.cuconnect.model;

import java.sql.Timestamp;

/**
 * Model class for NoticeRead, tracking notice read status.
 * Satisfies Phase 3 requirements.
 */
public class NoticeRead {
    private int userId;
    private int noticeId;
    private Timestamp readAt;

    public NoticeRead() {}

    public NoticeRead(int userId, int noticeId, Timestamp readAt) {
        this.userId = userId;
        this.noticeId = noticeId;
        this.readAt = readAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public Timestamp getReadAt() {
        return readAt;
    }

    public void setReadAt(Timestamp readAt) {
        this.readAt = readAt;
    }

    @Override
    public String toString() {
        return "NoticeRead{" +
                "userId=" + userId +
                ", noticeId=" + noticeId +
                ", readAt=" + readAt +
                '}';
    }
}
