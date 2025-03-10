package com.example.tripkey;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tripkey.databinding.ActivityAddTripBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTripActivity extends AppCompatActivity {

    private ActivityAddTripBinding binding;
    private LinearLayout mustVisitContainer;
    private TextView startDateInput, endDateInput;
    private String selectedWho = "";
    private String selectedStyle = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EditText travelNameInput = binding.travelNameInput;
        EditText locationInput = binding.locationInput;
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

        mustVisitContainer = findViewById(R.id.must_visit_container);
        ImageButton addPlaceButton = findViewById(R.id.add_place_button);
        addPlaceButton.setOnClickListener(v -> addNewPlaceField());

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        startDateInput.setOnClickListener(v -> showDatePickerDialog(true));
        endDateInput.setOnClickListener(v -> showDatePickerDialog(false));

        whoAloneButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoAloneButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="혼자";
        });

        whoCoupleButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoCoupleButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="연인";
        });

        whoFriendButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoFriendButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="친구";
        });
        whoFamilyButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoFamilyButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="가족";
        });

        whoParentButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoParentButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="부모님";
        });

        whoChildButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoParentButton, whoChildButton);
            whoChildButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="아이";
        });


        styleKeepButton.setOnClickListener(v -> {
            resetStyleButtons(styleKeepButton, styleAnalyzeButton);
            styleKeepButton.setBackgroundResource(R.drawable.green_button);
            selectedStyle="유지";
        });

        styleAnalyzeButton.setOnClickListener(v -> {
            resetStyleButtons(styleKeepButton, styleAnalyzeButton);
            styleAnalyzeButton.setBackgroundResource(R.drawable.green_button);
            selectedStyle="다시 분석";
        });

        binding.aiScheduleButton.setOnClickListener(v -> saveTripData());
    }

    private void saveTripData() {
        String travelName = binding.travelNameInput.getText().toString().trim();
        String location = binding.locationInput.getText().toString().trim();
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();


        if (travelName.isEmpty() || location.isEmpty() || startDate.isEmpty() || endDate.isEmpty()|| selectedWho.isEmpty()||selectedStyle.isEmpty()) {
            Toast.makeText(this, "모든 항목을 채워주세요!", Toast.LENGTH_SHORT).show();
            return;
        }


        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
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
        travelData.put("who", selectedWho);
        travelData.put("travelStyle", selectedStyle);


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

        db.collection("users").document(userId)
                .collection("travel").document(travelId)
                .set(travelData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "여행 일정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
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
    private void addNewPlaceField() {
        LinearLayout newFieldLayout = new LinearLayout(this);
        newFieldLayout.setOrientation(LinearLayout.HORIZONTAL);
        newFieldLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        EditText newPlaceField = new EditText(this);
        newPlaceField.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        newPlaceField.setHint("장소 입력");

        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(R.drawable.delete);
        deleteButton.setBackground(null);
        deleteButton.setOnClickListener(v -> mustVisitContainer.removeView(newFieldLayout));

        newFieldLayout.addView(newPlaceField);
        newFieldLayout.addView(deleteButton);

        mustVisitContainer.addView(newFieldLayout);
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
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
}