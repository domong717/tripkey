package com.example.tripkey;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class FriendSearchActivity extends AppCompatActivity {

    private EditText searchIdEditText;
    private Button searchButton;
    private TextView resultTextView;
    private Button addFriendButton;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_search);

        db = FirebaseFirestore.getInstance();
        currentUserId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");

        searchIdEditText = findViewById(R.id.searchIdEditText);
        searchButton = findViewById(R.id.searchButton);
        resultTextView = findViewById(R.id.resultTextView);
        addFriendButton = findViewById(R.id.addFriendButton);

        searchButton.setOnClickListener(v -> searchUser());
        addFriendButton.setOnClickListener(v -> addFriend());


        // 뒤로가기 버튼 클릭 이벤트
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());
    }

    private void searchUser() {
        String searchId = searchIdEditText.getText().toString().trim();
        if (searchId.isEmpty()) {
            Toast.makeText(this, "검색할 ID를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(searchId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("userName");
                        resultTextView.setText("사용자 찾음: " + userName);
                        addFriendButton.setEnabled(true);
                    } else {
                        resultTextView.setText("사용자를 찾을 수 없습니다.");
                        addFriendButton.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "검색 중 오류 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addFriend() {
        String friendId = searchIdEditText.getText().toString().trim();
        if (friendId.equals(currentUserId)) {
            Toast.makeText(this, "자기 자신을 친구로 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(currentUserId)
                .collection("friends").document(friendId)
                .set(new HashMap<>())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "친구 추가 성공!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "친구 추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
