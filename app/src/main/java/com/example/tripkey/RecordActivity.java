package com.example.tripkey;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record); // XML 파일과 연결

        // 백버튼 클릭 이벤트
        ImageButton buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 백버튼 클릭 시 현재 액티비티 종료
                finish();
            }
        });

        // 여행지도 확인하기 클릭 이벤트
        ImageButton buttonTripmap = findViewById(R.id.button_tripmap);
        buttonTripmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecordActivity.this, TripmapActivity.class);
                startActivity(intent);
            }
        });

        // 여행장소 피드백남기기 클릭 이벤트
        ImageButton buttonFeedback = findViewById(R.id.button_feedback);
        buttonFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecordActivity.this,FeedbackActivity.class);
                startActivity(intent);
            }
        });

        // 기록 추가하기 클릭 이벤트
        ImageButton buttonPlusRecord = findViewById(R.id.button_plus_record);
        buttonPlusRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecordActivity.this,PlusRecordActivity.class);
                startActivity(intent);
            }
        });

        // 기록 모아보기 클릭 이벤트
        ImageButton buttonViewRecord = findViewById(R.id.button_view_record);
        buttonViewRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecordActivity.this,ViewRecordActivity.class);
                startActivity(intent);
            }
        });

    }
}
