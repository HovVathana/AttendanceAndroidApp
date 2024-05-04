package com.example.attendance.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.attendance.R;
import com.example.attendance.adapter.AttendanceAdapter;
import com.example.attendance.model.Attendance;
import com.example.attendance.model.Class;
import com.example.attendance.model.Schedule;
import com.example.attendance.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HistoryFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private List<Attendance> attendanceList;

    private AttendanceAdapter adapter;


    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        attendanceList = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.attendanceView);
        adapter = new AttendanceAdapter(attendanceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        fetchAttendanceData();

    }

    private void fetchAttendanceData() {
        db.collection("attendance")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Handle errors
                        Log.e("Firestore", "Error fetching schedule data: " + error.getMessage());
                        return;
                    }

                    // Clear the scheduleList before adding new items
                    attendanceList.clear();

                    for (QueryDocumentSnapshot document : value) {
                        // Extract data
                        String scheduleStr = document.getString("schedule");
                        String studentStr = document.getString("student");
                        Timestamp time = document.getTimestamp("time");
                        String state = document.getString("state");

                        if (scheduleStr != null) {
                            db.collection("schedule")
                                    .document(scheduleStr)
                                    .get()
                                    .addOnCompleteListener(scheduleDocumentTask -> {
                                        if (scheduleDocumentTask.isSuccessful()) {
                                            DocumentSnapshot scheduleDocument = scheduleDocumentTask.getResult();
                                            if (scheduleDocument != null && scheduleDocument.exists()) {
                                                String classStr = scheduleDocument.getString("class");
                                                String day = scheduleDocument.getString("day");
                                                String start_time = scheduleDocument.getString("start_time");
                                                String end_time = scheduleDocument.getString("end_time");
                                                String room = scheduleDocument.getString("room");

                                                if (classStr != null) {
                                                    db.collection("class")
                                                            .document(classStr)
                                                            .get()
                                                            .addOnCompleteListener(classDocumentTask -> {
                                                                if (classDocumentTask.isSuccessful()) {
                                                                    DocumentSnapshot classDocument = classDocumentTask.getResult();
                                                                    if (classDocument != null && classDocument.exists()) {
                                                                        String class_name = classDocument.getString("class_name");
                                                                        String teacherStr = classDocument.getString("teacher");

                                                                        if (teacherStr != null) {
                                                                            db.collection("users")
                                                                                    .document(teacherStr)
                                                                                    .get()
                                                                                    .addOnSuccessListener(userDocument -> {
                                                                                        // Extract data from userDocument
                                                                                        String email = userDocument.getString("email");
                                                                                        String first_name = userDocument.getString("first_name");
                                                                                        String last_name = userDocument.getString("last_name");
                                                                                        String role = userDocument.getString("role");

                                                                                        User teacher = new User("1", first_name, last_name, email, role);

                                                                                        List<User> students = new ArrayList<>();
                                                                                        Class class_course = new Class(class_name, teacher, students);

                                                                                        // Create Schedule object and add to the list
                                                                                        Schedule schedule = new Schedule(class_course, day, start_time, end_time, room);
//                                                                                        scheduleList.add(schedule);

                                                                                        if (studentStr != null) {
                                                                                            db.collection("users")
                                                                                                    .document(studentStr)
                                                                                                    .get()
                                                                                                    .addOnSuccessListener(uDocument -> {
                                                                                                        // Extract data from userDocument
                                                                                                        String em = uDocument.getString("email");
                                                                                                        String f_name = uDocument.getString("first_name");
                                                                                                        String l_name = uDocument.getString("last_name");
                                                                                                        String role1 = uDocument.getString("role");

                                                                                                        User student = new User("1", f_name, l_name, em, role1);


                                                                                                        Attendance attendance = new Attendance(student, schedule, state, time);

                                                                                                        attendanceList.add(attendance);

                                                                                                        System.out.println("here" + attendanceList);

                                                                                                        // Notify the adapter if needed
                                                                                                        adapter.notifyDataSetChanged();
                                                                                                    })
                                                                                                    .addOnFailureListener(classError -> {
                                                                                                        // Handle error
                                                                                                        Log.e("Firestore", "Error fetching user data: " + classError.getMessage());
                                                                                                    });
                                                                                        }

                                                                                        // Notify the adapter if needed
                                                                                        adapter.notifyDataSetChanged();
                                                                                    })
                                                                                    .addOnFailureListener(classError -> {
                                                                                        // Handle error
                                                                                        Log.e("Firestore", "Error fetching user data: " + classError.getMessage());
                                                                                    });
                                                                        }
                                                                    }
                                                                } else {
                                                                    // Handle unsuccessful task
                                                                    Exception e = classDocumentTask.getException();
                                                                    if (e != null) {
                                                                        Log.e("Firestore", "Error fetching class data: " + e.getMessage());
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        } else {
                                            // Handle unsuccessful task
                                            Exception e = scheduleDocumentTask.getException();
                                            if (e != null) {
                                                Log.e("Firestore", "Error fetching class data: " + e.getMessage());
                                            }
                                        }
                                    });
                        }



                    }
                });
    }




}