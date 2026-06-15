package com.cuconnect.ui;

import java.time.LocalDate;
import java.util.List;
import com.cuconnect.controller.NoticeFormController;
import com.cuconnect.model.Notice;
import com.cuconnect.model.Section;
import com.cuconnect.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Modal dialog for creating or editing a Notice.
 * Used by Faculty only.
 *
 * Design Decision: We use a separate Stage (modal window) rather than
 * embedding a form in the dashboard, so the user gets a focused writing experience.
 */
public class NoticeFormView extends Stage {

    private final NoticeFormController controller;
    // NOTE: No DAO references here — sections come from controller.loadSections()
    private final Notice existingNotice; // null when creating a new notice

    private TextField        txtTitle;
    private TextArea         txtContent;
    private ComboBox<Object> cmbSection;   // Object: "All Sections" string or a Section
    private CheckBox         chkPinned;
    private DatePicker       dpExpiry;

    /** Use this to track whether a notice was saved so the dashboard can refresh. */
    private boolean noticeSaved = false;

    // ─── Constructor: Create Mode ──────────────────────────────────────────────
    public NoticeFormView(Stage owner, User currentUser) {
        this(owner, currentUser, null);
    }

    // ─── Constructor: Edit Mode ────────────────────────────────────────────────
    public NoticeFormView(Stage owner, User currentUser, Notice existingNotice) {
        this.existingNotice = existingNotice;
        this.controller     = new NoticeFormController(currentUser);

        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        setTitle(existingNotice == null ? "Create New Notice" : "Edit Notice");
        setResizable(true);

        VBox root = buildLayout();
        Scene scene = new Scene(root, 520, 560);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        setScene(scene);

        // If editing, pre-populate fields
        if (existingNotice != null) populateFields();
    }

    private VBox buildLayout() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #111827;");

        // ── Header ────────────────────────────────────────────────────────────
        Label lblHeader = new Label(existingNotice == null ? "📋 Compose Notice" : "✏️ Edit Notice");
        lblHeader.getStyleClass().add("section-header");

        // ── Target Section ─────────────────────────────────────────────────────
        Label lblSection = new Label("Target Audience:");
        lblSection.getStyleClass().add("label-normal");

        cmbSection = new ComboBox<>();
        cmbSection.getItems().add("All Sections (General)");
        // Ask the controller — NOT the DAO directly (MVC rule)
        List<Section> sections = controller.loadSections();
        cmbSection.getItems().addAll(sections);
        cmbSection.setValue("All Sections (General)");
        cmbSection.setMaxWidth(Double.MAX_VALUE);

        // ── Title ─────────────────────────────────────────────────────────────
        Label lblTitle = new Label("Notice Title:");
        lblTitle.getStyleClass().add("label-normal");
        txtTitle = new TextField();
        txtTitle.setPromptText("Enter a clear, concise title...");
        txtTitle.getStyleClass().add("text-input");

        // ── Content ───────────────────────────────────────────────────────────
        Label lblContent = new Label("Notice Content:");
        lblContent.getStyleClass().add("label-normal");
        txtContent = new TextArea();
        txtContent.setPromptText("Write the full notice details here...");
        txtContent.setWrapText(true);
        txtContent.setPrefRowCount(8);
        txtContent.getStyleClass().add("text-input");

        // ── Options Row (Pin + Expiry) ─────────────────────────────────────────
        HBox optionsRow = new HBox(20);
        optionsRow.setAlignment(Pos.CENTER_LEFT);

        chkPinned = new CheckBox("📌 Pin this notice");
        chkPinned.setStyle("-fx-text-fill: #e5e7eb;");

        VBox expiryBox = new VBox(4);
        Label lblExpiry = new Label("Expiry Date (optional):");
        lblExpiry.getStyleClass().add("label-normal");
        dpExpiry = new DatePicker();
        dpExpiry.setPromptText("No expiry");
        dpExpiry.setStyle("-fx-background-color: #1f2937; -fx-text-fill: #f3f4f6;");
        expiryBox.getChildren().addAll(lblExpiry, dpExpiry);

        optionsRow.getChildren().addAll(chkPinned, expiryBox);

        // ── Action Buttons ────────────────────────────────────────────────────
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Cancel");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> close());

        Button btnSave = new Button(existingNotice == null ? "Publish Notice" : "Save Changes");
        btnSave.getStyleClass().add("button-primary");
        btnSave.setOnAction(e -> handleSave());

        btnRow.getChildren().addAll(btnCancel, btnSave);

        root.getChildren().addAll(
                lblHeader,
                lblSection, cmbSection,
                lblTitle, txtTitle,
                lblContent, txtContent,
                optionsRow,
                new Separator(),
                btnRow
        );
        return root;
    }

    private void handleSave() {
        String    title      = txtTitle.getText().trim();
        String    content    = txtContent.getText().trim();
        Section   section    = getSectionFromCombo();
        boolean   pinned     = chkPinned.isSelected();
        LocalDate expiryDate = dpExpiry.getValue();

        boolean success;
        if (existingNotice == null) {
            success = controller.createNotice(title, content, section, pinned, expiryDate);
        } else {
            success = controller.updateNotice(
                    existingNotice.getId(), title, content, section, pinned, expiryDate);
        }

        if (success) {
            noticeSaved = true;
            close();
        }
    }

    private void populateFields() {
        txtTitle.setText(existingNotice.getTitle());
        txtContent.setText(existingNotice.getContent());
        chkPinned.setSelected(existingNotice.isPinned());
        if (existingNotice.getExpiryDate() != null) {
            dpExpiry.setValue(existingNotice.getExpiryDate().toLocalDate());
        }
        // Set section combo
        if (existingNotice.getSectionId() != null) {
            for (Object item : cmbSection.getItems()) {
                if (item instanceof Section s && s.getId() == existingNotice.getSectionId()) {
                    cmbSection.setValue(s);
                    break;
                }
            }
        }
    }

    private Section getSectionFromCombo() {
        Object selected = cmbSection.getValue();
        return (selected instanceof Section s) ? s : null;
    }

    public boolean isNoticeSaved() { return noticeSaved; }
}
