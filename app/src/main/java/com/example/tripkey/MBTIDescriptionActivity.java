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
    private TextView yourMbtiTextView, mbtiDescriptionTextView, textYouAre;
    private FirebaseFirestore db;
    private String userId;

    // 여행 유형별 설명 저장
    private final Map<String, String> travelDescriptions = new HashMap<String, String>() {{
        put("IBRFT", "조용한 호텔에서 여유롭게, 하지만 하루는 알차게! 맛집을 향해 부지런히 달린다!\n\n고급 호텔에서 여유로운 아침을 맞이하지만, 하루 일정은 꽉 채워 보낸다.\n대중교통을 이용해 유명 맛집을 여러 곳 빠르게 방문하며, 비용은 크게 신경 쓰지 않는 알찬 미식 여행자 스타일.");
        put("IBRFL","호텔에서 한껏 여유 부리고, 맛집은 줄 서서라도 꼭 간다!\n\n조용하고 고급스러운 숙소에서 느긋하게 하루를 시작하고, 대중교통으로 천천히 도시를 누빈다.\n맛집 투어는 여유롭게 즐기되, 비용은 아끼지 않는다.\n많은 곳을 방문하기보다 퀄리티와 분위기에 집중하는 ‘플렉스’와 ‘힐링’을 동시에 즐기는 여유형 여행자.");
        put("IBRMT","조용한 호텔에서 하루를 시작해 전시와 유적지를 빠짐없이 돌아본다!\n\n고급 숙소에서 차분히 아침을 맞이하고, 대중교통을 타고 여러 문화 명소를 효율적으로 탐방한다.\n입장료와 기념품 구매에 아낌없으며, 빡빡한 일정 속에서도 문화와 효율을 모두 챙기는 탐방형 여행자.");
        put("IBRML","전시도 유적지도 천천히, 하루 한두 곳이면 충분해!\n\n조용한 고급 숙소에서 충분히 쉬고, 대중교통을 이용해 문화 명소를 여유롭게 방문한다.\n여러 곳을 빠르게 돌기보다는 한 곳에서 깊이 있게 즐기며, 시간과 마음 모두 넉넉하게 ‘문화 힐링’에 집중하는 여유형 여행자.");
        put("IBEFT","가성비 좋은 숙소에서 휴식하되, 맛집은 하루에 알차게 공략한다!\n\n알뜰한 숙소에서 편안히 쉬면서, 대중교통을 이용해 여러 맛집을 빠르게 찾아다닌다.\n비용을 아끼면서도 맛있는 음식을 놓치지 않고, 빡빡한 일정 속에서 최대한 많은 미식을 즐기는 알뜰 여행자.");
        put("IBEFL","가성비 좋은 숙소에서 여유롭게 쉬고, 맛있는 음식도 천천히 즐긴다!\n\n가성비 좋은 호텔에서 느긋한 아침을 보내고, 대중교통으로 맛집을 천천히 찾아간다.\n돈을 아끼면서도 여행의 즐거움을 놓치지 않고, 맛과 휴식을 조화롭게 즐기는 알뜰 미식가 스타일.");
        put("IBEMT","가성비 좋은 숙소에서 휴식, 하지만 문화 탐방은 알차게!\n\n가성비 좋은 숙소에서 편안히 쉬면서, 대중교통을 이용해 여러 박물관과 유적지를 빠르게 돌아다닌다.\n비용은 절약하지만, 하루에 가능한 많은 문화 명소를 방문하며 알찬 일정을 소화하는 실속형 탐방 여행자.");
        put("IBEML","가성비 좋은 숙소에서 여유롭게 쉬고, 문화 명소도 천천히 즐긴다!\n\n가성비 좋은 호텔에서 느긋한 아침을 보내고, 대중교통을 타고 박물관이나 유적지를 천천히 방문한다.\n돈은 아끼면서도 문화 여행의 깊이를 놓치지 않고, 여유로운 일정으로 힐링하는 알뜰 문화 여행자.");
        put("ICRFT","럭셔리한 숙소에서 차를 타고, 하루도 빠짐없이 미식 탐방!\n\n고급 숙소에서 편안히 머물며, 차나 택시를 이용해 유명 맛집을 바쁘게 찾아다닌다.\n돈을 아끼지 않고, 하루에 여러 곳을 알차게 방문해 최고의 음식과 경험을 즐기는 고급 미식 여행자.");
        put("ICRFL","럭셔리한 숙소에서 여유롭게, 차를 타고 천천히 맛집을 즐긴다!\n\n고급 숙소에서 한껏 휴식하며, 차나 택시를 타고 맛집을 여유롭게 방문한다.비용 걱정 없이 최고의 음식과 분위기를 느끼며, 느긋한 일정으로 미식과 힐링을 동시에 누리는 여행자.");
        put("ICRMT","럭셔리 숙소에서 시작해, 차로 빠르게 문화 탐방!\n\n고급 숙소에서 편안히 머문 뒤, 차나 택시를 타고 박물관과 미술관, 유적지를 빡빡하게 여러 곳 방문한다.\n비용은 아끼지 않고, 문화와 예술을 최대한 많이 경험하며 알찬 일정을 소화하는 탐방형 여행자.");
        put("ICRML","럭셔리 숙소에서 여유롭게 쉬고, 차로 천천히 문화 명소를 즐긴다!\n\n고급 숙소에서 충분히 휴식하며, 차나 택시를 이용해 박물관과 미술관을 느긋하게 방문한다.\n돈을 아끼지 않고, 한두 곳을 깊이 있게 즐기며 여유로운 문화 힐링을 추구하는 여행자.");
        put("ICEFT","가성비 좋은 숙소에서 차로 빠르게 맛집 공략!\n\n가성비 좋은 숙소에서 편안히 쉬면서, 차나 택시를 타고 여러 맛집을 바쁘게 방문한다.\n돈을 아끼면서도 하루에 가능한 많은 맛집을 알차게 즐기는 실속파 미식 여행자.");
        put("ICEFL","가성비 좋은 숙소에서 여유롭게, 차로 천천히 맛집을 즐긴다!\n\n가성비 좋은 숙소에서 느긋하게 쉬고, 차나 택시를 타고 맛집을 여유롭게 찾아다닌다.\n비용을 절약하면서도 맛있는 음식을 천천히 즐기며 힐링하는 알뜰 미식 여행자.");
        put("ICEMT","가성비 좋은 숙소에서 차로 빠르게 문화 탐방!\n\n가성비 좋은 숙소에서 편안히 쉬고, 차나 택시를 타고 박물관, 미술관, 유적지를 바쁘게 여러 곳 방문한다.\n비용을 아끼면서도 하루에 가능한 많은 문화 명소를 알차게 돌아보는 실속형 탐방 여행자.");
        put("ICEML","가성비 좋은 숙소에서 여유롭게, 차로 천천히 문화 명소를 즐긴다!\n\n가성비 좋은 숙소에서 느긋하게 휴식하며, 차나 택시로 박물관과 미술관을 천천히 방문한다.\n비용을 절약하면서도 깊이 있게 문화 여행을 즐기며 힐링하는 알뜰 탐방 여행자.");
        put("OBRFT","활동적인 하루! 버스 타고 맛집을 빠르게 공략한다!\n\n야외 활동을 즐기며, 대중교통을 타고 고급 맛집을 바쁘게 돌아다닌다.\n비용 걱정 없이 다양한 음식을 빠짐없이 경험하며, 하루 일정이 빡빡한 액티브 미식 여행자.");
        put("OBRFL","야외 활동 즐기며, 버스로 여유롭게 미식 여행!\n\n바깥에서 활발히 움직이고, 대중교통으로 맛집을 천천히 찾아다닌다.\n돈을 아끼지 않고 맛과 분위기를 중시하며, 느긋하게 여행을 즐기는 여유로운 미식가.");
        put("OBRMT","활동적인 하루! 버스로 문화 탐방을 빠르게 소화한다!\n\n야외 활동을 즐기며, 대중교통을 이용해 고급 박물관과 미술관을 여러 곳 바쁘게 방문한다.\n비용은 아끼지 않고, 알찬 일정으로 문화와 예술을 풍성하게 경험하는 탐방형 여행자.");
        put("OBRML","야외 활동 즐기며, 버스로 여유롭게 문화 명소를 둘러본다!\n\n바깥 활동을 좋아하고, 대중교통으로 박물관과 미술관을 천천히 방문하며 휴식도 챙긴다.\n비용은 신경 쓰지 않고, 깊이 있는 문화 체험과 여유로운 힐링 여행을 즐기는 스타일.");
        put("OBEFT","야외에서 활발하게, 가성비 맛집을 빠르게 공략한다!\n\n바깥 활동을 즐기며, 대중교통을 이용해 가성비 좋은 맛집을 빡빡하게 여러 곳 방문한다.\n돈을 절약하면서도 최대한 많은 맛집을 경험하는 알뜰하고 활동적인 미식 여행자.");
        put("OBEFL","야외 활동 즐기며, 버스로 여유롭게 가성비 맛집 탐방!\n\n활동적인 하루를 보내면서, 대중교통으로 가성비 좋은 맛집을 느긋하게 찾아다닌다.\n비용을 아끼면서도 맛있는 음식과 여유로운 여행을 동시에 즐기는 실속파 미식가.");
        put("OBEMT","활동적인 야외 일정! 버스로 가성비 좋은 문화 탐방을 빠르게!\n\n야외 활동을 즐기면서, 대중교통으로 여러 박물관과 미술관을 빠르게 방문한다.\n비용은 아끼면서도 하루에 최대한 많은 문화 명소를 알차게 경험하는 실속형 탐방 여행자.");
        put("OBEML","야외 활동과 여유로운 일정, 버스로 천천히 문화 탐방!\n\n활동적인 하루를 보내면서, 대중교통을 이용해 가성비 좋은 문화 명소를 여유롭게 방문한다.\n비용을 절약하면서도 깊이 있는 문화 체험과 편안한 힐링 여행을 즐기는 알뜰 탐방가.");
        put("OCRFT","활동적인 야외 일정, 차로 빠르게 고급 맛집을 공략한다!\n\n야외 활동을 즐기면서 차나 택시를 타고 고급 맛집을 빡빡하게 여러 곳 방문한다.\n비용을 아끼지 않고, 다양한 미식을 빠짐없이 즐기는 액티브 미식 여행자.");
        put("OCRFL","야외에서 여유롭게, 차로 고급 맛집을 천천히 즐긴다.\n\n활동적인 하루를 보내며, 차나 택시로 고급 맛집을 느긋하게 찾아다닌다.\n돈을 아끼지 않고, 품격 있는 미식과 편안한 여행을 동시에 즐기는 여유파 미식가.");
        put("OCRMT","활동적인 야외 일정, 차로 고급 문화 탐방을 빠르게!\n\n야외 활동을 즐기며, 차나 택시로 여러 고급 박물관과 미술관을 바쁘게 방문한다.\n비용은 아끼지 않고, 알찬 일정으로 문화와 예술을 풍성하게 경험하는 탐방형 여행자.");
        put("OCRML","야외에서 여유롭게, 차로 고급 문화 명소를 천천히!\n\n활동적인 하루를 보내며, 차나 택시로 고급 박물관과 미술관을 느긋하게 방문한다.\n비용은 신경 쓰지 않고, 깊이 있는 문화 체험과 편안한 힐링 여행을 즐기는 스타일.");
        put("OCEFT","야외 활동 즐기며, 차로 가성비 좋은 맛집을 빠르게 공략한다!\n\n활동적인 하루를 보내면서, 차나 택시로 가성비 좋은 맛집을 빡빡하게 여러 곳 방문한다.\n돈을 아끼면서도 최대한 많은 맛집을 경험하는 알뜰하고 활동적인 미식 여행자.");
        put("OCEFL","야외 활동과 함께, 차로 여유롭게 가성비 맛집을 즐긴다!\n\n활동적인 일정 속에서도 차나 택시로 가성비 좋은 맛집을 천천히 찾아다닌다.\n비용을 절약하면서도 맛있는 음식과 편안한 여행을 조화롭게 즐기는 실속파 미식가.");
        put("OCEMT","야외 활동하며, 차로 가성비 좋은 문화 탐방을 빠르게!\n\n활동적인 하루를 보내면서, 차나 택시로 여러 가성비 좋은 박물관과 미술관을 빠르게 방문한다.\n비용을 아끼면서도 하루에 최대한 많은 문화 명소를 알차게 경험하는 실속형 탐방 여행자.");
        put("OCEML","야외 활동 즐기며, 차로 여유롭게 가성비 문화 체험!\n\n활동적인 일정 중에도 차나 택시로 가성비 좋은 박물관과 미술관을 느긋하게 방문한다.\n비용 절약과 편안한 여행을 중시하며, 깊이 있는 문화 탐방을 즐기는 알뜰 탐방가.");
    }};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbti_description);

        db = FirebaseFirestore.getInstance();

        // SharedPreferences에서 로그인한 사용자 ID 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        Intent intent = getIntent();
        String from = intent.getStringExtra("from");

        // UI 요소 초기화
        yourMbtiTextView = findViewById(R.id.your_mbti);
        textYouAre = findViewById(R.id.text_you_are); // 당신은
        mbtiDescriptionTextView = findViewById(R.id.mbti_description);
        ImageButton backButton = findViewById(R.id.button_back);
        Button mbtiTestButton = findViewById(R.id.mbti_test_button);

        Log.d("MBTI", "from: " + from);
        if ("profileCard".equals(from)) {
            Log.d("MBTI", "from: profileCard");
            mbtiTestButton.setVisibility(View.GONE);
            String friendUserId = intent.getStringExtra("friendName"); // 친구의 userId가 여기 있다고 가정
            if (friendUserId != null) {
                textYouAre.setText(friendUserId + " 은/는 ");
                loadFriendMBTI(friendUserId); // 친구 ID로 Firestore에서 MBTI 읽어오기
            } else {
                textYouAre.setText("ㅇㅇ은");
            }
        } else {
            Log.d("MBTI", "from: else");
            mbtiTestButton.setVisibility(View.VISIBLE);
            textYouAre.setText("당신은 ");
            if (userId != null) {
                loadUserMBTI();
            }
        }

        // 뒤로가기 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> finish());

        // MBTI 테스트 버튼 클릭 이벤트
        mbtiTestButton.setOnClickListener(v -> {
            Intent intentToTest = new Intent(MBTIDescriptionActivity.this, MBTITestActivity.class);
            startActivity(intentToTest);
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
    private void loadFriendMBTI(String friendUserId) {
        Log.d("MBTI", "friend ID: " + friendUserId);
        DocumentReference friendRef = db.collection("users").document(friendUserId);
        friendRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String mbti = document.getString("mbti");
                    if (mbti != null && !mbti.isEmpty()) {
                        yourMbtiTextView.setText(mbti);
                        mbtiDescriptionTextView.setText(travelDescriptions.getOrDefault(mbti, "여행 유형 정보를 찾을 수 없습니다."));
                    }
                } else {
                    Log.d("MBTI", "친구 문서가 존재하지 않습니다.");
                }
            } else {
                Log.w("MBTI", "친구 데이터 가져오기 실패", task.getException());
            }
        });
    }

}
