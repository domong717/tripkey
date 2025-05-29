package com.example.tripkey;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ViewRecordActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_RECORD = 200;
    private FirebaseFirestore db;
    private TextView textViewTravelPlace, travel_place, travel_date, travel_people, travel_one_person_pay, noRecordsTextView;
    private RecyclerView recordRecyclerView;
    private ViewRecordAdapter recordAdapter;
    private ArrayList<RecordItem> recordList = new ArrayList<>();

    private String travelId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_record);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        travelId = intent.getStringExtra("travelId");
        userId = intent.getStringExtra("userId");

        if (travelId == null || userId == null) {
            Toast.makeText(this, "여행 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish(); // travelId나 userId가 없으면 종료
            return;
        }


        // View 연결
        textViewTravelPlace = findViewById(R.id.textViewTravelPlace);
        travel_place = findViewById(R.id.travel_place);
        travel_date = findViewById(R.id.travel_date);
        travel_people = findViewById(R.id.travel_people);
        travel_one_person_pay = findViewById(R.id.travel_one_person_pay);
        noRecordsTextView = findViewById(R.id.noRecordsTextView);
        recordRecyclerView = findViewById(R.id.recordRecyclerView);
        Button addRecordButton = findViewById(R.id.add_record_button);
        ImageButton backButton = findViewById(R.id.button_back);
        ImageButton calculateButton = findViewById(R.id.btn_calculate);
        ImageButton detailButton = findViewById(R.id.detailButton);

        calculateButton.setOnClickListener(v -> {
            Intent c_intent = new Intent(ViewRecordActivity.this, RegisterMoneyActivity.class);
            c_intent.putExtra("travelId", travelId);
            startActivity(c_intent);
        });

        // 리사이클러뷰 설정
        recordAdapter = new ViewRecordAdapter(
                this,
                recordList,
                (recordId, place, record, photoUris) -> {
                    Intent editIntent = new Intent(ViewRecordActivity.this, EditRecordActivity.class);
                    editIntent.putExtra("recordId", recordId);
                    editIntent.putExtra("place", place);
                    editIntent.putExtra("record", record);
                    editIntent.putStringArrayListExtra("photoUris", photoUris);
                    editIntent.putExtra("travelId", travelId); // travelId도 필요!
                    startActivityForResult(editIntent, REQUEST_EDIT_RECORD);
                }
        );

        recordRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recordRecyclerView.setAdapter(recordAdapter);

        // 여행 정보 불러오기
        loadTravelInfo();

        // 여행 기록 불러오기
        loadRecordList();

        // 버튼 리스너
        backButton.setOnClickListener(v -> finish());

        addRecordButton.setOnClickListener(v -> {
            Intent a_intent = new Intent(ViewRecordActivity.this, PlusRecordActivity.class);
            a_intent.putExtra("travelId", travelId);
            startActivity(a_intent);
        });
    }

    private void loadTravelInfo() {
        db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String travelName = documentSnapshot.getString("travelName");
                        String location = documentSnapshot.getString("location");
                        String startDate = documentSnapshot.getString("startDate");
                        String endDate = documentSnapshot.getString("endDate");
                        String who = documentSnapshot.getString("who");
                        Long total = documentSnapshot.getLong("total");

                        textViewTravelPlace.setText(travelName);
                        travel_place.setText("장소: " + location);
                        travel_date.setText("날짜: " + startDate + " ~ " + endDate);
                        travel_people.setText("여행 인원: " + who);
                        travel_one_person_pay.setText("1인당 비용: "+ total);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "여행 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRecordList() {
        db.collection("users")
                .document(userId)
                .collection("travel")
                .document(travelId)
                .collection("records")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recordList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String recordId = doc.getId();
                        String place = doc.getString("place");
                        String record = doc.getString("record");
                        ArrayList<String> photoUris = (ArrayList<String>) doc.get("photos");

                        RecordItem item = new RecordItem(recordId, place, record, photoUris);
                        recordList.add(item);
                    }

                    recordAdapter.notifyDataSetChanged();

                    if (recordList.isEmpty()) {
                        noRecordsTextView.setVisibility(View.VISIBLE);
                    } else {
                        noRecordsTextView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "기록을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}
