package com.example.tripkey;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;


import com.example.tripkey.ui.trip.TripFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;


public class GptTripPlanActivity extends AppCompatActivity {
    private Button previouslySelectedButton = null; // 이전에 선택된 버튼을 추적하는 변수

    private List<GptPlan> gptPlanList; // 파싱된 GPT 일정 목록
    private ListView planListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpt_trip_plan);

        planListView = findViewById(R.id.place_list_view);
        TextView tripTitleTextView = findViewById(R.id.tv_trip_title);
        TextView tripDateTextView = findViewById(R.id.tv_trip_date);

        // Intent에서 데이터 꺼내기
        String travelName = getIntent().getStringExtra("travelName");
        String startDate = getIntent().getStringExtra("startDate");
        String endDate = getIntent().getStringExtra("endDate");
        String gptScheduleJson = getIntent().getStringExtra("gpt_schedule");

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        // AI Plan Plus 버튼 설정
        ImageButton aiPlanPlusButton = findViewById(R.id.ai_plan_add);
        aiPlanPlusButton.setOnClickListener(v -> saveToFirebase());


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
                Type listType = new TypeToken<List<GptPlan>>() {
                }.getType();
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

        if (travelName == null || travelId == null) {
            Toast.makeText(this, "여행 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gptPlanList == null || gptPlanList.isEmpty()) {
            Toast.makeText(this, "저장할 일정이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gptPlanList == null || gptPlanList.isEmpty()) {
            Toast.makeText(this, "저장할 일정이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }


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

}


