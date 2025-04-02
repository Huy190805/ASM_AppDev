package com.example.asm2_ad_team1;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class ViewFeedbackActivity extends AppCompatActivity {

    private LinearLayout feedbackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feedback);

        feedbackList = findViewById(R.id.feedback_list);
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Fetch feedbacks from Firebase
        FirebaseDatabase.getInstance().getReference("feedbacks")
                .get().addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "No feedback found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DataSnapshot fb : snapshot.getChildren()) {
                        String user = fb.child("username").getValue(String.class);
                        String msg = fb.child("message").getValue(String.class);
                        String time = fb.child("timestamp").getValue(String.class);

                        if (user != null && msg != null && time != null) {
                            addFeedbackCard(user, msg, time);
                        }
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load feedback", Toast.LENGTH_SHORT).show()
                );
    }

    private void addFeedbackCard(String user, String msg, String time) {
        CardView card = new CardView(this);
        card.setCardElevation(4);
        card.setRadius(12);
        card.setUseCompatPadding(true);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(20, 20, 20, 20);

        TextView tvUser = new TextView(this);
        tvUser.setText(" From user: " + user);

        TextView tvTime = new TextView(this);
        tvTime.setText(" At: " + time);

        TextView tvMsg = new TextView(this);
        tvMsg.setText("Note From User: " + msg);

        inner.addView(tvUser);
        inner.addView(tvTime);
        inner.addView(tvMsg);
        card.addView(inner);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 20, 20, 20);

        feedbackList.addView(card, 0, params);
    }
}
