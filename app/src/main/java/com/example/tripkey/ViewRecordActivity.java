package com.example.tripkey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ViewRecordActivity extends AppCompatActivity {
    private RecyclerView photoRecyclerView;
    private PhotoAdapter photoAdapter;
    private FirebaseFirestore db;
    private String travelId, userId;
    private LinearLayout pastTripsContainer;
    private String place;
    private String record;
    private ArrayList<String> photoUris;

    //    private static final String TAG = "ViewRecordActivity";
    private TextView noRecordsTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_record);

        // 🔹 Intent에서 값 받아오기
        travelId = getIntent().getStringExtra("travelId");

        pastTripsContainer = findViewById(R.id.past_trips_container);
        photoRecyclerView = findViewById(R.id.photoRecyclerView);
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photoAdapter = new PhotoAdapter(new ArrayList<Uri>(), false, new PhotoAdapter.OnPhotoDeleteListener() {
            @Override
            public void onPhotoDelete(Uri photoUri) {
                // 삭제 동작 처리 코드 작성
            }
        });

        photoRecyclerView.setAdapter(photoAdapter);

        noRecordsTextView = findViewById(R.id.noRecordsTextView);
        ImageButton buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        Log.d("ViewRecordActivity", "userId: " + userId);
        Log.d("ViewRecordActivity", "travelId: " + travelId);

        if (userId != null && travelId != null) {
            loadTravelRecord(travelId); // Load travel data
        } else {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        ImageButton addRecordButton = findViewById(R.id.add_record_button);
        addRecordButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewRecordActivity.this, PlusRecordActivity.class);
            intent.putExtra("travelId", travelId);
            startActivity(intent);
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // 새로운 기록이 추가되었을 때
            String place = data.getStringExtra("place");
            String record = data.getStringExtra("record");

            // Firebase에서 새로운 기록을 불러오고 화면에 갱신
            loadTravelRecord(travelId);
        }
    }

    private void loadTravelRecord(String travelId) {
//        Log.d(TAG, "loadTravelRecord 시작, travelId: " + travelId);
        db.collection("users").document(userId)
                .collection("travel")
                .document(travelId)
                .get() // 여행 정보 가져오기
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 여행 이름, 장소, 기간 정보 가져오기
                        String travelName = documentSnapshot.getString("travelName");
                        String location = documentSnapshot.getString("location");
                        String startDate = documentSnapshot.getString("startDate");
                        String endDate = documentSnapshot.getString("endDate");

                        // TextView에 여행 정보 설정
                        TextView travelNameTextView = findViewById(R.id.textViewTravelPlace); // 여행 이름 표시 TextView
                        if (travelName != null) {
                            travelNameTextView.setText(travelName); // 여행 이름
                        }

                        TextView travelInfoTextView = findViewById(R.id.travel_info); // 여행 장소, 기간 표시 TextView
                        String travelInfoText = "";
                        if (location != null) {
                            travelInfoText += "여행지 : " + location + "\n"; // 장소
                        }
                        if (startDate != null && endDate != null) {
                            travelInfoText += "여행 기간: " + startDate + " ~ " + endDate; // 여행 기간
                        }
                        travelInfoTextView.setText(travelInfoText);


                        // 여행 기록 불러오기
                        db.collection("users").document(userId)
                                .collection("travel")
                                .document(travelId)
                                .collection("records")
                                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (queryDocumentSnapshots.isEmpty()) {
                                        noRecordsTextView.setVisibility(View.VISIBLE);
                                        pastTripsContainer.setVisibility(View.GONE);
                                        Log.d("ViewRecordActivity", "기록 없음: " + queryDocumentSnapshots.isEmpty());

                                    } else {
                                        noRecordsTextView.setVisibility(View.GONE);
                                        pastTripsContainer.setVisibility(View.VISIBLE);
                                        // records 컬렉션에 데이터가 있는 경우
                                        for (QueryDocumentSnapshot recordDoc : queryDocumentSnapshots) {
                                            String place = recordDoc.getString("place");
                                            String record = recordDoc.getString("record");
                                            ArrayList<String> photoUris = (ArrayList<String>) recordDoc.get("photos");

                                            // 각 기록을 세트로 추가
                                            addRecordToView(place, record, photoUris);

                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "기록 불러오기 실패", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "여행 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "여행 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                    Log.e("ViewRecordActivity", "Error getting travel document", e);
                });
    }
    private void addRecordToView(String place, String record, ArrayList<String> photoUris) {
        // 여행 기록을 하나의 LinearLayout으로 묶기
        LinearLayout recordLayout = new LinearLayout(this);
        recordLayout.setOrientation(LinearLayout.VERTICAL);
        recordLayout.setPadding(25,20,0,50);
        recordLayout.setBackgroundResource(R.drawable.record_background); // 배경 설정
        // 배경 간 마진을 추가해주기 위한 LayoutParams 설정
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 20, 0, 20); // 배경들 간에 간격을 두기 위해 상단과 하단에 마진 추가 (예: 20dp)


        // 여행 장소
        TextView placeTextView = new TextView(this);
        placeTextView.setText("\uD83D\uDD16"+place);
        placeTextView.setTextSize(20);
        placeTextView.setTextColor(getResources().getColor(R.color.black));



        RecyclerView photoRecyclerView = new RecyclerView(this);
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 여행 기록
        TextView recordTextView = new TextView(this);
        recordTextView.setText(record);
        recordTextView.setTextSize(16);
        recordTextView.setTextColor(getResources().getColor(R.color.black));

        PhotoAdapter newPhotoAdapter = new PhotoAdapter(new ArrayList<>(), false, new PhotoAdapter.OnPhotoDeleteListener() {
            @Override
            public void onPhotoDelete(Uri photoUri) {

            }
        });

        if (photoUris != null && !photoUris.isEmpty()) {
            ArrayList<Uri> photoUriList = new ArrayList<>();
            for (Object uriObject : photoUris) {
                if (uriObject instanceof String) {
                    photoUriList.add(Uri.parse((String) uriObject));
                }
            }
            newPhotoAdapter.updatePhotoList(photoUriList);
        }
        photoRecyclerView.setAdapter(newPhotoAdapter);

        // 레이아웃에 추가
        recordLayout.addView(placeTextView);
        recordLayout.addView(photoRecyclerView);
        recordLayout.addView(recordTextView);
        // 레이아웃에 마진을 적용
        recordLayout.setLayoutParams(layoutParams);
        // main container에 추가
        pastTripsContainer.addView(recordLayout);


    }
}
