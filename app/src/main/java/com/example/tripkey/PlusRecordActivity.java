package com.example.tripkey;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;
import android.provider.MediaStore;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlusRecordActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1; // 이미지 선택 in 갤러리
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;  // 권한 요청 코드
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
        setContentView(R.layout.activity_plus_record);

        // 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION);
        }

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

        // 여행 기록 저장 버튼
        saveRecordButton.setOnClickListener(v -> {
            String place = ((EditText) findViewById(R.id.place_edit_text)).getText().toString();
            String record = ((EditText) findViewById(R.id.travel_place_record_text)).getText().toString();

            if (place.isEmpty() || record.isEmpty() || photoList.isEmpty()) {
                Toast.makeText(PlusRecordActivity.this, "모든 항목을 채워주세요 (장소, 기록, 사진)", Toast.LENGTH_SHORT).show();
            } else {
                // 모든 필드가 작성되었으면 Firestore에 저장
                ArrayList<String> photoUris = new ArrayList<>();
                for (Uri uri : photoList) {
                    photoUris.add(uri.toString());
                }

                // Firestore 저장할 데이터 생성
                Map<String, Object> recordData = new HashMap<>();
                recordData.put("place", place);
                recordData.put("record", record);
                recordData.put("photos", photoUris);

                db.collection("travelRecords")
                        .add(recordData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(PlusRecordActivity.this, "저장 완료!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PlusRecordActivity.this, RecordActivity.class);
                            startActivity(intent);
                            finish(); // 현재 액티비티 종료
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PlusRecordActivity.this, "저장 실패!", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    // 갤러리 열기
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 여러 장 선택 가능
        startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_IMAGE);
    }


    // 갤러리에서 선택한 사진 받아오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // 여러 장 선택한 경우
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    photoList.add(imageUri);
                }
            } else if (data.getData() != null) {
                // 단일 이미지 선택한 경우
                Uri imageUri = data.getData();
                photoList.add(imageUri);
            }
            photoAdapter.notifyDataSetChanged(); // RecyclerView 업데이트
        }
    }
}
