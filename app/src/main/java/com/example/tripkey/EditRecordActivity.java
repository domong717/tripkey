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

import java.util.ArrayList;

public class EditRecordActivity extends AppCompatActivity implements PhotoAdapter.OnPhotoDeleteListener {
    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private Button plusPhotoBtn;
    private ImageButton backButton;
    private ImageButton saveRecordButton;
    private RecyclerView photoRecyclerView;
    private PhotoAdapter photoAdapter;
    private ArrayList<Uri> photoList = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId, travelId, recordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_record);

        // RecyclerView 설정
        photoRecyclerView = findViewById(R.id.photoRecyclerView);
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photoAdapter = new PhotoAdapter(photoList, true, this);
        photoRecyclerView.setAdapter(photoAdapter);

        // 인텐트에서 데이터 받아오기
        travelId = getIntent().getStringExtra("travelId");
        recordId = getIntent().getStringExtra("recordId");
        String place = getIntent().getStringExtra("place");
        String record = getIntent().getStringExtra("record");
        ArrayList<String> photoUris = getIntent().getStringArrayListExtra("photoUris");

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        // 기존 값 세팅
        ((EditText) findViewById(R.id.place_edit_text)).setText(place);
        ((EditText) findViewById(R.id.travel_place_record_text)).setText(record);
        if (photoUris != null) {
            for (String uri : photoUris) {
                photoList.add(Uri.parse(uri));
            }
            photoAdapter.notifyDataSetChanged();
        }

        // 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION);
        }

        db = FirebaseFirestore.getInstance();

        // UI 요소 초기화
        plusPhotoBtn = findViewById(R.id.plus_photo_btn);
        backButton = findViewById(R.id.button_back);
        saveRecordButton = findViewById(R.id.save_record_button);

        // 사진 추가 버튼 클릭 이벤트
        plusPhotoBtn.setOnClickListener(v -> openGallery());

        // 뒤로 가기 버튼
        backButton.setOnClickListener(v -> finish());

        // 기록 수정 저장 버튼
        saveRecordButton.setOnClickListener(v -> updateRecordToFirestore());
    }

    @Override
    public void onPhotoDelete(Uri photoUri) {
        removePhoto(photoUri);
        deletePhotoFromFirebase(photoUri);
    }

    public void removePhoto(Uri photoUri) {
        photoList.remove(photoUri);
        photoAdapter.notifyDataSetChanged();
    }

    private void deletePhotoFromFirebase(Uri photoUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUri.toString());
        storageRef.delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "사진 삭제 완료", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "사진 삭제 실패", Toast.LENGTH_SHORT).show());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    uploadImageToFirebase(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (photoList.contains(imageUri)) {
            Toast.makeText(this, "이미 추가된 사진입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("travel_images/" + userId + "/" + travelId + "/" + System.currentTimeMillis() + ".jpg");
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            photoList.add(uri);
                            photoAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "사진 업로드 완료!", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> Toast.makeText(this, "사진 업로드 실패!", Toast.LENGTH_SHORT).show());
    }

    private void updateRecordToFirestore() {
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

        db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .collection("records")
                .document(recordId)
                .update("place", place,
                        "record", record,
                        "photos", photoUrls)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "수정 완료!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "수정 실패!", Toast.LENGTH_SHORT).show());
    }
}
