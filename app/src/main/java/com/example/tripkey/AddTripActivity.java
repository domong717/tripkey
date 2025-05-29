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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tripkey.databinding.ActivityAddTripBinding;
import com.example.tripkey.network.ApiClient;
import com.example.tripkey.network.ApiService;
import com.example.tripkey.network.GptRequest;
import com.example.tripkey.network.GptResponse;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

public class AddTripActivity extends AppCompatActivity {

    private ActivityAddTripBinding binding;
    private LinearLayout mustVisitContainer;
    private TextView startDateInput, endDateInput, currentMBTI;
    private String selectedWho = "";
    private String selectedStyle = "";
    private String teamId;
    private static final String TAG = "AddTripActivity";
    private static final int REQUEST_CODE_LOCATION = 1001;
    private LinearLayout loadingLayout;

    private ActivityResultLauncher<Intent> mbtiResultLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingLayout = findViewById(R.id.loading_layout);

        teamId = getIntent().getStringExtra("teamId");

        // 선택된 친구들의 ID 리스트 받기
        ArrayList<String> selectedFriendsIds = getIntent().getStringArrayListExtra("selectedFriendsIds");
        if (selectedFriendsIds != null) {
            Log.d(TAG, "선택된 친구 ID 리스트: " + selectedFriendsIds);
            calculateGroupMBTI(selectedFriendsIds, teamMBTI -> currentMBTI.setText(teamMBTI));
        }

        EditText travelNameInput = binding.travelNameInput;
        EditText locationInput = binding.locationInput;
        EditText placeToStayInput = binding.placeToStayInput;
        startDateInput = binding.startDateInput;
        endDateInput = binding.endDateInput;
        currentMBTI = binding.currentMbtiText;


        Button whoAloneButton = binding.whoAloneButton;
        Button whoCoupleButton = binding.whoCoupleButton;
        Button whoFriendButton = binding.whoFriendButton;
        Button whoFamilyButton = binding.whoFamilyButton;
        Button whoCoworkerButton = binding.whoCoworkerButton;
        Button whoPetButton = binding.whoPetButton;

        Button styleKeepButton = binding.styleKeepButton;
        Button styleAnalyzeButton = binding.styleAnalyzeButton;

        mustVisitContainer = findViewById(R.id.must_visit_container);
        ImageButton addPlaceButton = findViewById(R.id.add_place_button);
        addPlaceButton.setOnClickListener(v -> addNewPlaceField());

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());


        // 장소
        ImageButton searchLocationBtn = binding.locationSearchButton;
        searchLocationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationSearchActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOCATION);
        });

        // 장소
        ImageButton searchAccomodationBtn = binding.accomodationSearchButton;
        searchAccomodationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccomodationSearchActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOCATION);
        });

        startDateInput.setOnClickListener(v -> showDatePickerDialog(true));
        endDateInput.setOnClickListener(v -> showDatePickerDialog(false));

        whoAloneButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoCoworkerButton, whoPetButton);
            whoAloneButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="혼자";
        });

        whoCoupleButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoCoworkerButton, whoPetButton);
            whoCoupleButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="연인";
        });

        whoFriendButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoCoworkerButton, whoPetButton);
            whoFriendButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="친구";
        });
        whoFamilyButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoCoworkerButton, whoPetButton);
            whoFamilyButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="가족";
        });

        whoCoworkerButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoCoworkerButton, whoPetButton);
            whoCoworkerButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="동료";
        });

        whoPetButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoCoworkerButton, whoPetButton);
            whoPetButton.setBackgroundResource(R.drawable.green_button);
            selectedWho="반려동물";
        });


        styleKeepButton.setOnClickListener(v -> {
            resetStyleButtons(styleKeepButton, styleAnalyzeButton);
            styleKeepButton.setBackgroundResource(R.drawable.green_button);
            selectedStyle = "유지";
        });

        mbtiResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String newMBTI = data.getStringExtra("mbti_result");
                            if (newMBTI != null) {
                                TextView currentMBTIText = findViewById(R.id.current_mbti_text);
                                currentMBTIText.setText(newMBTI);
                            }
                        }
                    }
                });

        styleAnalyzeButton.setOnClickListener(v -> {
            resetStyleButtons(styleKeepButton, styleAnalyzeButton);
            styleAnalyzeButton.setBackgroundResource(R.drawable.green_button);
            selectedStyle = "다시 분석";

            // 🔽 ReMBTITestActivity로 이동
            Intent intent = new Intent(this, ReMBTITestActivity.class);
            mbtiResultLauncher.launch(intent);
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
                    }else {
                        // startDate와 비교
                        String startDateText = startDateInput.getText().toString();
                        if (!startDateText.isEmpty()) {
                            String[] startDateParts = startDateText.split("-");
                            int startYear = Integer.parseInt(startDateParts[0]);
                            int startMonth = Integer.parseInt(startDateParts[1]);
                            int startDay = Integer.parseInt(startDateParts[2]);

                            // 선택한 날짜가 startDate보다 이전인지 확인
                            if (selectedYear < startYear ||
                                    (selectedYear == startYear && selectedMonth + 1 < startMonth) ||
                                    (selectedYear == startYear && selectedMonth + 1 == startMonth && selectedDayOfMonth < startDay)) {
                                Toast.makeText(this, "종료 날짜는 시작 날짜 이후여야 합니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        endDateInput.setText(selectedDate);
                    }
                },
                year, month, dayOfMonth
        );

        // endDate 선택 시, startDate 이후 날짜만 가능하도록 제한
        if (!isStartDate) {
            String startDateText = startDateInput.getText().toString();
            if (!startDateText.isEmpty()) {
                String[] startDateParts = startDateText.split("-");
                int startYear = Integer.parseInt(startDateParts[0]);
                int startMonth = Integer.parseInt(startDateParts[1]) - 1; // Calendar에서 0부터 시작
                int startDay = Integer.parseInt(startDateParts[2]);

                Calendar minDate = Calendar.getInstance();
                minDate.set(startYear, startMonth, startDay);
                datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            }
        }

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
                    if (currentUserMBTI[0] == null || currentUserMBTI[0].length() != 5) {
                        currentUserMBTI[0] = "분석필요"; // fallback
                    }

                    Map<Character, Integer> mbtiCount = new HashMap<>();
                    int[] processedCount = {0};
                    int totalCount = selectedFriendsIds.size();

                    for (String friendId : selectedFriendsIds) {
                        db.collection("users").document(friendId).get()
                                .addOnSuccessListener(friendSnapshot -> {
                                    if (friendSnapshot.exists() && friendSnapshot.contains("mbti")) {
                                        String mbti = friendSnapshot.getString("mbti");
                                        if (mbti != null && mbti.length() == 5) {
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
        char[] mbtiPositions = {'I', 'O', 'B', 'C', 'R', 'E', 'M', 'F','T','L'};
        StringBuilder groupMBTI = new StringBuilder();

        for (int i = 0; i < 5; i++) {
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
        String placeToStay=binding.placeToStayInput.getText().toString().trim();
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();
        String groupMBTI = currentMBTI.getText().toString().trim();
        String who = selectedWho;

        if (travelName.isEmpty() || location.isEmpty() || placeToStay.isEmpty()|| startDate.isEmpty() || endDate.isEmpty() || selectedWho.isEmpty() || selectedStyle.isEmpty()) {
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
            travelData.put("travelId", travelId); // 친구 쪽에도 travelId 동일하게 저장
            travelData.put("travelName", travelName);
            travelData.put("location", location);
            travelData.put("startDate", startDate);
            travelData.put("endDate", endDate);
            travelData.put("who", selectedWho);
            travelData.put("travelStyle", selectedStyle);
            travelData.put("teamMBTI", teamMBTI);
            travelData.put("teamId", teamId);
            travelData.put("creatorId", userId); // 누가 만든 여행인지 명시
            travelData.put("placeToStay",placeToStay);


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

            // 나의 travel 경로에 저장
            db.collection("users").document(userId)
                    .collection("travel").document(travelId)
                    .set(travelData)
                    .addOnSuccessListener(aVoid -> {
                        // 2. 친구들 travel 경로에도 동일하게 저장
                        if (selectedFriendsIds != null && !selectedFriendsIds.isEmpty()) {
                            for (String friendId : selectedFriendsIds) {
                                db.collection("users").document(friendId)
                                        .collection("travel").document(travelId)
                                        .set(travelData);
                            }
                        }

//                        Toast.makeText(this, "여행 일정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(this, MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

            // 여행 MBTI에 맞는 스타일 설명
            final String groupMBTIStyle;

            switch (groupMBTI) {
                case "IBRFT":
                    groupMBTIStyle = "조용한 호텔에서 여유롭게, 하지만 하루는 알차게! 맛집을 향해 부지런히 달린다!고급 호텔에서 여유로운 아침을 맞이하지만, 하루 일정은 꽉 채워 보낸다.대중교통을 이용해 유명 맛집을 여러 곳 빠르게 방문하며, 비용은 크게 신경 쓰지 않는 알찬 미식 여행자 스타일.";
                    break;
                case "IBRFL":
                    groupMBTIStyle = "호텔에서 한껏 여유 부리고, 맛집은 줄 서서라도 꼭 간다!조용하고 고급스러운 숙소에서 느긋하게 하루를 시작하고, 대중교통으로 천천히 도시를 누빈다.맛집 투어는 여유롭게 즐기되, 비용은 아끼지 않는다.\n" +
                            "많은 곳을 방문하기보다 퀄리티와 분위기에 집중하는 ‘플렉스’와 ‘힐링’을 동시에 즐기는 여유형 여행자.";
                    break;
                case "IBRMT":
                    groupMBTIStyle = "조용한 호텔에서 하루를 시작해 전시와 유적지를 빠짐없이 돌아본다!고급 숙소에서 차분히 아침을 맞이하고, 대중교통을 타고 여러 문화 명소를 효율적으로 탐방한다.입장료와 기념품 구매에 아낌없으며, 빡빡한 일정 속에서도 문화와 효율을 모두 챙기는 탐방형 여행자.";
                    break;
                case "IBRML":
                    groupMBTIStyle = "전시도 유적지도 천천히, 하루 한두 곳이면 충분하다!조용한 고급 숙소에서 충분히 쉬고, 대중교통을 이용해 문화 명소를 여유롭게 방문한다.여러 곳을 빠르게 돌기보다는 한 곳에서 깊이 있게 즐기며, 시간과 마음 모두 넉넉하게 ‘문화 힐링’에 집중하는 여유형 여행자.";
                    break;
                case "IBEFT":
                    groupMBTIStyle = "가성비 좋은 숙소에서 휴식하되, 맛집은 하루에 알차게 공략한다!알뜰한 숙소에서 편안히 쉬면서, 대중교통을 이용해 여러 맛집을 빠르게 찾아다닌다.비용을 아끼면서도 맛있는 음식을 놓치지 않고, 빡빡한 일정 속에서 최대한 많은 미식을 즐기는 알뜰 여행자.";
                    break;
                case "IBEFL":
                    groupMBTIStyle = "가성비 좋은 숙소에서 여유롭게 쉬고, 맛있는 음식도 천천히 즐긴다!가성비 좋은 호텔에서 느긋한 아침을 보내고, 대중교통으로 맛집을 천천히 찾아간다.돈을 아끼면서도 여행의 즐거움을 놓치지 않고, 맛과 휴식을 조화롭게 즐기는 알뜰 미식가 스타일.";
                    break;
                case "IBEMT":
                    groupMBTIStyle = "가성비 좋은 숙소에서 휴식, 하지만 문화 탐방은 알차게!가성비 좋은 숙소에서 편안히 쉬면서, 대중교통을 이용해 여러 박물관과 유적지를 빠르게 돌아다닌다.비용은 절약하지만, 하루에 가능한 많은 문화 명소를 방문하며 알찬 일정을 소화하는 실속형 탐방 여행자.";
                    break;
                case "IBEML":
                    groupMBTIStyle = "가성비 좋은 숙소에서 여유롭게 쉬고, 문화 명소도 천천히 즐긴다.가성비 좋은 호텔에서 느긋한 아침을 보내고, 대중교통을 타고 박물관이나 유적지를 천천히 방문한다.돈은 아끼면서도 문화 여행의 깊이를 놓치지 않고, 여유로운 일정으로 힐링하는 알뜰 문화 여행자.";
                    break;
                case "ICRFT":
                    groupMBTIStyle = "럭셔리한 숙소에서 차를 타고, 하루도 빠짐없이 미식 탐방!고급 숙소에서 편안히 머물며, 차나 택시를 이용해 유명 맛집을 바쁘게 찾아다닌다.돈을 아끼지 않고, 하루에 여러 곳을 알차게 방문해 최고의 음식과 경험을 즐기는 고급 미식 여행자.";
                    break;
                case "ICRFL":
                    groupMBTIStyle = "럭셔리한 숙소에서 여유롭게, 차를 타고 천천히 맛집을 즐긴다.고급 숙소에서 한껏 휴식하며, 차나 택시를 타고 맛집을 여유롭게 방문한다.비용 걱정 없이 최고의 음식과 분위기를 느끼며, 느긋한 일정으로 미식과 힐링을 동시에 누리는 여행자.";
                    break;
                case "ICRMT":
                    groupMBTIStyle = "럭셔리 숙소에서 시작해, 차로 빠르게 문화 탐방!고급 숙소에서 편안히 머문 뒤, 차나 택시를 타고 박물관과 미술관, 유적지를 빡빡하게 여러 곳 방문한다.비용은 아끼지 않고, 문화와 예술을 최대한 많이 경험하며 알찬 일정을 소화하는 탐방형 여행자.";
                    break;
                case "ICRML":
                    groupMBTIStyle = "럭셔리 숙소에서 여유롭게 쉬고, 차로 천천히 문화 명소를 즐긴다.고급 숙소에서 충분히 휴식하며, 차나 택시를 이용해 박물관과 미술관을 느긋하게 방문한다.돈을 아끼지 않고, 한두 곳을 깊이 있게 즐기며 여유로운 문화 힐링을 추구하는 여행자.";
                    break;
                case "ICEFT":
                    groupMBTIStyle = "가성비 좋은 숙소에서 차로 빠르게 맛집 공략!가성비 좋은 숙소에서 편안히 쉬면서, 차나 택시를 타고 여러 맛집을 바쁘게 방문한다.돈을 아끼면서도 하루에 가능한 많은 맛집을 알차게 즐기는 실속파 미식 여행자.";
                    break;
                case "ICEFL":
                    groupMBTIStyle = "가성비 좋은 숙소에서 여유롭게, 차로 천천히 맛집 즐기기.가성비 좋은 숙소에서 느긋하게 쉬고, 차나 택시를 타고 맛집을 여유롭게 찾아다닌다.비용을 절약하면서도 맛있는 음식을 천천히 즐기며 힐링하는 알뜰 미식 여행자.";
                    break;
                case "ICEMT":
                    groupMBTIStyle = "가성비 좋은 숙소에서 차로 빠르게 문화 탐방!가성비 좋은 숙소에서 편안히 쉬고, 차나 택시를 타고 박물관, 미술관, 유적지를 바쁘게 여러 곳 방문한다.비용을 아끼면서도 하루에 가능한 많은 문화 명소를 알차게 돌아보는 실속형 탐방 여행자.";
                    break;
                case "ICEML":
                    groupMBTIStyle = "가성비 좋은 숙소에서 여유롭게, 차로 천천히 문화 명소 즐기기.가성비 좋은 숙소에서 느긋하게 휴식하며, 차나 택시로 박물관과 미술관을 천천히 방문한다.비용을 절약하면서도 깊이 있게 문화 여행을 즐기며 힐링하는 알뜰 탐방 여행자.";
                    break;
                case "OBRFT":
                    groupMBTIStyle = "동적인 하루! 버스 타고 맛집을 빠르게 공략한다! 야외 활동을 즐기며, 대중교통을 타고 고급 맛집을 바쁘게 돌아다닌다. 비용 걱정 없이 다양한 음식을 빠짐없이 경험하며, 하루 일정이 빡빡한 액티브 미식 여행자.";
                    break;

                case "OBRFL":
                    groupMBTIStyle = "야외 활동 즐기며, 버스로 여유롭게 미식 여행! 바깥에서 활발히 움직이고, 대중교통으로 맛집을 천천히 찾아다닌다. 돈을 아끼지 않고 맛과 분위기를 중시하며, 느긋하게 여행을 즐기는 여유로운 미식가.";
                    break;

                case "OBRMT":
                    groupMBTIStyle = "활동적인 하루! 버스로 문화 탐방을 빠르게 소화한다! 야외 활동을 즐기며, 대중교통을 이용해 고급 박물관과 미술관을 여러 곳 바쁘게 방문한다. 비용은 아끼지 않고, 알찬 일정으로 문화와 예술을 풍성하게 경험하는 탐방형 여행자.";
                    break;

                case "OBRML":
                    groupMBTIStyle = "야외 활동 즐기며, 버스로 여유롭게 문화 명소를 둘러본다. 바깥 활동을 좋아하고, 대중교통으로 박물관과 미술관을 천천히 방문하며 휴식도 챙긴다. 비용은 신경 쓰지 않고, 깊이 있는 문화 체험과 여유로운 힐링 여행을 즐기는 스타일.";
                    break;

                case "OBEFT":
                    groupMBTIStyle = "야외에서 활발하게, 가성비 맛집을 빠르게 공략한다! 바깥 활동을 즐기며, 대중교통을 이용해 가성비 좋은 맛집을 빡빡하게 여러 곳 방문한다. 돈을 절약하면서도 최대한 많은 맛집을 경험하는 알뜰하고 활동적인 미식 여행자.";
                    break;

                case "OBEFL":
                    groupMBTIStyle = "야외 활동 즐기며, 버스로 여유롭게 가성비 맛집 탐방! 활동적인 하루를 보내면서, 대중교통으로 가성비 좋은 맛집을 느긋하게 찾아다닌다. 비용을 아끼면서도 맛있는 음식과 여유로운 여행을 동시에 즐기는 실속파 미식가.";
                    break;

                case "OBEMT":
                    groupMBTIStyle = "활동적인 야외 일정! 버스로 가성비 좋은 문화 탐방을 빠르게! 야외 활동을 즐기면서, 대중교통으로 여러 박물관과 미술관을 빠르게 방문한다. 비용은 아끼면서도 하루에 최대한 많은 문화 명소를 알차게 경험하는 실속형 탐방 여행자.";
                    break;

                case "OBEML":
                    groupMBTIStyle = "야외 활동과 여유로운 일정, 버스로 천천히 문화 탐방! 활동적인 하루를 보내면서, 대중교통을 이용해 가성비 좋은 문화 명소를 여유롭게 방문한다. 비용을 절약하면서도 깊이 있는 문화 체험과 편안한 힐링 여행을 즐기는 알뜰 탐방가.";
                    break;

                case "OCRFT":
                    groupMBTIStyle = "활동적인 야외 일정, 차로 빠르게 고급 맛집을 공략한다! 야외 활동을 즐기면서 차나 택시를 타고 고급 맛집을 빡빡하게 여러 곳 방문한다. 비용을 아끼지 않고, 다양한 미식을 빠짐없이 즐기는 액티브 미식 여행자.";
                    break;

                case "OCRFL":
                    groupMBTIStyle = "야외에서 여유롭게, 차로 고급 맛집을 천천히 즐긴다. 활동적인 하루를 보내며, 차나 택시로 고급 맛집을 느긋하게 찾아다닌다. 돈을 아끼지 않고, 품격 있는 미식과 편안한 여행을 동시에 즐기는 여유파 미식가.";
                    break;

                case "OCRMT":
                    groupMBTIStyle = "활동적인 야외 일정, 차로 고급 문화 탐방을 빠르게! 야외 활동을 즐기며, 차나 택시로 여러 고급 박물관과 미술관을 바쁘게 방문한다. 비용은 아끼지 않고, 알찬 일정으로 문화와 예술을 풍성하게 경험하는 탐방형 여행자.";
                    break;

                case "OCRML":
                    groupMBTIStyle = "야외에서 여유롭게, 차로 고급 문화 명소를 천천히! 활동적인 하루를 보내며, 차나 택시로 고급 박물관과 미술관을 느긋하게 방문한다. 비용은 신경 쓰지 않고, 깊이 있는 문화 체험과 편안한 힐링 여행을 즐기는 스타일.";
                    break;

                case "OCEFT":
                    groupMBTIStyle = "야외 활동 즐기며, 차로 가성비 좋은 맛집을 빠르게 공략한다! 활동적인 하루를 보내면서, 차나 택시로 가성비 좋은 맛집을 빡빡하게 여러 곳 방문한다. 돈을 아끼면서도 최대한 많은 맛집을 경험하는 알뜰하고 활동적인 미식 여행자.";
                    break;

                case "OCEFL":
                    groupMBTIStyle = "야외 활동과 함께, 차로 여유롭게 가성비 맛집을 즐긴다. 활동적인 일정 속에서도 차나 택시로 가성비 좋은 맛집을 천천히 찾아다닌다. 비용을 절약하면서도 맛있는 음식과 편안한 여행을 조화롭게 즐기는 실속파 미식가.";
                    break;

                case "OCEMT":
                    groupMBTIStyle = "야외 활동하며, 차로 가성비 좋은 문화 탐방을 빠르게! 활동적인 하루를 보내면서, 차나 택시로 여러 가성비 좋은 박물관과 미술관을 빠르게 방문한다. 비용을 아끼면서도 하루에 최대한 많은 문화 명소를 알차게 경험하는 실속형 탐방 여행자.";
                    break;

                case "OCEML":
                    groupMBTIStyle = "야외 활동 즐기며, 차로 여유롭게 가성비 문화 체험! 활동적인 일정 중에도 차나 택시로 가성비 좋은 박물관과 미술관을 느긋하게 방문한다. 비용 절약과 편안한 여행을 중시하며, 깊이 있는 문화 탐방을 즐기는 알뜰 탐방가.";
                    break;

                default:
                    groupMBTIStyle = "이 유형은 아직 정의되지 않았습니다.";
                    break;
            }
            // GPT API 프롬프트 다시 하기
            // GPT 프롬프트
            StringBuilder prompt = new StringBuilder();
            prompt.append("너는 유명한 여행 계획 전문가야.");
            prompt.append("나는 ").append(startDate).append("부터 ").append(endDate).append("까지 여행을 가.\n");
            prompt.append("장소는 ").append(location).append("야.");
            prompt.append("숙소는 ").append(placeToStay).append("에 있어. 숙소 위치를 중심으로 반경 20km까지만,동선을 고려해서 짜줘.\n");
            prompt.append("만약 꼭 가고 싶은 장소가 반경 20km를 넘는다면, 그 날의 일정은 꼭 가고 싶은 장소 주변으로 동선을 짜줘.");
            prompt.append("여행 스타일은 ").append(groupMBTI).append("이고 ").append("이 스타일은 ").append(groupMBTIStyle).append("이라고 할 수 있어.");
            prompt.append("여행 스타일을 통해 알 수 있는 선호하는 교통 수단을 중심으로 짜줘");
            prompt.append(who).append("와(과) 함께 가\n");

            if (!travelData.isEmpty()) {
                List<String> places = new ArrayList<>();
                for (Map.Entry<String, Object> entry : travelData.entrySet()) {
                    // "place_"로 시작하는 키만 필터링
                    if (entry.getKey().startsWith("place_") && entry.getValue() instanceof String) {
                        String place = ((String) entry.getValue()).trim();
                        if (!place.isEmpty()) {
                            places.add(place);
                        }
                    }
                }
                prompt.append("꼭 가야 하는 장소는 ").append(String.join(", ", places)).append(" 이야.\n");
            }
            prompt.append("아래와 같은 JSON 배열 형식으로 응답해줘. 전부 한국어로 출력해주고 설명은 절대 하지 말고 JSON 데이터만 반환해. 형식은 다음과 같아:\n\n");

            prompt.append("[\n");
            prompt.append("  {\n");
            prompt.append("    \"date\": \"YYYY.MM.DD\",\n");
            prompt.append("    \"places\": [\n");
            prompt.append("      {\n");
            prompt.append("        \"place\": \"장소 이름\",\n");
            prompt.append("        \"coord\": \"위도,경도\",\n");
            prompt.append("        \"category\": \"관광지, 음식점, 카페 등\",\n");
            prompt.append("        \"transport\": \"도보, 택시, 버스 등\",\n");
            prompt.append("        \"time\": \"이전 장소에서 해당 장소를 가는데 예상 이동 시간\",\n");
            prompt.append("         \"supply\" : \"해당 장소에서 꼭 필요한 준비물\"");
            prompt.append("      }\n");
            prompt.append("    ]\n");
            prompt.append("  }\n");
            prompt.append("]\n");


            prompt.append("이런 형식으로 하루하루를 나눠서 JSON 배열로 구성해서 줘. 예시 말고 진짜 데이터를 넣어서 날짜별로 장소를 넣어줘.\n");
            prompt.append("만약").append(groupMBTI).append("의 맨 마지막이 T인 경우엔 하루에 7곳의 일정을 짜주고, L인 경우엔 하루에 3곳의 일정을 짜줘.");
            prompt.append("식사는 하루 적어도 2곳이 포함되어야 하고, 카페는 여행 스타일 설명에 따라 넣어줘. 그리고 모든 가게는 실제로 존재해야돼.\n");
            prompt.append("그리고 전에 갔던 장소를 또 가는 건 원하지 않아.");
            prompt.append("그리고 해당 장소에서 추천하는 준비물도 알려줘. 필요 없는 경우엔 null으로 알려줘도 돼. 예를 들자면 한라산을 방문하기 위해서는 등산화, 편한 옷이 필요하니 supply에 {등산화, 편한옷}을 넣어주면 되고 카페처럼 준비물이 없는 경우 null 값을 넣어줘.");
            prompt.append("꼭 방문해야 하는 장소는 하루에 모두 넣을 필요는 없어. \n");
            prompt.append("그리고 숙박시설은 내 숙소 외에는 절대 넣어주지마.");
            prompt.append("그리고 마지막은 절대 '이상입니다' 같은 말 없이 JSON만 반환하고 무조건 한글로만 답해줘.");

            loadingLayout.setVisibility(View.VISIBLE);
            ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

            List<GptRequest.Message> messages = new ArrayList<>();
            messages.add(new GptRequest.Message("user", prompt.toString()));

            GptRequest gptRequest = new GptRequest("gpt-4o-mini", messages);

            // 요청 데이터를 JSON 형식으로 로그에 출력
            Log.d("GPT", "Sending Request: " + new Gson().toJson(gptRequest));

            // GPT 요청 보내기
            apiService.getGptAnswer(gptRequest).enqueue(new retrofit2.Callback<GptResponse>() {
                @Override
                public void onResponse(Call<GptResponse> call, retrofit2.Response<GptResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        loadingLayout.setVisibility(View.GONE);
                        String gptReply = response.body().choices.get(0).message.content;
                        Log.d("GPT", "GPT Reply: " + gptReply);

                        // GPT 응답을 GptTripPlanActivity로 넘기기
                        Intent intent = new Intent(AddTripActivity.this, GptTripPlanActivity.class);
                        intent.putExtra("groupMBTIstyle",groupMBTIStyle);
                        intent.putExtra("travelName", travelName);
                        intent.putExtra("travelId",travelId);
                        intent.putExtra("travelData", (Serializable) travelData);
                        intent.putExtra("gpt_schedule", gptReply);
                        intent.putExtra("teamId",teamId);
                        startActivity(intent);
                    } else {
                        Log.e("GPT", "Response error: " + response.code());
                        if (response.errorBody() != null) {
                            try {
                                String errorResponse = response.errorBody().string();
                                Log.e("GPT", "Error body: " + errorResponse);
                            } catch (IOException e) {
                                Log.e("GPT", "Error reading error body", e);
                            }
                        }
                        Toast.makeText(AddTripActivity.this, "GPT 응답 실패", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<GptResponse> call, Throwable t) {
                    loadingLayout.setVisibility(View.GONE);
                    Toast.makeText(AddTripActivity.this, "GPT 호출 에러", Toast.LENGTH_SHORT).show();
                    Log.e("GPT", "에러: " + t.getMessage());
                }
            });
        });
    }

    // 장소가 올바르게 선택
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION && resultCode == RESULT_OK && data != null) {
            String selectedLocation = data.getStringExtra("selected_location");
            String selectedAccomodation = data.getStringExtra("selected_accomodation");
            if (selectedLocation != null) {
                binding.locationInput.setText(selectedLocation);
            }
            if (selectedAccomodation != null) {
                binding.placeToStayInput.setText(selectedAccomodation);
            }
        }
    }
}