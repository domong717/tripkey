package com.example.tripkey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class MBTIDescriptionActivity extends AppCompatActivity {
    private TextView yourMbtiTextView, mbtiDescriptionTextView;
    private FirebaseFirestore db;
    private String userId;

    // 여행 유형별 설명 저장
    private final Map<String, String> travelDescriptions = new HashMap<String, String>() {{
        put("IBLF", "조용한 호텔에서 여유롭게, 하지만 미식은 포기 못해! 고급 호텔에서 아늑한 하루를 보내고, 대중교통을 타고 맛집을 찾아 떠난다. 하루 종일 미식을 즐기며 여유롭게 여행을 즐기는 당신!");
        put("IBLM", "역사와 문화는 필수! 우아한 여행을 즐기는 스타일. 조용한 실내 공간에서 머물며, 고급 숙소에서 힐링! 낮에는 박물관과 미술관을 탐방하며 지적인 여행을 즐긴다.");
        put("IBSF", "편안한 숙소, 가성비 여행, 하지만 맛집 투어는 필수! 비싼 호텔보다는 깔끔한 숙소를 선택! 대중교통을 이용하며 지역 맛집을 찾아다니는 알뜰한 미식가.");
        put("IBSM", "조용한 곳에서 힐링하며, 지적 탐구는 멈출 수 없어! 럭셔리는 필요 없고, 실내에서 차분하게 시간을 보내는 걸 좋아한다. 박물관, 서점, 전시회 탐방이 여행의 핵심.");
        put("ITLF", "편리함과 고급스러움, 그리고 미식은 놓칠 수 없지! 이동은 무조건 택시! 고급 호텔에서 머물고, 특별한 레스토랑에서 인생 맛집을 경험하는 럭셔리 미식 여행자.");
        put("ITLM", "우아한 감성과 편안한 이동, 예술을 사랑하는 당신. 명품 박물관과 미술관을 거닐며, 감성을 채우는 여행자. 럭셔리한 숙소에서 머물고, 택시로 이동하며 편리함도 놓치지 않는다.");
        put("ITSF", "숙소는 가성비, 이동은 편리하게, 그리고 맛집은 필수! 이동은 무조건 편해야 한다! 하지만 숙소는 실용적인 곳으로, 대신 택시 타고 현지 맛집 투어는 포기 못 한다.");
        put("ITSM", "조용히 문화 여행, 하지만 이동은 편해야 해! 실내에서 편히 머물고, 박물관과 역사 명소를 찾아다니는 지적인 여행자. 택시로 편하게 이동하는 걸 선호한다.");
        put("OBLF", "대중교통으로 돌아다니며, 자연과 미식을 함께 즐기자! 럭셔리한 숙소에서 머물지만, 낮에는 열심히 움직이며 맛있는 음식을 찾아 떠나는 여행자. 자연과 미식의 조화를 사랑한다.");
        put("OBLM", "도시와 자연을 넘나들며, 예술과 역사를 탐방하는 여행자. 대중교통을 이용해 구석구석 탐방하면서도, 박물관과 전시회 방문을 빼놓지 않는다. 감성 넘치는 여행을 즐긴다.");
        put("OBSF", "가성비 최고! 현지 음식과 자연을 즐기는 실속 여행자. 대중교통을 활용하며, 맛있는 길거리 음식과 시장 탐방을 좋아하는 스타일. 비싼 숙소보다는 여행을 더 많이 경험하는 것이 중요하다.");
        put("OBSM", "알뜰한 여행자로서 문화와 역사를 즐기는 탐험가. 박물관과 역사적 명소를 방문하며, 가성비 숙소와 대중교통을 이용하는 합리적인 여행자.");
        put("OTLF", "이동은 무조건 편하게, 럭셔리한 여행과 미식 탐방! 럭셔리한 숙소에서 머물며, 맛집과 자연을 찾아 떠나는 여행자. 택시로 자유롭게 이동하며 최고의 미식을 경험하는 스타일.");
        put("OTLM", "우아하게 떠나는 문화 여행, 편리한 이동은 필수! 고급 숙소에서 머물며, 예술과 역사적 명소를 찾아다니는 감성 여행자. 택시를 이용해 효율적인 여행을 한다.");
        put("OTSF", "현지 음식을 찾아 떠나는 자유로운 여행자! 비싼 숙소보다 가성비가 중요한 스타일! 택시를 타고 자유롭게 이동하며, 지역 특산물을 찾아다닌다.");
        put("OTSM", "편리한 이동과 합리적인 여행, 그리고 문화 체험! 가성비 숙소에서 머물며, 택시를 이용해 자연과 박물관을 모두 경험하는 여행자.");
    }};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbti_description);

        // UI 요소 초기화
        yourMbtiTextView = findViewById(R.id.your_mbti);
        mbtiDescriptionTextView = findViewById(R.id.mbti_description);
        ImageButton backButton = findViewById(R.id.button_back);
        Button mbtiTestButton = findViewById(R.id.mbti_test_button);

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        // SharedPreferences에서 로그인한 사용자 ID 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            loadUserMBTI(); // Firestore에서 MBTI 가져오기
        } else {
            Log.w("MBTI", "로그인한 사용자 ID를 찾을 수 없습니다.");
        }

        // 뒤로가기 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> finish());

        // MBTI 테스트 버튼 클릭 이벤트
        mbtiTestButton.setOnClickListener(v -> {
            Intent intent = new Intent(MBTIDescriptionActivity.this, MBTITestActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserMBTI() {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String mbti = document.getString("mbti");
                    if (mbti != null && !mbti.isEmpty()) {
                        yourMbtiTextView.setText(mbti);
                        mbtiDescriptionTextView.setText(travelDescriptions.getOrDefault(mbti, "여행 유형 정보를 찾을 수 없습니다."));
                    }
                } else {
                    Log.d("MBTI", "사용자 문서가 존재하지 않습니다.");
                }
            } else {
                Log.w("MBTI", "데이터 가져오기 실패", task.getException());
            }
        });
    }
}
