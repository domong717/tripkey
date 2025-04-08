package com.example.tripkey;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class MBTITestActivity extends AppCompatActivity {

    private int inside = 0; // inside 선택지 카운트
    private int outside = 0; // outside 선택지 카운트
    private int bus = 0; // bus 선택지 카운트 + walk
    private int taxi = 0; // taxi 선택지 카운트 +car
    private int luxury = 0; // luxury 선택지 카운트
    private int simple = 0; // simple 선택지 카운트
    private int food = 0; // food 선택지 카운트
    private int museum = 0; // museum 선택지 카운트

    private FirebaseFirestore db;

    // 버튼 변수 선언
    private Button q1Option1, q1Option2, q2Option1, q2Option2, q3Option1, q3Option2;
    private Button q4Option1, q4Option2, q5Option1, q5Option2, q6Option1, q6Option2;
    private Button q7Option1, q7Option2, q8Option1, q8Option2, q9Option1, q9Option2;
    private Button q10Option1, q10Option2, q11Option1, q11Option2, q12Option1, q12Option2;
    private Button mbtiButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbti_test);  // 여기서 activity_mbti_test.xml 파일을 참조

        ImageButton backButton = findViewById(R.id.button_back);
        // 뒤로가기 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> finish());

        // 버튼 초기화
        q1Option1 = findViewById(R.id.q1_option1);//inside++
        q1Option2 = findViewById(R.id.q1_option2);//outside++
        q2Option1 = findViewById(R.id.q2_option1);//bus++
        q2Option2 = findViewById(R.id.q2_option2);//taxi++
        q3Option1 = findViewById(R.id.q3_option1);//luxury++
        q3Option2 = findViewById(R.id.q3_option2);//simple++
        q4Option1 = findViewById(R.id.q4_option1);//food++
        q4Option2 = findViewById(R.id.q4_option2);//museum++
        q5Option1 = findViewById(R.id.q5_option1);//inside++
        q5Option2 = findViewById(R.id.q5_option2);//outside++
        q6Option1 = findViewById(R.id.q6_option1);//car++
        q6Option2 = findViewById(R.id.q6_option2);//walk++
        q7Option1 = findViewById(R.id.q7_option1);//luxury++
        q7Option2 = findViewById(R.id.q7_option2);//simple++
        q8Option1 = findViewById(R.id.q8_option1);//food++
        q8Option2 = findViewById(R.id.q8_option2);//museum++
        q9Option1 = findViewById(R.id.q9_option1);//inside++
        q9Option2 = findViewById(R.id.q9_option2);//outside++
        q10Option1 = findViewById(R.id.q10_option1);//taxi++
        q10Option2 = findViewById(R.id.q10_option2);//bus++
        q11Option1 = findViewById(R.id.q11_option1);//luxury++
        q11Option2 = findViewById(R.id.q11_option2);//simple++
        q12Option1 = findViewById(R.id.q12_option1);//food++
        q12Option2 = findViewById(R.id.q12_option2);//museum++
        mbtiButton = findViewById(R.id.mbtiButton);

        q1Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inside++;  // inside 증가
                onOptionSelected(q1Option1, q1Option2);
            }
        });

        q1Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outside++;  // outside 증가
                onOptionSelected(q1Option2, q1Option1);
            }
        });

        q2Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bus++;  // bus 증가
                onOptionSelected(q2Option1, q2Option2);
            }
        });

        q2Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taxi++;  // taxi 증가
                onOptionSelected(q2Option2, q2Option1);
            }
        });

        q3Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                luxury++;  // luxury 증가
                onOptionSelected(q3Option1, q3Option2);
            }
        });

        q3Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simple++;  // simple 증가
                onOptionSelected(q3Option2, q3Option1);
            }
        });

        q4Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                food++;  // food 증가
                onOptionSelected(q4Option1, q4Option2);
            }
        });

        q4Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                museum++;  // museum 증가
                onOptionSelected(q4Option2, q4Option1);
            }
        });

        q5Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inside++;  // inside 증가
                onOptionSelected(q5Option1, q5Option2);
            }
        });

        q5Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outside++;  // outside 증가
                onOptionSelected(q5Option2, q5Option1);
            }
        });

        q6Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taxi++;  // taxi 증가
                onOptionSelected(q6Option1, q6Option2);
            }
        });

        q6Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bus++;  // bus 증가
                onOptionSelected(q6Option2, q6Option1);
            }
        });

        q7Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                luxury++;  // luxury 증가
                onOptionSelected(q7Option1, q7Option2);
            }
        });

        q7Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simple++;  // simple 증가
                onOptionSelected(q7Option2, q7Option1);
            }
        });

        q8Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                food++;  // food 증가
                onOptionSelected(q8Option1, q8Option2);
            }
        });

        q8Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                museum++;  // museum 증가
                onOptionSelected(q8Option2, q8Option1);
            }
        });

        q9Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inside++;  // inside 증가
                onOptionSelected(q9Option1, q9Option2);
            }
        });

        q9Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outside++;  // outside 증가
                onOptionSelected(q9Option2, q9Option1);
            }
        });

        q10Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taxi++;  // bus 증가
                onOptionSelected(q10Option1, q10Option2);
            }
        });

        q10Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bus++;  // walk 증가
                onOptionSelected(q10Option2, q10Option1);
            }
        });

        q11Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                luxury++;  // luxury 증가
                onOptionSelected(q11Option1, q11Option2);
            }
        });

        q11Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simple++;  // simple 증가
                onOptionSelected(q11Option2, q11Option1);
            }
        });

        q12Option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                food++;  // food 증가
                onOptionSelected(q12Option1, q12Option2);
            }
        });

        q12Option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                museum++;  // museum 증가
                onOptionSelected(q12Option2, q12Option1);
            }
        });


        // MBTI 분석하기 버튼 클릭 이벤트
        mbtiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 12문제 중 선택한 문제 수를 계산
                int totalSelections = inside + outside + bus + taxi + luxury + simple + food + museum;

                // 각 문항은 두 항목 중 하나만 선택되므로 총 12개가 선택됐는지 확인
                if (totalSelections < 12) {
                    Toast.makeText(MBTITestActivity.this, "선택하지 않은 문항이 있어요!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String mbtiType = getMBTIResult();
                Toast.makeText(MBTITestActivity.this, "당신의 MBTI는: " + mbtiType, Toast.LENGTH_SHORT).show();

                saveMBTIResult(mbtiType);

                Intent intent = new Intent(MBTITestActivity.this, MBTIDescriptionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });


    }

    // 선택지 버튼 클릭 시 색상 변경 함수
    private void onOptionSelected(Button selectedButton, Button unselectedButton) {
        selectedButton.setBackgroundColor(ContextCompat.getColor(MBTITestActivity.this, R.color.dark_green));  // 선택한 버튼 색상 변경
        unselectedButton.setBackgroundColor(ContextCompat.getColor(MBTITestActivity.this, R.color.mid_green));  // 선택되지 않은 버튼 원래 색상
    }

    private void saveMBTIResult(String mbtiType) {
        db = FirebaseFirestore.getInstance();  // Firestore 초기화

        // 현재 로그인한 사용자의 이름을 SharedPreferences에서 가져오기
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPref.getString("userId", null);

        if (userName == null) {
            Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firestore에 저장할 데이터 생성
        Map<String, Object> data = new HashMap<>();
        data.put("mbti", mbtiType);

        // Firestore의 "users" 컬렉션에서 해당 userName 문서 업데이트 (없으면 생성)
        db.collection("users").document(userName)
                .set(data, SetOptions.merge()) // 기존 데이터 유지하면서 "mbti"만 추가/업데이트
                .addOnSuccessListener(aVoid -> Log.d("MBTI", "MBTI 저장 완료!"))
                .addOnFailureListener(e -> Log.e("MBTI", "MBTI 저장 실패: " + e.getMessage()));
    }



    private String getMBTIResult() {
        StringBuilder mbti = new StringBuilder();

        // inside/outside
        if (inside > outside) {
            mbti.append("I");  // inside가 더 많으면 I
        } else {
            mbti.append("O");  // outside가 더 많으면 O
        }

        // bus/taxi/walk/car
        if (bus > taxi) {
            mbti.append("B");  // bus+walk가 가장 많으면 B
        } else {
            mbti.append("T");  // taxi+bus가 가장 많으면 T
        }

        // luxury/simple
        if (luxury > simple) {
            mbti.append("L");  // luxury가 더 많으면 L
        } else {
            mbti.append("S");  // simple이 더 많으면 S
        }

        // food/museum
        if (food > museum) {
            mbti.append("F");  // food가 더 많으면 F
        } else {
            mbti.append("M");  // museum이 더 많으면 M
        }

        return mbti.toString();
    }

}
