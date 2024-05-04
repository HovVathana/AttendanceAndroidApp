package com.example.attendance.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.attendance.CONSTANT;
import com.example.attendance.R;
import com.example.attendance.model.Class;
import com.example.attendance.model.Schedule;
import com.example.attendance.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public class QRActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;
    private FirebaseFirestore db;

    private String studentId;

    // Radius of the Earth in kilometers
    private static final double EARTH_RADIUS_KM = 6371;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private double userLatitude;
    private double userLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qractivity);

        db = FirebaseFirestore.getInstance();

        SharedPreferences settings = getSharedPreferences(CONSTANT.LOGIN_PREF, 0);
        studentId = settings.getString("id", "");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();

                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Request location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);


        permissionCheck();

        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double lat1 = 11.5692235; // Latitude of school
                        double lon1 = 104.9147305; // Longitude of school

                        // Calculate distance between the coordinates
                        double distance = calculateDistance(lat1, lon1, userLatitude, userLongitude);

                        // if user stay within 0.5 meter radius
                        if (distance < 0.5) {
                            fetchState(result.getText());
                        } else {
                            Toast.makeText(QRActivity.this, "You not at school!", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }


    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate the differences between coordinates
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Calculate the distance using the Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        return distance; // Distance in kilometers
    }

    private void fetchState(String scheduleId) {

        db.collection("schedule")
                .document(scheduleId)
                .get()
                .addOnSuccessListener(document -> {
                    String state = "";

                    // Extract data from userDocument
                    String day_of_week = document.getString("day");
                    String start_time = document.getString("start_time");
                    String end_time = document.getString("end_time");

                    LocalDateTime currentDateTime = LocalDateTime.now();
                    DayOfWeek currentDayOfWeek = currentDateTime.getDayOfWeek();
                    String currentDay = currentDayOfWeek.name().toLowerCase(); // Convert to lowercase to match sample data

                    // Convert schedule start and end times to LocalTime
                    LocalTime scheduleStartTime = LocalTime.parse(start_time, DateTimeFormatter.ofPattern("h:mm a"));
                    LocalTime scheduleEndTime = LocalTime.parse(end_time, DateTimeFormatter.ofPattern("h:mm a"));
                    LocalTime currentTime = LocalTime.now();

                    // Compare current day with schedule day
                    if (currentDay.equals(day_of_week.toLowerCase())) {
                        // Compare current time with schedule start and end times
                        if (currentTime.isBefore(scheduleStartTime)) {
                            state = "Absent";
                        } else if (currentTime.isAfter(scheduleEndTime)) {
                            state = "Absent";
                        } else if (currentTime.isAfter(scheduleStartTime)) {
                            state = "Late";
                        } else {
                            state = "Present";
                        }
                    } else {
                        state = "Absent";
                    }

                    addAttendance(scheduleId, state, studentId);


                })
                .addOnFailureListener(classError -> {
                    // Handle error
                    Log.e("Firestore", "Error fetching schedule data: " + classError.getMessage());
                });


    }

    private void addAttendance(String schedule, String state, String student) {
        // Define the data
        Map<String, Object> data = new HashMap<>();
        data.put("schedule", schedule);
        data.put("state", state);
        data.put("student", student);
        data.put("time", Timestamp.now());

        // Add data to Firestore
        db.collection("attendance")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    // Document added successfully
                    String documentId = documentReference.getId();
                    System.out.println("Document added with ID: " + documentId);

                    Toast.makeText(QRActivity.this, "Scan successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(QRActivity.this, MainActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    System.err.println("Error adding document: " + e.getMessage());
                });
    }

    private void permissionCheck() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != 12) {
            permissionCheck();
        }
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // If permission granted, request location updates
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}