package com.example.attendance.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.example.attendance.CONSTANT;
import com.example.attendance.R;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;

public class MakeQRActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_qractivity);

        db = FirebaseFirestore.getInstance();
        spinner = findViewById(R.id.spinnerClass);

        SharedPreferences settings = getSharedPreferences(CONSTANT.LOGIN_PREF, 0);
        String teacherId = settings.getString("id", "");

        if (!teacherId.equals("")) {
            fetchClasses(teacherId);
        }


    }

    private void generateQR(String content) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 1000, 1000);
            ImageView imageViewQrCode = (ImageView) findViewById(R.id.qr_code);
            imageViewQrCode.setImageBitmap(bitmap);
        } catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    private void fetchClasses(String teacherId) {
        db.collection("class")
                .whereEqualTo("teacher", teacherId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> classIds = new ArrayList<>();
                        List<String> classNames = new ArrayList<>();
                        List<Task<QuerySnapshot>> scheduleTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String class_id = document.getId();
                            String class_name = document.getString("class_name");

                            Task<QuerySnapshot> scheduleTask = db.collection("schedule")
                                    .whereEqualTo("class", class_id)
                                    .get();
                            scheduleTasks.add(scheduleTask);

                            scheduleTask.addOnCompleteListener(scheduleTaskResult -> {
                                if (scheduleTaskResult.isSuccessful()) {
                                    for (QueryDocumentSnapshot scheduleDocument : scheduleTaskResult.getResult()) {
                                        // Retrieve schedule details and add them to classNames and classIds
                                        String id = scheduleDocument.getId();
                                        String day = scheduleDocument.getString("day");
                                        String capitalizedDay = day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase();
                                        String abbreviatedDay = capitalizedDay.substring(0, 3);
                                        String start_time = scheduleDocument.getString("start_time");
                                        String end_time = scheduleDocument.getString("end_time");
                                        String room = scheduleDocument.getString("room");

                                        String classStr = String.format("%s (%s %s - %s)", class_name, abbreviatedDay, start_time, end_time);

                                        classNames.add(classStr);
                                        classIds.add(id);
                                    }
                                } else {
                                    // Handle errors
                                }

                                // If all schedule tasks are completed, populate the spinner
                                if (scheduleTasks.size() == classNames.size()) {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MakeQRActivity.this,
                                            android.R.layout.simple_spinner_item, classNames);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spinner.setAdapter(adapter);

                                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            String selectedClassIds = classIds.get(position);

                                            generateQR(selectedClassIds);
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {
                                            // Handle nothing selected
                                        }
                                    });
                                }
                            });
                        }
                    } else {
                        // Handle errors
                    }
                });
    }

}