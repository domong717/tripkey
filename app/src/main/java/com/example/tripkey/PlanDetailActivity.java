package com.example.tripkey;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.tripkey.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.firebase.firestore.QuerySnapshot;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.LabelIconStyle;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.label.LabelTextStyle;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class PlanDetailActivity extends AppCompatActivity {

    private static final String TAG = "PlanDetailActivity";

    private MapView mapView;
    private KakaoMap kakaoMap;

    private String userId;
    private String travelId;
    private FirebaseFirestore db;

    private List<GptPlan> planList = new ArrayList<>();
    private LinearLayout dayButtonContainer;

    private List<GptPlan.Place> tempPlaceList = new ArrayList<>();



    private ListView listPlaces;
    private TextView tvTripTitle, tvTripDate;
    private FloatingActionButton btnCalculate, btnTeam,btnChecklist;

    //private Map<Integer, List<TripPlace>> dayPlaces = new HashMap<>();
    private int currentDay = 1;



    private final int startZoomLevel = 15;
    private final LatLng startPosition = LatLng.from(37.394660, 127.111182);   // 판교역 기본 위치

    private String selectedDate = ""; // 선택된 날짜 변수 필요

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_detail);


        tvTripTitle = findViewById(R.id.tv_trip_title);
        tvTripDate = findViewById(R.id.tv_trip_date);


        // Intent 및 SharedPreferences에서 데이터 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);
        travelId = getIntent().getStringExtra("travelId");
        Log.d(TAG, "travelId 값: " + travelId);
        dayButtonContainer = findViewById(R.id.buttonContainer);
        listPlaces=findViewById(R.id.place_list_view);
        db = FirebaseFirestore.getInstance();

        String travelName = getIntent().getStringExtra("travelName");
        String startDate = getIntent().getStringExtra("startDate");
        String endDate = getIntent().getStringExtra("endDate");

        // 뒤로가기 버튼
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        if (travelName != null) {
            tvTripTitle.setText(travelName);
        }
        if (startDate != null && endDate != null) {
            tvTripDate.setText(startDate + " ~ " + endDate);
        }
        if (startDate != null && endDate == null) {
            tvTripDate.setText(startDate);
        }

        mapView = findViewById(R.id.map_view);
        mapView.start(lifeCycleCallback, readyCallback);

        initViews();
        loadGptPlan();
    }

    private void initViews() {
        listPlaces = findViewById(R.id.place_list_view);

        btnCalculate = findViewById(R.id.btn_calculate);
        btnTeam = findViewById(R.id.btn_team);
        btnChecklist=findViewById(R.id.btn_checklist);

        // 인텐트에서 "from" 정보 받아오기
        String from = getIntent().getStringExtra("from");
        if ("home".equals(from)) {
            // 홈에서 왔으면 FAB 버튼 숨기기
            btnCalculate.setVisibility(View.INVISIBLE);
            btnTeam.setVisibility(View.INVISIBLE);
            btnChecklist.setVisibility(View.INVISIBLE);
        }

        btnCalculate.setOnClickListener(v -> {
            Intent intent = new Intent(PlanDetailActivity.this, RegisterMoneyActivity.class);
            intent.putExtra("travelId", travelId);
            startActivity(intent);
        });

        btnTeam.setOnClickListener(v -> {
            Intent intent = new Intent(PlanDetailActivity.this, TeamActivity.class);
            intent.putExtra("travelId", travelId);
            startActivity(intent);
        });
        btnChecklist.setOnClickListener(v->{
            Intent intent = new Intent(PlanDetailActivity.this, ChecklistActivity.class);
            intent.putExtra("travelId",travelId); // travelId마다 checkList 제공하기에 travelId 필요..
            startActivity(intent);
        });
    }
    private void createDayButtons(Map<String, List<GptPlan.Place>> dateToPlaces) {
        final Button[] previouslySelectedButton = {null};

        int dayNumber = 1;
        for (String date : dateToPlaces.keySet()) {
            Button dayButton = new Button(this);
            dayButton.setText("Day " + dayNumber);
            dayButton.setBackgroundColor(ContextCompat.getColor(this, R.color.mid_green));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 0, 0);
            dayButton.setLayoutParams(params);

            List<GptPlan.Place> placesForDate = dateToPlaces.get(date);

            dayButton.setOnClickListener(v -> {
                if (previouslySelectedButton[0] != null) {
                    previouslySelectedButton[0].setBackgroundColor(ContextCompat.getColor(this, R.color.mid_green));
                }
                dayButton.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_green));
                previouslySelectedButton[0] = dayButton;

                // 선택된 날짜 장소 목록 보여주기
                List<String> placeInfoList = new ArrayList<>();
                placeInfoList.add("날짜: " + date);
                //placeInfoList.add("");

                for (GptPlan.Place place : placesForDate) {
                    placeInfoList.add("📍 " + place.getPlace() + "\n" +
                            "  ∘ 카테고리: " + place.getCategory() + "\n" +
                            "  ∘ 이동수단: " + place.getTransport() + "\n" +
                            "  ∘ 예상 소요 시간: " + place.getTime());

                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, placeInfoList);
                listPlaces.setAdapter(adapter);
            });

            dayButtonContainer.addView(dayButton);
            dayNumber++;
        }

        if (dayButtonContainer.getChildCount() > 0) {
            dayButtonContainer.getChildAt(0).performClick();
        }
    }

    private void loadGptPlan() {
        Log.d("PlanDetailActivity", "userId: " + userId + ", travelId: " + travelId);
        if (userId != null && travelId != null) {

            CollectionReference gptPlanRef = db.collection("users")
                    .document(userId)
                    .collection("travel")
                    .document(travelId)
                    .collection("gpt_plan");


            gptPlanRef.get()
                    .addOnSuccessListener(gptPlanDocs -> {

                        Log.d("PlanDetailActivity", "gptPlanDocs: " + gptPlanDocs.isEmpty());
                        if (gptPlanDocs.isEmpty()) {

                            Toast.makeText(this, "일정이 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, List<GptPlan.Place>> dateToPlaces = new TreeMap<>();

                        for (QueryDocumentSnapshot dateDoc : gptPlanDocs) {
                            String dateKey = dateDoc.getId();


                            gptPlanRef.document(dateKey)
                                    .collection("places")
                                    .orderBy(FieldPath.documentId())
                                    .get()
                                    .addOnSuccessListener(places -> {

                                        List<GptPlan.Place> placeList = new ArrayList<>();

                                        for (QueryDocumentSnapshot placeDoc : places) {
                                            GptPlan.Place place = placeDoc.toObject(GptPlan.Place.class);
                                            // 좌표 파싱 추가 부분
                                            String coordString = placeDoc.getString("coord");
                                            if (coordString != null) {
                                                try {
                                                    String[] parts = coordString.split(",");
                                                    place.setLatitude(Double.parseDouble(parts[0].trim()));
                                                    place.setLongitude(Double.parseDouble(parts[1].trim()));
                                                } catch (Exception e) {
                                                    Log.e(TAG, "좌표 파싱 오류: " + coordString);
                                                }
                                            }

                                            placeList.add(place);

                                        }

                                        tempPlaceList = placeList;

                                        dateToPlaces.put(dateKey, placeList);

                                        if (dateToPlaces.size() == gptPlanDocs.size()) {

                                            createDayButtons(dateToPlaces);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "dateKey + 의 장소 불러오기 실패", e);
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(this, "일정 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "userId 또는 travelId가 null");
        }
    }

    private void createMapMarkers(List<GptPlan.Place> places) {
        if (kakaoMap == null || places == null) return;

        LabelLayer layer = kakaoMap.getLabelManager().getLayer();
        LabelStyles styles = kakaoMap.getLabelManager().addLabelStyles(
                LabelStyles.from(LabelStyle.from(R.drawable.map_pin))
        );

        for (int i = 0; i < places.size(); i++) {
            GptPlan.Place place = places.get(i);
            LatLng position = LatLng.from(place.getLatitude(), place.getLongitude());

            // LabelOptions 생성
            LabelOptions options = LabelOptions.from(position)
                    .setStyles(styles);

            layer.addLabel(options); // 마커 추가
        }
    }

    // MapReadyCallback 을 통해 지도가 정상적으로 시작된 후에 수신할 수 있다.
    private KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull KakaoMap kakaoMap) {
            PlanDetailActivity.this.kakaoMap = kakaoMap;

            createMapMarkers(tempPlaceList);

            Toast.makeText(getApplicationContext(), "Map Start!", Toast.LENGTH_SHORT).show();

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


/*
    @Override
    protected void onResume() {
        super.onResume();
        mapView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.pause();
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

 */
}