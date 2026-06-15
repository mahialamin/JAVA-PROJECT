package com.cuconnect.service;

import com.cuconnect.dao.NoticeDAO;
import com.cuconnect.dao.NoticeReadDAO;
import java.util.HashMap;
import java.util.Map;

/**
 * Service layer for notice read tracking and analytics.
 * Satisfies Phase 6.
 */
public class AnalyticsService {
    private final NoticeReadDAO noticeReadDAO = new NoticeReadDAO();
    private final NoticeDAO noticeDAO = new NoticeDAO();

    /**
     * Mark notice as read.
     * @param userId user id of the student
     * @param noticeId notice id
     * @return true if marked as read successfully
     */
    public boolean markAsRead(int userId, int noticeId) {
        return noticeReadDAO.markAsRead(userId, noticeId);
    }

    /**
     * Get read count for a notice.
     * @param noticeId notice id
     * @return count of students who have read the notice
     */
    public int getReadCount(int noticeId) {
        return noticeDAO.getReadCount(noticeId);
    }

    /**
     * Get unread count for a student.
     * @param userId student user id
     * @param sectionId student section id
     * @return count of unread notices
     */
    public int getUnreadCount(int userId, Integer sectionId) {
        return noticeReadDAO.countUnread(userId, sectionId);
    }

    /**
     * Retrieve statistics for a specific notice.
     * @param noticeId notice id
     * @param sectionId targeted section id (null = general)
     * @return map of statistic labels to values
     */
    public Map<String, Integer> getNoticeStatistics(int noticeId, Integer sectionId) {
        Map<String, Integer> stats = new HashMap<>();
        int readCount = noticeDAO.getReadCount(noticeId);
        int totalStudents = noticeDAO.getTotalEligibleStudents(sectionId);
        int unreadCount = Math.max(0, totalStudents - readCount);
        
        stats.put("readCount", readCount);
        stats.put("unreadCount", unreadCount);
        stats.put("totalStudents", totalStudents);
        return stats;
    }
}
