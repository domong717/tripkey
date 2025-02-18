package com.example.tripkey.ui.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Toast;

import com.example.tripkey.ChecklistActivity;
import com.example.tripkey.MBTIDescriptionActivity;
import com.example.tripkey.RecordActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.tripkey.databinding.FragmentInfoBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // View Binding 초기화
        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // SharedPreferences에서 userId 가져오기
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            // Firestore에서 사용자 이름 가져오기
            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Firestore에서 가져온 사용자 이름
                                String userName = document.getString("userName");
                                if (userName != null) {
                                    binding.myName.setText(userName);
                                } else {
                                    binding.myName.setText("이름 없음");
                                }
                            } else {
                                binding.myName.setText("사용자 정보 없음");
                            }
                        } else {
                            Toast.makeText(getContext(), "데이터 불러오기 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // 로그인하지 않은 경우 처리
            binding.myName.setText("로그인 필요");
        }

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
}
