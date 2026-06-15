package com.cuconnect;

import com.cuconnect.dao.DatabaseConnection;
import com.cuconnect.dao.SectionDAO;
import com.cuconnect.model.Section;
import com.cuconnect.service.AuthService;
import com.cuconnect.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * CUConnect — Main JavaFX Application Entry Point.
 *
 * Startup sequence:
 * 1. Initialize DatabaseConnection (creates DB + tables if missing)
 * 2. Seed default sections and demo accounts if the DB is empty
 * 3. Apply CSS stylesheet
 * 4. Show the Login screen
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Step 1: Initialize database (auto-creates schema if needed)
        try {
            DatabaseConnection.getConnection();
        } catch (Exception e) {
            System.err.println("[Main] Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Step 2: Seed demo data on first run
        seedDemoDataIfEmpty();

        // Step 3: Build and show Login screen
        primaryStage.setTitle("CUConnect — Login");

        LoginView loginView = new LoginView(primaryStage);
        Scene scene = new Scene(loginView, 480, 620);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    // ─── Demo Data Seeding ────────────────────────────────────────────────────

    private void seedDemoDataIfEmpty() {
        SectionDAO  sectionDAO  = new SectionDAO();
        AuthService authService = new AuthService();

        // Only seed if no sections exist (first-run detection)
        if (!sectionDAO.getAllSections().isEmpty()) {
            System.out.println("[Main] Database already populated. Skipping seed.");
            return;
        }

        System.out.println("[Main] First run detected. Seeding demo data...");

        // ── Sections ──────────────────────────────────────────────────────────
        Section cseA = new Section(0, "CSE-A", "Computer Science");
        Section cseB = new Section(0, "CSE-B", "Computer Science");
        Section eeeA = new Section(0, "EEE-A", "Electrical Engineering");
        Section bbaA = new Section(0, "BBA-A", "Business Administration");

        sectionDAO.createSection(cseA);
        sectionDAO.createSection(cseB);
        sectionDAO.createSection(eeeA);
        sectionDAO.createSection(bbaA);
        System.out.println("[Main] ✓ Sections seeded: CSE-A, CSE-B, EEE-A, BBA-A");

        // ── Demo Student ──────────────────────────────────────────────────────
        boolean studentCreated = authService.registerStudent(
                "password",              // password (will be SHA-256 hashed)
                "Alice Rahman",         // full name
                "alice@cu.edu",         // email
                "STU-2024-001",         // student ID
                "Computer Science",     // department
                2024,                   // batch (year)
                cseA.getId()            // assigned section
        );
        System.out.println("[Main] " + (studentCreated ? "✓" : "✗") +
                " Demo student: username='student'  password='password'");

        // ── Demo Faculty ──────────────────────────────────────────────────────
        boolean facultyCreated = authService.registerFaculty(
                "faculty",              // username
                "password",             // password (will be SHA-256 hashed)
                "Dr. Kamal Hossain",    // full name
                "kamal@cu.edu",         // email
                "Computer Science",     // department
                "Associate Professor"   // designation
        );
        System.out.println("[Main] " + (facultyCreated ? "✓" : "✗") +
                " Demo faculty:  username='faculty'  password='password'");

        System.out.println("[Main] ─── Seed complete ───────────────────────────");
    }

    // ─── Entry Point ──────────────────────────────────────────────────────────

    public static void main(String[] args) {
        launch(args);
    }
}
