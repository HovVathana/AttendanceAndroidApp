package com.example.attendance.model;

import com.google.firebase.Timestamp;
import com.google.type.DateTime;

public class Attendance {

    private User student;
    private Schedule schedule;
    private String state;
    private Timestamp register_time;

    public Attendance() {
    }

    public Attendance(User student, Schedule schedule, String state, Timestamp register_time) {
        this.student = student;
        this.schedule = schedule;
        this.state = state;
        this.register_time = register_time;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getRegister_time() {
        return register_time;
    }

    public void setRegister_time(Timestamp register_time) {
        this.register_time = register_time;
    }
}