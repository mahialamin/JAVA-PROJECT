package com.cuconnect.service;

import java.util.List;
import com.cuconnect.dao.MessageDAO;
import com.cuconnect.model.Message;

/**
 * Service for section group chat.
 *
 * RULE: Faculty CANNOT send messages — only view. Role enforcement is here.
 * RULE: Students can only send to their own section (validated by caller/controller).
 */
public class ChatService {
    private final MessageDAO messageDAO = new MessageDAO();

    /**
     * Send a message. Only allowed for STUDENT role.
     *
     * @param senderRole  The role of the sender ("STUDENT" or "FACULTY")
     * @param senderId    The user ID of the sender
     * @param sectionId   The section chat room
     * @param content     The message text
     * @return true if successfully saved; false if validation fails or DB error
     */
    public boolean sendMessage(String senderRole, int senderId, int sectionId, String content) {
        if (!"STUDENT".equals(senderRole)) {
            System.err.println("[ChatService] Faculty cannot send messages — view only.");
            return false;
        }
        if (content == null || content.isBlank()) return false;

        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setSectionId(sectionId);
        msg.setContent(content.trim());
        return messageDAO.saveMessage(msg);
    }

    /**
     * Fetch the 100 most recent messages for a section (in chronological order).
     */
    public List<Message> getRecentMessages(int sectionId) {
        return messageDAO.getRecentMessages(sectionId, 100);
    }
}
