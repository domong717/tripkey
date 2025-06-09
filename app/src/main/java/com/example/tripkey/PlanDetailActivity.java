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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.firebase.firestore.QuerySnapshot;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.LatLngBounds;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.LabelIconStyle;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.label.LabelTextBuilder;
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
    private static final int REQUEST_CODE_PLACE_SEARCH = 1001;

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
    private FloatingActionButton btnCalculate, btnTeam,btnChecklist, btnAddPlan;

    //private Map<Integer, List<TripPlace>> dayPlaces = new HashMap<>();
    private int currentDay = 1;

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

        String from = getIntent().getStringExtra("from");
        if ("home".equals(from)) {
            userId = getIntent().getStringExtra("ownerId");
        }

        travelId = getIntent().getStringExtra("travelId");

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
        btnAddPlan=findViewById(R.id.btn_plus);

        // 인텐트에서 "from" 정보 받아오기
        String from = getIntent().getStringExtra("from");
        if ("home".equals(from) || "detail".equals(from)) {
            btnCalculate.setVisibility(View.INVISIBLE);
            btnTeam.setVisibility(View.INVISIBLE);
            btnChecklist.setVisibility(View.INVISIBLE);
            btnAddPlan.setVisibility(View.INVISIBLE);
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
        btnAddPlan.setOnClickListener(v->{
            Intent e_intent = new Intent(PlanDetailActivity.this, PlaceSearchActivity.class);
            startActivityForResult(e_intent, 1001);
        });
    }
    private void createDayButtons(Map<String, List<GptPlan.Place>> dateToPlaces) {
        // 기존 버튼 삭제
        dayButtonContainer.removeAllViews();

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
                selectedDate = date;
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
                            "  ∘ 이동수단: " + place.getTransport());

                }

                PlaceAdapter adapter = new PlaceAdapter(this, placesForDate, true, userId, travelId, date);
                listPlaces.setAdapter(adapter);

                createMapMarkers(placesForDate);
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
                                            Log.d("coord", coordString);
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

                                        createMapMarkers(tempPlaceList);

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
        layer.removeAll();

        LabelStyles styles = kakaoMap.getLabelManager().addLabelStyles(
                LabelStyles.from(LabelStyle.from(R.drawable.big_map_pin)
                        .setTextStyles(LabelTextStyle.from(20, Color.BLACK, 1, Color.WHITE)))
        );

        // 1. LatLngBounds 계산
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (int i = 0; i < places.size(); i++) {
            GptPlan.Place place = places.get(i);
            LatLng position = LatLng.from(place.getLatitude(), place.getLongitude());
            boundsBuilder.include(position);

            LabelTextBuilder textBuilder = new LabelTextBuilder().setTexts(place.getPlace());
            // LabelOptions 생성
            LabelOptions options = LabelOptions.from(position)
                    .setStyles(styles)
                    .setTexts(textBuilder);

            layer.addLabel(options); // 마커 추가
        }

        // 첫 번째 위치로 지도 이동
        if (!places.isEmpty()) {
            // 2. 모든 라벨이 보이게 카메라 이동
            LatLngBounds bounds = boundsBuilder.build();
            int padding = 100; // 화면 여백(px), 필요에 따라 조정

            kakaoMap.moveCamera(CameraUpdateFactory.fitMapPoints(
                    bounds, padding
            ));
        }
    }

    // MapReadyCallback 을 통해 지도가 정상적으로 시작된 후에 수신할 수 있다.
    private KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull KakaoMap kakaoMap) {
            PlanDetailActivity.this.kakaoMap = kakaoMap;

            Toast.makeText(getApplicationContext(), "Map Start!", Toast.LENGTH_SHORT).show();

            Log.i("k3f", "startPosition: "
                    + kakaoMap.getCameraPosition().getPosition().toString());
            Log.i("k3f", "startZoomLevel: "
                    + kakaoMap.getZoomLevel());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PLACE_SEARCH && resultCode == RESULT_OK && data != null) {
            String placeName = data.getStringExtra("place_name");
            Double latitude = data.getDoubleExtra("latitude", 0);
            Double longitude = data.getDoubleExtra("longitude", 0);
            String category = data.getStringExtra("category");
            String transport = data.getStringExtra("transport");
            String supply = data.getStringExtra("supply");

            GptPlan.Place newPlace = new GptPlan.Place();
            newPlace.setPlace(placeName);
            newPlace.setLatitude(latitude);
            newPlace.setLongitude(longitude);
            newPlace.setCategory(category);
            newPlace.setCoord(latitude + "," + longitude);
            // newPlace.setTransport(transport); // 필요시
            // newPlace.setSupply(supply); // 필요시

            // Firebase에 저장
            DocumentReference dateRef = db.collection("users")
                    .document(userId)
                    .collection("travel")
                    .document(travelId)
                    .collection("gpt_plan")
                    .document(selectedDate);

            // 날짜별 places 컬렉션 참조
            CollectionReference placesRef = db.collection("users")
                    .document(userId)
                    .collection("travel")
                    .document(travelId)
                    .collection("gpt_plan")
                    .document(selectedDate)
                    .collection("places");
            // places 컬렉션에서 id 순으로 내림차순 정렬, 1개만 가져오기
            placesRef.orderBy("id", Query.Direction.DESCENDING).limit(1).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        String newId;
                        if (queryDocumentSnapshots.isEmpty()) {
                            newId = "01"; // 첫 번째 장소라면 01로 시작
                        } else {
                            DocumentSnapshot lastDoc = queryDocumentSnapshots.getDocuments().get(0);
                            String lastId = lastDoc.getId(); // 또는 lastDoc.getString("id")로 필드값 사용
                            // 숫자로 변환 후 1 증가
                            int nextId = Integer.parseInt(lastId) + 1;
                            newId = String.format("%02d", nextId); // 2자리로 맞춤
                        }
                        // newId로 저장
                        placesRef.document(newId).set(newPlace)
                                .addOnSuccessListener(aVoid -> {
                                    // 저장 성공 시 처리
                                    loadGptPlan(); // 전체 데이터 다시 불러오기
                                });
                    });
        }
    }

}