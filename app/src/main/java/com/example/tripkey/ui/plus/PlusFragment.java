package com.example.tripkey.ui.plus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.app.DatePickerDialog;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.tripkey.MainActivity;
import com.example.tripkey.ui.home.HomeFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;
import com.example.tripkey.R;
import com.example.tripkey.databinding.FragmentPlusBinding;

public class PlusFragment extends Fragment {

    private FragmentPlusBinding binding;

    private LinearLayout mustVisitContainer;
    private TextView startDateInput, endDateInput;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PlusViewModel plusViewModel =
                new ViewModelProvider(this).get(PlusViewModel.class);

        binding = FragmentPlusBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // UI 요소 참조
        EditText travelNameInput = binding.travelNameInput; // 여행 이름 입력
        EditText locationInput = binding.locationInput; // 장소 입력
        startDateInput = binding.startDateInput;
        endDateInput = binding.endDateInput;

        Button whoAloneButton = binding.whoAloneButton;
        Button whoCoupleButton = binding.whoCoupleButton;
        Button whoFriendButton = binding.whoFriendButton;
        Button whoFamilyButton = binding.whoFamilyButton;
        Button whoParentButton = binding.whoParentButton;
        Button whoChildButton = binding.whoChildButton;

        Button styleKeepButton = binding.styleKeepButton;
        Button styleAnalyzeButton = binding.styleAnalyzeButton;

        ImageButton searchButton = binding.locationSearchButton; // 검색 버튼

        mustVisitContainer = root.findViewById(R.id.must_visit_container);
        ImageButton addPlaceButton = root.findViewById(R.id.add_place_button);

        addPlaceButton.setOnClickListener(v -> addNewPlaceField());

        // 시작 날짜 클릭 이벤트 처리
        startDateInput.setOnClickListener(v -> showDatePickerDialog(true));

        // 종료 날짜 클릭 이벤트 처리
        endDateInput.setOnClickListener(v -> showDatePickerDialog(false));

        // 검색 버튼 클릭 이벤트 처리
        searchButton.setOnClickListener(v -> {
            String location = locationInput.getText().toString();
            if (location.isEmpty()) {
            } else {
                // 실제 검색 로직은 여기서 구현합니다.
            }
        });

        whoAloneButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoAloneButton.setBackgroundResource(R.drawable.green_button);
        });

        whoCoupleButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoCoupleButton.setBackgroundResource(R.drawable.green_button);
        });

        whoFriendButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoFriendButton.setBackgroundResource(R.drawable.green_button);
        });
        whoFamilyButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoFamilyButton.setBackgroundResource(R.drawable.green_button);
        });

        whoParentButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoParentButton.setBackgroundResource(R.drawable.green_button);
        });

        whoChildButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoChildButton.setBackgroundResource(R.drawable.green_button);
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
            String travelName = binding.travelNameInput.getText().toString().trim();
            String location = binding.locationInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();

            if (travelName.isEmpty() || location.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(getContext(), "모든 항목을 채워주세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 현재 로그인된 사용자 ID 가져오기
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("userId", null);
            if (userId == null) {
                Toast.makeText(getContext(), "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String travelId = db.collection("users").document(userId)
                    .collection("travel").document().getId();

            Map<String, Object> travelData = new HashMap<>();
            travelData.put("travelName", travelName);
            travelData.put("location", location);
            travelData.put("startDate", startDate);
            travelData.put("endDate", endDate);

            // '꼭 들르고 싶은 곳' 목록 저장
            for (int i = 0; i < mustVisitContainer.getChildCount(); i++) {
                View child = mustVisitContainer.getChildAt(i);
                if (child instanceof LinearLayout) {
                    EditText placeInput = (EditText) ((LinearLayout) child).getChildAt(0);
                    String place = placeInput.getText().toString().trim();
                    if (!place.isEmpty()) {
                        travelData.put("place_" + i, place);
                    }
                }
            }

            // Firestore에 저장
            db.collection("users").document(userId)
                    .collection("travel").document(travelId)
                    .set(travelData)
                    .addOnSuccessListener(aVoid ->{
                        Toast.makeText(getContext(), "여행 일정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        getActivity().finish();

                            })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

        });



        return root;
    }

    private void resetWhoButtons(Button whoAloneButton, Button whoCoupleButton, Button whoFriendButton,Button whoFamilyButton, Button whoParentButton, Button whoChildButton) {
        whoAloneButton.setBackgroundResource(R.drawable.gray_box_full);
        whoCoupleButton.setBackgroundResource(R.drawable.gray_box_full);
        whoFriendButton.setBackgroundResource(R.drawable.gray_box_full);
        whoFamilyButton.setBackgroundResource(R.drawable.gray_box_full);
        whoParentButton.setBackgroundResource(R.drawable.gray_box_full);
        whoChildButton.setBackgroundResource(R.drawable.gray_box_full);

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

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    // 날짜를 항상 두 자릿수로 포맷
                    String formattedMonth = String.format("%02d", selectedMonth + 1);
                    String formattedDay = String.format("%02d", selectedDayOfMonth);

                    String selectedDate = selectedYear + "-" + formattedMonth + "-" + formattedDay;
                    if (isStartDate) {
                        startDateInput.setText(selectedDate);
                    } else {
                        endDateInput.setText(selectedDate);
                    }
                },
                year, month, dayOfMonth
        );

        datePickerDialog.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}