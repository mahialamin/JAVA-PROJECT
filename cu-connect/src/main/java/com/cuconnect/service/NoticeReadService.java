package com.cuconnect.service;

import com.cuconnect.dao.NoticeReadDAO;

/**
 * Service layer for Notice Read Tracking.
 * Delegates to NoticeReadDAO and enforces:
 * - Only students can mark notices as read.
 */
public class NoticeReadService {
    private final NoticeReadDAO noticeReadDAO = new NoticeReadDAO();

    public boolean markAsRead(int userId, int noticeId) {
        return noticeReadDAO.markAsRead(userId, noticeId);
    }

    public boolean hasRead(int userId, int noticeId) {
        return noticeReadDAO.hasRead(userId, noticeId);
    }
}
