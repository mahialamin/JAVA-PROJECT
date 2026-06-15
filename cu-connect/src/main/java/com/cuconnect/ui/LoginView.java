package com.cuconnect.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.cuconnect.controller.LoginController;
import com.cuconnect.model.Section;
import java.util.List;

public class LoginView extends StackPane {
    private final Stage           stage;
    private final LoginController controller;
    // NOTE: No DAO references here! Data comes from the controller.

    // Containers
    private VBox loginCard;
    private VBox registerCard;

    // Login Fields
    private TextField txtLoginUsername;
    private PasswordField txtLoginPassword;
    private TextField txtLoginPasswordVisible;
    private CheckBox chkShowPassword;

    // Common Register Fields
    private TextField txtRegId;
    private PasswordField txtRegPassword;
    private TextField txtRegName;
    private TextField txtRegEmail;
    private ComboBox<String> cmbRegRole;

    // Student specific fields
    private VBox boxStudentFields;
    private TextField txtRegStudentDept;
    private TextField txtRegBatch;
    private ComboBox<Section> cmbRegSection;

    // Faculty specific fields
    private VBox boxFacultyFields;
    private TextField txtRegFacultyDept;
    private TextField txtRegFacultyDesignation;

    public LoginView(Stage stage) {
        this.stage = stage;
        this.controller = new LoginController(this);

        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(20));

        // Create the Login and Register sub-panels
        createLoginCard();
        createRegisterCard();

        // Start by showing the Login panel
        showLoginCard();
    }

    public void showLoginCard() {
        this.getChildren().clear();
        this.getChildren().add(loginCard);
    }

    public void showRegisterCard() {
        this.getChildren().clear();
        this.getChildren().add(registerCard);
        refreshSections();
    }

    private void createLoginCard() {
        loginCard = new VBox(15);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxSize(400, 500);
        loginCard.setStyle("-fx-background-color: #1f2937; -fx-padding: 30; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 8);");

        Label lblTitle = new Label("CUConnect");
        lblTitle.getStyleClass().add("header-title");

        Label lblSub = new Label("Connecting Campus Communities");
        lblSub.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");

        VBox boxUser = new VBox(5);
        boxUser.setAlignment(Pos.CENTER_LEFT);
        Label lblUser = new Label("ID:");
        lblUser.getStyleClass().add("label-normal");
        txtLoginUsername = new TextField();
        txtLoginUsername.setPromptText("Enter your ID");
        boxUser.getChildren().addAll(lblUser, txtLoginUsername);

        VBox boxPass = new VBox(5);
        boxPass.setAlignment(Pos.CENTER_LEFT);
        Label lblPass = new Label("Password:");
        lblPass.getStyleClass().add("label-normal");
        
        StackPane passPane = new StackPane();
        txtLoginPassword = new PasswordField();
        txtLoginPassword.setPromptText("Enter your password");
        txtLoginPasswordVisible = new TextField();
        txtLoginPasswordVisible.setPromptText("Enter your password");
        txtLoginPasswordVisible.setVisible(false);
        
        passPane.getChildren().addAll(txtLoginPassword, txtLoginPasswordVisible);
        
        chkShowPassword = new CheckBox("Show Password");
        chkShowPassword.setStyle("-fx-text-fill: #9ca3af;");
        chkShowPassword.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                txtLoginPasswordVisible.setText(txtLoginPassword.getText());
                txtLoginPasswordVisible.setVisible(true);
                txtLoginPassword.setVisible(false);
            } else {
                txtLoginPassword.setText(txtLoginPasswordVisible.getText());
                txtLoginPassword.setVisible(true);
                txtLoginPasswordVisible.setVisible(false);
            }
        });

        boxPass.getChildren().addAll(lblPass, passPane, chkShowPassword);

        Button btnLogin = new Button("Sign In");
        btnLogin.getStyleClass().add("button-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> {
            String pass = chkShowPassword.isSelected() ? txtLoginPasswordVisible.getText() : txtLoginPassword.getText();
            controller.processLogin(
                txtLoginUsername.getText().trim(),
                pass,
                stage
            );
        });

        Hyperlink linkRegister = new Hyperlink("Don't have an account? Register here");
        linkRegister.getStyleClass().add("hyperlink-custom");
        linkRegister.setOnAction(e -> showRegisterCard());

        loginCard.getChildren().addAll(lblTitle, lblSub, boxUser, boxPass, btnLogin, linkRegister);
    }

    private void createRegisterCard() {
        registerCard = new VBox(10);
        registerCard.setAlignment(Pos.CENTER);
        registerCard.setMaxSize(420, 620);
        registerCard.setStyle("-fx-background-color: #1f2937; -fx-padding: 25; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 8);");

        Label lblTitle = new Label("Create Account");
        lblTitle.getStyleClass().add("section-header");

        // Basic Info Form Grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        // ID (Student ID or Faculty ID)
        grid.add(createLabel("ID:"), 0, 0);
        txtRegId = new TextField();
        grid.add(txtRegId, 1, 0);

        // Password
        grid.add(createLabel("Password:"), 0, 1);
        txtRegPassword = new PasswordField();
        grid.add(txtRegPassword, 1, 1);

        // Name
        grid.add(createLabel("Full Name:"), 0, 2);
        txtRegName = new TextField();
        grid.add(txtRegName, 1, 2);

        // Email
        grid.add(createLabel("Email:"), 0, 3);
        txtRegEmail = new TextField();
        grid.add(txtRegEmail, 1, 3);

        // Role Selector
        grid.add(createLabel("Role:"), 0, 4);
        cmbRegRole = new ComboBox<>();
        cmbRegRole.getItems().addAll("STUDENT", "FACULTY");
        cmbRegRole.setValue("STUDENT");
        cmbRegRole.setMaxWidth(Double.MAX_VALUE);
        grid.add(cmbRegRole, 1, 4);

        // Dynamic Panels for Student / Faculty
        createStudentFields();
        createFacultyFields();

        VBox dynamicContainer = new VBox(5);
        dynamicContainer.getChildren().add(boxStudentFields); // Default

        cmbRegRole.setOnAction(e -> {
            dynamicContainer.getChildren().clear();
            if ("STUDENT".equals(cmbRegRole.getValue())) {
                dynamicContainer.getChildren().add(boxStudentFields);
            } else {
                dynamicContainer.getChildren().add(boxFacultyFields);
            }
        });

        // Register Button
        Button btnRegister = new Button("Create Account");
        btnRegister.getStyleClass().add("button-success");
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setOnAction(e -> handleRegistrationSubmit());

        // Back link
        Hyperlink linkLogin = new Hyperlink("Already have an account? Sign In");
        linkLogin.getStyleClass().add("hyperlink-custom");
        linkLogin.setOnAction(e -> showLoginCard());

        registerCard.getChildren().addAll(lblTitle, grid, dynamicContainer, btnRegister, linkLogin);
    }

    private void createStudentFields() {
        boxStudentFields = new VBox(8);
        boxStudentFields.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        // Student ID already captured as ID field, no separate field needed
        // Department
        grid.add(createLabel("Dept Name:"), 0, 0);
        txtRegStudentDept = new TextField();
        grid.add(txtRegStudentDept, 1, 0);

        // Batch
        grid.add(createLabel("Batch:"), 0, 1);
        txtRegBatch = new TextField();
        grid.add(txtRegBatch, 1, 1);

        // Section
        grid.add(createLabel("Section:"), 0, 2);
        cmbRegSection = new ComboBox<>();
        cmbRegSection.setMaxWidth(Double.MAX_VALUE);
        grid.add(cmbRegSection, 1, 2);

        boxStudentFields.getChildren().add(grid);
    }

    private void createFacultyFields() {
        boxFacultyFields = new VBox(8);
        boxFacultyFields.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        grid.add(createLabel("Dept Name:"), 0, 0);
        txtRegFacultyDept = new TextField();
        grid.add(txtRegFacultyDept, 1, 0);

        grid.add(createLabel("Designation:"), 0, 1);
        txtRegFacultyDesignation = new TextField();
        grid.add(txtRegFacultyDesignation, 1, 1);

        boxFacultyFields.getChildren().add(grid);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-normal");
        return label;
    }

    /**
     * Loads sections from the controller (which delegates to SectionService → SectionDAO).
     * The View never touches the DAO directly — that would violate MVC.
     */
    private void refreshSections() {
        cmbRegSection.getItems().clear();
        // Ask the controller, not the DAO
        List<Section> list = controller.loadSections();
        cmbRegSection.getItems().addAll(list);
        if (!list.isEmpty()) {
            cmbRegSection.setValue(list.get(0));
        }
    }

    private void handleRegistrationSubmit() {
        // ID field (Student ID or Faculty ID)
        String id = txtRegId.getText().trim();
        String password = txtRegPassword.getText();
        String name = txtRegName.getText().trim();
        String email = txtRegEmail.getText().trim();
        String role = cmbRegRole.getValue();

        if ("STUDENT".equals(role)) {
            // Student specific fields
            String department = txtRegStudentDept.getText().trim();
            String batchStr = txtRegBatch.getText().trim();
            Integer batch = null;
            try {
                batch = Integer.parseInt(batchStr);
            } catch (NumberFormatException e) {
                // If parsing fails, leave batch as null; validation can be added later
            }
            Section selectedSection = cmbRegSection.getValue();
            Integer sectionId = selectedSection != null ? selectedSection.getId() : null;

            controller.processRegisterStudent(password, name, email, id, department, batch, sectionId, stage);
        } else {
            // Faculty specific fields
            String department = txtRegFacultyDept.getText().trim();
            String designation = txtRegFacultyDesignation.getText().trim();

            controller.processRegisterFaculty(id, password, name, email, department, designation, stage);
        }
    }
}
