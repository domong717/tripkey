package com.example.tripkey.ui.info;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tripkey.RecordActivity;
import com.example.tripkey.databinding.FragmentInfoBinding;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InfoViewModel infoViewModel =
                new ViewModelProvider(this).get(InfoViewModel.class);

        // 바인딩을 사용하여 레이아웃 설정
        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // TextView 데이터 설정
        final TextView textView = binding.textInfo;
        infoViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // 버튼 클릭 이벤트 설정
        binding.buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RecordActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
