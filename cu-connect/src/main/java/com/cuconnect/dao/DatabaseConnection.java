package com.cuconnect.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;
import com.cuconnect.config.DatabaseConfig;

public class DatabaseConnection {
    private static Connection connection = null;
    private static boolean isSQLite = false;

    public static boolean isSQLite() {
        return isSQLite;
    }

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || !connection.isValid(2)) {
                String fullUrl  = DatabaseConfig.getDbUrl();
                String username = DatabaseConfig.getDbUsername();
                String password = DatabaseConfig.getDbPassword();

                // Always use local SQLite storage (file based) for this project
                Class.forName("org.sqlite.JDBC");
                String sqliteUrl = "jdbc:sqlite:cuconnect.db";
                // Check existing DB for schema correctness
                java.io.File dbFile = new java.io.File("cuconnect.db");
                if (dbFile.exists()) {
                    try (java.sql.Connection tempConn = java.sql.DriverManager.getConnection(sqliteUrl)) {
                        java.sql.DatabaseMetaData meta = tempConn.getMetaData();
                        try (java.sql.ResultSet rs = meta.getColumns(null, null, "users", "student_id")) {
                            if (!rs.next()) {
                                System.out.println("[DB] Missing 'student_id' column, deleting DB to recreate schema.");
                                tempConn.close();
                                dbFile.delete();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[DB] Error checking DB schema: " + e.getMessage());
                    }
                }
                connection = java.sql.DriverManager.getConnection(sqliteUrl);
                isSQLite = true;
                System.out.println("[DB] Connected to local SQLite database: " + sqliteUrl);

                // Verify and seed tables if necessary
                verifyTablesExist();
            }
        } catch (Exception e) {
            System.err.println("[DB] Error establishing database connection: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    private static void createMySQLDatabaseIfNeeded(String fullUrl, String username, String password) {
        if (fullUrl == null) return;
        String serverUrl = fullUrl;
        if (fullUrl.contains("/cuconnect")) {
            serverUrl = fullUrl.replace("/cuconnect", "/");
        }
        try (Connection rootConn = DriverManager.getConnection(serverUrl, username, password);
             Statement stmt = rootConn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS cuconnect");
        } catch (Exception e) {
            System.err.println("[DB] Warning: Root server connection failed (could not auto-create database 'cuconnect'): " + e.getMessage());
        }
    }

    private static void verifyTablesExist() {
        if (connection == null) return;
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            // Pass null for catalog to work with both SQLite and MySQL
            try (ResultSet tables = dbm.getTables(null, null, "users", null)) {
                if (!tables.next()) {
                    runSchemaScript(connection);
                }
            }
        } catch (Exception e) {
            System.err.println("[DB] Warning: Table verification/initialization failed: " + e.getMessage());
        }
    }

    private static void runSchemaScript(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            String sqlSchema = "";
            if (isSQLite) {
                System.out.println("[DB] Seeding database schema into SQLite...");
                sqlSchema = getSQLiteSchema();
            } else {
                System.out.println("[DB] Seeding database schema into MySQL...");
                File schemaFile = new File("database/schema.sql");
                if (!schemaFile.exists()) {
                    schemaFile = new File("cu-connect/database/schema.sql");
                }

                if (schemaFile.exists()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(schemaFile))) {
                        sqlSchema = br.lines().collect(Collectors.joining("\n"));
                    }
                } else {
                    System.err.println("database/schema.sql not found! Running fallback inline schema.");
                    sqlSchema = getFallbackSchema();
                }
            }

            // Split statements by semicolon and run sequentially
            String[] queries = sqlSchema.split(";");
            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query.trim());
                }
            }
            System.out.println("[DB] Database schema setup complete.");
        } catch (Exception e) {
            System.err.println("[DB] Failed to run schema script: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getSQLiteSchema() {
        return "CREATE TABLE IF NOT EXISTS users (" +
               "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  student_id VARCHAR(50) UNIQUE," +
                "  faculty_id VARCHAR(50) UNIQUE," +
                "  password VARCHAR(255) NOT NULL," +
                "  role TEXT CHECK(role IN ('STUDENT', 'FACULTY')) NOT NULL," +
                "  name VARCHAR(100) NOT NULL," +
                "  email VARCHAR(100) UNIQUE NOT NULL," +
                "  department VARCHAR(100) DEFAULT NULL," +
                "  designation VARCHAR(100) DEFAULT NULL," +
                "  batch VARCHAR(20) DEFAULT NULL," +
                "  section VARCHAR(10) DEFAULT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");" +
               "CREATE TABLE IF NOT EXISTS sections (" +
               "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
               "  name VARCHAR(50) UNIQUE NOT NULL," +
               "  department VARCHAR(100) NOT NULL" +
               ");" +
               "CREATE TABLE IF NOT EXISTS student_sections (" +
               "  user_id INTEGER PRIMARY KEY," +
               "  student_id VARCHAR(50) UNIQUE NOT NULL," +
               "  department VARCHAR(100) NOT NULL," +
               "  section_id INTEGER," +
               "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
               "  FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE SET NULL" +
               ");" +
               "CREATE TABLE IF NOT EXISTS notices (" +
               "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
               "  title VARCHAR(150) NOT NULL," +
               "  content TEXT NOT NULL," +
               "  created_by INTEGER NOT NULL," +
               "  section_id INTEGER DEFAULT NULL," +
               "  is_pinned BOOLEAN DEFAULT FALSE," +
               "  expiry_date DATE DEFAULT NULL," +
               "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
               "  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE," +
               "  FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE" +
               ");" +
               "CREATE TABLE IF NOT EXISTS notice_reads (" +
               "  user_id INTEGER NOT NULL," +
               "  notice_id INTEGER NOT NULL," +
               "  read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
               "  PRIMARY KEY (user_id, notice_id)," +
               "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
               "  FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE" +
               ");" +
               "CREATE TABLE IF NOT EXISTS messages (" +
               "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
               "  sender_id INTEGER NOT NULL," +
               "  section_id INTEGER NOT NULL," +
               "  content TEXT NOT NULL," +
               "  sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
               "  FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE," +
               "  FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE" +
               ");" +
                "CREATE INDEX IF NOT EXISTS idx_student_id ON users(student_id);" +
                "CREATE INDEX IF NOT EXISTS idx_faculty_id ON users(faculty_id);" +
                "CREATE INDEX IF NOT EXISTS idx_created_at ON notices(created_at);" +
               "CREATE INDEX IF NOT EXISTS idx_expiry ON notices(expiry_date);" +
               "CREATE INDEX IF NOT EXISTS idx_section_sent ON messages(section_id, sent_at);";
    }

    private static String getFallbackSchema() {
        return "CREATE TABLE IF NOT EXISTS users (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  student_id VARCHAR(50) UNIQUE," +
                "  faculty_id VARCHAR(50) UNIQUE," +
                "  password VARCHAR(255) NOT NULL," +
                "  role ENUM('STUDENT', 'FACULTY') NOT NULL," +
                "  name VARCHAR(100) NOT NULL," +
                "  email VARCHAR(100) UNIQUE NOT NULL," +
                "  department VARCHAR(100) DEFAULT NULL," +
                "  designation VARCHAR(100) DEFAULT NULL," +
                "  batch VARCHAR(20) DEFAULT NULL," +
                "  section VARCHAR(10) DEFAULT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");" +
                "CREATE TABLE IF NOT EXISTS sections (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  name VARCHAR(50) UNIQUE NOT NULL," +
                "  department VARCHAR(100) NOT NULL" +
                ");" +
                "CREATE TABLE IF NOT EXISTS student_sections (" +
                "  user_id INT PRIMARY KEY," +
                "  student_id VARCHAR(50) UNIQUE NOT NULL," +
                "  department VARCHAR(100) NOT NULL," +
                "  batch VARCHAR(20) DEFAULT NULL," +
                "  section VARCHAR(10) DEFAULT NULL," +
                "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ");" +
               "CREATE TABLE IF NOT EXISTS notices (" +
               "  id INT AUTO_INCREMENT PRIMARY KEY," +
               "  title VARCHAR(150) NOT NULL," +
               "  content TEXT NOT NULL," +
               "  created_by INT NOT NULL," +
               "  section_id INT DEFAULT NULL," +
               "  is_pinned BOOLEAN DEFAULT FALSE," +
               "  expiry_date DATE DEFAULT NULL," +
               "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
               "  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE," +
               "  FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE" +
               ");" +
               "CREATE TABLE IF NOT EXISTS notice_reads (" +
               "  user_id INT NOT NULL," +
               "  notice_id INT NOT NULL," +
               "  read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
               "  PRIMARY KEY (user_id, notice_id)," +
               "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
               "  FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE" +
               ");" +
               "CREATE TABLE IF NOT EXISTS messages (" +
               "  id INT AUTO_INCREMENT PRIMARY KEY," +
               "  sender_id INT NOT NULL," +
               "  section_id INT NOT NULL," +
               "  content TEXT NOT NULL," +
               "  sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
               "  FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE," +
               "  FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE" +
               ");";
    }
}
