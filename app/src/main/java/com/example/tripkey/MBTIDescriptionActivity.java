package com.example.tripkey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class MBTIDescriptionActivity extends AppCompatActivity {
    private TextView yourMbtiTextView;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbti_description);

        // UI 요소 초기화
        yourMbtiTextView = findViewById(R.id.your_mbti);
        ImageButton backButton = findViewById(R.id.button_back);
        Button mbtiTestButton = findViewById(R.id.mbti_test_button);

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        // SharedPreferences에서 로그인한 사용자 ID 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            loadUserMBTI(); // Firestore에서 MBTI 가져오기
        } else {
            Log.w("MBTI", "로그인한 사용자 ID를 찾을 수 없습니다.");
        }

        // 뒤로가기 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> finish());

        // MBTI 테스트 버튼 클릭 이벤트
        mbtiTestButton.setOnClickListener(v -> {
            Intent intent = new Intent(MBTIDescriptionActivity.this, MBTITestActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserMBTI() {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String mbti = document.getString("mbti");
                    if (mbti != null && !mbti.isEmpty()) {
                        yourMbtiTextView.setText(mbti);
                    }
                } else {
                    Log.d("MBTI", "사용자 문서가 존재하지 않습니다.");
                }
            } else {
                Log.w("MBTI", "데이터 가져오기 실패", task.getException());
            }
        });
    }
}
