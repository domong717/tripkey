package com.example.tripkey;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlusRecordActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1; // 갤러리에서 이미지 선택을 위한 코드
    private Button plusPhotoBtn;
    private ImageButton backButton;
    private ImageButton saveRecordButton;
    private ImageView plusPhotoButton;
    private RecyclerView photoRecyclerView;
    private PhotoAdapter photoAdapter;
    private ArrayList<Uri> photoList = new ArrayList<>(); // 추가한 사진 리스트
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus_record); // XML 파일과 연결

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // UI 요소 초기화
        plusPhotoButton = findViewById(R.id.plus_photo_button);
        plusPhotoBtn = findViewById(R.id.plus_photo_btn);
        backButton = findViewById(R.id.button_back);
        saveRecordButton = findViewById(R.id.save_record_button);
        photoRecyclerView = findViewById(R.id.photoRecyclerView);

        // RecyclerView 설정
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photoAdapter = new PhotoAdapter(photoList);
        photoRecyclerView.setAdapter(photoAdapter);

        // 사진 추가 버튼 클릭 이벤트
        plusPhotoBtn.setOnClickListener(v -> openGallery());

        // 뒤로 가기 버튼
        backButton.setOnClickListener(v -> finish());

        // 여행 기록 저장 버튼 (추후 기능 추가 가능)
        saveRecordButton.setOnClickListener(v -> {
            // 저장 기능 추가 예정
        });
    }
    // 갤러리 열기
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    // 갤러리에서 선택한 사진 받아오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                photoList.add(selectedImage); // 리스트에 추가
                photoAdapter.notifyDataSetChanged(); // RecyclerView 업데이트
            }
        }
    }
    // 여행 기록 저장
    private void saveTravelRecord() {
        // 여행 기록 정보 (예: 여행 장소, 여행 사진 등)
        String travelPlace = "방문 장소 이름"; // 실제로 사용자의 입력을 받도록 구현해야 함
        String recordDescription = "여행 장소 기록"; // 실제로 사용자의 입력을 받도록 구현해야 함

        // Firestore에 저장할 데이터
        Map<String, Object> recordData = new HashMap<>();
        recordData.put("travelPlace", travelPlace);
        recordData.put("recordDescription", recordDescription);
        recordData.put("photos", photoList); // 사진 데이터 추가

        // Firestore에 데이터 저장
        db.collection("travel_records")
                .add(recordData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PlusRecordActivity.this, "기록이 저장되었습니다!", Toast.LENGTH_SHORT).show();
                    finish(); // 저장 후 이전 화면으로 돌아가기
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PlusRecordActivity.this, "기록 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}

