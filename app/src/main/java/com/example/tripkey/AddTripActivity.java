package com.example.tripkey;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTripActivity extends AppCompatActivity {

    private ActivityAddTripBinding binding;
    private LinearLayout mustVisitContainer;
    private TextView startDateInput, endDateInput, currentMBTI;
    private String selectedWho = "";
    private String selectedStyle = "";
    private static final String TAG = "AddTripActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 선택된 친구들의 ID 리스트 받기
        ArrayList<String> selectedFriendsIds = getIntent().getStringArrayListExtra("selectedFriendsIds");
        if (selectedFriendsIds != null) {
            Log.d(TAG, "선택된 친구 ID 리스트: " + selectedFriendsIds);
            calculateGroupMBTI(selectedFriendsIds, teamMBTI -> currentMBTI.setText(teamMBTI));
        }

        EditText travelNameInput = binding.travelNameInput;
        EditText locationInput = binding.locationInput;
        startDateInput = binding.startDateInput;
        endDateInput = binding.endDateInput;
        currentMBTI = binding.currentMbtiText;

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
    private void calculateGroupMBTI(ArrayList<String> selectedFriendsIds, OnMBTICalculatedListener listener) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String[] currentUserMBTI = new String[1]; // 💡 final 배열로 래핑

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentUserMBTI[0] = documentSnapshot.getString("mbti");
                    if (currentUserMBTI[0] == null || currentUserMBTI[0].length() != 4) {
                        currentUserMBTI[0] = "INFP"; // fallback
                    }

                    Map<Character, Integer> mbtiCount = new HashMap<>();
                    int[] processedCount = {0};
                    int totalCount = selectedFriendsIds.size();

                    for (String friendId : selectedFriendsIds) {
                        db.collection("users").document(friendId).get()
                                .addOnSuccessListener(friendSnapshot -> {
                                    if (friendSnapshot.exists() && friendSnapshot.contains("mbti")) {
                                        String mbti = friendSnapshot.getString("mbti");
                                        if (mbti != null && mbti.length() == 4) {
                                            for (char c : mbti.toCharArray()) {
                                                mbtiCount.put(c, mbtiCount.getOrDefault(c, 0) + 1);
                                            }
                                        }
                                    }
                                    processedCount[0]++;
                                    if (processedCount[0] == totalCount) {
                                        listener.onMBTICalculated(determineGroupMBTI(mbtiCount, currentUserMBTI[0]));
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalCount) {
                                        listener.onMBTICalculated(determineGroupMBTI(mbtiCount, currentUserMBTI[0]));
                                    }
                                });
                    }
                });
    }



    private String determineGroupMBTI(Map<Character, Integer> mbtiCount, String currentUserMBTI) {
        char[] mbtiPositions = {'I', 'O', 'B', 'T', 'L', 'S', 'M', 'F'};
        StringBuilder groupMBTI = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            char first = mbtiPositions[i * 2];
            char second = mbtiPositions[i * 2 + 1];

            int countFirst = mbtiCount.getOrDefault(first, 0);
            int countSecond = mbtiCount.getOrDefault(second, 0);

            if (countFirst > countSecond) {
                groupMBTI.append(first);
            } else if (countFirst < countSecond) {
                groupMBTI.append(second);
            } else {
                // 같을 경우 현재 유저의 MBTI에서 해당 위치의 값 사용
                groupMBTI.append(currentUserMBTI.charAt(i));
            }
        }

        return groupMBTI.toString();
    }


    private interface OnMBTICalculatedListener {
        void onMBTICalculated(String teamMBTI);
    }

    private void saveTripData() {
        String travelName = binding.travelNameInput.getText().toString().trim();
        String location = binding.locationInput.getText().toString().trim();
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();

        if (travelName.isEmpty() || location.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || selectedWho.isEmpty() || selectedStyle.isEmpty()) {
            Toast.makeText(this, "모든 항목을 채워주세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> selectedFriendsIds = getIntent().getStringArrayListExtra("selectedFriendsIds");
        calculateGroupMBTI(selectedFriendsIds, teamMBTI -> {
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
            travelData.put("teamMBTI", teamMBTI);

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
        });
    }

}