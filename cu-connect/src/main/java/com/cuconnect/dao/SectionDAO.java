package com.cuconnect.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.cuconnect.model.Section;

public class SectionDAO {

    public List<Section> getAllSections() {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT * FROM sections ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                sections.add(new Section(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("department")
                ));
            }
        } catch (Exception e) {
            System.err.println("Error fetching sections: " + e.getMessage());
            e.printStackTrace();
        }
        return sections;
    }

    public boolean createSection(Section section) {
        String sql = "INSERT INTO sections (name, department) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, section.getName());
            pstmt.setString(2, section.getDepartment());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        section.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving section: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
