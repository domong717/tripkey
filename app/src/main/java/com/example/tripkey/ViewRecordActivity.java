package com.example.tripkey;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ViewRecordActivity extends AppCompatActivity {
    private TextView placeTextView, recordTextView;
    private RecyclerView photoRecyclerView;
    private PhotoAdapter photoAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_record);

        // UI 요소 연결
        placeTextView = findViewById(R.id.placeTextView);
        recordTextView = findViewById(R.id.recordTextView);
        photoRecyclerView = findViewById(R.id.photoImageView);

        // 백버튼 클릭 이벤트
        ImageButton buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(v -> finish());

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // RecyclerView 설정
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photoAdapter = new PhotoAdapter(new ArrayList<>());
        photoRecyclerView.setAdapter(photoAdapter);

        // Firestore에서 데이터 가져오기
        loadTravelRecord();
    }

    private void loadTravelRecord() {
        db.collection("travelRecords")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String place = document.getString("place");
                            String record = document.getString("record");
                            ArrayList<String> photoUris = (ArrayList<String>) document.get("photos");

                            // TextView에 데이터 설정
                            if (place != null) placeTextView.setText(place);
                            if (record != null) recordTextView.setText(record);

                            // 사진 목록 RecyclerView에 추가
                            if (photoUris != null && !photoUris.isEmpty()) {
                                // String 리스트를 Uri 리스트로 변환
                                ArrayList<Uri> photoUriList = new ArrayList<>();
                                for (String uriString : photoUris) {
                                    photoUriList.add(Uri.parse(uriString)); // String을 Uri로 변환
                                }
                                photoAdapter.updatePhotoList(photoUriList); // Uri 리스트 전달
                            }
                        }
                    } else {
                        Toast.makeText(this, "저장된 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "기록 불러오기 실패", Toast.LENGTH_SHORT).show());
    }
}
