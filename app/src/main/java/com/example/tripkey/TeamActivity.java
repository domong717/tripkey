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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        ImageButton addFriendButton = findViewById(R.id.button_add);
        addFriendButton.setOnClickListener(v -> showFriendSelectionDialog());



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

    private void showFriendSelectionDialog() {
        db.collection("users").document(currentUserId).collection("friends")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> friendIds = new ArrayList<>();
                    List<String> friendNames = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String friendId = doc.getId();
                        String name = doc.getString("userId"); // 저장된 이름
                        friendIds.add(friendId);
                        friendNames.add(name != null ? name : friendId); // 이름 없으면 ID로 표시
                    }

                    // boolean 배열로 체크 상태 저장
                    boolean[] checkedItems = new boolean[friendIds.size()];

                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("친구 초대하기")
                            .setMultiChoiceItems(friendNames.toArray(new String[0]), checkedItems, (dialog, which, isChecked) -> {
                                checkedItems[which] = isChecked;
                            })
                            .setPositiveButton("초대", (dialog, which) -> {
                                for (int i = 0; i < checkedItems.length; i++) {
                                    if (checkedItems[i]) {
                                        String selectedFriendId = friendIds.get(i);
                                        inviteFriendToTravel(selectedFriendId, travelId, currentUserId);
                                    }
                                }
                                new android.os.Handler().postDelayed(() -> loadTeamMembers(), 1000);
                            })
                            .setNegativeButton("취소", null)
                            .show();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "친구 목록 불러오기 실패", e));
    }

    private void inviteFriendToTravel(String friendId, String travelId, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 현재 사용자 travel 문서 참조
        DocumentReference myTravelRef = db.collection("users").document(userId)
                .collection("travel").document(travelId);

        // 1. 현재 travel 문서 가져오기
        myTravelRef.get().addOnSuccessListener(myTravelSnap -> {
            if (myTravelSnap.exists()) {
                Map<String, Object> travelData = myTravelSnap.getData();

                // 2. 친구의 travel 문서에 복사
                DocumentReference friendTravelRef = db.collection("users").document(friendId)
                        .collection("travel").document(travelId);

                friendTravelRef.set(travelData).addOnSuccessListener(unused -> {
                    Log.d("Team", "여행 문서 복사 완료");

                    // 3. gpt_plan 복사
                    copyGptPlanToFriend(userId, friendId, travelId);

                    // 4. teamId 찾기 및 친구를 team에 추가
                    if (travelData.containsKey("teamId")) {
                        String teamId = (String) travelData.get("teamId");
                        addFriendToTeam(friendId, userId, teamId);
                    }

                }).addOnFailureListener(e -> Log.e("team", "여행 문서 복사 실패", e));
            }
        });
    }

    private void copyGptPlanToFriend(String fromUserId, String toUserId, String travelId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference fromGptRef = db.collection("users").document(fromUserId)
                .collection("travel").document(travelId)
                .collection("gpt_plan");

        fromGptRef.get().addOnSuccessListener(gptDocs -> {
            for (DocumentSnapshot dateDoc : gptDocs.getDocuments()) {
                String date = dateDoc.getId();

                // 날짜 문서 생성
                DocumentReference toDateRef = db.collection("users").document(toUserId)
                        .collection("travel").document(travelId)
                        .collection("gpt_plan").document(date);

                toDateRef.set(new HashMap<>());

                // 장소들 복사
                dateDoc.getReference().collection("places").get().addOnSuccessListener(placeDocs -> {
                    for (DocumentSnapshot placeDoc : placeDocs.getDocuments()) {
                        toDateRef.collection("places")
                                .document(placeDoc.getId())
                                .set(placeDoc.getData());
                    }
                });
            }
        });
    }

    private void addFriendToTeam(String friendId, String ownerUserId, String teamId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 원래 owner의 team 문서 참조
        DocumentReference teamRef = db.collection("users").document(ownerUserId)
                .collection("teams").document(teamId);

        // 1. 먼저 members에 friendId 추가
        teamRef.update("members", FieldValue.arrayUnion(friendId))
                .addOnSuccessListener(aVoid -> {
                    // 2. members까지 반영된 최신 team 데이터 가져오기
                    teamRef.get().addOnSuccessListener(teamSnap -> {
                        if (teamSnap.exists()) {
                            Map<String, Object> teamData = teamSnap.getData();

                            if (teamData != null) {
                                // 3. 친구에게 복사
                                db.collection("users").document(friendId)
                                        .collection("teams").document(teamId)
                                        .set(teamData)
                                        .addOnSuccessListener(unused -> Log.d("Team", "친구의 teams에 최신 team 복사 완료"))
                                        .addOnFailureListener(e -> Log.e("Team", "친구의 team 복사 실패", e));
                            }
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e("Team", "members 업데이트 실패", e));
    }


    private void setupGetoutButton(String teamId) {
        getoutButton = findViewById(R.id.getout);

        Log.d("Firestore", "members 배열에서 '" + currentUserId + "' 제거 시도");
        getoutButton.setOnClickListener(v -> {
            // 1. 팀 멤버에서 현재 유저 ID 제거
            db.collection("users")
                    .document(currentUserId)
                    .collection("teams")
                    .document(teamId)
                    .get()
                    .addOnSuccessListener(teamDoc -> {
                        if (teamDoc.exists()) {
                            List<String> members = (List<String>) teamDoc.get("members");
                            if (members != null) {
                                for (String memberId : members) {
                                    // 각 멤버 문서에서 나간 유저 ID 삭제
                                    db.collection("users")
                                            .document(memberId)
                                            .collection("teams")
                                            .document(teamId)
                                            .update("members", FieldValue.arrayRemove(currentUserId))
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("Firestore", "멤버 " + memberId + " 문서에서 " + currentUserId + " 삭제 완료");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("Firestore", "멤버 " + memberId + " 문서에서 삭제 실패", e);
                                            });
                                }

                                // 그리고 나서 내 travel 문서 삭제
                                db.collection("users")
                                        .document(currentUserId)
                                        .collection("travel")
                                        .document(travelId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "travel 문서 삭제됨");

                                            Intent intent = new Intent(TeamActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            intent.putExtra("selectTab", "home");
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> Log.e("Firestore", "travel 문서 삭제 실패", e));
                            }
                        }
                    });
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