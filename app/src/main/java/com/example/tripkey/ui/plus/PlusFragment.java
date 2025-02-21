package com.example.tripkey.ui.plus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tripkey.R;
import com.example.tripkey.databinding.FragmentPlusBinding;

public class PlusFragment extends Fragment {

    private FragmentPlusBinding binding;

    private LinearLayout mustVisitContainer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PlusViewModel plusViewModel =
                new ViewModelProvider(this).get(PlusViewModel.class);

        binding = FragmentPlusBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // UI 요소 참조
        EditText travelNameInput = binding.travelNameInput; // 여행 이름 입력
        EditText locationInput = binding.locationInput; // 장소 입력
        EditText startDateInput = binding.startDateInput; // 시작 날짜
        EditText endDateInput = binding.endDateInput; // 종료 날짜

        Button whoAloneButton = binding.whoAloneButton;
        Button whoCoupleButton = binding.whoCoupleButton;
        Button whoFriendButton = binding.whoFriendButton;

        Button styleKeepButton = binding.styleKeepButton;
        Button styleAnalyzeButton = binding.styleAnalyzeButton;

        ImageButton searchButton = binding.locationSearchButton; // 검색 버튼

        mustVisitContainer = root.findViewById(R.id.must_visit_container);
        ImageButton addPlaceButton = root.findViewById(R.id.add_place_button);

        addPlaceButton.setOnClickListener(v -> addNewPlaceField());

        // 검색 버튼 클릭 이벤트 처리
        searchButton.setOnClickListener(v -> {
            String location = locationInput.getText().toString();
            if (location.isEmpty()) {
            } else {
                // 실제 검색 로직은 여기서 구현합니다.
            }
        });

        whoAloneButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton);
            whoAloneButton.setBackgroundResource(R.drawable.green_button);
        });

        whoCoupleButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton);
            whoCoupleButton.setBackgroundResource(R.drawable.green_button);
        });

        whoFriendButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton);
            whoFriendButton.setBackgroundResource(R.drawable.green_button);
        });


        styleKeepButton.setOnClickListener(v -> {
            resetStyleButtons(styleKeepButton, styleAnalyzeButton); // Reset all buttons to default state
            styleKeepButton.setBackgroundResource(R.drawable.green_button); // Highlight selected button
        });

        styleAnalyzeButton.setOnClickListener(v -> {
            resetStyleButtons(styleKeepButton, styleAnalyzeButton);
            styleAnalyzeButton.setBackgroundResource(R.drawable.green_button);
        });


        binding.aiScheduleButton.setOnClickListener(v -> {

        });



        return root;
    }

    private void resetWhoButtons(Button whoAloneButton, Button whoCoupleButton, Button whoFriendButton) {
        whoAloneButton.setBackgroundResource(R.drawable.gray_box_full);
        whoCoupleButton.setBackgroundResource(R.drawable.gray_box_full);
        whoFriendButton.setBackgroundResource(R.drawable.gray_box_full);
    }

    private void resetStyleButtons(Button styleKeepButton, Button styleAnalyzeButton) {
        styleKeepButton.setBackgroundResource(R.drawable.gray_box_full);
        styleAnalyzeButton.setBackgroundResource(R.drawable.gray_box_full);
    }

    // 새로운 EditText와 삭제 버튼을 추가하는 메서드
    private void addNewPlaceField() {
        // 새로 추가될 LinearLayout (EditText + 삭제 버튼 포함)
        LinearLayout newFieldLayout = new LinearLayout(getContext());
        newFieldLayout.setOrientation(LinearLayout.HORIZONTAL);
        newFieldLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        newFieldLayout.setPadding(0, 16, 0, 16);

        // EditText 생성
        EditText newPlaceField = new EditText(getContext());
        newPlaceField.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1 // 가중치로 공간 채우기
        ));
        newPlaceField.setHint("장소 입력");
        newPlaceField.setBackgroundResource(R.drawable.gray_box_full);
        newPlaceField.setPadding(16, 16, 16, 16);

        // 삭제 버튼 생성
        ImageButton deleteButton = new ImageButton(getContext());
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                100,
                100
        ));
        deleteButton.setImageResource(R.drawable.delete);
        deleteButton.setBackground(null); // 배경 제거
        deleteButton.setContentDescription("Delete Place");

        // 삭제 버튼 클릭 이벤트 처리
        deleteButton.setOnClickListener(v -> mustVisitContainer.removeView(newFieldLayout));

        // EditText와 삭제 버튼을 새로 만든 레이아웃에 추가
        newFieldLayout.addView(newPlaceField);
        newFieldLayout.addView(deleteButton);

        // 최종적으로 "꼭 들르고 싶은 곳" 컨테이너에 새 레이아웃 추가
        mustVisitContainer.addView(newFieldLayout);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}