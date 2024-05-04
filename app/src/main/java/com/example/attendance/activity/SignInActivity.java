package com.example.attendance.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.attendance.CONSTANT;
import com.example.attendance.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


public class SignInActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        SharedPreferences settings = getSharedPreferences(CONSTANT.LOGIN_PREF, 0);
        if (settings.getString("logged", "").toString().equals("logged")) {
            if (settings.getString("role", "user").toString().equals("teacher")) {
                Intent intent = new Intent(SignInActivity.this, TeacherMainActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }

        db = FirebaseFirestore.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this::onLoginClicked);



    }

    public void onLoginClicked(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        TextView txtError = findViewById(R.id.txtError);

        // Query Firestore to find a document with the provided email
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                String storedPassword = document.getString("password");
                                if (password.equals(storedPassword)) {
                                    String role = document.getString("role");
                                    String id = document.getId();
                                    String first_name = document.getString("first_name");
                                    String last_name = document.getString("last_name");

                                    SharedPreferences.Editor editor = getSharedPreferences(CONSTANT.LOGIN_PREF, 0).edit();
                                    editor.putString("id", id);
                                    editor.putString("first_name", first_name);
                                    editor.putString("last_name", last_name);
                                    editor.putString("email", email);
                                    editor.putString("password", password);
                                    editor.putString("role", role);
                                    editor.putString("logged", "logged");
                                    editor.apply();

                                    if (role.equals("teacher")) {
                                        Intent intent = new Intent(SignInActivity.this, TeacherMainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }

                                    txtError.setVisibility(View.GONE); // Hide the error message
                                    // Proceed with your login logic here
                                } else {
                                    txtError.setVisibility(View.VISIBLE);
                                    txtError.setText("Wrong password!");
                                }
                            } else {
                                txtError.setVisibility(View.VISIBLE);
                                txtError.setText("Email not found!");
                            }
                        } else {
                            txtError.setVisibility(View.VISIBLE);
                            txtError.setText("Error: " + task.getException().getMessage()); // Print the error message
                        }
                    }
                });

    }

}
