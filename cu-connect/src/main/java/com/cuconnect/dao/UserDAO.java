package com.cuconnect.dao;

import java.sql.Connection;
import com.cuconnect.util.DatabaseConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.cuconnect.model.User;
import com.cuconnect.model.Student;
import com.cuconnect.model.Faculty;

public class UserDAO {

    public boolean createStudent(Student student) {
        Connection conn = null;
        PreparedStatement pstmtUser = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlUser = "INSERT INTO users (student_id, password, role, name, email, department, batch, section) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            pstmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            pstmtUser.setString(1, student.getStudentId());
            pstmtUser.setString(2, student.getPassword());
            pstmtUser.setString(3, "STUDENT");
            pstmtUser.setString(4, student.getName());
            pstmtUser.setString(5, student.getEmail());
            pstmtUser.setString(6, student.getDepartment());
            pstmtUser.setObject(7, student.getBatch(), java.sql.Types.INTEGER);
            pstmtUser.setObject(8, student.getSectionId(), java.sql.Types.INTEGER);

            int affectedRows = pstmtUser.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            generatedKeys = pstmtUser.getGeneratedKeys();
            if (generatedKeys.next()) {
                student.setId(generatedKeys.getInt(1));
            } else {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (pstmtUser != null) pstmtUser.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (Exception e) { e.printStackTrace(); }
        }
        return false;
    }

    public boolean createFaculty(Faculty faculty) {
        Connection conn = null;
        PreparedStatement pstmtUser = null;
        ResultSet generatedKeys = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            String sqlUser = "INSERT INTO users (faculty_id, password, role, name, email, department, designation) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            pstmtUser.setString(1, faculty.getFacultyId());
            pstmtUser.setString(2, faculty.getPassword());
            pstmtUser.setString(3, "FACULTY");
            pstmtUser.setString(4, faculty.getName());
            pstmtUser.setString(5, faculty.getEmail());
            pstmtUser.setString(6, faculty.getDepartment());
            pstmtUser.setString(7, faculty.getDesignation());
            int affectedRows = pstmtUser.executeUpdate();
            if (affectedRows == 0) { conn.rollback(); return false; }
            generatedKeys = pstmtUser.getGeneratedKeys();
            if (generatedKeys.next()) { faculty.setId(generatedKeys.getInt(1)); } else { conn.rollback(); return false; }
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
        } finally {
            try { if (generatedKeys != null) generatedKeys.close(); if (pstmtUser != null) pstmtUser.close(); if (conn != null) conn.setAutoCommit(true); } catch (Exception e) { e.printStackTrace(); }
        }
        return false;
    }

    // Retrieve user by either student_id or faculty_id
    public User getUserById(String id) {
        String sql = "SELECT id, name, password, role, email, student_id, faculty_id, department, batch, section, designation FROM users WHERE student_id = ? OR faculty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Determine role and populate appropriate subclass
                    String role = rs.getString("role");
                    if ("STUDENT".equalsIgnoreCase(role)) {
                        Student student = new Student();
                        student.setId(rs.getInt("id"));
                        student.setName(rs.getString("name"));
                        student.setPassword(rs.getString("password"));
                        student.setRole(role);
                        student.setEmail(rs.getString("email"));
                        student.setStudentId(rs.getString("student_id"));
                        student.setDepartment(rs.getString("department"));
                        // Safe retrieval for nullable INTEGER columns
                        Integer batch = null;
                        int batchVal = rs.getInt("batch");
                        if (!rs.wasNull()) {
                            batch = batchVal;
                        }
                        student.setBatch(batch);
                        Integer section = null;
                        int sectionVal = rs.getInt("section");
                        if (!rs.wasNull()) {
                            section = sectionVal;
                        }
                        student.setSectionId(section);
                        return student;
                    } else if ("FACULTY".equalsIgnoreCase(role)) {
                        Faculty faculty = new Faculty();
                        faculty.setId(rs.getInt("id"));
                        faculty.setName(rs.getString("name"));
                        faculty.setPassword(rs.getString("password"));
                        faculty.setRole(role);
                        faculty.setEmail(rs.getString("email"));
                        faculty.setFacultyId(rs.getString("faculty_id"));
                        faculty.setDepartment(rs.getString("department"));
                        faculty.setDesignation(rs.getString("designation"));
                        return faculty;
                    } else {
                        // Generic user fallback
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setName(rs.getString("name"));
                        user.setPassword(rs.getString("password"));
                        user.setRole(role);
                        user.setEmail(rs.getString("email"));
                        return user;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT id, name, password, role, email FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    user.setEmail(rs.getString("email"));
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
