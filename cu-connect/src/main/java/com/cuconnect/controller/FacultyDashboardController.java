package com.cuconnect.controller;

import java.util.List;
import com.cuconnect.model.Notice;
import com.cuconnect.model.Section;
import com.cuconnect.model.User;
import com.cuconnect.service.NoticeService;
import com.cuconnect.service.SectionService;
import com.cuconnect.util.AlertHelper;

/**
 * Controller for the Faculty Dashboard.
 *
 * Responsibilities:
 * - Load this faculty member's notices (with analytics enriched)
 * - Delete notices (creator-only enforced in DAO)
 * - Toggle pin status
 * - Provide list of all sections for the chat selector
 */
public class FacultyDashboardController {

    private final NoticeService  noticeService  = new NoticeService();
    private final SectionService sectionService = new SectionService(); // ✅ uses Service, not DAO
    private final User           currentUser;

    public FacultyDashboardController(User currentUser) {
        this.currentUser = currentUser;
    }

    /** Load this faculty's notices, each enriched with readCount + totalStudents. */
    public List<Notice> loadNotices() {
        return noticeService.getNoticesWithAnalytics(currentUser.getId());
    }

    /** Delete a notice. Returns true on success. */
    public boolean deleteNotice(int noticeId) {
        boolean deleted = noticeService.deleteNotice(noticeId, currentUser.getId());
        if (!deleted) {
            AlertHelper.showError("Delete Failed",
                    "Could not delete notice. You may only delete your own notices.");
        }
        return deleted;
    }

    /** Toggle pin on a notice. */
    public boolean togglePin(Notice notice) {
        boolean newState = !notice.isPinned();
        boolean ok = newState
                ? noticeService.pinNotice(notice.getId(), currentUser.getId())
                : noticeService.unpinNotice(notice.getId(), currentUser.getId());
        if (!ok) AlertHelper.showError("Pin Error", "Could not update pin status.");
        return ok;
    }

    /**
     * Returns all sections for the chat room section picker.
     * Delegates to SectionService — never touches SectionDAO directly.
     */
    public List<Section> loadAllSections() {
        return sectionService.getAllSections();
    }

    public User getCurrentUser() { return currentUser; }
}
