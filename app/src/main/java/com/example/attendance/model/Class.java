package com.example.attendance.model;

import java.util.List;

public class Class {

    private String class_name;

    private User teacher;
    private List<User> students;

    public Class() {
        // Required empty constructor
    }

    public Class(String class_name, User teacher, List<User> students) {
        this.class_name = class_name;
        this.teacher = teacher;
        this.students = students;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public List<User> getStudents() {
        return students;
    }

    public void setStudents(List<User> students) {
        this.students = students;
    }
}

