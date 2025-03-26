package com.example.tripkey;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendSearchActivity extends AppCompatActivity implements FriendRequestAdapter.OnRequestClickListener {

    private EditText searchIdEditText;
    private Button searchButton;
    private TextView resultTextView;
    private FirebaseFirestore db;
    private String currentUserId;

    private Button sendRequestButton;
    private RecyclerView recyclerViewRequests;
    private FriendRequestAdapter requestAdapter;
    private List<FriendItem> requestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_search);

        db = FirebaseFirestore.getInstance();
        currentUserId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");

        searchIdEditText = findViewById(R.id.searchIdEditText);
        searchButton = findViewById(R.id.searchButton);
        resultTextView = findViewById(R.id.resultTextView);

        searchButton.setOnClickListener(v -> searchUser());

        sendRequestButton = findViewById(R.id.sendRequestButton);
        recyclerViewRequests = findViewById(R.id.recyclerViewRequests);

        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(this));
        requestList = new ArrayList<>();
        requestAdapter = new FriendRequestAdapter(requestList, this);
        recyclerViewRequests.setAdapter(requestAdapter);

        sendRequestButton.setOnClickListener(v -> sendFriendRequest(searchIdEditText.getText().toString().trim()));

        loadReceivedRequests();


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
                        sendRequestButton.setEnabled(true);
                    } else {
                        resultTextView.setText("사용자를 찾을 수 없습니다.");
                        sendRequestButton.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "검색 중 오류 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendFriendRequest(String targetUserId) {
        db.collection("users").document(currentUserId)
                .collection("sentRequests")
                .document(targetUserId)
                .set(new HashMap<>())
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(targetUserId)
                            .collection("receivedRequests")
                            .document(currentUserId)
                            .set(new HashMap<>())
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void loadReceivedRequests() {
        db.collection("users").document(currentUserId)
                .collection("receivedRequests")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        requestList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String requestId = document.getId();
                            db.collection("users").document(requestId).get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        if (userSnapshot.exists()) {
                                            String requestName = userSnapshot.getString("userName");
                                            FriendItem friendItem = new FriendItem(requestName, requestId);
                                            requestList.add(friendItem);
                                            requestAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    }
                });
    }
    @Override
    public void onAcceptClick(String targetUserId) {
        db.collection("users").document(currentUserId)
                .collection("friends")
                .document(targetUserId)
                .set(new HashMap<>())
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(targetUserId)
                            .collection("friends")
                            .document(currentUserId)
                            .set(new HashMap<>())
                            .addOnSuccessListener(aVoid2 -> {
                                db.collection("users").document(currentUserId)
                                        .collection("receivedRequests")
                                        .document(targetUserId)
                                        .delete()
                                        .addOnSuccessListener(aVoid3 -> {
                                            db.collection("users").document(targetUserId)
                                                    .collection("sentRequests")
                                                    .document(currentUserId)
                                                    .delete()
                                                    .addOnSuccessListener(aVoid4 -> {
                                                        loadReceivedRequests();
                                                        Toast.makeText(this, "친구 요청을 수락했습니다.", Toast.LENGTH_SHORT).show();
                                                    });
                                        });
                            });
                });
    }

    @Override
    public void onRejectClick(String targetUserId) {
        db.collection("users").document(currentUserId)
                .collection("receivedRequests")
                .document(targetUserId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(targetUserId)
                            .collection("sentRequests")
                            .document(currentUserId)
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "친구 요청을 거절했습니다.", Toast.LENGTH_SHORT).show();
                            });
                });
    }
}
