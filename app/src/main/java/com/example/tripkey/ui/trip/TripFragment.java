package com.example.tripkey.ui.trip;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tripkey.AddTripActivity;
import com.example.tripkey.R;
import com.example.tripkey.TravelAdapter;
import com.example.tripkey.TravelItem;
import com.example.tripkey.databinding.FragmentTripBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.content.Intent;
import java.util.List;
import java.util.Locale;

public class TripFragment extends Fragment {

    private FragmentTripBinding binding;
    private List<TravelItem> travelList;
    private FirebaseFirestore db;
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTripBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // '여행 추가' 버튼 클릭 이벤트
        binding.btnAddTrip.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTripActivity.class);
            startActivity(intent);
        });

        // LinearLayout으로 변경 (RecyclerView 대신 사용)
        binding.travelItemLayout.setOrientation(LinearLayout.VERTICAL);  // 세로로 배치
        travelList = new ArrayList<>();

        // Firestore 및 사용자 ID 가져오기
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        // 여행 일정 데이터 로드
        if (userId != null) {
            loadTravelData();
        }

        return root;
    }

    private void loadTravelData() {
        // 사용자 ID가 제대로 로딩되는지 확인
        if (userId == null) {
            Log.e("HomeFragment", "User ID is null");
        }

        db.collection("users").document(userId)
                .collection("travel")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    travelList.clear();  // 기존 데이터 클리어

                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String travelName = doc.getString("travelName");
                        String location = doc.getString("location");
                        String startDate = doc.getString("startDate");
                        String endDate = doc.getString("endDate");

                        if (endDate != null && endDate.compareTo(today) >= 0) {
                            travelList.add(new TravelItem(travelName, location, startDate, endDate));
                        }
                    }

                    // LinearLayout에 항목 추가
                    updateTravelItems();

                    // 데이터가 없으면 처리
                    if (travelList.isEmpty()) {

                        binding.noPlanImage.setVisibility(View.VISIBLE); // 이미지 보이게 설정
                        binding.emptyMessage.setVisibility(View.VISIBLE); // 텍스트 메시지 보이게 설정
                    } else {

                        binding.noPlanImage.setVisibility(View.GONE); // 이미지 숨기기
                        binding.emptyMessage.setVisibility(View.GONE); // 메시지 숨기기
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error fetching data: " + e.getMessage());
                    Toast.makeText(getContext(), "데이터 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateTravelItems() {
        // LinearLayout에 필터링된 여행 항목들 추가
        for (TravelItem item : travelList) {
            // Yellow Box Layout 생성
            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(100, 70, 16, 16);
            itemLayout.setBackgroundResource(R.drawable.yellow_box); // yellow_box 드로어블 사용

            // 여행 이름
            TextView travelNameText = new TextView(getContext());
            travelNameText.setText(item.getTravelName());
            travelNameText.setTextSize(18);
            travelNameText.setTextColor(getResources().getColor(R.color.black));

            // 여행 장소
            TextView travelLocationText = new TextView(getContext());
            travelLocationText.setText(item.getLocation());
            travelLocationText.setTextSize(16);
            travelLocationText.setTextColor(getResources().getColor(R.color.black));

            // 여행 기간
            TextView travelPeriodText = new TextView(getContext());
            travelPeriodText.setText(item.getStartDate() + " ~ " + item.getEndDate());
            travelPeriodText.setTextSize(14);
            travelPeriodText.setTextColor(getResources().getColor(R.color.gray));

            // 추가된 TextView들을 itemLayout에 추가
            itemLayout.addView(travelNameText);
            itemLayout.addView(travelLocationText);
            itemLayout.addView(travelPeriodText);

            // itemLayout을 travelItemLayout에 추가
            binding.travelItemLayout.addView(itemLayout);

            // 아이템 간의 간격 추가 (위/아래 여백)
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemLayout.getLayoutParams();
            params.setMargins(0, 16, 0, 16); // 위아래 여백을 추가하여 연결되지 않게 만듦
            itemLayout.setLayoutParams(params);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
