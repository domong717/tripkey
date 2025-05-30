package com.example.tripkey;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


import androidx.appcompat.app.AppCompatActivity;

public class FeedbackActivity extends AppCompatActivity {

    private RatingBar rating1, rating2, rating3, rating4, rating5, rating6, rating7;
    private Button buttonSubmit;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rating1 = findViewById(R.id.rating1);
        rating2 = findViewById(R.id.rating2);
        rating3 = findViewById(R.id.rating3);
        rating4 = findViewById(R.id.rating4);
        rating5 = findViewById(R.id.rating5);
        rating6 = findViewById(R.id.rating6);
        rating7 = findViewById(R.id.rating7);
        buttonSubmit = findViewById(R.id.button_submit);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 별점 값 받아오기
                float score1 = rating1.getRating();
                float score2 = rating2.getRating();
                float score3 = rating3.getRating();
                float score4 = rating4.getRating();
                float score5 = rating5.getRating();
                float score6 = rating6.getRating();
                float score7 = rating7.getRating();

                // 유효성 검사 (모든 문항 점수 체크)
                if (score1 == 0 || score2 == 0 || score3 == 0 || score4 == 0 || score5 == 0 || score6 == 0 || score7 == 0) {
                    Toast.makeText(FeedbackActivity.this, "모든 문항에 별점을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String userId = sharedPref.getString("userId", null);

                if (userId == null) {
                    Toast.makeText(FeedbackActivity.this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firestore에 저장할 데이터 구성
                Map<String, String> feedbackData = new HashMap<>();
                feedbackData.put("1", score1 + "/5");
                feedbackData.put("2", score2 + "/5");
                feedbackData.put("3", score3 + "/5");
                feedbackData.put("4", score4 + "/5");
                feedbackData.put("5", score5 + "/5");
                feedbackData.put("6", score6 + "/5");
                feedbackData.put("7", score7 + "/5");
                db.collection("users")
                        .document(userId)
                        .collection("feedback")
                        .document("feedback_result") // document 이름을 고정하거나, .add(feedbackData)로 랜덤 ID 생성도 가능
                        .set(feedbackData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(FeedbackActivity.this, "피드백이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                            finish(); // 저장 후 액티비티 종료
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(FeedbackActivity.this, "저장에 실패했습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }
}
