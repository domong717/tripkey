package com.example.tripkey;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChecklistActivity extends AppCompatActivity {

    private List<ChecklistItem> checklistItems; // 체크리스트 데이터
    private ChecklistAdapter adapter;          // RecyclerView 어댑터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist); // XML 레이아웃 연결

        // 데이터 초기화
        initializeData();

        // RecyclerView 설정
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChecklistAdapter(checklistItems);
        recyclerView.setAdapter(adapter);

        // 초기화 버튼 클릭 이벤트
        ImageButton resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> resetChecklist());

        // 뒤로가기 버튼 클릭 이벤트
        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * 체크리스트 데이터를 초기화합니다.
     */
    private void initializeData() {
        checklistItems = new ArrayList<>();
        checklistItems.add(new ChecklistItem("충전기", false));
        checklistItems.add(new ChecklistItem("샴푸", true));
        checklistItems.add(new ChecklistItem("드라이기", false));
        // 필요한 항목 추가 가능
    }

    /**
     * 체크리스트를 초기화(모든 체크박스 해제)합니다.
     */
    private void resetChecklist() {
        for (ChecklistItem item : checklistItems) {
            item.setChecked(false); // 모든 항목의 체크 상태를 해제
        }
        adapter.notifyDataSetChanged(); // RecyclerView 갱신
    }
}
