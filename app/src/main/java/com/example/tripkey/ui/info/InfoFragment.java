package com.example.tripkey.ui.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Toast;
import android.widget.TextView;

import com.example.tripkey.ChecklistActivity;
import com.example.tripkey.MBTIDescriptionActivity;
import com.example.tripkey.RecordActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.tripkey.databinding.FragmentInfoBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private TextView userNameTextView;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";
    private DocumentReference userRef;  // 사용자 정보를 가져올 DocumentReference 추가

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Firestore 초기화
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // View Binding 초기화
        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        userNameTextView = binding.myName;

        String userId = getUserIdFromPreferences();
        if (userId != null) {
            userRef = db.collection("users").document(userId);  // userRef 초기화
            loadUserName();
        } else {
            showUserIdDialog();
        }

        // 버튼 클릭 이벤트 처리
        binding.mbtiLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MBTIDescriptionActivity.class);
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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Firestore에서 사용자 이름을 가져오는 메서드
    private void loadUserName() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String userName = document.getString("userName");
                    userNameTextView.setText(userName != null ? userName : "이름 불러오기 실패");
                } else {
                    Toast.makeText(getActivity(), "사용자 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "데이터 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // SharedPreferences에서 사용자 ID를 가져오는 메서드
    private String getUserIdFromPreferences() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    // SharedPreferences에 사용자 ID를 저장하는 메서드
    private void saveUserIdToPreferences(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    // 사용자 ID 설정 다이얼로그 (미구현)
    private void showUserIdDialog() {
        // ID 설정 다이얼로그 구현 필요
    }
}
