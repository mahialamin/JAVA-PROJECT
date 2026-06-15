package com.cuconnect.ui;

import java.text.SimpleDateFormat;
import java.util.List;
import com.cuconnect.controller.ChatRoomController;
import com.cuconnect.model.Message;
import com.cuconnect.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Reusable Chat Room panel (VBox) embedded inside dashboards.
 *
 * Design:
 * - Self messages: right-aligned, indigo bubble
 * - Other student messages: left-aligned, gray bubble
 * - Faculty messages: left-aligned, amber bubble with "★ Faculty" label
 * - Auto-refreshes every 2 seconds via a JavaFX Timeline
 * - Faculty UI: input area is hidden (view-only mode)
 */
public class ChatRoomView extends VBox {

    private final ChatRoomController controller;
    private final SimpleDateFormat   timeFormat = new SimpleDateFormat("hh:mm a");

    private VBox     messagesBox;
    private ScrollPane scrollPane;
    private TextField  txtInput;
    private Button     btnSend;
    private Timeline   refreshTimer;

    private int lastMessageCount = 0;

    public ChatRoomView(User currentUser, int sectionId) {
        this.controller = new ChatRoomController(currentUser, sectionId);
        buildLayout();
        loadMessages(true);
        startAutoRefresh();
    }

    // ─── Layout ───────────────────────────────────────────────────────────────

    private void buildLayout() {
        setSpacing(0);
        setStyle("-fx-background-color: #111827;");
        VBox.setVgrow(this, Priority.ALWAYS);

        // Messages area
        messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(15));
        messagesBox.setStyle("-fx-background-color: #111827;");

        scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: #111827; -fx-background: #111827;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Input bar (hidden for Faculty)
        HBox inputBar = buildInputBar();

        getChildren().addAll(scrollPane, inputBar);

        // Faculty: hide input bar entirely
        if ("FACULTY".equals(controller.getCurrentUser().getRole())) {
            inputBar.setVisible(false);
            inputBar.setManaged(false);
        }
    }

    private HBox buildInputBar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(12, 15, 12, 15));
        bar.setAlignment(Pos.CENTER);
        bar.setStyle("-fx-background-color: #1f2937; -fx-border-color: #374151; -fx-border-width: 1 0 0 0;");

        txtInput = new TextField();
        txtInput.setPromptText("Type a message...");
        txtInput.getStyleClass().add("text-input");
        HBox.setHgrow(txtInput, Priority.ALWAYS);
        txtInput.setOnAction(e -> handleSend());

        btnSend = new Button("Send");
        btnSend.getStyleClass().add("button-primary");
        btnSend.setOnAction(e -> handleSend());

        bar.getChildren().addAll(txtInput, btnSend);
        return bar;
    }

    // ─── Message Bubbles ──────────────────────────────────────────────────────

    private void renderMessages(List<Message> messages) {
        messagesBox.getChildren().clear();
        User me = controller.getCurrentUser();

        for (Message msg : messages) {
            boolean isSelf    = msg.getSenderId() == me.getId();
            boolean isFaculty = "FACULTY".equals(msg.getSenderRole());

            HBox row = new HBox();
            row.setMaxWidth(Double.MAX_VALUE);

            VBox bubble = buildBubble(msg, isSelf, isFaculty);

            if (isSelf) {
                row.setAlignment(Pos.CENTER_RIGHT);
            } else {
                row.setAlignment(Pos.CENTER_LEFT);
            }
            row.getChildren().add(bubble);
            messagesBox.getChildren().add(row);
        }
        scrollToBottom();
    }

    private VBox buildBubble(Message msg, boolean isSelf, boolean isFaculty) {
        VBox bubble = new VBox(4);
        bubble.setPadding(new Insets(9, 14, 9, 14));
        bubble.setMaxWidth(480);

        // Style
        String bgColor, textColor, metaColor;
        if (isSelf) {
            bgColor   = "#6366f1";   // Indigo
            textColor = "#ffffff";
            metaColor = "#c7d2fe";
        } else if (isFaculty) {
            bgColor   = "#f59e0b";   // Amber (faculty)
            textColor = "#111827";
            metaColor = "#78350f";
        } else {
            bgColor   = "#374151";   // Slate gray
            textColor = "#f3f4f6";
            metaColor = "#9ca3af";
        }

        String radius = isSelf ? "12 12 0 12" : "12 12 12 0";
        bubble.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: %s;", bgColor, radius));

        // Sender + Time header
        String senderLabel = isFaculty ? "★ " + msg.getSenderName() + " (Faculty)" : msg.getSenderName();
        String timeStr     = msg.getSentAt() != null ? timeFormat.format(msg.getSentAt()) : "";

        Label lblSender = new Label(senderLabel + "  " + timeStr);
        lblSender.setStyle(String.format("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: %s;", metaColor));

        // Message text (wrapping)
        Text txtContent = new Text(msg.getContent());
        txtContent.setWrappingWidth(430);
        txtContent.setStyle(String.format("-fx-fill: %s; -fx-font-size: 13px;", textColor));

        bubble.getChildren().addAll(lblSender, txtContent);
        return bubble;
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    private void handleSend() {
        String content = txtInput.getText().trim();
        if (content.isEmpty()) return;

        boolean sent = controller.sendMessage(content);
        if (sent) {
            txtInput.clear();
            loadMessages(true);
        }
    }

    // ─── Refresh Logic ────────────────────────────────────────────────────────

    private void loadMessages(boolean forceScroll) {
        List<Message> messages = controller.loadMessages();

        // Only re-render if the message count changed (avoids unnecessary redraws)
        if (!forceScroll && messages.size() == lastMessageCount) return;

        lastMessageCount = messages.size();
        Platform.runLater(() -> renderMessages(messages));
    }

    private void startAutoRefresh() {
        refreshTimer = new Timeline(new KeyFrame(
                Duration.seconds(2),
                e -> loadMessages(false)
        ));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();

        // Stop the timer when this node is removed from the scene graph
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) refreshTimer.stop();
        });
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    /** Called externally when embedding dashboard switches away from chat tab. */
    public void stopRefresh() {
        if (refreshTimer != null) refreshTimer.stop();
    }
}
