package com.cuconnect.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.cuconnect.model.Message;

/**
 * DAO for section group chat messages.
 * Fetches last N messages in chronological order (oldest first for display).
 */
public class MessageDAO {

    // ─── CREATE ──────────────────────────────────────────────────────────────

    public boolean saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, section_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, message.getSenderId());
            pstmt.setInt(2, message.getSectionId());
            pstmt.setString(3, message.getContent());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        message.setId(keys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[MessageDAO] saveMessage error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    /**
     * Fetch the last `limit` messages for a section.
     * Results returned in ascending order (oldest → newest) for chat display.
     */
    public List<Message> getRecentMessages(int sectionId, int limit) {
        List<Message> messages = new ArrayList<>();
        // Subquery trick: fetch latest N in DESC, then reverse to show ASC
        String sql = "SELECT * FROM (" +
                     "  SELECT m.id, m.sender_id, u.name AS sender_name, u.role AS sender_role, " +
                     "         m.section_id, m.content, m.sent_at " +
                     "  FROM messages m JOIN users u ON m.sender_id = u.id " +
                     "  WHERE m.section_id = ? " +
                     "  ORDER BY m.sent_at DESC, m.id DESC LIMIT ?" +
                     ") AS recent ORDER BY sent_at ASC, id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sectionId);
            pstmt.setInt(2, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Message msg = new Message();
                    msg.setId(rs.getInt("id"));
                    msg.setSenderId(rs.getInt("sender_id"));
                    msg.setSenderName(rs.getString("sender_name"));
                    msg.setSenderRole(rs.getString("sender_role"));
                    msg.setSectionId(rs.getInt("section_id"));
                    msg.setContent(rs.getString("content"));
                    msg.setSentAt(rs.getTimestamp("sent_at"));
                    messages.add(msg);
                }
            }
        } catch (Exception e) {
            System.err.println("[MessageDAO] getRecentMessages error: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }
}
