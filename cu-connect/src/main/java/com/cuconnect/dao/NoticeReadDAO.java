package com.cuconnect.dao;

import java.sql.*;
import com.cuconnect.model.Notice;

/**
 * DAO for tracking which students have read which notices.
 * Uses an INSERT IGNORE pattern to avoid duplicate entries safely.
 */
public class NoticeReadDAO {

    /**
     * Mark a notice as read by a student.
     * INSERT IGNORE means it won't fail if already marked read.
     */
    public boolean markAsRead(int userId, int noticeId) {
        String sql = DatabaseConnection.isSQLite()
            ? "INSERT OR IGNORE INTO notice_reads (user_id, notice_id) VALUES (?, ?)"
            : "INSERT IGNORE INTO notice_reads (user_id, notice_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, noticeId);
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("[NoticeReadDAO] markAsRead error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if a specific student has read a specific notice.
     */
    public boolean hasRead(int userId, int noticeId) {
        String sql = "SELECT 1 FROM notice_reads WHERE user_id=? AND notice_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, noticeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.err.println("[NoticeReadDAO] hasRead error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Count total unread notices for a student given their sectionId.
     */
    public int countUnread(int userId, Integer sectionId) {
        String curDateFunc = DatabaseConnection.isSQLite() ? "date('now')" : "CURDATE()";
        String sql = "SELECT COUNT(*) FROM notices n " +
                     "WHERE (n.expiry_date IS NULL OR n.expiry_date >= " + curDateFunc + ") " +
                     "AND (n.section_id IS NULL " + (sectionId != null ? "OR n.section_id = ?" : "") + ") " +
                     "AND n.id NOT IN (SELECT notice_id FROM notice_reads WHERE user_id = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (sectionId != null) pstmt.setInt(paramIndex++, sectionId);
            pstmt.setInt(paramIndex, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("[NoticeReadDAO] countUnread error: " + e.getMessage());
        }
        return 0;
    }
}
