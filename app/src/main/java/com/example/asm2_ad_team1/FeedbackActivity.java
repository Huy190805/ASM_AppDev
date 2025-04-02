package com.example.asm2_ad_team1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {
    private EditText inputFeedback;
    private Button btnSubmit;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        inputFeedback = findViewById(R.id.input_feedback);
        btnSubmit = findViewById(R.id.btn_submit_feedback);
        username = getIntent().getStringExtra("username");

        btnSubmit.setOnClickListener(v -> {
            String message = inputFeedback.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            String feedbackId = FirebaseDatabase.getInstance().getReference("feedbacks").push().getKey();
            Map<String, Object> feedback = new HashMap<>();
            feedback.put("username", username);
            feedback.put("message", message);
            feedback.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            FirebaseDatabase.getInstance().getReference("feedbacks").child(feedbackId).setValue(feedback)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Feedback sent!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        });
    }
}
