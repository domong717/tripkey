package com.example.tripkey;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendListAdapter adapter;
    private List<FriendItem> friendList;
    private FirebaseFirestore db;
    private String currentUserId;
    private ImageButton addFriendButton; // 친구 추가 버튼


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        // 친구 추가 버튼 설정
        addFriendButton = findViewById(R.id.button_add_friend);
        addFriendButton.setOnClickListener(v -> {
            Intent intent = new Intent(FriendListActivity.this, FriendSearchActivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendList = new ArrayList<>();
        adapter = new FriendListAdapter(friendList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");

        loadFriends();
    }

    private void loadFriends() {
        db.collection("users").document(currentUserId)
                .collection("friends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        friendList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String friendId = document.getId();
                            db.collection("users").document(friendId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String friendName = documentSnapshot.getString("userName");
                                            FriendItem friendItem = new FriendItem(friendName, friendId);
                                            friendList.add(friendItem);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    } else {
                        // 오류 처리
                    }
                });
    }
}
