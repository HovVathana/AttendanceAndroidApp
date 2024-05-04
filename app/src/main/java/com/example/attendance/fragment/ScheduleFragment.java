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
import com.example.attendance.adapter.ScheduleAdapter;
import com.example.attendance.model.Class;
import com.example.attendance.model.Schedule;
import com.example.attendance.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ScheduleFragment extends Fragment {

    private FirebaseFirestore db;
    private List<Schedule> scheduleList;
    private ScheduleAdapter adapter;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        scheduleList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.classView);
        adapter = new ScheduleAdapter(scheduleList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        fetchScheduleData();


    }

    private void fetchScheduleData() {
        db.collection("schedule")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Handle errors
                        Log.e("Firestore", "Error fetching schedule data: " + error.getMessage());
                        return;
                    }

                    // Clear the scheduleList before adding new items
                    scheduleList.clear();

                    for (QueryDocumentSnapshot document : value) {
                        // Extract data
                        String classStr = document.getString("class");
                        String day = document.getString("day");
                        String start_time = document.getString("start_time");
                        String end_time = document.getString("end_time");
                        String room = document.getString("room");

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
                                                                scheduleList.add(schedule);

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
                });
    }



}
