package com.example.tripkey;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;




public class GptTripPlanActivity extends AppCompatActivity {
    private Button previouslySelectedButton = null; // 이전에 선택된 버튼을 추적하는 변수

    private List<GptPlan> gptPlanList; // 파싱된 GPT 일정 목록

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpt_trip_plan);


        TextView tripTitleTextView = findViewById(R.id.tv_trip_title);
        TextView tripDateTextView = findViewById(R.id.tv_trip_date);

        // Intent에서 데이터 꺼내기
        String travelName = getIntent().getStringExtra("travelName");
        String startDate = getIntent().getStringExtra("startDate");
        String endDate = getIntent().getStringExtra("endDate");

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        // gpt_schedule 키로 전달된 데이터 받기
        String gptScheduleJson = getIntent().getStringExtra("gpt_schedule");

        // TextView에 값 설정
        if (travelName != null) {
            tripTitleTextView.setText(travelName);
        }

        if (startDate != null && endDate != null) {
            tripDateTextView.setText(startDate + " ~ " + endDate);
        }

        if (gptScheduleJson != null) {
            try{
                //JSON 문자열을 GptPlan 리스트로 파싱
                Gson gson= new Gson();
                Type listType = new TypeToken<List<GptPlan>>() {}.getType();
                gptPlanList=gson.fromJson(gptScheduleJson, listType);

                if (gptPlanList == null || gptPlanList.isEmpty()){
                    throw new IllegalAccessException("일정이 비어있습니다.");
                }

                TextView scheduleTextView = findViewById(R.id.scheduleTextView);
                LinearLayout buttonContainer = findViewById(R.id.buttonContainer);

                for (int i=0;i<gptPlanList.size();i++){
                    final int indexCopy = i;
                    Button dayButton = new Button(this);
                    dayButton.setText("Day"+(i+1));
                    dayButton.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_green));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(10,10,0,0);
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
                        if (places != null) {
                            for (GptPlan.Place place : places) {
                                daySchedule.append("\uD83D\uDCCD  ").append(place.getPlace()).append("\n")
                                        .append("  ∘ 카테고리: ").append(place.getCategory()).append("\n")
                                        .append("  ∘ 이동 수단: ").append(place.getTransport()).append("\n")
                                        .append("  ∘ 예상 소요 시간: ").append(place.getTime()).append("\n\n");
                            }
                        }


                        scheduleTextView.setText(daySchedule.toString().trim());
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
}
