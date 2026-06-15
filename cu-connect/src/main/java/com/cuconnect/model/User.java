package com.cuconnect.model;

public class User {
    private int id;
    private String name;

    private String password;
    private String role; // "STUDENT" or "FACULTY"

    private String email;

    public User() {}

    public User(int id, String password, String role, String name, String email) {
        this.id = id;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }



    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }



    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
