package com.cuconnect.config;

import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("Warning: db.properties not found on classpath. Using default database settings.");
                properties.setProperty("db.url", "jdbc:mysql://localhost:3306/cuconnect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                properties.setProperty("db.username", "root");
                properties.setProperty("db.password", "root");
            } else {
                properties.load(input);
            }
        } catch (Exception e) {
            System.err.println("Error loading db.properties: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public static String getDbUsername() {
        return properties.getProperty("db.username");
    }

    public static String getDbPassword() {
        return properties.getProperty("db.password");
    }
}
