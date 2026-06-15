package com.cuconnect.model;

public class Student extends User {
    private String studentId;
    private String department;
    private Integer sectionId;
    private Integer batch;

    public Student() {
        super();
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public Integer getSectionId() {
        return sectionId;
    }
    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }
    public Integer getBatch() {
        return batch;
    }
    public void setBatch(Integer batch) {
        this.batch = batch;
    }

}
