package com.example.tripkey.ui.info;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.tripkey.ChecklistActivity;
import com.example.tripkey.FriendListActivity;
import com.example.tripkey.LoginActivity;
import com.example.tripkey.MBTIDescriptionActivity;
import com.example.tripkey.RecordActivity;
import com.example.tripkey.WishListActivity;
import com.example.tripkey.databinding.FragmentInfoBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InfoFragment extends Fragment {

    private ImageView profileImageView;
    private FragmentInfoBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private SharedPreferences sharedPreferences;
    private TextView userNameTextView;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";
    private DocumentReference userRef;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        profileImageView = binding.profileImageView;
        userNameTextView = binding.myName;

        userId = getUserIdFromPreferences();
        if (userId != null) {
            userRef = db.collection("users").document(userId);
            loadUserName();
            loadAccountInfo();
            loadProfileImage();
        } else {
            showUserIdDialog();
        }

        profileImageView.setOnClickListener(v -> openGallery());

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageToFirebase(imageUri);
                        }
                    }
                });

        binding.accountEditButton.setOnClickListener(v -> showAccountEditDialog());


        // 버튼 클릭 이벤트 처리
        binding.mbtiLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MBTIDescriptionActivity.class);
            intent.putExtra("from", "info");
            startActivity(intent);
        });

        binding.recordLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RecordActivity.class);
            startActivity(intent);
        });

        binding.checklistLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChecklistActivity.class);
            startActivity(intent);
        });

        binding.heartLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), WishListActivity.class); //수정 필
            startActivity(intent);
        });

        // 로그아웃 버튼 클릭 이벤트 처리
        binding.logoutButton.setOnClickListener(v -> handleLogout());

        return root;
    }


    private void showAccountEditDialog() {
        Context context = getContext();
        if (context == null) return;

        android.widget.EditText editText = new android.widget.EditText(context);
        editText.setHint("계좌번호 입력");

        // EditText 좌우에 15dp padding만 적용
        int sidePadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 15, context.getResources().getDisplayMetrics());

        LinearLayout container = new LinearLayout(context);
        container.setPadding(sidePadding, 0, sidePadding, 0); // 좌우 패딩
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(editText);

        new android.app.AlertDialog.Builder(context)
                .setTitle("계좌번호 수정")
                .setView(container)
                .setPositiveButton("확인", (dialog, which) -> {
                    String newAccount = editText.getText().toString().trim();
                    if (!newAccount.isEmpty()) {
                        binding.accountText.setText(newAccount); // 텍스트뷰 갱신
                        saveAccountToFirestore(newAccount); // Firestore에 저장
                    } else {
                        Toast.makeText(context, "계좌번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }


    private void saveAccountToFirestore(String accountNumber) {
        if (userRef == null) {
            Toast.makeText(getActivity(), "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.update("account", accountNumber)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getActivity(), "계좌번호가 저장되었습니다.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "계좌번호 저장 실패", Toast.LENGTH_SHORT).show());
    }

    private void loadAccountInfo() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String account = document.getString("account");
                    binding.accountText.setText(account != null ? account : "");
                }
            }
        });
    }

    // 로그아웃 처리 메서드
    private void handleLogout() {
        // SharedPreferences에서 사용자 ID 삭제
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_ID);
        editor.apply();

        // 로그아웃 메시지 표시
        Toast.makeText(getActivity(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

        // 로그인 화면으로 이동
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 현재 액티비티 종료
        getActivity().finish();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (userId == null) {
            Toast.makeText(getActivity(), "사용자 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference profileRef = storageRef.child("profile_images/" + userId + ".jpg");

        profileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveProfileImageUrl(imageUrl);
                }))
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show());
    }

    private void saveProfileImageUrl(String imageUrl) {
        userRef.update("profileImage", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "프로필 이미지 업데이트 완료", Toast.LENGTH_SHORT).show();
                    loadProfileImage();
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "프로필 이미지 저장 실패", Toast.LENGTH_SHORT).show());
    }

    private void loadProfileImage() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String imageUrl = document.getString("profileImage");
                    if (imageUrl != null) {
                        Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .into(profileImageView);
                    }
                }
            }
        });
    }

    private void loadUserName() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String userName = document.getString("userName");
                    userNameTextView.setText(userName != null ? userName : "이름 불러오기 실패");
                }
            }
        });
    }

    private String getUserIdFromPreferences() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    private void showUserIdDialog() {
        Toast.makeText(getActivity(), "사용자 ID를 설정해주세요.", Toast.LENGTH_SHORT).show();
    }
}
