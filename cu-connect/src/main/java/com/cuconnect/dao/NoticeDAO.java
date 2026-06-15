package com.cuconnect.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.cuconnect.model.Notice;

/**
 * DAO for Notice CRUD operations.
 * All SQL lives here — never in service or UI classes.
 * Uses PreparedStatement and try-with-resources throughout.
 */
public class NoticeDAO {

    // ─── CREATE ──────────────────────────────────────────────────────────────

    public boolean createNotice(Notice notice) {
        String sql = "INSERT INTO notices (title, content, created_by, section_id, is_pinned, expiry_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, notice.getTitle());
            pstmt.setString(2, notice.getContent());
            pstmt.setInt(3, notice.getCreatedBy());

            if (notice.getSectionId() != null) {
                pstmt.setInt(4, notice.getSectionId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.setBoolean(5, notice.isPinned());

            if (notice.getExpiryDate() != null) {
                pstmt.setDate(6, notice.getExpiryDate());
            } else {
                pstmt.setNull(6, Types.DATE);
            }

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        notice.setId(keys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[NoticeDAO] createNotice error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    /**
     * Fetch all non-expired notices visible to a student.
     * Includes general notices (section_id IS NULL) and their section's notices.
     * Pinned notices sort first, then by created_at descending.
     */
    public List<Notice> getNoticesForStudent(Integer sectionId) {
        List<Notice> notices = new ArrayList<>();
        String curDateFunc = DatabaseConnection.isSQLite() ? "date('now')" : "CURDATE()";
        String sql = "SELECT n.*, u.name AS creator_name " +
                     "FROM notices n JOIN users u ON n.created_by = u.id " +
                     "WHERE (n.expiry_date IS NULL OR n.expiry_date >= " + curDateFunc + ") " +
                     "AND (n.section_id IS NULL " + (sectionId != null ? "OR n.section_id = ?" : "") + ") " +
                     "ORDER BY n.is_pinned DESC, n.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (sectionId != null) {
                pstmt.setInt(1, sectionId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notices.add(mapNotice(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[NoticeDAO] getNoticesForStudent error: " + e.getMessage());
            e.printStackTrace();
        }
        return notices;
    }

    /**
     * Fetch all notices created by a specific faculty member (for management).
     */
    public List<Notice> getNoticesByFaculty(int facultyUserId) {
        List<Notice> notices = new ArrayList<>();
        String sql = "SELECT n.*, u.name AS creator_name " +
                     "FROM notices n JOIN users u ON n.created_by = u.id " +
                     "WHERE n.created_by = ? " +
                     "ORDER BY n.is_pinned DESC, n.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facultyUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notices.add(mapNotice(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[NoticeDAO] getNoticesByFaculty error: " + e.getMessage());
            e.printStackTrace();
        }
        return notices;
    }

    /**
     * Count of students who have read a specific notice.
     */
    public int getReadCount(int noticeId) {
        String sql = "SELECT COUNT(*) FROM notice_reads WHERE notice_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, noticeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("[NoticeDAO] getReadCount error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Total students who are eligible to read a notice
     * (all students for general notices, or section members for section notices).
     */
    public int getTotalEligibleStudents(Integer sectionId) {
        String sql;
        if (sectionId == null) {
            sql = "SELECT COUNT(*) FROM student_sections";
        } else {
            sql = "SELECT COUNT(*) FROM student_sections WHERE section_id = ?";
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (sectionId != null) {
                pstmt.setInt(1, sectionId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("[NoticeDAO] getTotalEligibleStudents error: " + e.getMessage());
        }
        return 0;
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    public boolean updateNotice(Notice notice) {
        String sql = "UPDATE notices SET title=?, content=?, section_id=?, is_pinned=?, expiry_date=? " +
                     "WHERE id=? AND created_by=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notice.getTitle());
            pstmt.setString(2, notice.getContent());

            if (notice.getSectionId() != null) {
                pstmt.setInt(3, notice.getSectionId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            pstmt.setBoolean(4, notice.isPinned());

            if (notice.getExpiryDate() != null) {
                pstmt.setDate(5, notice.getExpiryDate());
            } else {
                pstmt.setNull(5, Types.DATE);
            }

            pstmt.setInt(6, notice.getId());
            pstmt.setInt(7, notice.getCreatedBy()); // Security: only the creator can update

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("[NoticeDAO] updateNotice error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean togglePin(int noticeId, int facultyUserId, boolean pinned) {
        String sql = "UPDATE notices SET is_pinned=? WHERE id=? AND created_by=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, pinned);
            pstmt.setInt(2, noticeId);
            pstmt.setInt(3, facultyUserId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("[NoticeDAO] togglePin error: " + e.getMessage());
        }
        return false;
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    public boolean deleteNotice(int noticeId, int facultyUserId) {
        String sql = "DELETE FROM notices WHERE id=? AND created_by=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, noticeId);
            pstmt.setInt(2, facultyUserId); // Security: only the creator can delete
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("[NoticeDAO] deleteNotice error: " + e.getMessage());
        }
        return false;
    }

    // ─── MAPPER ───────────────────────────────────────────────────────────────

    private Notice mapNotice(ResultSet rs) throws SQLException {
        Notice notice = new Notice();
        notice.setId(rs.getInt("id"));
        notice.setTitle(rs.getString("title"));
        notice.setContent(rs.getString("content"));
        notice.setCreatedBy(rs.getInt("created_by"));
        notice.setCreatorName(rs.getString("creator_name"));
        int sectionId = rs.getInt("section_id");
        notice.setSectionId(rs.wasNull() ? null : sectionId);
        notice.setPinned(rs.getBoolean("is_pinned"));
        notice.setExpiryDate(rs.getDate("expiry_date"));
        notice.setCreatedAt(rs.getTimestamp("created_at"));
        return notice;
    }
}
