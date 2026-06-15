package com.cuconnect.service;

import java.util.List;
import com.cuconnect.dao.SectionDAO;
import com.cuconnect.model.Section;

/**
 * Service layer for Section operations.
 *
 * WHY THIS CLASS EXISTS:
 * Controllers and Views must NEVER call DAO classes directly.
 * This service is the single entry point for any section-related
 * data retrieval. Business rules about sections (e.g., filtering
 * inactive sections in the future) belong here, not in the DAO.
 *
 * ARCHITECTURE RULE:
 *   View → Controller → SectionService → SectionDAO → MySQL
 *   View ✗→ SectionDAO  (never allowed)
 *   Controller ✗→ SectionDAO  (never allowed)
 */
public class SectionService {

    // The DAO is the only class that talks to the database
    private final SectionDAO sectionDAO = new SectionDAO();

    /**
     * Returns all sections ordered by name.
     * Used by:
     * - LoginController (student registration dropdown)
     * - NoticeFormController (notice target audience dropdown)
     * - FacultyDashboardController (chat room section picker)
     *
     * @return list of all sections; empty list if none exist
     */
    public List<Section> getAllSections() {
        return sectionDAO.getAllSections();
    }

    /**
     * Creates a new section.
     * Reserved for future admin functionality.
     *
     * @param section the section to create (id will be set on the object after save)
     * @return true if created successfully
     */
    public boolean createSection(Section section) {
        if (section.getName() == null || section.getName().isBlank()) {
            System.err.println("[SectionService] Section name cannot be empty.");
            return false;
        }
        if (section.getDepartment() == null || section.getDepartment().isBlank()) {
            System.err.println("[SectionService] Department cannot be empty.");
            return false;
        }
        return sectionDAO.createSection(section);
    }
}
