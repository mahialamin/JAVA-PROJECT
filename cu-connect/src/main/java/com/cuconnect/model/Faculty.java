package com.cuconnect.model;

public class Faculty extends User {
    private String department;
    private String designation;

    public Faculty() {
        super();
    }

    private String facultyId;

    public Faculty(int id, String password, String role, String name, String email,
                   String facultyId, String department, String designation) {
        super(id, password, role, name, email);
        this.facultyId = facultyId;
        this.department = department;
        this.designation = designation;
    }

    // Getter and Setter for facultyId
    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }

    // Getters and Setters
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
