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
import com.example.tripkey.network.ApiClient;
import com.example.tripkey.network.ApiService;
import com.example.tripkey.network.GptRequest;
import com.example.tripkey.network.GptResponse;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.IOException;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        styleAnalyzeButton.setOnClickListener(v -> {
            resetStyleButtons(styleKeepButton, styleAnalyzeButton);
            styleAnalyzeButton.setBackgroundResource(R.drawable.green_button);
            selectedStyle = "다시 분석";

            // 🔽 MBTITestActivity로 이동
            Intent intent = new Intent(this, MBTITestActivity.class);
            startActivity(intent);
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

                        Toast.makeText(this, "여행 일정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

            // 여행 MBTI에 맞는 스타일 설명
            String groupMBTIStyle = "";

            switch (groupMBTI) {
                case "IBLF":
                    groupMBTIStyle = "IBLF: 고급 호텔에서 아늑한 하루를 보내고, 대중교통을 타고 맛집을 찾아 떠나는 여유로운 여행 스타일";
                    break;
                case "IBLM":
                    groupMBTIStyle = "IBLM: 고급 숙소에서 힐링하고, 박물관과 미술관을 탐방하는 지적인 여행 스타일";
                    break;
                case "IBSF":
                    groupMBTIStyle = "IBSF: 깔끔한 숙소에서 대중교통을 이용해 지역 맛집을 탐방하는 알뜰한 미식가 스타일";
                    break;
                case "IBSM":
                    groupMBTIStyle = "IBSM: 실내에서 차분하게 시간을 보내고, 박물관과 전시회 탐방을 좋아하는 스타일";
                    break;
                case "ITLF":
                    groupMBTIStyle = "ITLF: 택시를 이용해 고급 호텔에서 특별한 레스토랑을 경험하는 럭셔리 미식 여행자 스타일";
                    break;
                case "ITLM":
                    groupMBTIStyle = "ITLM: 감성적인 여행으로, 고급 숙소에서 택시를 이용해 박물관과 미술관을 탐방하는 스타일";
                    break;
                case "ITSF":
                    groupMBTIStyle = "ITSF: 이동은 택시로 편리하게, 실용적인 숙소에서 현지 맛집을 탐방하는 스타일";
                    break;
                case "ITSM":
                    groupMBTIStyle = "ITSM: 실내에서 편히 머물며, 택시로 편하게 박물관과 역사 명소를 찾아다니는 지적인 스타일";
                    break;
                case "OBLF":
                    groupMBTIStyle = "OBLF: 럭셔리 숙소에서 미식을 즐기며, 대중교통으로 다양한 장소를 탐방하는 자연과 미식의 조화를 사랑하는 스타일";
                    break;
                case "OBLM":
                    groupMBTIStyle = "OBLM: 대중교통을 이용해 감성 넘치는 여행을 즐기고, 박물관과 전시회도 빼놓지 않는 스타일";
                    break;
                case "OBSF":
                    groupMBTIStyle = "OBSF: 대중교통을 이용해 시장과 길거리 음식을 탐방하며 가성비를 중시하는 스타일";
                    break;
                case "OBSM":
                    groupMBTIStyle = "OBSM: 박물관과 역사적 명소를 방문하고, 대중교통을 이용한 가성비 좋은 여행 스타일";
                    break;
                case "OTLF":
                    groupMBTIStyle = "OTLF: 럭셔리 숙소에서 미식을 즐기며, 택시로 편하게 이동하는 여행 스타일";
                    break;
                case "OTLM":
                    groupMBTIStyle = "OTLM: 고급 숙소에서 예술과 역사적 명소를 찾아 다니는 감성적인 여행 스타일";
                    break;
                case "OTSF":
                    groupMBTIStyle = "OTSF: 비싼 숙소보다는 가성비가 중요하며, 택시로 이동해 지역 특산물을 찾아 떠나는 여행 스타일";
                    break;
                case "OTSM":
                    groupMBTIStyle = "OTSM: 가성비 숙소에서 택시로 박물관과 자연을 모두 경험하는 여행 스타일";
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
            prompt.append("숙소는 ").append(placeToStay).append("에 있어. 숙소 위치를 중심으로 동선을 고려해서 짜줘.\n");
            prompt.append("여행 스타일은 ").append(groupMBTI).append("이고 ").append("이 스타일은 ").append(groupMBTIStyle).append("이라고 할 수 있어.");
            prompt.append("여행 스타일을 통해 알 수 있는 선호하는 교통 수단을 중심으로 짜줘도 되지만 너무 해당 교통수단만 이용하지 않아도 돼.");
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
            prompt.append("아래와 같은 JSON 배열 형식으로 응답해줘. 설명은 절대 하지 말고 JSON 데이터만 반환해. 형식은 다음과 같아:\n\n");

            prompt.append("[\n");
            prompt.append("  {\n");
            prompt.append("    \"date\": \"YYYY.MM.DD\",\n");
            prompt.append("    \"places\": [\n");
            prompt.append("      {\n");
            prompt.append("        \"place\": \"장소 이름\",\n");
            prompt.append("        \"coord\": \"위도,경도\",\n");
            prompt.append("        \"category\": \"관광지, 음식점, 카페 등\",\n");
            prompt.append("        \"transport\": \"도보, 택시, 버스 등\",\n");
            prompt.append("        \"time\": \"이전 장소에서 해당 장소를 가는데 예상 이동 시간\"\n");
            prompt.append("      }\n");
            prompt.append("    ]\n");
            prompt.append("  }\n");
            prompt.append("]\n");


            prompt.append("이런 형식으로 하루하루를 나눠서 JSON 배열로 구성해서 줘. 예시 말고 진짜 데이터를 넣어서, 날짜별로 하루에 5~7개 장소를 넣어줘.\n");
            prompt.append("식사는 하루 3번 포함되어야 하고, 카페는 하루에 한 번 정도가 좋은 것 같아.\n");
            prompt.append("그리고 전에 갔던 장소를 또 가는 건 원하지 않아.");
            prompt.append("꼭 방문해야 하는 장소는 하루에 모두 넣을 필요는 없어. 이동 시간은 반드시 30분 이내가 되도록 동선을 고려해서 짜줘.\n");
            prompt.append("그리고 마지막은 절대 '이상입니다' 같은 말 없이 JSON만 반환하고 무조건 한글로만 답해줘.");

            ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

            List<GptRequest.Message> messages = new ArrayList<>();
            messages.add(new GptRequest.Message("user", prompt.toString()));

            GptRequest gptRequest = new GptRequest("gpt-3.5-turbo", messages);

            // 요청 데이터를 JSON 형식으로 로그에 출력
            Log.d("GPT", "Sending Request: " + new Gson().toJson(gptRequest));

            // GPT 요청 보내기
            apiService.getGptAnswer(gptRequest).enqueue(new retrofit2.Callback<GptResponse>() {
                @Override
                public void onResponse(Call<GptResponse> call, retrofit2.Response<GptResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String gptReply = response.body().choices.get(0).message.content;
                        Log.d("GPT", "GPT Reply: " + gptReply);

                        // GPT 응답을 GptTripPlanActivity로 넘기기
                        Intent intent = new Intent(AddTripActivity.this, GptTripPlanActivity.class);
                        intent.putExtra("travelName", travelName);
                        intent.putExtra("startDate", startDate);
                        intent.putExtra("endDate", endDate);
                        intent.putExtra("travelId",travelId);
                        intent.putExtra("gpt_schedule", gptReply);
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
            if (selectedLocation != null) {
                binding.locationInput.setText(selectedLocation);
            }
        }
    }
}