package com.example.tripkey.ui.home;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.example.tripkey.WishlistItem;
import com.example.tripkey.databinding.FragmentHomeBinding;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.EventListener;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ImageButton friendsButton;
    private String selectedMBTI = "전체";
    private String selectedLocation = "전체";


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

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        // RecyclerView 초기화
        recyclerView = rootView.findViewById(R.id.rv_trip_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tripPostAdapter = new TripPostAdapter(getContext(), tripList, tripPost -> {
            String travelId = tripPost.getTravelId();

            Log.d("HomeFragment", "userId: " + userId+"travelId: " + travelId);
            Map<String, Object> data = new HashMap<>();
            data.put("travelId", travelId);
            if (userId != null && travelId != null) {

                Log.d("HomeFragment", "찜 클릭됨");
                db.collection("users")
                        .document(userId)
                        .collection("wishlist")
                        .document(travelId)
                        .set(new HashMap<String, Object>() {{
                            put("ownerId", tripPost.getOwnerId()); // travel 주인의 uid
                            put("timestamp", FieldValue.serverTimestamp());
                        }});
            }
        });

        recyclerView.setAdapter(tripPostAdapter);
        AutoCompleteTextView mbtiDropdown = rootView.findViewById(R.id.mbti_dropdown);

        AutoCompleteTextView locationDropdown = rootView.findViewById(R.id.location_dropdown);
        String[] mbtiList = {
                "전체", "IBRFT", "IBRFL", "IBRMT", "IBRML", "IBEFT", "IBEFL", "IBEMT",
                "IBEML", "ICRFT", "ICRFL", "ICRMT", "ICRML", "ICEFT", "ICEFL", "ICEMT",
                "ICEML", "OBRFT", "OBRFL", "OBRMT", "OBRML", "OBEFT", "OBEFL", "OBEMT",
                "OBEML", "OCRFT", "OCRFL", "OCRMT", "OCRML", "OCEFT", "OCEFL", "OCEMT", "OCEML"
        };
        String[] locationList = {
                "전체", "서울", "경기","인천", "강원", "부산", "울산", "대구", "대전", "광주", "충북", "충남", "전북", "전남", "경북","제주"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_mbti_dropdown,
                mbtiList);
        mbtiDropdown.setAdapter(adapter);

        ArrayAdapter<String> l_adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_location_dropdown,
                locationList);
        locationDropdown.setAdapter(l_adapter);

        mbtiDropdown.setOnItemClickListener((parent, view1, position, id) -> {
            selectedMBTI = parent.getItemAtPosition(position).toString();
            applyCombinedFilters(); // AND 조건 적용
        });

        locationDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedLocation = parent.getItemAtPosition(position).toString();
            applyCombinedFilters(); // AND 조건 적용
        });


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

        db.collection("users")
                .get()
                .addOnSuccessListener(usersSnap -> {
                    for (QueryDocumentSnapshot userDoc : usersSnap) {
                        String uid = userDoc.getId();

                        db.collection("users")
                                .document(uid)
                                .collection("travel")
                                .get()
                                .addOnSuccessListener(travelSnap -> {
                                    for (QueryDocumentSnapshot travelDoc : travelSnap) {
                                        String travelId = travelDoc.getId();

                                        // records 존재 여부 확인
                                        db.collection("users")
                                                .document(uid)
                                                .collection("travel")
                                                .document(travelId)
                                                .collection("records")
                                                .limit(1)
                                                .get()
                                                .addOnSuccessListener(recordsSnap -> {
                                                    if (!recordsSnap.isEmpty()) {
                                                        String title = travelDoc.getString("travelName");
                                                        String startDate = travelDoc.getString("startDate");
                                                        String endDate = travelDoc.getString("endDate");
                                                        String date = startDate + " ~ " + endDate;
                                                        String location = travelDoc.getString("location");
                                                        Long total = travelDoc.contains("total") ? travelDoc.getLong("total") : null;
                                                        String teamId = travelDoc.getString("teamId");
                                                        String teamMBTI = travelDoc.getString("teamMBTI");

                                                        // 팀 정보 및 장소 정보 로딩
                                                        loadTeamMembersAndPlaces(uid, teamId, total, travelId, result -> {
                                                            int peopleCount = result.first;
                                                            String costPerPerson = (total != null && peopleCount > 0)
                                                                    ? String.format("%,d 원", total / peopleCount)
                                                                    : "알 수 없음";

                                                            TripPost trip = new TripPost(
                                                                    title, date, location,
                                                                    peopleCount, costPerPerson,
                                                                    result.second, teamMBTI, travelId, uid
                                                            );
                                                            tripList.add(trip);
                                                            tripPostAdapter.notifyDataSetChanged();
                                                        });
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
                    placeList.add(new Place(place, comment, firstPhotoUrl));
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


    private void applyCombinedFilters() {
        filteredList.clear();

        for (TripPost trip : tripList) {
            boolean mbtiMatches = selectedMBTI.equals("전체") ||
                    (trip.getTeamMBTI() != null && trip.getTeamMBTI().equals(selectedMBTI));

            boolean locationMatches = selectedLocation.equals("전체") ||
                    (trip.getLocation() != null && trip.getLocation().contains(selectedLocation));

            if (mbtiMatches && locationMatches) {
                filteredList.add(trip);
            }
        }

        tripPostAdapter.updateList(filteredList);
    }



}