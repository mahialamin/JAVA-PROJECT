package com.cuconnect.ui;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.cuconnect.controller.FacultyDashboardController;
import com.cuconnect.model.Faculty;
import com.cuconnect.model.Notice;
import com.cuconnect.model.Section;
import com.cuconnect.model.User;
import com.cuconnect.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Faculty Dashboard — the main screen after a faculty member logs in.
 *
 * Layout:
 * ┌─────────────────────────────────────────────────────────┐
 * │  SIDEBAR  │       TABBED CONTENT AREA                   │
 * │  Profile  │  [My Notices]  [Section Chats]              │
 * │           │                                             │
 * │           │  Notice cards with analytics (reads/total)  │
 * │  Logout   │  + Edit / Delete / Pin buttons              │
 * └─────────────────────────────────────────────────────────┘
 */
public class FacultyDashboardView extends BorderPane {

    private final Stage                       stage;
    private final FacultyDashboardController  controller;
    private final SimpleDateFormat            dateFormat   = new SimpleDateFormat("dd MMM yyyy");
    /** Maps section_id → section name (e.g. 3 → "CSE-A") for fast notice card lookup. */
    private final Map<Integer, String>        sectionNameMap = new HashMap<>();

    private VBox         noticeListBox;
    private ComboBox<Section> cmbSectionPicker;
    private StackPane    chatContainer;
    private ChatRoomView currentChatRoom;

    public FacultyDashboardView(Stage stage, User user) {
        this.stage      = stage;
        this.controller = new FacultyDashboardController(user);

        // Pre-load section names once so notice cards can display "CSE-A" instead of "Section #3"
        for (Section s : controller.loadAllSections()) {
            sectionNameMap.put(s.getId(), s.getName());
        }

        buildLayout();
        refreshNotices();   // Data loaded AFTER layout is fully constructed
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
        sidebar.setStyle("-fx-background-color: #0f172a;");

        VBox header = new VBox(4);
        header.setPadding(new Insets(28, 20, 20, 20));

        Label lblApp = new Label("CUConnect");
        lblApp.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #6366f1;");
        Label lblPortal = new Label("Faculty Dashboard");
        lblPortal.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        header.getChildren().addAll(lblApp, lblPortal);

        Separator sep = new Separator();

        VBox profileBox = new VBox(12);
        profileBox.setPadding(new Insets(20, 20, 20, 20));

        Label lblSection = new Label("FACULTY PROFILE");
        lblSection.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 10px; -fx-font-weight: bold;");

        User u = controller.getCurrentUser();
        String dept  = (u instanceof Faculty f) ? f.getDepartment()  : "N/A";
        String desig = (u instanceof Faculty f) ? f.getDesignation() : "N/A";

        profileBox.getChildren().addAll(
                lblSection,
                sidebarField("Name",        u.getName()),
                sidebarField("ID", String.valueOf(u.getId())),
                sidebarField("Email",       u.getEmail()),
                sidebarField("Department",  dept),
                sidebarField("Designation", desig)
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox bottomBox = new VBox();
        bottomBox.setPadding(new Insets(15, 20, 20, 20));
        Button btnLogout = new Button("Logout");
        btnLogout.getStyleClass().add("button-danger");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setOnAction(e -> handleLogout());
        bottomBox.getChildren().add(btnLogout);

        sidebar.getChildren().addAll(header, sep, profileBox, spacer, bottomBox);
        return sidebar;
    }

    // ─── Tab Pane ─────────────────────────────────────────────────────────────

    private TabPane buildTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #111827;");

        tabPane.getTabs().addAll(buildNoticeTab(), buildChatTab());
        return tabPane;
    }

    // ─── Notice Management Tab ────────────────────────────────────────────────

    private Tab buildNoticeTab() {
        Tab tab = new Tab("📋 My Notices");

        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: #111827;");

        // Top action bar
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Published Notices");
        lblTitle.getStyleClass().add("section-header");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh    = new Button("⟳ Refresh");
        btnRefresh.getStyleClass().add("button-secondary");
        btnRefresh.setOnAction(e -> refreshNotices());

        Button btnNewNotice  = new Button("＋ New Notice");
        btnNewNotice.getStyleClass().add("button-primary");
        btnNewNotice.setOnAction(e -> openNoticeForm(null));

        topBar.getChildren().addAll(lblTitle, spacer, btnRefresh, btnNewNotice);

        // Notice list
        noticeListBox = new VBox(10);
        noticeListBox.setPadding(new Insets(0, 15, 15, 15));

        ScrollPane scroll = new ScrollPane(noticeListBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #111827; -fx-background: #111827;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        content.getChildren().addAll(topBar, new Separator(), scroll);
        tab.setContent(content);
        return tab;
    }

    // ─── Chat Tab ─────────────────────────────────────────────────────────────

    private Tab buildChatTab() {
        Tab tab = new Tab("💬 Section Chats");

        VBox content = new VBox(10);
        content.setStyle("-fx-background-color: #111827;");

        HBox selectorBar = new HBox(12);
        selectorBar.setPadding(new Insets(15));
        selectorBar.setAlignment(Pos.CENTER_LEFT);

        Label lblPick = new Label("Select Section:");
        lblPick.getStyleClass().add("label-normal");

        cmbSectionPicker = new ComboBox<>();
        List<Section> sections = controller.loadAllSections();
        cmbSectionPicker.getItems().addAll(sections);
        cmbSectionPicker.setPromptText("Choose a section...");
        cmbSectionPicker.setOnAction(e -> switchChatRoom());

        selectorBar.getChildren().addAll(lblPick, cmbSectionPicker);

        chatContainer = new StackPane();
        chatContainer.setStyle("-fx-background-color: #111827;");
        VBox.setVgrow(chatContainer, Priority.ALWAYS);

        Label placeholder = new Label("Select a section above to view its chat.");
        placeholder.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");
        chatContainer.getChildren().add(placeholder);

        if (!sections.isEmpty()) {
            cmbSectionPicker.setValue(sections.get(0));
            switchChatRoom();
        }

        content.getChildren().addAll(selectorBar, new Separator(), chatContainer);
        tab.setContent(content);
        return tab;
    }

    // ─── Notice Card Builder ──────────────────────────────────────────────────

    private void refreshNotices() {
        noticeListBox.getChildren().clear();
        List<Notice> notices = controller.loadNotices();

        if (notices.isEmpty()) {
            Label lbl = new Label("You haven't posted any notices yet. Click '＋ New Notice' to get started.");
            lbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px; -fx-font-style: italic;");
            lbl.setWrapText(true);
            noticeListBox.getChildren().add(lbl);
            return;
        }

        for (Notice notice : notices) {
            noticeListBox.getChildren().add(buildNoticeCard(notice));
        }
    }

    private VBox buildNoticeCard(Notice notice) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setMaxWidth(Double.MAX_VALUE);

        String borderColor = notice.isPinned() ? "#a855f7" : "#374151";
        String bgColor     = notice.isPinned() ? "#1e1033" : "#1f2937";
        card.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;",
                bgColor, borderColor));

        // Title row
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        if (notice.isPinned()) {
            Label pin = new Label("📌 PINNED");
            pin.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; " +
                         "-fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
            titleRow.getChildren().add(pin);
        }

        if (notice.isExpired()) {
            Label exp = new Label("EXPIRED");
            exp.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                         "-fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
            titleRow.getChildren().add(exp);
        }

        Label lblTitle = new Label(notice.getTitle());
        lblTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");
        titleRow.getChildren().add(lblTitle);

        // Meta line — resolve section name from the map (e.g. "CSE-A" not "Section #3")
        String audience;
        if (notice.getSectionId() == null) {
            audience = "All Sections";
        } else {
            // Look up the real name; fall back gracefully if section was deleted
            audience = sectionNameMap.getOrDefault(notice.getSectionId(),
                    "Section #" + notice.getSectionId());
        }
        String expiry   = notice.getExpiryDate() != null ? "  ·  Expires: " + dateFormat.format(notice.getExpiryDate()) : "";
        Label lblMeta   = new Label(dateFormat.format(notice.getCreatedAt()) + "  ·  " + audience + expiry);
        lblMeta.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        // Analytics row
        int reads   = notice.getReadCount();
        int total   = notice.getTotalStudents();
        int unread  = Math.max(0, total - reads);
        Label lblAnalytics = new Label(
                "👁  " + reads + " Read   ·   🔔 " + unread + " Unread   ·   👥 " + total + " Total");
        lblAnalytics.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981; -fx-font-weight: bold;");

        // Excerpt
        String excerpt = notice.getContent();
        if (excerpt.length() > 100) excerpt = excerpt.substring(0, 97) + "…";
        Label lblExcerpt = new Label(excerpt);
        lblExcerpt.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
        lblExcerpt.setWrapText(true);

        // Action buttons
        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("✏ Edit");
        btnEdit.getStyleClass().add("button-secondary");
        btnEdit.setOnAction(e -> openNoticeForm(notice));

        Button btnPin = new Button(notice.isPinned() ? "📌 Unpin" : "📌 Pin");
        btnPin.getStyleClass().add("button-secondary");
        btnPin.setOnAction(e -> {
            if (controller.togglePin(notice)) refreshNotices();
        });

        Button btnDelete = new Button("🗑 Delete");
        btnDelete.getStyleClass().add("button-danger");
        btnDelete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete \"" + notice.getTitle() + "\"?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES && controller.deleteNotice(notice.getId())) {
                    refreshNotices();
                }
            });
        });

        btnRow.getChildren().addAll(btnEdit, btnPin, btnDelete);
        card.getChildren().addAll(titleRow, lblMeta, lblAnalytics, lblExcerpt, btnRow);
        return card;
    }

    // ─── Notice Form ──────────────────────────────────────────────────────────

    private void openNoticeForm(Notice existingNotice) {
        NoticeFormView form = existingNotice == null
                ? new NoticeFormView(stage, controller.getCurrentUser())
                : new NoticeFormView(stage, controller.getCurrentUser(), existingNotice);
        form.showAndWait();
        if (form.isNoticeSaved()) refreshNotices();
    }

    // ─── Chat Switcher ────────────────────────────────────────────────────────

    private void switchChatRoom() {
        if (currentChatRoom != null) currentChatRoom.stopRefresh();
        chatContainer.getChildren().clear();

        Section selected = cmbSectionPicker.getValue();
        if (selected != null) {
            currentChatRoom = new ChatRoomView(controller.getCurrentUser(), selected.getId());
            chatContainer.getChildren().add(currentChatRoom);
        }
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

    private void handleLogout() {
        if (currentChatRoom != null) currentChatRoom.stopRefresh();
        AuthService.logout();
        LoginView loginView = new LoginView(stage);
        Scene scene = new Scene(loginView, 480, 620);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CUConnect — Login");
        stage.centerOnScreen();
    }
}
