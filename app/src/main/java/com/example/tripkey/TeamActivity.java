package com.example.tripkey;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FieldValue;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TeamActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendListAdapter adapter;
    private List<FriendItem> friendList;
    private FirebaseFirestore db;
    private String currentUserId;
    private Button getoutButton;
    String travelId, teamId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendList = new ArrayList<>();
        adapter = new FriendListAdapter(friendList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "");

        travelId = getIntent().getStringExtra("travelId");
        loadTeamMembers();

    }


    private void loadTeamMembers() {
        db.collection("users")
                .document(currentUserId)
                .collection("travel")
                .document(travelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String teamId = documentSnapshot.getString("teamId");
                        if (teamId != null && !teamId.isEmpty()) {
                            Log.d("Firestore", "불러온 teamId: " + teamId);
                            loadMembersFromTeam(teamId);
                            setupGetoutButton(teamId);  // 여기서 나가기 버튼도 설정
                        } else {
                            Log.e("Firestore", "teamId가 존재하지 않음");
                        }
                    } else {
                        Log.e("Firestore", "travel 문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "travel 문서 가져오기 실패", e));
    }

    private void setupGetoutButton(String teamId) {
        getoutButton = findViewById(R.id.getout);
        getoutButton.setOnClickListener(v -> {
            // 1. 팀 멤버에서 현재 유저 ID 제거
            db.collection("users")
                    .document(currentUserId)
                    .collection("teams")
                    .document(teamId)
                    .update("members", FieldValue.arrayRemove(currentUserId))
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "팀에서 성공적으로 나감");

                        // 2. travel 문서 삭제
                        db.collection("users")
                                .document(currentUserId)
                                .collection("travel")
                                .document(travelId)
                                .delete()
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d("Firestore", "travel 문서 삭제됨");
                                    finish(); // 액티비티 종료
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "travel 문서 삭제 실패", e));
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "멤버 삭제 실패", e));
        });
    }


    private void loadMembersFromTeam(String teamId) {
        db.collection("users")
                .document(currentUserId)
                .collection("teams")
                .document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> members = (List<String>) documentSnapshot.get("members");
                        if (members != null) {
                            friendList.clear();
                            for (String memberId : members) {
                                db.collection("users").document(memberId)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists()) {
                                                String name = userDoc.getString("userName");
                                                String profileUrl = userDoc.getString("profileImage");

                                                FriendItem item = new FriendItem(name, memberId, profileUrl);
                                                friendList.add(item);
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "팀 멤버 로드 실패", e));
    }


}
