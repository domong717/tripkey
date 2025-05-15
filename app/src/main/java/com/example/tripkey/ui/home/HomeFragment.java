package com.example.tripkey.ui.home;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripkey.Place;
import com.example.tripkey.R;
import com.example.tripkey.FriendListActivity;
import com.example.tripkey.TripPost;
import com.example.tripkey.TripPostAdapter;
import com.example.tripkey.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.EventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ImageButton friendsButton;

    private RecyclerView recyclerView;
    private TripPostAdapter tripPostAdapter;
    private ArrayList<TripPost> tripList = new ArrayList<>();
    private ArrayList<TripPost> filteredList = new ArrayList<>(); // 필터 적용 목록

    private FirebaseFirestore db;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // 친구 버튼
        friendsButton = rootView.findViewById(R.id.friends);
        friendsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FriendListActivity.class);
            startActivity(intent);
        });

        // RecyclerView 초기화
        recyclerView = rootView.findViewById(R.id.rv_trip_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tripPostAdapter = new TripPostAdapter(getContext(), tripList);
        recyclerView.setAdapter(tripPostAdapter);
        AutoCompleteTextView mbtiDropdown = rootView.findViewById(R.id.mbti_dropdown);

        String[] mbtiList = {
                "전체", "ENFP", "ISTJ", "ENTP", "INFP", "ESTJ", "ESFP", "INTJ",
                "ISFJ", "INFJ", "ESTP", "ISTP", "ENFJ", "ISFP", "ENTJ", "INTP"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_mbti_dropdown,
                mbtiList);
        mbtiDropdown.setAdapter(adapter);

        mbtiDropdown.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedMBTI = parent.getItemAtPosition(position).toString();
            filterTripPostsByMBTI(selectedMBTI); // 필터링 메서드 호출
        });

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        // userId 가져오기
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            loadTripsFromFirestore();
        }
        return rootView;
    }

    private void loadTripsFromFirestore() {
        tripList.clear();

        // 나 + 친구 userId 리스트 수집
        List<String> allUserIds = new ArrayList<>();
        allUserIds.add(userId); // 나 자신 추가

        db.collection("users")
                .document(userId)
                .collection("friends")
                .get().addOnSuccessListener(friendSnap -> {
            for (QueryDocumentSnapshot doc : friendSnap) {
                String friendId = doc.getId(); // 친구의 userId
                allUserIds.add(friendId);
            }

            // 나와 친구들의 travel 데이터 조회
            for (String uid : allUserIds) {
                db.collection("users")
                        .document(uid)
                        .collection("travel")
                        .get().addOnSuccessListener(travelSnap -> {
                    for (QueryDocumentSnapshot travelDoc : travelSnap) {
                        String travelId = travelDoc.getId();

                        // records 컬렉션 문서 존재 여부 체크
                        db.collection("users").document(uid)
                                .collection("travel").document(travelId)
                                .collection("records")
                                .limit(1)
                                .get().addOnSuccessListener(recordsSnap -> {
                                    if (!recordsSnap.isEmpty()) {
                                        String title = travelDoc.getString("travelName");
                                        String startDate = travelDoc.getString("startDate");
                                        String endDate = travelDoc.getString("endDate");
                                        String date = startDate + " ~ " + endDate;
                                        String location = travelDoc.getString("location");
                                        Long total = travelDoc.contains("total") ? travelDoc.getLong("total") : null;
                                        String teamId = travelDoc.getString("teamId");
                                        String teamMBTI = travelDoc.getString("teamMBTI");

                                        // 팀 정보 및 records 로딩
                                        loadTeamMembersAndPlaces(uid, teamId, total, travelId, result -> {
                                            int peopleCount = result.first;
                                            String costPerPerson = (total != null && peopleCount > 0)
                                                    ? String.format("%,d", total / peopleCount)
                                                    : "알 수 없음";

                                            TripPost trip = new TripPost(title, date, location, peopleCount, costPerPerson, result.second, teamMBTI);
                                            tripList.add(trip);
                                            tripPostAdapter.notifyDataSetChanged();
                                        });
                                    } else {

                                    }
                                });
                    }
                });
            }
        });
    }


    private void loadTeamMembersAndPlaces(String userId, String teamId, Long total, String travelId, OnTripDataReadyListener listener) {
        DocumentReference teamRef = db.collection("users").document(userId).collection("teams").document(teamId);

        teamRef.get().addOnSuccessListener(teamDoc -> {
            List<String> members = (List<String>) teamDoc.get("members");
            int memberCount = (members != null) ? members.size() : 1;

            // 이어서 장소 정보 가져오기
            loadPlaces(userId, travelId, places -> {
                listener.onTripDataReady(new Pair<>(memberCount, places));
            });
        });
    }

    private interface OnTripDataReadyListener {
        void onTripDataReady(Pair<Integer, List<Place>> result);
    }

    private void loadPlaces(String userId, String travelId, OnPlacesLoadedListener listener) {
        CollectionReference recordsRef = db.collection("users").document(userId)
                .collection("travel").document(travelId).collection("records");

        recordsRef.get().addOnCompleteListener(task -> {
            List<Place> placeList = new ArrayList<>();
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String place = doc.getString("place");
                    String comment = doc.getString("record");
                    List<String> photos = (List<String>) doc.get("photos");
                    String firstPhotoUrl = null;
                    if (photos != null && !photos.isEmpty()) {
                        firstPhotoUrl = photos.get(0);
                    }
                    placeList.add(new Place(place, comment, firstPhotoUrl)); // photoResId는 0으로 설정하거나 Glide로 처리
                }
            }
            listener.onPlacesLoaded(placeList);
        });
    }

    private interface OnPlacesLoadedListener {
        void onPlacesLoaded(List<Place> places);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void filterTripPostsByMBTI(String selectedMBTI) {
        filteredList.clear();

        if (selectedMBTI.equals("전체")) {
            filteredList.addAll(tripList);
        } else {
            for (TripPost trip : tripList) {
                if (trip.getTeamMBTI() != null && trip.getTeamMBTI().equals(selectedMBTI)) {
                    filteredList.add(trip);
                }
            }
        }

        tripPostAdapter.notifyDataSetChanged();
    }

}
