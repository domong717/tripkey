package com.example.tripkey;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tripkey.databinding.ActivityAddTripBinding;
import com.example.tripkey.network.ApiClient;
import com.example.tripkey.PlaceInfo;
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
    private List<PlaceInfo> cafeFoodList = new ArrayList<>();
    private String seletedFriendsIds;
    private double accommodationLatitude = 37.5665;   // 기본값
    private double accommodationLongitude = 126.9780; // 기본값
    private boolean OneDay = false; // 당일치기

    private ActivityResultLauncher<Intent> mbtiResultLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingLayout = findViewById(R.id.loading_layout);

        teamId = getIntent().getStringExtra("teamId");

        String suggestedDestination = getIntent().getStringExtra("suggestedDestination");
        if (suggestedDestination != null) {
            EditText destinationInput = findViewById(R.id.location_input); // 예시 id
            destinationInput.setText(suggestedDestination);
        }

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
        Button whoChildButton = binding.whoChildButton;
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
            intent.putExtra("result_type", "location");
            startActivityForResult(intent, REQUEST_CODE_LOCATION);
        });

        // 장소
        ImageButton searchAccomodationBtn = binding.accomodationSearchButton;
        searchAccomodationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccomodationSearchActivity.class);
            intent.putExtra("result_type", "accommodation");
            startActivityForResult(intent, REQUEST_CODE_LOCATION);
        });

        startDateInput.setOnClickListener(v -> showDatePickerDialog(true));
        endDateInput.setOnClickListener(v -> showDatePickerDialog(false));

        whoAloneButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoChildButton, whoPetButton);
            whoAloneButton.setBackgroundResource(R.drawable.green_button);
            selectedWho = "혼자";
        });

        whoCoupleButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoChildButton, whoPetButton);
            whoCoupleButton.setBackgroundResource(R.drawable.green_button);
            selectedWho = "연인";
        });

        whoFriendButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoChildButton, whoPetButton);
            whoFriendButton.setBackgroundResource(R.drawable.green_button);
            selectedWho = "친구";
        });
        whoFamilyButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoChildButton, whoPetButton);
            whoFamilyButton.setBackgroundResource(R.drawable.green_button);
            selectedWho = "가족";
        });

        whoChildButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoChildButton, whoPetButton);
            whoChildButton.setBackgroundResource(R.drawable.green_button);
            selectedWho = "아이";
        });

        whoPetButton.setOnClickListener(v -> {
            resetWhoButtons(whoAloneButton, whoCoupleButton, whoFriendButton, whoFamilyButton, whoChildButton, whoPetButton);
            whoPetButton.setBackgroundResource(R.drawable.green_button);
            selectedWho = "반려동물";
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

            // ReMBTITestActivity로 이동
            Intent intent = new Intent(this, ReMBTITestActivity.class);
            mbtiResultLauncher.launch(intent);
        });



    }


    private void resetWhoButtons(Button whoAloneButton, Button whoCoupleButton, Button whoFriendButton, Button whoFamilyButton, Button whoParentButton, Button whoChildButton) {
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

        // 🔍 돋보기(검색) 버튼 추가
        ImageButton searchButton = new ImageButton(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics())
        );
        searchButton.setLayoutParams(params);
        searchButton.setImageResource(R.drawable.search);
        searchButton.setBackgroundColor(Color.TRANSPARENT);
        searchButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        searchButton.setPadding(4, 4, 4, 4);

        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(R.drawable.delete);
        deleteButton.setBackground(null);
        deleteButton.setOnClickListener(v -> mustVisitContainer.removeView(newFieldLayout));

        // 🔍 검색 버튼 클릭 이벤트
        searchButton.setOnClickListener(v -> {
            // 장소 검색 액티비티 호출 (requestCode를 동적으로 관리해야 함)
            Intent intent = new Intent(this, PlaceSearchActivity.class);

            int fieldIndex = mustVisitContainer.indexOfChild(newFieldLayout);
            intent.putExtra("result_type", "must_visit");
            intent.putExtra("field_index", fieldIndex);
            startActivityForResult(intent, REQUEST_CODE_LOCATION);
        });

        newFieldLayout.addView(newPlaceField);
        newFieldLayout.addView(searchButton);  // ← 돋보기 버튼 추가
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
                        OneDay = false; // 시작 날짜 선택 시 초기화
                    } else {
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

                            // 당일치기 여부 확인
                            if (selectedYear == startYear &&
                                    (selectedMonth + 1) == startMonth &&
                                    selectedDayOfMonth == startDay) {
                                OneDay = true;
                            } else {
                                OneDay = false;
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
        char[] mbtiPositions = {'I', 'O', 'B', 'C', 'R', 'E', 'M', 'F', 'T', 'L'};
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

    private boolean hasAtLeastOneMustVisit() {
        for (int i = 0; i < mustVisitContainer.getChildCount(); i++) {
            View child = mustVisitContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) child;
                for (int j = 0; j < layout.getChildCount(); j++) {
                    View subChild = layout.getChildAt(j);
                    if (subChild instanceof EditText) {
                        EditText editText = (EditText) subChild;
                        if (!editText.getText().toString().trim().isEmpty()) {
                            return true;  // 유효한 장소 1개 이상 있음
                        }
                    }
                }
            }
        }
        return false;  // 입력된 장소 없음
    }


    private void saveTripData() {
        String travelName = binding.travelNameInput.getText().toString().trim();
        String location = binding.locationInput.getText().toString().trim();
        String placeToStay = binding.placeToStayInput.getText().toString().trim();
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();
        String groupMBTI = currentMBTI.getText().toString().trim();
        String who = selectedWho;
        int mustCount = mustVisitContainer.getChildCount();

        Log.d("OneDay", "MustCount: " + mustCount);
        Log.d("OneDay", "당일치기" + OneDay);
        Log.d("GPTsend", "Latitude: " + accommodationLatitude + ", Longitude: " + accommodationLongitude);
        // 기본 필수 항목 검사
        if (travelName.isEmpty() || location.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || selectedWho.isEmpty() || selectedStyle.isEmpty()) {
            Toast.makeText(this, "모든 항목을 채워주세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 당일치기일 경우 필수 조건 검사
        if (OneDay) {
            if (!hasAtLeastOneMustVisit()) {
                Toast.makeText(this, "당일치기 여행은 반드시 방문 장소를 1개 이상 추가해야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            // 일반 여행일 경우 숙소도 필수
            if (placeToStay.isEmpty()) {
                Toast.makeText(this, "숙소 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
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
            travelData.put("placeToStay", placeToStay);
            travelData.put("selectedFriendsIds", selectedFriendsIds);


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

            // 여행 MBTI에 맞는 스타일 설명
            final String groupMBTIStyle;
            groupMBTIStyle = getGroupMBTIStyle(groupMBTI);

            // GPT API 프롬프트 다시 하기
            // GPT 프롬프트
            StringBuilder prompt = new StringBuilder();
            prompt.append("여행을 계획해줘.");
            prompt.append("여행기간 :").append(startDate).append("~").append(endDate);
            prompt.append("장소 : ").append(location);
            prompt.append("숙소 이름, 위도, 경도: ").append(placeToStay).append(accommodationLatitude).append(accommodationLongitude).append("숙소 중심으로 반경 20km의 장소들로 여행 생성. 동선을 고려하는 게 가장 중요.\n");
            prompt.append("여행 스타일: ").append(groupMBTIStyle);
            prompt.append("여행 스타일을 무조건 반영하여 동선이 좋은 일정 생성하는 것이 핵심.");
            prompt.append(who).append("와(과) 함께 여행\n").append("추천 장소에 꼭 반영할 것. 아이와일 경우 키즈카페 포함");

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
                prompt.append("꼭 가야 하는 장소: ").append(String.join(", ", places));
                prompt.append("꼭 가고 싶은 장소가 반경 20km를 넘는다면, 그 날의 일정은 꼭 가고 싶은 장소 주변으로 동선 생성");
            }
            prompt.append("아래와 같은 JSON 배열 형식으로 응답 필수. 전부 한국어로 출력 필수. 설명은 절대 없이 JSON 데이터만 반환 필수. 형식 :\n\n");

            prompt.append("[\n");
            prompt.append("  {\n");
            prompt.append("    \"date\": \"YYYY.MM.DD\",\n");
            prompt.append("    \"places\": [\n");
            prompt.append("      {\n");
            prompt.append("        \"place\": \"장소 이름\",\n");
            prompt.append("        \"coord\": \"위도,경도\",\n");
            prompt.append("        \"category\": \"관광지, 음식점, 카페 등\",\n");
            prompt.append("        \"transport\": \"도보, 택시, 버스 등\",\n");
            prompt.append("         \"supply\" : \"해당 장소에서 꼭 필요한 준비물\"");
            prompt.append("      }\n");
            prompt.append("    ]\n");
            prompt.append("  }\n");
            prompt.append("]\n");


            prompt.append("하루하루를 나눠서 JSON 배열로 구성. 진짜 데이터를 넣어서 날짜별로 장소 생성.\n");
            if (cafeFoodList != null && !cafeFoodList.isEmpty()) {
                Log.d("GPTActivity", "cafefoodlist: " + cafeFoodList);

                // 내가 만든 함수로 cafeFoodList -> 문자열 변환
                String placeListStr = convertPlaceListToString(cafeFoodList);

                prompt.append("카페 및 음식점 추천 리스트: \n")
                        .append(placeListStr)
                        .append("\n");
            }
            prompt.append(teamMBTI).append("에 I 있으면 쇼핑몰, 박물관, 실내 위주 추천, O있으면 자연경관, 야외, 바다와 같은 야외 위주 추천");
            prompt.append("식사/카페 제외 관광지와 쇼핑몰, 자연경관 등을 추천하여 일정에 추가 필수\n");
            prompt.append(teamMBTI).append("에 F 있으면 날마다 카페 2곳 추가, M 있으면 카페이름 넘기기 절대 금지.");
            prompt.append("식사는 날마다 2곳 추가.\n");
            prompt.append("중복 장소 추천 금지");
            prompt.append("해당 장소에서 추천하는 준비물도 알려줘. 필요 없는 경우엔 null으로 알려줘도 돼. 예를 들자면 한라산을 방문하기 위해서는 등산화, 편한 옷이 필요하니 supply에 {등산화, 편한옷}을 넣어주면 되고 카페처럼 준비물이 없는 경우 null 값을 넣어줘.");
            prompt.append("꼭 방문해야 하는 장소는 하루에 모두 넣을 필요는 없어. \n");
            prompt.append("숙소 추천 절대 금지");
            prompt.append("절대 '이상입니다' 말 없이 형식 그대로의 JSON만 반환할것. 무조건 한글로만 대답 필수.");

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
                        intent.putExtra("groupMBTIstyle", groupMBTIStyle);
                        intent.putExtra("travelName", travelName);
                        intent.putExtra("travelId", travelId);
                        intent.putExtra("travelData", (Serializable) travelData);
                        intent.putExtra("gpt_schedule", gptReply);
                        intent.putExtra("teamId", teamId);
                        intent.putExtra("startDate", startDate);
                        intent.putExtra("endDate", endDate);
                        intent.putExtra("selectedFriendsIds", selectedFriendsIds);
                        intent.putExtra("accommodation_latitude", accommodationLatitude);
                        intent.putExtra("accommodation_longitude", accommodationLongitude);
                        Log.d("GPTsend", "Latitude: " + accommodationLatitude + ", Longitude: " + accommodationLongitude);
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

    private void searchPlacesFromKakaoByCategory(String categoryCode, double longitude, double latitude, int radius) {
        Log.d("KakaoMap", "SearchPlacesFromKakaoByCategory called");
        KakaoApiService apiService = KakaoApiClient.getRetrofitInstance().create(KakaoApiService.class);
        String authorization = "KakaoAK " + "42d61720c6096d7a9ec5e7c8d0950740";

        Log.d("KakaoMap", "Authorization: " + authorization);

        Call<KakaoSearchResponse> call = apiService.searchPlacesByCategory(authorization, categoryCode, longitude, latitude, radius, 15);

        call.enqueue(new retrofit2.Callback<KakaoSearchResponse>() {
            @Override
            public void onResponse(Call<KakaoSearchResponse> call, retrofit2.Response<KakaoSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<KakaoSearchResponse.Document> places = response.body().documents;
                    for (KakaoSearchResponse.Document place : places) {
                        String placeName = place.place_name;
                        double lat = Double.parseDouble(place.y);
                        double lng = Double.parseDouble(place.x);

                        String category;
                        if ("FD6".equals(place.category_group_code)) {
                            category = "음식점";
                        } else if ("CE7".equals(place.category_group_code)) {
                            category = "카페";
                        } else {
                            category = "기타";
                        }

                        PlaceInfo placeInfo = new PlaceInfo(placeName, lat, lng, category);
                        cafeFoodList.add(placeInfo);

                        Log.d("GPTActivity", "카페/음식점: " + cafeFoodList);
                        Log.d("KakaoMap", "장소 이름: " + placeName + ", 위도: " + lat + ", 경도: " + lng + ", 카테고리: " + category);
                    }

                } else {
                    Log.e("KakaoMap", "응답 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<KakaoSearchResponse> call, Throwable t) {
                Log.e("KakaoMap", "요청 실패: " + t.getMessage());
            }
        });
    }


    private String getGroupMBTIStyle(String groupMBTI) {
        if (groupMBTI.length() != 5) return "이 유형은 아직 정의되지 않았습니다.";

        // 1,2 글자: 실내 vs 야외, 교통수단
        String firstTwo = groupMBTI.substring(0, 2);
        String indoorOutdoor = firstTwo.charAt(0) == 'I' || firstTwo.charAt(0) == 'B' || firstTwo.charAt(0) == 'C' || firstTwo.charAt(0) == 'E' ? "실내를 선호" : "야외활동을 선호";
        String transport = (firstTwo.charAt(1) == 'B' || firstTwo.charAt(1) == 'E') ? "대중교통으로 이동" : "택시나 차를 이용";

        // 3 글자: 재정
        char finance = groupMBTI.charAt(2);
        String financeDesc = (finance == 'R') ? "여유로운 재정" : "조금 적은 재정이라 가성비 선호";

        // 4 글자: 음식 취향
        char food = groupMBTI.charAt(3);
        String foodDesc = (food == 'F') ? "미식가라서 음식 중요" : "음식보다 쇼핑몰, 자연경관, 박물관 관람이 중요";

        // 5 글자: 일정 개수
        char scheduleCount = groupMBTI.charAt(4);
        String scheduleDesc = (scheduleCount == 'T') ? "일정은 8개" : (scheduleCount == 'L') ? "일정은 딱 4개" : "";

        return String.format("%s, %s, %s, %s, %s", indoorOutdoor, transport, financeDesc, foodDesc, scheduleDesc);
    }

    // PlaceInfo 리스트를 받아서 각 장소 정보를 문자열로 합쳐 반환하는 함수 예시
    private String convertPlaceListToString(List<PlaceInfo> placeList) {
        if (placeList == null || placeList.isEmpty()) {
            return "장소 리스트가 없습니다.";
        }

        StringBuilder sb = new StringBuilder();

        for (PlaceInfo place : placeList) {
            sb.append("이름: ").append(place.getName()).append(", ");
            sb.append("카테고리: ").append(place.getCategory()).append("\n");
        }

        return sb.toString();
    }


    // 장소가 올바르게 선택
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOCATION && resultCode == RESULT_OK && data != null) {
            String resultType = data.getStringExtra("result_type");

            switch (resultType) {
                case "location": {
                    String selectedLocation = data.getStringExtra("selected_location");
                    if (selectedLocation != null) {
                        binding.locationInput.setText(selectedLocation);
                    }
                    break;
                }
                case "accommodation": {
                    String selectedAccomodation = data.getStringExtra("selected_accomodation");
                    if (selectedAccomodation != null) {
                        binding.placeToStayInput.setText(selectedAccomodation);
                    }

                    if (data.hasExtra("latitude") && data.hasExtra("longitude")) {
                        accommodationLatitude = data.getDoubleExtra("latitude", 37.5665);
                        accommodationLongitude = data.getDoubleExtra("longitude", 126.9780);
                        Log.d("AddTripActivity", "숙소 위도: " + accommodationLatitude + ", 경도: " + accommodationLongitude);
                    }
                    Log.d("AddTripActivity", "숙소 위도: " + accommodationLatitude + ", 경도: " + accommodationLongitude);
                    break;
                }
                case "must_visit": {
                    int fieldIndex = data.getIntExtra("field_index", -1);
                    String selectedPlace = data.getStringExtra("selected_place_name");

                    if (fieldIndex >= 0 && selectedPlace != null &&
                            fieldIndex < mustVisitContainer.getChildCount()) {

                        View child = mustVisitContainer.getChildAt(fieldIndex);
                        if (child instanceof LinearLayout) {
                            EditText placeInput = (EditText) ((LinearLayout) child).getChildAt(0);
                            placeInput.setText(selectedPlace);
                        }
                    }
                    Log.d("DEBUG", "fieldIndex: " + fieldIndex + ", selectedPlace: " + selectedPlace);
                    Log.d("DEBUg", "data.hasExtra" + data.hasExtra("latitude") + ", " + data.hasExtra("longitude"));
                    if (OneDay) {
                        if (fieldIndex == 1 && data.hasExtra("latitude") && data.hasExtra("longitude")) {
                            accommodationLatitude = data.getDoubleExtra("latitude", 37.5665);
                            accommodationLongitude = data.getDoubleExtra("longitude", 126.9780);
                            Log.d("AddTripActivity", "당일치기: 숙소 대신 첫 장소 위도: " + accommodationLatitude + ", 경도: " + accommodationLongitude);
                        }
                    }
                    break;
                }
            }
        }

        binding.aiScheduleButton.setOnClickListener(v -> {
            int radius = 19999; // 20km 반경

            Log.d("GPTActivity", "기준 위도경도" + accommodationLatitude + "," + accommodationLongitude);
            searchPlacesFromKakaoByCategory("FD6", accommodationLongitude, accommodationLatitude, radius);
            searchPlacesFromKakaoByCategory("CE7", accommodationLongitude, accommodationLatitude, radius);

            Log.d("GPTActivity", "카페/음식점: " + cafeFoodList);
            saveTripData();
        });
    }
}