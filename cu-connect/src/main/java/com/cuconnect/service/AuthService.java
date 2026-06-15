package com.cuconnect.service;

import com.cuconnect.dao.UserDAO;
import com.cuconnect.model.User;
import com.cuconnect.model.Student;
import com.cuconnect.model.Faculty;
import com.cuconnect.util.PasswordHasher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.cuconnect.util.DatabaseConnection;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private static User currentUser = null;

    // Retrieve full user (Student or Faculty) by ID using UserDAO
    public User getUserById(String id) {
        // Delegate to DAO which returns the appropriate subclass with all profile data
        return userDAO.getUserById(id);
    }

    // Retrieve user by email for duplicate check
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

    public User login(String id, String password) {
        User user = userDAO.getUserById(id);
        if (user != null) {
            String hashedInput = PasswordHasher.hash(password);
            if (user.getPassword().equals(hashedInput)) {
                currentUser = user;
                com.cuconnect.util.SessionManager.setCurrentUser(user);
                return user;
            }
        }
        return null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
        com.cuconnect.util.SessionManager.clearSession();
    }

    public boolean registerStudent(String password, String name, String email,
                                   String studentId, String department, Integer batch, Integer sectionId) {
        // Duplicate checks
        if (userDAO.getUserById(studentId) != null) {
            // ID already exists
            return false;
        }
        if (userDAO.getUserByEmail(email) != null) {
            // Email already exists
            return false;
        }
        String hashedPassword = PasswordHasher.hash(password);
        Student student = new Student();
        student.setPassword(hashedPassword);
        student.setName(name);
        student.setEmail(email);
        student.setStudentId(studentId);
        student.setDepartment(department);
        student.setBatch(batch);
        student.setSectionId(sectionId);
        return userDAO.createStudent(student);
    }

    public boolean registerFaculty(String facultyId, String password, String name, String email,
                                   String department, String designation) {
        // Duplicate checks
        if (userDAO.getUserById(facultyId) != null) {
            return false; // ID already exists
        }
        if (userDAO.getUserByEmail(email) != null) {
            return false; // Email already exists
        }
        String hashedPassword = PasswordHasher.hash(password);
        Faculty faculty = new Faculty();
        faculty.setFacultyId(facultyId);
        faculty.setPassword(hashedPassword);
        faculty.setName(name);
        faculty.setEmail(email);
        faculty.setDepartment(department);
        faculty.setDesignation(designation);
        faculty.setRole("FACULTY");
        return userDAO.createFaculty(faculty);
    }
}
