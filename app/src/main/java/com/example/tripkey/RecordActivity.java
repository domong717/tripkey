package com.example.tripkey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordActivity extends AppCompatActivity {

    private LinearLayout pastTripsContainer;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        pastTripsContainer = findViewById(R.id.past_trips_container); // XML에서 해당 레이아웃 가져오기
        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            loadPastTrips(); // 지난 여행 데이터 불러오기
        } else {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

        // 백버튼 클릭 이벤트
        ImageButton buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> finish());

        // 여행지도 확인하기 클릭 이벤트
        ImageButton buttonTripmap = findViewById(R.id.button_tripmap);
        buttonTripmap.setOnClickListener(v -> {
            Intent intent = new Intent(RecordActivity.this, TripmapActivity.class);
            startActivity(intent);
        });

        // 여행장소 피드백남기기 클릭 이벤트
        ImageButton buttonFeedback = findViewById(R.id.button_feedback);
        buttonFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(RecordActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });
    }

    private void loadPastTrips() {
        db.collection("users").document(userId)
                .collection("travel")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date today = new Date();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String travelId = document.getId();
                        String travelName = document.getString("travelName");
                        String location = document.getString("location");
                        String startDate = document.getString("startDate");
                        String endDate = document.getString("endDate");
                        String who = document.getString("who");
                        String travelStyle = document.getString("travelStyle");

                        try {
                            Date tripEndDate = sdf.parse(endDate);
                            if (tripEndDate != null && tripEndDate.before(today)) {
                                addPastTripView(travelId, travelName, location, startDate, endDate, who, travelStyle);
                            }
                        } catch (ParseException e) {
                            Log.e("RecordActivity", "날짜 변환 오류: " + e.getMessage());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "지난 여행 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show());
    }
    private void addPastTripView(String travelId, String travelName, String location, String startDate, String endDate, String who, String travelStyle) {
        // Yellow Box Layout 생성
        LinearLayout tripLayout = new LinearLayout(this);
        tripLayout.setOrientation(LinearLayout.VERTICAL);
        tripLayout.setPadding(100, 70, 16, 16);  // 여백 설정
        tripLayout.setBackgroundResource(R.drawable.yellow_box); // yellow_box 드로어블 사용

        // 여행 이름
        TextView travelTitle = new TextView(this);
        travelTitle.setText(travelName);
        travelTitle.setTextSize(18);
        travelTitle.setTextColor(getResources().getColor(R.color.black));

        // 여행 장소
        TextView travelLocation = new TextView(this);
        travelLocation.setText(location);
        travelLocation.setTextSize(16);
        travelLocation.setTextColor(getResources().getColor(R.color.black));

        // 여행 기간
        TextView travelPeriod = new TextView(this);
        travelPeriod.setText(startDate + " ~ " + endDate);
        travelPeriod.setTextSize(14);
        travelPeriod.setTextColor(getResources().getColor(R.color.gray));

//        // 여행 스타일 및 누구와
//        TextView travelDetails = new TextView(this);
//        travelDetails.setText("여행 스타일: " + travelStyle + " | 누구와: " + who);
//        travelDetails.setTextSize(12);
//        travelDetails.setTextColor(getResources().getColor(R.color.gray));

        // 추가된 TextView들을 tripLayout에 추가
        tripLayout.addView(travelTitle);
        tripLayout.addView(travelLocation);
        tripLayout.addView(travelPeriod);
        //tripLayout.addView(travelDetails);

        // itemLayout을 pastTripsContainer에 추가
        pastTripsContainer.addView(tripLayout);

        // 아이템 간의 간격 추가 (위/아래 여백)
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tripLayout.getLayoutParams();
        params.setMargins(0, 16, 0, 16);  // 위아래 여백 추가
        tripLayout.setLayoutParams(params);

        // 클릭 이벤트 (여행 상세보기)
        tripLayout.setOnClickListener(v -> {
            Intent intent = new Intent(RecordActivity.this, ViewRecordActivity.class);
            intent.putExtra("travelId", travelId);
            startActivity(intent);
        });
    }
}
