package com.example.tripkey;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.content.SharedPreferences;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.FieldValue;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlusRecordActivity extends AppCompatActivity implements PhotoAdapter.OnPhotoDeleteListener {
    private static final int PICK_IMAGE = 1; // 이미지 선택 in 갤러리
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;  // 권한 요청 코드
    private Button plusPhotoBtn;
    private ImageButton backButton;
    private Button saveRecordButton;
    private RecyclerView photoRecyclerView;
    private PhotoAdapter photoAdapter;
    private ArrayList<Uri> photoList = new ArrayList<>(); // 추가한 사진 리스트
    private FirebaseFirestore db;
    private String userId, travelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus_record);

        // RecyclerView 설정
        photoRecyclerView = findViewById(R.id.photoRecyclerView);
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photoAdapter = new PhotoAdapter(photoList, true, new PhotoAdapter.OnPhotoDeleteListener() {
            @Override
            public void onPhotoDelete(Uri photoUri) {
                removePhoto(photoUri);
                deletePhotoFromFirebase(photoUri);
            }
        });
        photoRecyclerView.setAdapter(photoAdapter);

        travelId = getIntent().getStringExtra("travelId");
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(PlusRecordActivity.this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

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
        plusPhotoBtn = findViewById(R.id.plus_photo_btn);
        backButton = findViewById(R.id.button_back);
        saveRecordButton = findViewById(R.id.save_record_button);


        // 사진 추가 버튼 클릭 이벤트
        plusPhotoBtn.setOnClickListener(v -> openGallery());

        // 뒤로 가기 버튼
        backButton.setOnClickListener(v -> finish());

        // 여행 기록 저장 버튼
        saveRecordButton.setOnClickListener(v -> saveRecordToFirestore());
    }
    @Override
    public void onPhotoDelete(Uri photoUri) {
        removePhoto(photoUri);  // 리스트에서 사진 삭제
        deletePhotoFromFirebase(photoUri);  // Firebase에서 삭제
    }

    // 리스트에서 사진 삭제
    public void removePhoto(Uri photoUri) {
        photoList.remove(photoUri);
        photoAdapter.notifyDataSetChanged();
    }
    // Firebase에서 사진 삭제
    private void deletePhotoFromFirebase(Uri photoUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUri.toString());
        storageRef.delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "사진 삭제 완료", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "사진 삭제 실패", Toast.LENGTH_SHORT).show());
    }
    // 갤러리 열기
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 여러 장 선택 가능
        startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // 여러 장 선택
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    uploadImageToFirebase(imageUri);
                }
            } else if (data.getData() != null) {
                // 단일 이미지 선택
                Uri imageUri = data.getData();
                uploadImageToFirebase(imageUri);
            }
        }
    }
    // Firebase Storage에 이미지 업로드
    private void uploadImageToFirebase(Uri imageUri) {
        if (photoList.contains(imageUri)) {
            Toast.makeText(this, "이미 추가된 사진입니다.", Toast.LENGTH_SHORT).show();
            return; // 중복 이미지 추가 방지
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("travel_images/" + userId + "/" + travelId + "/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            photoList.add(uri); // RecyclerView 업데이트용 리스트에도 추가
                            photoAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "사진 업로드 완료!", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> Toast.makeText(this, "사진 업로드 실패!", Toast.LENGTH_SHORT).show());
    }
    // Firestore에 여행 기록 저장
    private void saveRecordToFirestore() {
        String place = ((EditText) findViewById(R.id.place_edit_text)).getText().toString();
        String record = ((EditText) findViewById(R.id.travel_place_record_text)).getText().toString();

        if (place.isEmpty() || record.isEmpty() || photoList.isEmpty()) {
            Toast.makeText(this, "모든 항목을 채워주세요 (장소, 기록, 사진)", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> photoUrls = new ArrayList<>();
        for (Uri uri : photoList) {
            photoUrls.add(uri.toString());
        }

        Map<String, Object> recordData = new HashMap<>();
        recordData.put("place", place);
        recordData.put("record", record);
        recordData.put("photos", photoUrls);
        recordData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .collection("records")
                .add(recordData)
                .addOnSuccessListener(documentReference -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newRecord", (Serializable) recordData);
                    setResult(RESULT_OK, resultIntent);
                    Toast.makeText(this, "저장 완료!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "저장 실패!", Toast.LENGTH_SHORT).show());
    }
}
