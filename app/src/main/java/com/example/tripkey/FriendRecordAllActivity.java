package com.example.tripkey;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendRecordAllActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TripPostAdapter tripPostAdapter;
    private List<TripPost> friendRecordList = new ArrayList<>();

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_record_all);

        ImageButton backButton = findViewById(R.id.button_back);

        db = FirebaseFirestore.getInstance();

        userId = getIntent().getStringExtra("userId");
        if (userId != null) {
            loadFriendTripsWithRecords(userId);
        } else {
            Toast.makeText(this, "친구 ID가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        recyclerView = findViewById(R.id.rv_trip_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tripPostAdapter = new TripPostAdapter(this, friendRecordList, new TripPostAdapter.OnHeartClickListener() {
            @Override
            public void onHeartClick(TripPost post) {
                // 찜 해제 처리
                db.collection("users")
                        .document(userId)
                        .collection("wishlist")
                        .document(post.getTravelId())  // travelId 기준으로 삭제
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            // UI에서도 목록에서 제거
                            friendRecordList.remove(post);
                            tripPostAdapter.notifyDataSetChanged();
                            Toast.makeText(FriendRecordAllActivity.this, "찜 해제되었습니다", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(FriendRecordAllActivity.this, "찜 해제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        recyclerView.setAdapter(tripPostAdapter);

        // 뒤로 가기 버튼
        backButton.setOnClickListener(v -> finish());
    }


    private void loadFriendTripsWithRecords(String userId) {
        friendRecordList.clear();

        db.collection("users")
                .document(userId)
                .collection("travel")
                .get()
                .addOnSuccessListener(travelSnapshots -> {
                    if (travelSnapshots.isEmpty()) {
                        Toast.makeText(this, "친구의 여행 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot travelDoc : travelSnapshots) {
                        String travelId = travelDoc.getId();

                        // records 컬렉션 존재 여부 확인
                        db.collection("users")
                                .document(userId)
                                .collection("travel")
                                .document(travelId)
                                .collection("records")
                                .get()
                                .addOnSuccessListener(recordsSnapshots -> {
                                    if (!recordsSnapshots.isEmpty()) {
                                        // 여행 정보 추출
                                        String title = travelDoc.getString("travelName");
                                        String startDate = travelDoc.getString("startDate");
                                        String endDate = travelDoc.getString("endDate");
                                        String date = startDate + " ~ " + endDate;
                                        String location = travelDoc.getString("location");
                                        Long total = travelDoc.contains("total") ? travelDoc.getLong("total") : null;
                                        String teamId = travelDoc.getString("teamId");
                                        String teamMBTI = travelDoc.getString("teamMBTI");

                                        loadTeamMembersAndPlaces(userId, teamId, total, travelId, result -> {
                                            int peopleCount = result.first;
                                            String costPerPerson = (total != null && peopleCount > 0)
                                                    ? String.format("%,d", total / peopleCount)
                                                    : "알 수 없음";

                                            TripPost trip = new TripPost(title, date, location, peopleCount, costPerPerson, result.second, teamMBTI, travelId, userId);
                                            friendRecordList.add(trip);
                                            tripPostAdapter.notifyDataSetChanged();
                                        });
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "친구의 여행 정보 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadTeamMembersAndPlaces(String ownerId, String teamId, Long total, String travelId, OnTripDataReadyListener listener) {
        if (teamId == null || teamId.isEmpty()) {
            // 팀 정보 없으면 기본값 처리
            loadPlaces(ownerId, travelId, places -> listener.onTripDataReady(new android.util.Pair<>(1, places)));
            return;
        }

        db.collection("users")
                .document(ownerId)
                .collection("teams")
                .document(teamId)
                .get()
                .addOnSuccessListener(teamDoc -> {
                    List<String> members = (List<String>) teamDoc.get("members");
                    int memberCount = (members != null) ? members.size() : 1;

                    loadPlaces(ownerId, travelId, places -> listener.onTripDataReady(new android.util.Pair<>(memberCount, places)));
                })
                .addOnFailureListener(e -> {
                    // 실패 시 기본값
                    loadPlaces(ownerId, travelId, places -> listener.onTripDataReady(new android.util.Pair<>(1, places)));
                });
    }

    private interface OnTripDataReadyListener {
        void onTripDataReady(android.util.Pair<Integer, List<Place>> result);
    }

    private void loadPlaces(String ownerId, String travelId, OnPlacesLoadedListener listener) {
        db.collection("users")
                .document(ownerId)
                .collection("travel")
                .document(travelId)
                .collection("records")
                .get()
                .addOnCompleteListener(task -> {
                    List<Place> placeList = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            String place = doc.getString("place");
                            String comment = doc.getString("record");
                            List<String> photos = (List<String>) doc.get("photos");
                            String firstPhotoUrl = null;
                            if (photos != null && !photos.isEmpty()) {
                                firstPhotoUrl = photos.get(0);
                            }
                            placeList.add(new Place(place, comment, firstPhotoUrl));
                            Log.d("PlacePhoto", "photo URL: " + firstPhotoUrl);

                        }
                    }
                    listener.onPlacesLoaded(placeList);
                });
    }

    private interface OnPlacesLoadedListener {
        void onPlacesLoaded(List<Place> places);
    }
}
