package com.example.tripkey;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeTeamActivity extends AppCompatActivity {
    private RecyclerView friendsRecyclerView, selectedRecyclerView;
    private FriendTeamAdapter friendTeamAdapter;
    private SelectedFriendAdapter selectedFriendAdapter;
    private List<FriendItem> friendList = new ArrayList<>();
    private List<FriendItem> selectedFriendsList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId; // 실제 로그인된 사용자의 ID

    private static final String TAG = "MakeTeamActivity"; // 로그 태그 추가
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_team);

        db = FirebaseFirestore.getInstance();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getString(KEY_USER_ID, null);

        if (currentUserId == null) {
            Log.e(TAG, "currentUserId가 null 입니다! 로그인 확인 필요");
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish(); // 액티비티 종료 또는 로그인 화면으로 이동
            return;
        }


        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        selectedRecyclerView = findViewById(R.id.selectedRecyclerView);

        // 어댑터 초기화 로그 추가
        friendTeamAdapter = new FriendTeamAdapter(friendList, this::onFriendSelected);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendTeamAdapter);
        Log.d(TAG, "friendTeamAdapter 설정 완료");

        Log.d(TAG, "현재 로그인된 사용자 ID: " + currentUserId);
        selectedFriendAdapter = new SelectedFriendAdapter(selectedFriendsList, currentUserId);
        selectedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedRecyclerView.setAdapter(selectedFriendAdapter);

        Log.d(TAG, "selectedFriendAdapter 설정 완료");

        addCurrentUserToSelectedList();
        loadFriends();

        Button buttonCreateTeam = findViewById(R.id.buttonCreateTeam);
        // 버튼 클릭 이벤트 수정
        buttonCreateTeam.setOnClickListener(v -> {
            if (selectedFriendsList.isEmpty()) {
                Toast.makeText(this, "팀원을 선택하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 팀 생성 후 -> AddTripActivity로 이동
            createTeamInFirestore(selectedFriendsList, teamId -> {
                Intent intent = new Intent(MakeTeamActivity.this, AddTripActivity.class);

                // 친구 목록 ID
                ArrayList<String> selectedFriendsIds = new ArrayList<>();
                for (FriendItem friend : selectedFriendsList) {
                    selectedFriendsIds.add(friend.getId());
                }

                intent.putStringArrayListExtra("selectedFriendsIds", selectedFriendsIds);
                intent.putExtra("teamId", teamId); // 🔹 팀 ID도 추가

                startActivity(intent);
            });
        });

    }

    private void createTeamInFirestore(List<FriendItem> selectedFriends, OnTeamCreatedListener listener) {
        String teamId = db.collection("teams").document().getId(); // 랜덤 팀 ID 생성
        Map<String, Object> teamData = new HashMap<>();
        List<String> memberIds = new ArrayList<>();

        for (FriendItem friend : selectedFriends) {
            memberIds.add(friend.getId());
        }

        teamData.put("teamId", teamId);
        teamData.put("members", memberIds);

        // 멤버 수만큼 저장 후 완료되었을 때 콜백
        final int[] successCount = {0};

        for (FriendItem friend : selectedFriends) {
            db.collection("users").document(friend.getId())
                    .collection("teams").document(teamId)
                    .set(teamData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "팀 저장 성공: " + friend.getId());
                        successCount[0]++;
                        if (successCount[0] == selectedFriends.size()) {
                            // 🔹 모든 저장 완료되었을 때 teamId 전달
                            listener.onTeamCreated(teamId);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "팀 저장 실패: " + friend.getId(), e));
        }
    }

    interface OnTeamCreatedListener {
        void onTeamCreated(String teamId);
    }



    private void loadFriends() {
        Log.d(TAG, "loadFriends() 호출됨");

        db.collection("users").document(currentUserId)
                .collection("friends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firestore에서 친구 목록 가져오기 성공");

                        friendList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String friendId = document.getId();
                            Log.d(TAG, "친구 ID: " + friendId);

                            db.collection("users").document(friendId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String friendName = documentSnapshot.getString("userName");
                                            String profileImageUrl = documentSnapshot.getString("profileImage");

                                            Log.d(TAG, "친구 이름: " + friendName + ", 프로필 이미지: " + profileImageUrl);

                                            FriendItem friendItem = new FriendItem(friendName, friendId, profileImageUrl);
                                            friendList.add(friendItem);
                                            friendTeamAdapter.notifyDataSetChanged();
                                        } else {
                                            Log.e(TAG, "친구 정보 없음: " + friendId);
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "친구 정보 가져오기 실패", e));
                        }
                    } else {
                        Log.e(TAG, "Firestore에서 친구 목록 가져오기 실패", task.getException());
                    }
                });
    }

    private void addCurrentUserToSelectedList() {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String myName = documentSnapshot.getString("userName");
                        String myProfileImage = documentSnapshot.getString("profileImage");

                        FriendItem myAccount = new FriendItem(myName, currentUserId, myProfileImage);

                        // 🔹 중복 방지: 이미 추가된 경우 다시 추가하지 않음
                        if (!selectedFriendsList.contains(myAccount)) {
                            selectedFriendsList.add(myAccount);
                            selectedFriendAdapter.notifyDataSetChanged();
                            Log.d(TAG, "내 계정 추가 완료: " + myName);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "내 계정 정보 가져오기 실패", e));
    }


    private void onFriendSelected(FriendItem friend) {
        if (selectedFriendsList.contains(friend)) {

            selectedFriendsList.remove(friend);
            Log.d(TAG, "선택 해제됨: " + friend.getName());
        } else {
            selectedFriendsList.add(friend);
            Log.d(TAG, "선택됨: " + friend.getName());
        }
        selectedFriendAdapter.notifyDataSetChanged(); // 🔹 즉시 UI 반영
    }

}
