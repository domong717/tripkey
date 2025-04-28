package com.example.tripkey;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GptTripPlanActivity extends AppCompatActivity {
    // 이전에 선택된 버튼을 추적하는 변수
    private Button previouslySelectedButton = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpt_trip_plan);

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        // gpt_schedule 키로 전달된 데이터 받기
        String gptSchedule = getIntent().getStringExtra("gpt_schedule");

        if (gptSchedule != null) {
            // "이상입니다" 이후의 텍스트를 제거
            int index = gptSchedule.indexOf("이상입니다");
            if (index != -1) {
                gptSchedule = gptSchedule.substring(0, index).trim();
            }

            // 받은 GPT 일정 데이터를 화면에 표시,사용
            TextView scheduleTextView = findViewById(R.id.scheduleTextView);

            // 날짜를 기준으로 분리
            // 날짜를 기준으로 분리
            String[] scheduleDays = gptSchedule.split("(?=\\d{4}-\\d{2}-\\d{2})");


            LinearLayout buttonLayout = findViewById(R.id.buttonLayout);

            // 버튼 가로 배치
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

            // 각 날짜별로 버튼 동적 생성
            for (int i = 0; i < scheduleDays.length; i++) {
                final int dayIndex = i; // 버튼 클릭 시 사용될 인덱스

                // 버튼 생성
                Button dayButton = new Button(this);
                dayButton.setText("Day " + (i + 1));  // "Day 1", "Day 2" 형식으로 버튼 텍스트 설정
                // 이미 buttonLayout에 버튼이 있다면 제거
                if (dayButton.getParent() != null) {
                    ((ViewGroup) dayButton.getParent()).removeView(dayButton);
                }
                buttonLayout.addView(dayButton);  // 동적으로 생성한 버튼을 LinearLayout에 추가

                dayButton.setBackgroundColor(getResources().getColor(R.color.mid_green));

                // 버튼 크기 설정
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, // Width set to wrap content
                        LinearLayout.LayoutParams.WRAP_CONTENT  // Height set to wrap content
                );
                params.setMargins(10, 10, 0, 0); // 버튼 간 마진 추가
                dayButton.setLayoutParams(params);

                // 버튼 클릭 리스너 설정
                dayButton.setOnClickListener(v -> {
                    // 이전에 선택된 버튼의 색상 초기화
                    if (previouslySelectedButton != null) {
                        previouslySelectedButton.setBackgroundColor(getResources().getColor(R.color.mid_green, null));
                    }

                    // 현재 버튼의 색상 변경
                    dayButton.setBackgroundColor(getResources().getColor(R.color.dark_green, null));
                    previouslySelectedButton = dayButton;

                    // 선택된 날짜에 해당하는 일정을 표시
                    scheduleTextView.setText(getDaySchedule(scheduleDays, dayIndex));
                });
            }
        } else {
            Toast.makeText(this, "일정 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // 일정에서 특정 날짜의 일정을 반환하는 메서드
    private String getDaySchedule(String[] scheduleDays, int dayIndex) {
        // 날짜별로 일정 내용 반환 (인덱스 0: 1일차, 1: 2일차, ...)
        if (dayIndex >= 0 && dayIndex < scheduleDays.length) {
            return scheduleDays[dayIndex].replaceAll("이상입니다", "").trim();
        }
        return "일정을 불러올 수 없습니다.";
    }
}
