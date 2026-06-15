package com.cuconnect.service;

import java.util.List;
import com.cuconnect.dao.NoticeDAO;
import com.cuconnect.dao.NoticeReadDAO;
import com.cuconnect.model.Notice;

/**
 * Service layer for Notice operations.
 * Validates business rules before delegating to DAO.
 * Computes analytics (readCount, unreadCount) for the Faculty dashboard.
 *
 * RULE: Students cannot create/edit/delete notices (enforced by role check).
 * RULE: Only the faculty member who created a notice can update or delete it.
 */
public class NoticeService {
    private final NoticeDAO noticeDAO = new NoticeDAO();
    private final NoticeReadDAO noticeReadDAO = new NoticeReadDAO();

    // ─── FACULTY OPERATIONS ───────────────────────────────────────────────────

    public boolean postNotice(Notice notice) {
        if (notice.getTitle() == null || notice.getTitle().isBlank()) return false;
        if (notice.getContent() == null || notice.getContent().isBlank()) return false;
        return noticeDAO.createNotice(notice);
    }

    public boolean editNotice(Notice notice) {
        if (notice.getTitle() == null || notice.getTitle().isBlank()) return false;
        if (notice.getContent() == null || notice.getContent().isBlank()) return false;
        return noticeDAO.updateNotice(notice);
    }

    public boolean deleteNotice(int noticeId, int facultyUserId) {
        return noticeDAO.deleteNotice(noticeId, facultyUserId);
    }

    public boolean pinNotice(int noticeId, int facultyUserId) {
        return noticeDAO.togglePin(noticeId, facultyUserId, true);
    }

    public boolean unpinNotice(int noticeId, int facultyUserId) {
        return noticeDAO.togglePin(noticeId, facultyUserId, false);
    }

    // ─── FACULTY ANALYTICS ────────────────────────────────────────────────────

    /**
     * Returns Faculty's notices enriched with analytics (read count, total students).
     */
    public List<Notice> getNoticesWithAnalytics(int facultyUserId) {
        List<Notice> notices = noticeDAO.getNoticesByFaculty(facultyUserId);
        for (Notice notice : notices) {
            int readCount = noticeDAO.getReadCount(notice.getId());
            int total = noticeDAO.getTotalEligibleStudents(notice.getSectionId());
            notice.setReadCount(readCount);
            notice.setTotalStudents(total);
        }
        return notices;
    }

    // ─── STUDENT OPERATIONS ───────────────────────────────────────────────────

    /**
     * Returns notices visible to a student. Pinned notices appear first.
     * Expired notices are filtered by the database query.
     */
    public List<Notice> getNoticesForStudent(Integer sectionId) {
        return noticeDAO.getNoticesForStudent(sectionId);
    }

    /**
     * Count of unread notices for a student.
     */
    public int getUnreadCount(int userId, Integer sectionId) {
        return noticeReadDAO.countUnread(userId, sectionId);
    }
}
