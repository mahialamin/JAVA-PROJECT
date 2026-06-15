package com.cuconnect.controller;

import java.util.List;
import com.cuconnect.model.Message;
import com.cuconnect.model.User;
import com.cuconnect.service.ChatService;
import com.cuconnect.util.AlertHelper;

/**
 * Controller for the Chat Room view.
 *
 * Responsibilities:
 * - Delegate send requests to ChatService (which enforces faculty-cannot-send rule)
 * - Fetch messages for display
 * - Hold a reference to the currently logged-in user so the view can
 *   distinguish "self" bubbles from "other" bubbles.
 */
public class ChatRoomController {

    private final ChatService chatService = new ChatService();
    private final User currentUser;
    private final int sectionId;

    public ChatRoomController(User currentUser, int sectionId) {
        this.currentUser = currentUser;
        this.sectionId = sectionId;
    }

    /**
     * Called when the user hits "Send".
     * The ChatService itself blocks faculty from sending.
     */
    public boolean sendMessage(String content) {
        if (content == null || content.isBlank()) return false;

        boolean sent = chatService.sendMessage(
                currentUser.getRole(), currentUser.getId(), sectionId, content);

        if (!sent && "FACULTY".equals(currentUser.getRole())) {
            AlertHelper.showError("Not Allowed", "Faculty members can only view chat — not send messages.");
        }
        return sent;
    }

    /**
     * Fetch the latest 100 messages for the section.
     * Called on initial load and by the 2-second refresh timer.
     */
    public List<Message> loadMessages() {
        return chatService.getRecentMessages(sectionId);
    }

    public User getCurrentUser() { return currentUser; }
    public int getSectionId()    { return sectionId; }
}
