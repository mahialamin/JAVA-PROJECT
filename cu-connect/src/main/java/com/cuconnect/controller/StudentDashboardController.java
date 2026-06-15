package com.cuconnect.controller;

import java.util.List;
import com.cuconnect.model.Notice;
import com.cuconnect.model.Student;
import com.cuconnect.model.User;
import com.cuconnect.service.NoticeReadService;
import com.cuconnect.service.NoticeService;

/**
 * Controller for the Student Dashboard.
 *
 * Responsibilities:
 * - Load notices visible to this student
 * - Mark a notice as read when the student opens it
 * - Provide unread count for the badge on the Notice tab
 *
 * The view calls these methods; this class never touches JavaFX nodes.
 */
public class StudentDashboardController {

    private final NoticeService noticeService       = new NoticeService();
    private final NoticeReadService noticeReadService = new NoticeReadService();

    private final User    currentUser;
    private final Student studentProfile;

    public StudentDashboardController(User currentUser, Student studentProfile) {
        this.currentUser    = currentUser;
        this.studentProfile = studentProfile;
    }

    /** Load all non-expired notices visible to this student. */
    public List<Notice> loadNotices() {
        Integer sectionId = studentProfile != null ? studentProfile.getSectionId() : null;
        return noticeService.getNoticesForStudent(sectionId);
    }

    /** Returns true if this student has already read the notice. */
    public boolean hasRead(int noticeId) {
        return noticeReadService.hasRead(currentUser.getId(), noticeId);
    }

    /**
     * Mark a notice as read. Called by the view when the student
     * opens the notice detail dialog.
     */
    public void markAsRead(int noticeId) {
        noticeReadService.markAsRead(currentUser.getId(), noticeId);
    }

    /** Count of unread notices — shown as badge on the Notice tab title. */
    public int getUnreadCount() {
        Integer sectionId = studentProfile != null ? studentProfile.getSectionId() : null;
        return noticeService.getUnreadCount(currentUser.getId(), sectionId);
    }

    public User    getCurrentUser()    { return currentUser; }
    public Student getStudentProfile() { return studentProfile; }
}
