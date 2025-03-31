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
import java.util.List;

public class MakeTeamActivity extends AppCompatActivity {
    private RecyclerView friendsRecyclerView, selectedRecyclerView;
    private FriendTeamAdapter friendTeamAdapter;
    private SelectedFriendAdapter selectedFriendAdapter;
    private List<FriendItem> friendList = new ArrayList<>();
    private List<FriendItem> selectedFriendsList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId; // 실제 로그인된 사용자의 ID

    private static final String TAG = "MakeTeamActivity"; // 로그 태그 추가
    private static final String PREFS_NAME = "UserPrefs"; // InfoFragment에서 사용한 키
    private static final String KEY_USER_ID = "userId";  // InfoFragment와 동일한 키 사용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_team);

        db = FirebaseFirestore.getInstance();

        // 🔹 InfoFragment와 동일한 방식으로 SharedPreferences에서 userId 가져오기
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getString(KEY_USER_ID, null);

        if (currentUserId == null) {
            Log.e(TAG, "currentUserId가 null 입니다! 로그인 확인 필요");
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish(); // 액티비티 종료 또는 로그인 화면으로 이동
            return;
        }

        Log.d(TAG, "현재 로그인된 사용자 ID: " + currentUserId);

        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        selectedRecyclerView = findViewById(R.id.selectedRecyclerView);

        // 어댑터 초기화 로그 추가
        friendTeamAdapter = new FriendTeamAdapter(friendList, this::onFriendSelected);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendTeamAdapter);
        Log.d(TAG, "friendTeamAdapter 설정 완료");

        selectedFriendAdapter = new SelectedFriendAdapter(selectedFriendsList);
        selectedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedRecyclerView.setAdapter(selectedFriendAdapter);
        Log.d(TAG, "selectedFriendAdapter 설정 완료");

        loadFriends();

        Button buttonCreateTeam = findViewById(R.id.buttonCreateTeam);
        buttonCreateTeam.setOnClickListener(v -> {
            // 선택된 친구들의 ID 리스트를 생성
            ArrayList<String> selectedFriendsIds = new ArrayList<>();
            for (FriendItem friend : selectedFriendsList) {
                selectedFriendsIds.add(friend.getId()); // 친구 ID 추가
            }

            // 로그로 선택된 친구 ID 리스트 확인
            Log.d(TAG, "선택된 친구 ID 리스트: " + selectedFriendsIds);

            // Intent를 통해 AddTripActivity로 이동
            Intent intent = new Intent(MakeTeamActivity.this, AddTripActivity.class);
            intent.putStringArrayListExtra("selectedFriendsIds", selectedFriendsIds);
            startActivity(intent);
        });
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


    private void onFriendSelected(FriendItem friend) {
        if (selectedFriendsList.contains(friend)) {
            // 🔹 이미 선택된 경우 -> 리스트에서 제거
            selectedFriendsList.remove(friend);
            Log.d(TAG, "선택 해제됨: " + friend.getName());
        } else {
            // 🔹 선택되지 않은 경우 -> 리스트에 추가
            selectedFriendsList.add(friend);
            Log.d(TAG, "선택됨: " + friend.getName());
        }
        selectedFriendAdapter.notifyDataSetChanged(); // 🔹 즉시 UI 반영
    }

}
