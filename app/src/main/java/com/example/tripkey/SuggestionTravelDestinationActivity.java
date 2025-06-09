package com.example.tripkey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripkey.network.ApiClient;
import com.example.tripkey.network.ApiService;
import com.example.tripkey.network.GptRequest;
import com.example.tripkey.network.GptRequest.Message;
import com.example.tripkey.network.GptResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;

public class SuggestionTravelDestinationActivity extends AppCompatActivity {

    private TextView resultView;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion_travel_destination); // xml 레이아웃


        resultView = findViewById(R.id.resultView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        loadingProgressBar.setVisibility(View.VISIBLE);
        getUserMBTIAndSuggestDestination();

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        String finalDestinationName;

        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(SuggestionTravelDestinationActivity.this, MakeTeamActivity.class);
            startActivity(intent);
        });
    }

    // 기존 메서드들(필드 context 대신 this 사용, resultView는 위에서 초기화된 멤버변수 사용)
    public void getUserMBTIAndSuggestDestination() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String mbti = documentSnapshot.getString("mbti");
                        if (mbti != null && !mbti.isEmpty()) {
                            requestTravelSuggestionFromGPT(mbti);
                        } else {
                            Toast.makeText(this, "MBTI 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "사용자 정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "MBTI 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    private void requestTravelSuggestionFromGPT(String mbti) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        String mbtiDescription = MbtiData.getMbtiDescription(mbti);

        String prompt = "사용자의 여행 MBTI는 " + mbti + " (" + mbtiDescription + ")야.\n\n" +
                "이 성향을 고려해서 아래 형식에 꼭 맞춰서 국내 여행지를 추천해줘.제주도만 있는 게 아니니까 국내에 있는 다른 여행지도 추천해줘. 한번에 한 여행지만 추천해줘.\n\n" +
                "출력 형식:\n" +
                "{사용자의 mbti 설명을 보고 좋아하는 것을 너가 적어}을 좋아하는 당신!\n"+"\n"+"[{여행지}]  여행은 어떠신가요?\n\n"+
                "⛸ 액티비티 : n/5\n설명\n\n\uD83E\uDD58 맛집 : n/5\n설명\n\n\uD83D\uDDFC 관광지 : n/5\n설명\n\n\uD83D\uDE0A 분위기 : n/5\n설명\n\n"+"내용은 꼭 위 형식으로만 작성해줘.";

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", prompt));

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        Call<GptResponse> call = apiService.getGptAnswer(new GptRequest("gpt-4o-mini", messages));

        call.enqueue(new Callback<GptResponse>() {
            @Override
            public void onResponse(Call<GptResponse> call, Response<GptResponse> response) {
                runOnUiThread(() -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                        String content = response.body().choices.get(0).message.content;
                        int start = content.indexOf("[");
                        int end = content.indexOf("]");

                        String destinationName = null;
                        if (start != -1 && end != -1 && end > start) {
                            SpannableString spannable = new SpannableString(content);
                            spannable.setSpan(new AbsoluteSizeSpan(25, true), start, end + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            resultView.setText(spannable);

                            destinationName = content.substring(start + 1, end);
                        } else {
                            resultView.setText(content);
                        }
                        // "여행 추가" 버튼 클릭 시 destination 전달
                        String finalDestinationName = destinationName; // effectively final
                        Button addButton = findViewById(R.id.add_button);
                        addButton.setOnClickListener(v -> {
                            Intent intent = new Intent(SuggestionTravelDestinationActivity.this, MakeTeamActivity.class);
                            if (finalDestinationName != null) {
                                intent.putExtra("suggestedDestination", finalDestinationName);
                            }
                            startActivity(intent);
                        });
                    } else {
                        Toast.makeText(SuggestionTravelDestinationActivity.this,
                                "추천 결과를 받아오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<GptResponse> call, Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(SuggestionTravelDestinationActivity.this,
                            "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
