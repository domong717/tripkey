package com.example.tripkey;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static com.example.tripkey.network.ApiClient.getRetrofit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        // AI Plan Plus 버튼 설정
        Button aiPlanPlusButton = findViewById(R.id.ai_plan_add);
        aiPlanPlusButton.setOnClickListener(v -> saveToFirebase());

        // reGpt 버튼 설정
        Button reGptButton = findViewById(R.id.re_gpt_button);
        reGptButton.setOnClickListener(v->requestGptResponse());

        // TextView에 값 설정
        if (travelName != null) {
            tripTitleTextView.setText(travelName);
        }

        if (startDate != null && endDate != null) {
            tripDateTextView.setText(startDate + " ~ " + endDate);
        }

        if (gptScheduleJson != null) {
            try {
                //JSON 문자열을 GptPlan 리스트로 파싱
                Gson gson = new Gson();
                Type listType = new TypeToken<List<GptPlan>>() {}.getType();
                gptPlanList = gson.fromJson(gptScheduleJson, listType);

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
                                    .append("  ∘ 이동 수단: ").append(place.getTransport()).append("\n")
                                    .append("  ∘ 예상 소요 시간: ").append(place.getTime()).append("\n\n");
                        }



                        PlaceAdapter adapter = new PlaceAdapter(this, places);
                        planListView.setAdapter(adapter);

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
    }

    private void saveToFirebase() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        // FirebaseFirestore 인스턴스 가져오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // travelName, travelId를 Intent에서 받기
        String travelName = getIntent().getStringExtra("travelName");
        String travelId = getIntent().getStringExtra("travelId");  // 각 여행에 고유한 ID를 사용
        String startDate = getIntent().getStringExtra("startDate");
        String teamId = getIntent().getStringExtra("teamId");

        if (travelName == null || travelId == null) {
            Toast.makeText(this, "여행 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (teamId == null) {
            Toast.makeText(this, "팀 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gptPlanList == null || gptPlanList.isEmpty()) {
            Toast.makeText(this, "저장할 일정이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference teamRef = db.collection("users")
                .document(userId)
                .collection("teams")
                .document(teamId);

        teamRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> members = (List<String>) documentSnapshot.get("members");

                if (members != null && !members.isEmpty()) {
                    for (String memberId : members) {
                        saveGptPlanToMember(db, memberId, travelId, startDate);  // 아래에서 구현
                    }
                } else {
                    Toast.makeText(this, "팀에 멤버가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "팀 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "팀 멤버 조회 실패", Toast.LENGTH_SHORT).show();
        });


        final int totalPlaces = gptPlanList.stream()
                .mapToInt(plan -> plan.getPlaces() != null ? plan.getPlaces().size() : 0)
                .sum();
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

            List<GptPlan.Place> places = plan.getPlaces();
            if (places != null) {
                DocumentReference dateRef = db.collection("users")
                        .document(userId)
                        .collection("travel")
                        .document(travelId)
                        .collection("gpt_plan")
                        .document(dateStr);


                dateRef.set(new HashMap<>())
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "날짜 문서 생성 " + dateStr))
                        .addOnFailureListener(e -> Log.e(TAG, "날짜 문서 생성 실패", e));


                for (int j = 0; j < places.size(); j++) {
                    GptPlan.Place place = places.get(j);
                    place.setDate(plan.getDate());

                    dateRef.collection("places")
                            .document(String.format("%02d", j))
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

        prompt.append("방금 짠 일정 되게 별로야. 다시 짜줘.");
        prompt.append("나는 ").append(startDate).append("부터 ").append(endDate).append("까지 여행을 가.");
        prompt.append("장소는 ").append(location).append("야.");
        prompt.append("숙소는 ").append(placeToStay).append("에 있어. 숙소 위치를 중심으로 반경 20km까지만,동선을 고려해서 짜줘.\n");
        prompt.append("만약 꼭 가고 싶은 장소가 반경 20km를 넘는다면, 그 날의 일정은 꼭 가고 싶은 장소 주변으로 동선을 짜줘.");
        prompt.append("여행 스타일은 ").append(teamMBTI).append("이고 ").append("이 스타일은 ").append(groupMBTIStyle).append("이라고 할 수 있어.");
        prompt.append("여행 스타일을 통해 알 수 있는 선호하는 교통 수단을 중심으로 짜줘");
        prompt.append(who).append("와(과) 함께 가\n");

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
            prompt.append("꼭 가야 하는 장소는 ").append(String.join(", ", places)).append(" 이야.\n");
        }
        prompt.append("아래와 같은 JSON 배열 형식으로 응답해줘. 전부 한국어로 출력해주고 설명은 절대 하지 말고 JSON 데이터만 반환해. 형식은 다음과 같아:\n\n");

        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"date\": \"YYYY.MM.DD\",\n");
        prompt.append("    \"places\": [\n");
        prompt.append("      {\n");
        prompt.append("        \"place\": \"장소 이름\",\n");
        prompt.append("        \"coord\": \"위도,경도\",\n");
        prompt.append("        \"category\": \"관광지, 음식점, 카페 등\",\n");
        prompt.append("        \"transport\": \"도보, 택시, 버스 등\",\n");
        prompt.append("        \"time\": \"이전 장소에서 해당 장소를 가는데 예상 이동 시간\",\n");
        prompt.append("         \"supply\" : \"해당 장소에서 꼭 필요한 준비물\"");
        prompt.append("      }\n");
        prompt.append("    ]\n");
        prompt.append("  }\n");
        prompt.append("]\n");


        prompt.append("이런 형식으로 하루하루를 나눠서 JSON 배열로 구성해서 줘. 예시 말고 진짜 데이터를 넣어서, 날짜별로 하루에 5~7개 장소를 넣어줘.\n");
        prompt.append("식사는 하루 3번 포함되어야 하고, 카페는 하루에 한 번 포함해줘. 모든 가게는 실제로 존재해야돼.\n");
        prompt.append("그리고 전에 갔던 장소를 또 가는 건 원하지 않아.");
        prompt.append("그리고 해당 장소에서 추천하는 준비물도 알려줘. 필요 없는 경우엔 null으로 알려줘도 돼. 예를 들자면 한라산을 방문하기 위해서는 등산화, 편한 옷이 필요하니 supply에 {등산화, 편한옷}을 넣어주면 되고 카페처럼 준비물이 없는 경우 null 값을 넣어줘.");
        prompt.append("꼭 방문해야 하는 장소는 하루에 모두 넣을 필요는 없어. \n");
        prompt.append("그리고 마지막은 절대 '이상입니다' 같은 말 없이 JSON만 반환하고 무조건 한글로만 답해줘.");

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

    private void saveGptPlanToMember(FirebaseFirestore db, String userId, String travelId, String startDate) {
        for (int i = 0; i < gptPlanList.size(); i++) {
            GptPlan plan = gptPlanList.get(i);
            plan.setDateFromStartDate(startDate, i);
            String dateStr = plan.getDate().replace('.', '-');

            List<GptPlan.Place> places = plan.getPlaces();
            if (places != null) {
                DocumentReference dateRef = db.collection("users")
                        .document(userId)
                        .collection("travel")
                        .document(travelId)
                        .collection("gpt_plan")
                        .document(dateStr);

                dateRef.set(new HashMap<>());

                for (int j = 0; j < places.size(); j++) {
                    GptPlan.Place place = places.get(j);
                    place.setDate(plan.getDate());

                    dateRef.collection("places")
                            .document(String.format("%02d", j))
                            .set(place);
                }
            }
        }
    }

}
