package com.cuconnect.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import com.cuconnect.model.Notice;
import com.cuconnect.model.Section;
import com.cuconnect.model.User;
import com.cuconnect.service.NoticeService;
import com.cuconnect.service.SectionService;
import com.cuconnect.util.AlertHelper;
import javafx.stage.Stage;

/**
 * Controller for the Notice Form dialog (Create / Edit).
 *
 * Architectural note:
 * The view passes raw field values here. This controller validates,
 * constructs a Notice object, and delegates to NoticeService.
 * UI classes NEVER call DAO methods directly.
 */
public class NoticeFormController {

    private final NoticeService   noticeService   = new NoticeService();
    private final SectionService  sectionService  = new SectionService();
    private final User currentUser;

    public NoticeFormController(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Returns all sections for the "Target Audience" dropdown.
     * The View calls THIS method — never the DAO directly.
     *
     * @return list of all sections
     */
    public List<Section> loadSections() {
        return sectionService.getAllSections();
    }

    /**
     * Create a brand-new notice.
     *
     * @param title       Notice title
     * @param content     Notice body text
     * @param section     Targeted section (null = general)
     * @param pinned      Whether to pin this notice
     * @param expiryDate  Expiry date (null = never expires)
     * @return true if saved successfully
     */
    public boolean createNotice(String title, String content,
                                Section section, boolean pinned,
                                LocalDate expiryDate) {
        if (!validate(title, content)) return false;

        Notice notice = buildNotice(0, title, content, section, pinned, expiryDate);
        boolean success = noticeService.postNotice(notice);

        if (success) {
            AlertHelper.showInfo("Notice Published", "Your notice has been published successfully.");
        } else {
            AlertHelper.showError("Publish Failed", "Could not save notice. Please try again.");
        }
        return success;
    }

    /**
     * Update an existing notice. The noticeId and createdBy are preserved
     * to enforce the creator-only edit rule inside NoticeDAO.
     */
    public boolean updateNotice(int noticeId, String title, String content,
                                Section section, boolean pinned,
                                LocalDate expiryDate) {
        if (!validate(title, content)) return false;

        Notice notice = buildNotice(noticeId, title, content, section, pinned, expiryDate);
        boolean success = noticeService.editNotice(notice);

        if (success) {
            AlertHelper.showInfo("Notice Updated", "Notice has been updated successfully.");
        } else {
            AlertHelper.showError("Update Failed", "Could not update notice. Please try again.");
        }
        return success;
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────────────────

    private boolean validate(String title, String content) {
        if (title == null || title.isBlank()) {
            AlertHelper.showError("Validation Error", "Title cannot be empty.");
            return false;
        }
        if (content == null || content.isBlank()) {
            AlertHelper.showError("Validation Error", "Content cannot be empty.");
            return false;
        }
        return true;
    }

    private Notice buildNotice(int id, String title, String content,
                               Section section, boolean pinned, LocalDate expiryDate) {
        Notice notice = new Notice();
        notice.setId(id);
        notice.setTitle(title.trim());
        notice.setContent(content.trim());
        notice.setCreatedBy(currentUser.getId());
        notice.setSectionId(section != null ? section.getId() : null);
        notice.setPinned(pinned);
        notice.setExpiryDate(expiryDate != null ? Date.valueOf(expiryDate) : null);
        return notice;
    }
}
