package com.example.tripkey;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tripkey.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.kakao.vectormap.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlanDetailActivity extends AppCompatActivity {

    private static final String TAG = "PlanDetailActivity";

    private MapView mapView; // 카카오맵 MapView
    private KakaoMap kakaoMap; // KakaoMap 객체
    private Button btnDay1, btnDay2, btnDay3;
    private ListView listPlaces;
    private TextView tvTripTitle, tvTripDate;
    private FloatingActionButton btnCalculate, btnTeam;

    private String tripId; // 여행 ID
    private FirebaseFirestore db; // Firestore 인스턴스

    private Map<Integer, List<TripPlace>> dayPlaces = new HashMap<>();
    private int currentDay = 1; // 현재 선택된 Day

    private TextView tvLat, tvLng, tvZoomLevel;

    private int startZoomLevel = 15;
    private LatLng startPosition = LatLng.from(37.394660,127.111182);   // 판교역

    // MapReadyCallback 을 통해 지도가 정상적으로 시작된 후에 수신할 수 있다.
    private KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull KakaoMap kakaoMap) {
            Toast.makeText(getApplicationContext(), "Map Start!", Toast.LENGTH_SHORT).show();

            tvLat.setText(String.valueOf(startPosition.getLatitude()));
            tvLng.setText(String.valueOf(startPosition.getLongitude()));
            tvZoomLevel.setText(String.valueOf(startZoomLevel));

            Log.i("k3f", "startPosition: "
                    + kakaoMap.getCameraPosition().getPosition().toString());
            Log.i("k3f", "startZoomLevel: "
                    + kakaoMap.getZoomLevel());
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return startPosition;
        }

        @NonNull
        @Override
        public int getZoomLevel() {
            return startZoomLevel;
        }
    };

    // MapLifeCycleCallback 을 통해 지도의 LifeCycle 관련 이벤트를 수신할 수 있다.
    private MapLifeCycleCallback lifeCycleCallback = new MapLifeCycleCallback() {

        @Override
        public void onMapResumed() {
            super.onMapResumed();
        }

        @Override
        public void onMapPaused() {
            super.onMapPaused();
        }

        @Override
        public void onMapDestroy() {
            Toast.makeText(getApplicationContext(), "onMapDestroy",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onMapError(Exception error) {
            Toast.makeText(getApplicationContext(), error.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_detail);

        mapView = findViewById(R.id.map_view);
        mapView.start(lifeCycleCallback, readyCallback);
        String travelId = getIntent().getStringExtra("travelId");

        // Firestore 초기화 및 여행 ID 가져오기
        db = FirebaseFirestore.getInstance();

        initViews();
        //loadTripData();
        //setupDayButtons();
    }

    private void initViews() {
        btnDay1 = findViewById(R.id.btn_day1);
        btnDay2 = findViewById(R.id.btn_day2);
        btnDay3 = findViewById(R.id.btn_day3);
        listPlaces = findViewById(R.id.list_places);
        tvTripTitle = findViewById(R.id.tv_trip_title);
        tvTripDate = findViewById(R.id.tv_trip_date);

        btnCalculate = findViewById(R.id.btn_calculate); // 계산하기 버튼
        btnTeam = findViewById(R.id.btn_team); // 팀 버튼

        btnCalculate.setOnClickListener(v -> {
            Intent intent = new Intent(PlanDetailActivity.this, CalculateActivity.class);
            intent.putExtra("travelId", getIntent().getStringExtra("travelId")); // travelId 전달
            startActivity(intent);
        });

        btnTeam.setOnClickListener(v -> {
            Intent intent = new Intent(PlanDetailActivity.this, TeamActivity.class);
            intent.putExtra("travelId", getIntent().getStringExtra("travelId")); // travelId 전달
            startActivity(intent);
        });
    }


//    private void loadTripData() {
//        db.collection("trips").document(tripId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        String title = documentSnapshot.getString("title");
//                        String startDate = documentSnapshot.getString("startDate");
//                        String endDate = documentSnapshot.getString("endDate");
//
//                        tvTripTitle.setText(title);
//                        tvTripDate.setText(startDate + " ~ " + endDate);
//
//                        loadPlaces();
//                    } else {
//                        Log.d(TAG, "여행 데이터가 없습니다.");
//                    }
//                })
//                .addOnFailureListener(e -> Log.e(TAG, "여행 데이터 로드 실패", e));
//    }
//
//    private void loadPlaces() {
//        db.collection("trips").document(tripId).collection("places")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        int day = document.getLong("day").intValue();
//                        String name = document.getString("name");
//                        double latitude = document.getDouble("latitude");
//                        double longitude = document.getDouble("longitude");
//
//                        TripPlace place = new TripPlace(name, latitude, longitude);
//                        dayPlaces.computeIfAbsent(day, k -> new ArrayList<>()).add(place);
//                    }
//
//                    displayDayPlaces(currentDay); // 현재 Day 장소 표시
//                })
//                .addOnFailureListener(e -> Log.e(TAG, "장소 데이터 로드 실패", e));
//    }
//
//    private void setupDayButtons() {
//        btnDay1.setOnClickListener(v -> displayDayPlaces(1));
//        btnDay2.setOnClickListener(v -> displayDayPlaces(2));
//        btnDay3.setOnClickListener(v -> displayDayPlaces(3));
//    }
//
//    private void displayDayPlaces(int day) {
//        currentDay = day;
//
//        if (kakaoMap != null) {
////            kakaoMap.clearPois(); // 기존 마커 제거
////
////            List<TripPlace> places = dayPlaces.getOrDefault(day, new ArrayList<>());
////            for (TripPlace place : places) {
////                Poi poi = new Poi(LatLng.from(place.latitude, place.longitude), place.name);
////                kakaoMap.addPoi(poi); // 지도에 마커 추가
////            }
////
////            if (!places.isEmpty()) {
////                TripPlace firstPlace = places.get(0);
////                kakaoMap.setCameraPosition(LatLng.from(firstPlace.latitude, firstPlace.longitude), 7);
////            }
//        }
//
//        // 리스트뷰 업데이트 (필요 시 구현)
//    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.resume();     // MapView 의 resume 호출
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.pause();    // MapView 의 pause 호출
    }

    static class TripPlace {
        String name;
        double latitude;
        double longitude;

        TripPlace(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
