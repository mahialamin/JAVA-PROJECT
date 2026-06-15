package com.cuconnect.ui;

import java.text.SimpleDateFormat;
import java.util.List;
import com.cuconnect.controller.StudentDashboardController;
import com.cuconnect.model.Notice;
import com.cuconnect.model.Student;
import com.cuconnect.model.User;
import com.cuconnect.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Student Dashboard — the main screen after a student logs in.
 *
 * Layout:
 * ┌─────────────────────────────────────────────────────────┐
 * │  SIDEBAR  │           TABBED CONTENT AREA               │
 * │  Profile  │  [Notice Board (N)]  [Class Chat]           │
 * │  Info     │                                             │
 * │           │  • Notice cards with NEW badge              │
 * │  Logout   │  • Double-click to read                     │
 * └─────────────────────────────────────────────────────────┘
 */
public class StudentDashboardView extends BorderPane {

    private final Stage                      stage;
    private final StudentDashboardController controller;
    private final Student                    studentProfile;
    private final SimpleDateFormat           dateFormat = new SimpleDateFormat("dd MMM yyyy");

    private Tab         noticeTab;
    private VBox        noticeListBox;
    private ChatRoomView chatRoomView;
    private Label        lblUnreadBadge;

    public StudentDashboardView(Stage stage, User user) {
        this.stage = stage;

        // Resolve full Student profile
        Student resolved = (user instanceof Student s) ? s : null;
        this.studentProfile = resolved;
        this.controller     = new StudentDashboardController(user, resolved);

        buildLayout();
        refreshNotices();   // Data loaded AFTER layout is complete — avoids init bug
    }

    // ─── Main Layout ──────────────────────────────────────────────────────────

    private void buildLayout() {
        setLeft(buildSidebar());
        setCenter(buildTabPane());
        setStyle("-fx-background-color: #111827;");
    }

    // ─── Sidebar ──────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: #0f172a; -fx-padding: 0;");

        // Top header
        VBox header = new VBox(4);
        header.setPadding(new Insets(28, 20, 20, 20));
        header.setStyle("-fx-background-color: #0f172a;");

        Label lblApp = new Label("CUConnect");
        lblApp.getStyleClass().add("header-title");
        lblApp.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #6366f1;");

        Label lblPortal = new Label("Student Portal");
        lblPortal.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        header.getChildren().addAll(lblApp, lblPortal);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #1f2937;");

        // Profile details
        VBox profileBox = new VBox(12);
        profileBox.setPadding(new Insets(20, 20, 20, 20));

        Label lblSection = new Label("PROFILE");
        lblSection.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 10px; -fx-font-weight: bold;");

        User u = controller.getCurrentUser();
        profileBox.getChildren().addAll(
                lblSection,
                sidebarField("Name",       u.getName()),
                sidebarField("ID", String.valueOf(u.getId())),
                sidebarField("Email",      u.getEmail()),
                sidebarField("Department", studentProfile != null ? studentProfile.getDepartment() : "N/A"),
                sidebarField("Student ID", studentProfile != null ? studentProfile.getStudentId()  : "N/A"),
                sidebarField("Batch",       studentProfile != null && studentProfile.getBatch() != null ? String.valueOf(studentProfile.getBatch()) : "N/A"),
                sidebarField("Section",     studentProfile != null && studentProfile.getSectionId() != null ? String.valueOf(studentProfile.getSectionId()) : "N/A")
        );

        // Unread badge summary
        lblUnreadBadge = new Label("Loading...");
        lblUnreadBadge.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 12px;");
        profileBox.getChildren().addAll(new Separator(), lblUnreadBadge);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout button
        VBox bottomBox = new VBox();
        bottomBox.setPadding(new Insets(15, 20, 20, 20));
        Button btnLogout = new Button("Logout");
        btnLogout.getStyleClass().add("button-danger");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setOnAction(e -> handleLogout());
        bottomBox.getChildren().add(btnLogout);

        sidebar.getChildren().addAll(header, sep1, profileBox, spacer, bottomBox);
        return sidebar;
    }

    // ─── Tab Pane ─────────────────────────────────────────────────────────────

    private TabPane buildTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #111827;");

        // ── Notice Board Tab ────────────────────────────────────────────────
        noticeTab = new Tab("📋 Notice Board");
        noticeListBox = new VBox(10);
        noticeListBox.setPadding(new Insets(15));
        noticeListBox.setStyle("-fx-background-color: #111827;");

        ScrollPane noticeScroll = new ScrollPane(noticeListBox);
        noticeScroll.setFitToWidth(true);
        noticeScroll.setStyle("-fx-background-color: #111827; -fx-background: #111827;");
        VBox.setVgrow(noticeScroll, Priority.ALWAYS);

        VBox noticeTabContent = new VBox(0);

        // Top bar
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(15, 15, 0, 15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label("Class Announcements");
        lblTitle.getStyleClass().add("section-header");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnRefresh = new Button("⟳ Refresh");
        btnRefresh.getStyleClass().add("button-secondary");
        btnRefresh.setOnAction(e -> refreshNotices());
        topBar.getChildren().addAll(lblTitle, spacer, btnRefresh);

        noticeTabContent.getChildren().addAll(topBar, noticeScroll);
        VBox.setVgrow(noticeScroll, Priority.ALWAYS);
        noticeTab.setContent(noticeTabContent);

        // ── Class Chat Tab ──────────────────────────────────────────────────
        Tab chatTab = new Tab("💬 Class Chat");
        if (studentProfile != null && studentProfile.getSectionId() != null) {
            chatRoomView = new ChatRoomView(controller.getCurrentUser(), studentProfile.getSectionId());
            chatTab.setContent(chatRoomView);
        } else {
            Label lblNoSection = new Label("You are not assigned to any section yet.");
            lblNoSection.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px;");
            StackPane emptyPane = new StackPane(lblNoSection);
            emptyPane.setStyle("-fx-background-color: #111827;");
            chatTab.setContent(emptyPane);
        }

        tabPane.getTabs().addAll(noticeTab, chatTab);
        return tabPane;
    }

    // ─── Notice Rendering ─────────────────────────────────────────────────────

    private void refreshNotices() {
        noticeListBox.getChildren().clear();
        List<Notice> notices = controller.loadNotices();

        if (notices.isEmpty()) {
            Label lbl = new Label("No announcements at this time.");
            lbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-font-style: italic;");
            noticeListBox.getChildren().add(new StackPane(lbl));
        } else {
            for (Notice notice : notices) {
                noticeListBox.getChildren().add(buildNoticeCard(notice));
            }
        }
        updateUnreadBadge();
    }

    private VBox buildNoticeCard(Notice notice) {
        boolean isRead   = controller.hasRead(notice.getId());
        boolean isPinned = notice.isPinned();

        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setMaxWidth(Double.MAX_VALUE);

        // Card border color: pinned = purple, unread = green, read = default
        String borderColor = isPinned ? "#a855f7" : (isRead ? "#374151" : "#10b981");
        String bgColor     = isPinned ? "#1e1033" : (isRead ? "#1f2937" : "#0f2d1f");
        card.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;",
                bgColor, borderColor));
        card.setCursor(javafx.scene.Cursor.HAND);

        // Title row with badges
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        if (isPinned) {
            Label pin = new Label("📌 PINNED");
            pin.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; " +
                         "-fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
            titleRow.getChildren().add(pin);
        }
        if (!isRead) {
            Label newBadge = new Label("NEW");
            newBadge.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                              "-fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
            titleRow.getChildren().add(newBadge);
        }

        Label lblTitle = new Label(notice.getTitle());
        lblTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");
        titleRow.getChildren().add(lblTitle);

        // Meta line
        String audience = notice.getSectionId() == null ? "All Sections" : "Your Section";
        Label lblMeta = new Label("By: " + notice.getCreatorName() +
                "  ·  " + dateFormat.format(notice.getCreatedAt()) +
                "  ·  " + audience);
        lblMeta.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        // Content excerpt
        String excerpt = notice.getContent();
        if (excerpt.length() > 120) excerpt = excerpt.substring(0, 117) + "…";
        Label lblExcerpt = new Label(excerpt);
        lblExcerpt.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
        lblExcerpt.setWrapText(true);

        card.getChildren().addAll(titleRow, lblMeta, lblExcerpt);

        // Double-click → show full detail and mark as read
        card.setOnMouseClicked(evt -> {
            if (evt.getClickCount() == 2) {
                showNoticeDetail(notice);
            }
        });

        return card;
    }

    // ─── Notice Detail Dialog ─────────────────────────────────────────────────

    private void showNoticeDetail(Notice notice) {
        controller.markAsRead(notice.getId());

        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.setTitle(notice.getTitle());

        VBox root = new VBox(14);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #111827;");

        Label lblTitle = new Label(notice.getTitle());
        lblTitle.getStyleClass().add("section-header");
        lblTitle.setWrapText(true);

        String audience = notice.getSectionId() == null ? "All Sections" : "Your Section";
        Label lblMeta = new Label("Posted by: " + notice.getCreatorName() +
                "  ·  " + dateFormat.format(notice.getCreatedAt()) +
                "  ·  " + audience);
        lblMeta.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

        TextArea txtContent = new TextArea(notice.getContent());
        txtContent.setEditable(false);
        txtContent.setWrapText(true);
        txtContent.setPrefRowCount(12);
        txtContent.setStyle("-fx-background-color: #1f2937; -fx-text-fill: #e5e7eb; " +
                            "-fx-font-size: 13px; -fx-background-radius: 6;");
        VBox.setVgrow(txtContent, Priority.ALWAYS);

        Button btnClose = new Button("Close");
        btnClose.getStyleClass().add("button-secondary");
        HBox btnRow = new HBox(btnClose);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnClose.setOnAction(e -> { dialog.close(); refreshNotices(); });

        root.getChildren().addAll(lblTitle, lblMeta, new Separator(), txtContent, btnRow);

        Scene scene = new Scene(root, 540, 440);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private VBox sidebarField(String label, String value) {
        VBox box = new VBox(2);
        Label lbl = new Label(label.toUpperCase());
        lbl.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 9px; -fx-font-weight: bold;");
        Label val = new Label(value != null ? value : "N/A");
        val.setStyle("-fx-text-fill: #d1d5db; -fx-font-size: 12px;");
        val.setWrapText(true);
        box.getChildren().addAll(lbl, val);
        return box;
    }

    private void updateUnreadBadge() {
        int count = controller.getUnreadCount();
        lblUnreadBadge.setText("Unread Notices: " + count);
        if (noticeTab != null) {
            noticeTab.setText(count > 0 ? "📋 Notice Board (" + count + ")" : "📋 Notice Board");
        }
    }

    private void handleLogout() {
        if (chatRoomView != null) chatRoomView.stopRefresh();
        AuthService.logout();
        LoginView loginView = new LoginView(stage);
        Scene scene = new Scene(loginView, 480, 620);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CUConnect — Login");
        stage.centerOnScreen();
    }
}
