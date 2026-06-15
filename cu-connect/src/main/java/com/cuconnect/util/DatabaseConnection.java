package com.cuconnect.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:cuconnect.db";

    static {
        try {
            // Ensure the SQLite driver is loaded
            Class.forName("org.sqlite.JDBC");
            // Initialize the database schema if not present
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS users ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "student_id TEXT UNIQUE,"
                        + "faculty_id TEXT UNIQUE,"
                        + "name TEXT NOT NULL,"
                        + "email TEXT UNIQUE NOT NULL,"
                        + "password TEXT NOT NULL,"
                        + "role TEXT NOT NULL,"
                        + "department TEXT,"
                        + "batch INTEGER,"
                        + "section INTEGER,"
                        + "designation TEXT"
                        + ");";
                stmt.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to initialize database schema", e);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
