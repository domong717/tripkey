package com.example.tripkey;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static com.example.tripkey.network.ApiClient.getRetrofit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.tripkey.network.ApiService;
import com.example.tripkey.network.ApiClient;
import com.example.tripkey.network.GptRequest;
import com.example.tripkey.network.GptResponse;
import com.example.tripkey.ui.trip.TripFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.LatLngBounds;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.label.LabelTextBuilder;
import com.kakao.vectormap.label.LabelTextStyle;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class GptTripPlanActivity extends AppCompatActivity {
    private Button previouslySelectedButton = null; // 이전에 선택된 버튼을 추적하는 변수

    private List<GptPlan> gptPlanList; // 파싱된 GPT 일정 목록
    private ListView planListView;
    private LinearLayout loadingLayout;
    private String travelName;
    private String startDate;
    private String endDate;
    private String location;
    private String placeToStay;
    private String teamMBTI;
    private String groupMBTIStyle;
    private String teamId;
    private String who;
    private Map<String, Object> travelData = new HashMap<>();
    private String travelId;

    private MapView mapView;
    private KakaoMap kakaoMap;
    private List<GptPlan.Place> pendingPlaces;
    private static final int REQUEST_CODE_PLACE_SEARCH = 1001;
    private int selectedDayIndex = 0; // 현재 선택된 Day 인덱스


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpt_trip_plan);

        planListView = findViewById(R.id.place_list_view);
        TextView tripTitleTextView = findViewById(R.id.tv_trip_title);
        TextView tripDateTextView = findViewById(R.id.tv_trip_date);
        loadingLayout = findViewById(R.id.loading_layout);

        // travelData 멤버 변수에 저장
        travelData = (Map<String, Object>) getIntent().getSerializableExtra("travelData");

        travelName = (String) travelData.get("travelName");
        startDate = (String) travelData.get("startDate");
        endDate = (String) travelData.get("endDate");
        location = (String) travelData.get("location");
        placeToStay = (String) travelData.get("placeToStay");
        teamMBTI = (String) travelData.get("teamMBTI");
        who = (String) travelData.get("who");
        teamId = (String) travelData.get("teamId");
        String gptScheduleJson = getIntent().getStringExtra("gpt_schedule");
        groupMBTIStyle = getIntent().getStringExtra("groupMBTIStyle");
        travelId = getIntent().getStringExtra("travelId");

        Intent intent = getIntent();
        double accommodationLatitude = intent.getDoubleExtra("accommodationLatitude", 37.5665);
        double accommodationLongitude = intent.getDoubleExtra("accommodation_longitude", 126.9780);
        Log.d("GptTripPlanActivity", "숙소 위치 - 위도: " + accommodationLatitude + ", 경도: " + accommodationLongitude);


        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        // AI Plan Plus 버튼 설정
        Button aiPlanPlusButton = findViewById(R.id.ai_plan_add);
        aiPlanPlusButton.setOnClickListener(v -> saveToFirebase());

        // reGpt 버튼 설정
        Button reGptButton = findViewById(R.id.re_gpt_button);
        reGptButton.setOnClickListener(v->requestGptResponse());

        // add plan 버튼 설정
        Button addPlan = findViewById(R.id.add_plan_button);
        addPlan.setOnClickListener(v -> {
            Intent e_intent = new Intent(this, PlaceSearchActivity.class);
            startActivityForResult(e_intent, 1001);
        });


        // TextView에 값 설정
        if (travelName != null) {
            tripTitleTextView.setText(travelName);
        }

        if (startDate != null && endDate != null) {
            tripDateTextView.setText(startDate + " ~ " + endDate);
        }

        if (gptScheduleJson != null) {

            gptScheduleJson = gptScheduleJson.trim();

            gptScheduleJson = gptScheduleJson.replaceAll("(?s)^\\s*```json\\s*", "");
            gptScheduleJson = gptScheduleJson.replaceAll("(?s)\\s*```\\s*$", "");

            try {
                //JSON 문자열을 GptPlan 리스트로 파싱
                Gson gson = new Gson();
                Type listType = new TypeToken<List<GptPlan>>() {}.getType();
                gptPlanList = gson.fromJson(gptScheduleJson, listType);
                Log.d("DEBUG", "gptPlanList: " + gptPlanList);

                updateAllCoordsFromKakao(gptPlanList);

                if (gptPlanList == null || gptPlanList.isEmpty()) {
                    throw new IllegalAccessException("일정이 비어있습니다.");
                }

                LinearLayout buttonContainer = findViewById(R.id.buttonContainer);

                for (int i = 0; i < gptPlanList.size(); i++) {
                    final int indexCopy = i;
                    Button dayButton = new Button(this);
                    dayButton.setText("Day" + (i + 1));
                    dayButton.setBackgroundColor(ContextCompat.getColor(this, R.color.mid_green));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(10, 10, 0, 0);
                    dayButton.setLayoutParams(params);

                    dayButton.setOnClickListener(v -> {
                        selectedDayIndex = indexCopy;
                        if (previouslySelectedButton != null) {
                            previouslySelectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.mid_green));
                        }
                        dayButton.setBackgroundColor(getResources().getColor(R.color.dark_green, null));
                        previouslySelectedButton = dayButton;

                        GptPlan selectedPlan = gptPlanList.get(indexCopy);
                        StringBuilder daySchedule = new StringBuilder();
                        daySchedule.append("  ").append(selectedPlan.getDate()).append("\n\n");


                        List<GptPlan.Place> places = selectedPlan.getPlaces();
                        for (GptPlan.Place place : places) {
                            daySchedule.append("📍 ").append(place.getPlace()).append("\n")
                                    .append("  ∘ 카테고리: ").append(place.getCategory()).append("\n")
                                    .append("  ∘ 이동 수단: ").append(place.getTransport()).append("\n");
                        }


                        PlaceAdapter adapter = new PlaceAdapter(this, places, false, null, null, null);
                        planListView.setAdapter(adapter);

                        // 지도 준비 여부 체크
                        if (kakaoMap != null) {
                            createMapMarkers(places);
                        } else {
                            // 지도 준비 전이면, 변수에 저장해뒀다가 readyCallback에서 마커 찍기
                            pendingPlaces = places; // pendingPlaces는 멤버 변수로 선언
                        }

                    });

                    buttonContainer.addView(dayButton);

                }
            } catch (Exception e) {
                Toast.makeText(this, "일정 데이터를 파싱하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "일정 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

        mapView = findViewById(R.id.map_view);
        mapView.start(lifeCycleCallback, readyCallback);
    }


    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void saveToFirebase() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 인텐트로부터 값 추출
        Intent intent = getIntent();
        String travelName = intent.getStringExtra("travelName");
        String travelId = intent.getStringExtra("travelId");
        String startDate = intent.getStringExtra("startDate");
        String teamId = intent.getStringExtra("teamId");

        double accommodationLatitude = intent.getDoubleExtra("accommodation_latitude", 37.5665);
        double accommodationLongitude = intent.getDoubleExtra("accommodation_longitude", 126.9780);

        if (travelName == null || travelId == null || teamId == null) {
            Toast.makeText(this, "여행 정보 또는 팀 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gptPlanList == null || gptPlanList.isEmpty()) {
            Toast.makeText(this, "저장할 일정이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // travelData 저장
        db.collection("users").document(userId)
                .collection("travel").document(travelId)
                .set(travelData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "기본 여행 정보 저장 완료"))
                .addOnFailureListener(e -> Log.e(TAG, "기본 여행 정보 저장 실패", e));

        // 팀 멤버들한테 gpt plan 저장
        db.collection("users").document(userId)
                .collection("teams").document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> members = (List<String>) documentSnapshot.get("members");
                        if (members != null && !members.isEmpty()) {
                            for (String memberId : members) {
                                saveGptPlanToMember(db, memberId, travelId, startDate, accommodationLatitude, accommodationLongitude);
                            }
                        }
                    }
                });

        // 👉 필터링된 장소 기준으로 totalPlaces 계산
        List<GptPlan.Place> allFilteredPlaces = new ArrayList<>();
        for (GptPlan plan : gptPlanList) {
            List<GptPlan.Place> originalPlaces = plan.getPlaces();
            if (originalPlaces != null) {
                for (GptPlan.Place place : originalPlaces) {
                    String[] coord = place.getCoord().split(",");
                    if (coord.length == 2) {
                        try {
                            double lat = Double.parseDouble(coord[0].trim());
                            double lon = Double.parseDouble(coord[1].trim());
                            double distance = calculateDistance(accommodationLatitude, accommodationLongitude, lat, lon);
                            if (distance <= 20.0) {
                                allFilteredPlaces.add(place);
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "좌표 파싱 오류: " + place.getCoord(), e);
                        }
                    }
                }
            }
        }

        final int totalPlaces = allFilteredPlaces.size();

        if (totalPlaces == 0) {
            Toast.makeText(this, "저장할 장소가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        final int[] successCount = {0};
        final int[] failureCount = {0};

        for (int i = 0; i < gptPlanList.size(); i++) {
            GptPlan plan = gptPlanList.get(i);
            plan.setDateFromStartDate(startDate, i);
            String dateStr = plan.getDate().replace('.', '-');

            List<GptPlan.Place> originalPlaces = plan.getPlaces();
            List<GptPlan.Place> filteredPlaces = new ArrayList<>();

            if (originalPlaces != null) {
                for (GptPlan.Place place : originalPlaces) {
                    String[] coord = place.getCoord().split(",");
                    if (coord.length == 2) {
                        try {
                            double lat = Double.parseDouble(coord[0].trim());
                            double lon = Double.parseDouble(coord[1].trim());
                            double distance = calculateDistance(accommodationLatitude, accommodationLongitude, lat, lon);

                            if (distance <= 20.0) {
                                filteredPlaces.add(place);
                            } else {
                                Log.w(TAG, "20km 초과 장소 제외: " + place.getPlace() + " (" + distance + "km)");
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "좌표 파싱 오류: " + place.getCoord(), e);
                        }
                    }
                }

                if (!filteredPlaces.isEmpty()) {
                    DocumentReference dateRef = db.collection("users")
                            .document(userId)
                            .collection("travel")
                            .document(travelId)
                            .collection("gpt_plan")
                            .document(dateStr);

                    dateRef.set(new HashMap<>());

                    for (int j = 0; j < filteredPlaces.size(); j++) {
                        GptPlan.Place place = filteredPlaces.get(j);
                        place.setDate(plan.getDate());
                        String placeId = String.format("%02d", j);
                        place.setPlaceId(placeId);

                        dateRef.collection("places")
                                .document(placeId)
                                .set(place)
                                .addOnSuccessListener(aVoid -> {
                                    successCount[0]++;
                                    checkCompletion(totalPlaces, successCount[0], failureCount[0]);
                                })
                                .addOnFailureListener(e -> {
                                    failureCount[0]++;
                                    checkCompletion(totalPlaces, successCount[0], failureCount[0]);
                                });
                    }
                }
            }
        }
    }


    private void checkCompletion(int total, int success, int failure) {
        if (success + failure == total) {
            if (failure == 0) {
                Toast.makeText(this, "일정이 모두 저장되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, failure + "건의 저장 실패가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
            // 저장 완료 후 MainActivity로 이동
            Intent intent = new Intent(this, MainActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void requestGptResponse() {
        loadingLayout.setVisibility(View.VISIBLE); // 로딩 시작 표시

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        StringBuilder prompt = new StringBuilder();

        prompt.append("일정 다시 생성");
        prompt.append("여행기간 :").append(startDate).append("~").append(endDate);
        prompt.append("장소 : ").append(location);
        prompt.append("숙소 : ").append(placeToStay).append("숙소 위치를 중심으로 반경 20km까지만,동선을 고려해서 일정 생성.\n");
        prompt.append("여행 스타일: ").append(groupMBTIStyle);
        prompt.append("여행 스타일을 무조건 반영하여 일정 생성.");
        prompt.append(who).append("와(과) 함께 여행\n");

        if (!travelData.isEmpty()) {
            List<String> places = new ArrayList<>();
            for (Map.Entry<String, Object> entry : travelData.entrySet()) {
                // "place_"로 시작하는 키만 필터링
                if (entry.getKey().startsWith("place_") && entry.getValue() instanceof String) {
                    String place = ((String) entry.getValue()).trim();
                    if (!place.isEmpty()) {
                        places.add(place);
                    }
                }
            }
            prompt.append("꼭 가야 하는 장소: ").append(String.join(", ", places));
            prompt.append("꼭 가고 싶은 장소가 반경 20km를 넘는다면, 그 날의 일정은 꼭 가고 싶은 장소 주변으로 동선 생성");
        }
        prompt.append("아래와 같은 JSON 배열 형식으로 응답 필수. 전부 한국어로 출력 필수. 설명은 절대 없이 JSON 데이터만 반환 필수. 형식 :\n\n");

        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"date\": \"YYYY.MM.DD\",\n");
        prompt.append("    \"places\": [\n");
        prompt.append("      {\n");
        prompt.append("        \"place\": \"장소 이름\",\n");
        prompt.append("        \"coord\": \"위도,경도\",\n");
        prompt.append("        \"category\": \"관광지, 음식점, 카페 등\",\n");
        prompt.append("        \"transport\": \"도보, 택시, 버스 등\",\n");
        prompt.append("         \"supply\" : \"해당 장소에서 꼭 필요한 준비물\"");
        prompt.append("      }\n");
        prompt.append("    ]\n");
        prompt.append("  }\n");
        prompt.append("]\n");


        prompt.append("하루하루를 나눠서 JSON 배열로 구성. 진짜 데이터를 넣어서 날짜별로 장소 생성.\n");
        prompt.append(teamMBTI).append("의 맨 마지막이 T인 경우엔 날마다 7곳의 일정 생성, L인 경우엔 날마다 4곳의 일정 생성.");

        prompt.append(teamMBTI).append("에 F 있으면 카페 1곳, M 있으면 카페 추천 금지.");
        prompt.append("식사는 날마다 2곳. 카페 및 음식점 추천 리스트에서 groupMBTI에 따라 추천하여 추가.\n");
        prompt.append("식사/카페 제외 관광지와 쇼핑몰, 자연경관 등을 추천하여 일정에 추가 필수\n");
        prompt.append("중복 장소 추천 금지");
        prompt.append("해당 장소에서 추천하는 준비물도 알려줘. 필요 없는 경우엔 null으로 알려줘도 돼. 예를 들자면 한라산을 방문하기 위해서는 등산화, 편한 옷이 필요하니 supply에 {등산화, 편한옷}을 넣어주면 되고 카페처럼 준비물이 없는 경우 null 값을 넣어줘.");
        prompt.append("꼭 방문해야 하는 장소는 하루에 모두 넣을 필요는 없어. \n");
        prompt.append("숙소 추천 절대 금지");
        prompt.append("절대 '이상입니다' 말 없이 형식 그대로의 JSON만 반환할것. 무조건 한글로만 대답 필수.");

        List<GptRequest.Message> messages = new ArrayList<>();
        messages.add(new GptRequest.Message("user", prompt.toString()));

        GptRequest gptRequest = new GptRequest("gpt-3.5-turbo", messages);

        // 요청 데이터를 JSON 형식으로 로그에 출력
        Log.d("GPT", "Sending Request: " + new Gson().toJson(gptRequest));

        // GPT 요청 보내기
        apiService.getGptAnswer(gptRequest).enqueue(new Callback<GptResponse>() {
            @Override
            public void onResponse(Call<GptResponse> call, Response<GptResponse> response) {
                loadingLayout.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    String gptReply = response.body().choices.get(0).message.content;

                    Log.d("DEBUG", "teamId before Intent: " + teamId);
                    Intent intent = new Intent(GptTripPlanActivity.this, GptTripPlanActivity.class);
                    intent.putExtra("gpt_schedule", gptReply);
                    intent.putExtra("travelData", (HashMap<String, Object>) travelData);
                    intent.putExtra("groupMBTIStyle", groupMBTIStyle);
                    intent.putExtra("travelName", travelName);
                    intent.putExtra("startDate", startDate);
                    intent.putExtra("endDate", endDate);
                    intent.putExtra("location", location);
                    intent.putExtra("placeToStay", placeToStay);
                    intent.putExtra("teamMBTI", teamMBTI);
                    intent.putExtra("who", who);
                    intent.putExtra("travelId", travelId);
                    intent.putExtra("teamId",teamId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    }  else {
                    Toast.makeText(GptTripPlanActivity.this, "응답이 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GptResponse> call, Throwable t) {
                loadingLayout.setVisibility(View.GONE);
                Toast.makeText(GptTripPlanActivity.this, "GPT 호출 에러", Toast.LENGTH_SHORT).show();
                Log.e("GPT", "에러: " + t.getMessage());
            }
        });
    }

    private void saveGptPlanToMember(FirebaseFirestore db, String userId, String travelId, String startDate, double accommodationLatitude, double accommodationLongitude) {
        for (int i = 0; i < gptPlanList.size(); i++) {
            GptPlan plan = gptPlanList.get(i);
            plan.setDateFromStartDate(startDate, i);
            String dateStr = plan.getDate().replace('.', '-');

            List<GptPlan.Place> originalPlaces = plan.getPlaces();
            List<GptPlan.Place> filteredPlaces = new ArrayList<>();

            if (originalPlaces != null) {
                for (GptPlan.Place place : originalPlaces) {
                    String[] coord = place.getCoord().split(",");
                    if (coord.length == 2) {
                        try {
                            double lat = Double.parseDouble(coord[0].trim());
                            double lon = Double.parseDouble(coord[1].trim());
                            double distance = calculateDistance(accommodationLatitude, accommodationLongitude, lat, lon);

                            if (distance <= 20.0) {
                                filteredPlaces.add(place);
                            } else {
                                Log.w(TAG, "[팀 복사 제외] 20km 초과 장소: " + place.getPlace() + " (" + distance + "km)");
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "[팀 복사 오류] 좌표 파싱 실패: " + place.getCoord(), e);
                        }
                    }
                }

                if (!filteredPlaces.isEmpty()) {
                    DocumentReference dateRef = db.collection("users")
                            .document(userId)
                            .collection("travel")
                            .document(travelId)
                            .collection("gpt_plan")
                            .document(dateStr);

                    dateRef.set(new HashMap<>());

                    for (int j = 0; j < filteredPlaces.size(); j++) {
                        GptPlan.Place place = filteredPlaces.get(j);
                        place.setDate(plan.getDate());

                        dateRef.collection("places")
                                .document(String.format("%02d", j))
                                .set(place);
                    }
                }
            }
        }
    }


    // MapReadyCallback 을 통해 지도가 정상적으로 시작된 후에 수신할 수 있다.
    private KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull KakaoMap kakaoMap) {
            GptTripPlanActivity.this.kakaoMap = kakaoMap;

            Toast.makeText(getApplicationContext(), "Map Start!", Toast.LENGTH_SHORT).show();

            Log.i("k3f", "startPosition: "
                    + kakaoMap.getCameraPosition().getPosition().toString());
            Log.i("k3f", "startZoomLevel: "
                    + kakaoMap.getZoomLevel());

            // 만약 pendingPlaces가 있으면 마커 찍기
            if (pendingPlaces != null) {
                createMapMarkers(pendingPlaces);
                pendingPlaces = null;
            }
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
            String coord = place.getCoord();
            if (coord == null) continue;

            String[] parts = coord.split(",");
            if (parts.length != 2) continue;

            try {
                double lat = Double.parseDouble(parts[0].trim());
                double lng = Double.parseDouble(parts[1].trim());
                LatLng position = LatLng.from(lat, lng);
                boundsBuilder.include(position);

                LabelTextBuilder textBuilder = new LabelTextBuilder().setTexts(place.getPlace());

                LabelOptions options = LabelOptions.from(position)
                        .setStyles(styles)
                        .setTexts(textBuilder);

                layer.addLabel(options); // 마커 추가
            } catch (NumberFormatException e) {
                Log.e("createMapMarkers", "잘못된 좌표 형식: " + coord, e);
                continue; // 좌표가 이상하면 그 마커는 건너뜀
            }
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PLACE_SEARCH && resultCode == RESULT_OK && data != null) {
            // 1. 인텐트에서 장소 정보 꺼내기
            String placeName = data.getStringExtra("place_name");
            Double latitude = data.getDoubleExtra("latitude", 0);
            Double longitude = data.getDoubleExtra("longitude", 0);
            String category = data.getStringExtra("category");
            String transport = data.getStringExtra("transport"); // 필요시
            String supply = data.getStringExtra("supply"); // 필요시

            // 2. GptPlan.Place 객체 생성 및 값 세팅
            GptPlan.Place newPlace = new GptPlan.Place();
            newPlace.setPlace(placeName);
            newPlace.setLatitude(latitude);
            newPlace.setLongitude(longitude);
            newPlace.setCategory(category);
            newPlace.setCoord(String.valueOf(latitude)+","+String.valueOf(longitude));
//            newPlace.setTransport(transport);
//            newPlace.setSupply(supply);

            // 3. 현재 선택된 Day의 places 리스트에 추가
            if (selectedDayIndex < 0 || selectedDayIndex >= gptPlanList.size()) {
                Toast.makeText(this, "Day 선택 오류", Toast.LENGTH_SHORT).show();
                return;
            }
            gptPlanList.get(selectedDayIndex).getPlaces().add(newPlace);

            // 4. 리스트뷰 갱신
            PlaceAdapter adapter = new PlaceAdapter(this, gptPlanList.get(selectedDayIndex).getPlaces(),
                    false, null, null, null);
            planListView.setAdapter(adapter);

            // 5. 지도 마커 갱신
            if (kakaoMap != null) {
                createMapMarkers(gptPlanList.get(selectedDayIndex).getPlaces());
            } else {
                pendingPlaces = gptPlanList.get(selectedDayIndex).getPlaces();
            }
        }
    }

    private void updateAllCoordsFromKakao(List<GptPlan> planList) {
        KakaoApiService api = KakaoApiClient.getRetrofitInstance().create(KakaoApiService.class);
        String kakaoKey = "KakaoAK 42d61720c6096d7a9ec5e7c8d0950740";

        // 실패한 장소 저장용 리스트 (동기화 필요할 수 있음)
        List<GptPlan.Place> failedPlaces = Collections.synchronizedList(new ArrayList<>());

        int totalPlacesCount = 0;
        for (GptPlan plan : planList) {
            for (GptPlan.Place place : plan.getPlaces()) {
                if (place.getPlace() != null && !place.getPlace().isEmpty()) {
                    totalPlacesCount++;
                }
            }
        }
        final int totalPlaces = totalPlacesCount;  // final 변수로

        AtomicInteger processedCount = new AtomicInteger(0);

        for (GptPlan plan : planList) {
            for (GptPlan.Place place : plan.getPlaces()) {
                String placeName = place.getPlace();
                if (placeName == null || placeName.isEmpty()) continue;

                Call<KakaoSearchResponse> call = api.searchKeyword(kakaoKey, placeName);
                call.enqueue(new Callback<KakaoSearchResponse>() {
                    @Override
                    public void onResponse(Call<KakaoSearchResponse> call, Response<KakaoSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().documents.isEmpty()) {
                            KakaoSearchResponse.Document doc = response.body().documents.get(0);
                            String coord = doc.y + "," + doc.x;
                            place.setCoord(coord);
                            Log.d("CoordUpdate", "✔ " + placeName + " → " + coord);
                        } else {
                            Log.w("CoordUpdate", "✖ " + placeName + " 검색 실패");
                            failedPlaces.add(place);
                        }
                        if (processedCount.incrementAndGet() == totalPlaces) {
                            removeFailedPlaces();
                        }
                    }

                    @Override
                    public void onFailure(Call<KakaoSearchResponse> call, Throwable t) {
                        Log.e("CoordUpdate", "API 실패: " + placeName, t);
                        failedPlaces.add(place);
                        if (processedCount.incrementAndGet() == totalPlaces) {
                            removeFailedPlaces();
                        }
                    }

                    private void removeFailedPlaces() {
                        for (GptPlan plan : planList) {
                            plan.getPlaces().removeAll(failedPlaces);
                        }
                        Log.d("CoordUpdate", "실패한 장소 제거 완료. 현재 남은 장소 수: " + planList.stream().mapToInt(p -> p.getPlaces().size()).sum());
                    }
                });
            }
        }
    }

}
