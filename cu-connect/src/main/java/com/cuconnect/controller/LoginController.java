package com.cuconnect.controller;

import java.util.List;
import com.cuconnect.model.Section;
import com.cuconnect.model.User;
import com.cuconnect.service.AuthService;
import com.cuconnect.service.SectionService;
import com.cuconnect.ui.LoginView;
import com.cuconnect.ui.StudentDashboardView;
import com.cuconnect.ui.FacultyDashboardView;
import com.cuconnect.util.AlertHelper;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class LoginController {

    private final AuthService    authService    = new AuthService();
    private final SectionService sectionService = new SectionService();
    private final LoginView      view;

    public LoginController(LoginView view) {
        this.view = view;
    }

    /**
     * Returns all sections for the student registration dropdown.
     * The View calls THIS method — never the DAO directly.
     *
     * @return list of all sections
     */
    public List<Section> loadSections() {
        return sectionService.getAllSections();
    }

    public void processLogin(String id, String password, Stage stage) {
        System.out.println("[LoginController] Login button clicked");
        // Trim inputs to avoid accidental whitespace issues
        id = id != null ? id.trim() : "";
        password = password != null ? password.trim() : "";
        // Trim inputs to avoid accidental whitespace issues
        id = id != null ? id.trim() : "";
        password = password != null ? password.trim() : "";
        if (id.isEmpty() || password.isEmpty()) {
            System.out.println("[LoginController] Empty fields");
            AlertHelper.showError("Login Error", "Fields cannot be empty.");
            return;
        }

        System.out.println("[LoginController] Attempting login for ID: " + id);
        User user = authService.login(id, password);
        if (user != null) {
            System.out.println("[LoginController] Login successful for user: " + user.getName());
            navigateToDashboard(user, stage);
            return;
        } else {
            System.out.println("[LoginController] Login failed for ID: " + id);
            AlertHelper.showError("Login Failed", "Invalid ID or password.");
        }

    }

    public void processRegisterStudent(String password, String name, String email,
                                       String studentId, String department, Integer batch, Integer sectionId, Stage stage) {
        if (password.isEmpty() || name.isEmpty() || email.isEmpty() ||
            studentId.isEmpty() || department.isEmpty()) {
            AlertHelper.showError("Registration Error", "All fields are required.");
            return;
        }

        boolean success = authService.registerStudent(password, name, email, studentId, department, batch, sectionId);
        if (success) {
            AlertHelper.showInfo("Registration Success", "Student account registered. Please sign in.");
            view.showLoginCard();
        } else {
            AlertHelper.showError("Registration Failed", "Student ID or Email already exists.");
        }
    }

    public void processRegisterFaculty(String facultyId, String password, String name, String email,
                                       String department, String designation, Stage stage) {
        if (password.isEmpty() || name.isEmpty() || email.isEmpty() ||
            department.isEmpty() || designation.isEmpty()) {
            AlertHelper.showError("Registration Error", "All fields are required.");
            return;
        }

        boolean success = authService.registerFaculty(facultyId, password, name, email, department, designation);
        if (success) {
            AlertHelper.showInfo("Registration Success", "Faculty account registered. Please sign in.");
            view.showLoginCard();
        } else {
            AlertHelper.showError("Registration Failed", "Faculty ID or Email already exists.");
        }
    }

    private void navigateToDashboard(User user, Stage stage) {
        try {
            if ("STUDENT".equals(user.getRole())) {
                StudentDashboardView dashboard = new StudentDashboardView(stage, user);
                Scene scene = new Scene(dashboard, 1000, 680);
                scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                stage.setScene(scene);
            } else if ("FACULTY".equals(user.getRole())) {
                FacultyDashboardView dashboard = new FacultyDashboardView(stage, user);
                Scene scene = new Scene(dashboard, 1000, 680);
                scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                stage.setScene(scene);
            }
            stage.centerOnScreen();
        } catch (Exception e) {
            AlertHelper.showError("Navigation Error", "Could not load dashboard view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
