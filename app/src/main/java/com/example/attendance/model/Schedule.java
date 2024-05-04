package com.example.attendance.model;

public class Schedule {

    private Class class_course;
    private String day;
    private String start_time;
    private String end_time;
    private String room;

    public Schedule() {
    }

    public Schedule(Class class_course, String day, String start_time, String end_time, String room) {
        this.class_course = class_course;
        this.day = day;
        this.start_time = start_time;
        this.end_time = end_time;
        this.room = room;
    }

    public Class getClass_course() {
        return class_course;
    }

    public void setClass_course(Class class_course) {
        this.class_course = class_course;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
