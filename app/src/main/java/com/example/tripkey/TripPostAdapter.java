package com.example.tripkey;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripkey.R;
import com.example.tripkey.Place;
import com.example.tripkey.TripPost;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TripPostAdapter extends RecyclerView.Adapter<TripPostAdapter.TripPostViewHolder> {

    private Context context;
    private List<TripPost> tripPostList;
    private OnHeartClickListener heartClickListener;
    private EditText startDateInput;

    public TripPostAdapter(Context context, List<TripPost> tripPostList, OnHeartClickListener heartClickListener) {
        this.context = context;
        this.tripPostList = tripPostList;
        this.heartClickListener = heartClickListener;
    }

    @NonNull
    @Override
    public TripPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip_post, parent, false);
        return new TripPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripPostViewHolder holder, int position) {
        TripPost post = tripPostList.get(position);

        holder.tvTitle.setText(post.getTitle());
        holder.tvOwner.setText("@" + post.getOwnerId());
        holder.tvDate.setText(post.getDate());
        holder.tvLocation.setText("여행지 | " + post.getLocation());
        holder.tvPeople.setText("여행 인원 | " + post.getPeopleCount() + "인");
        holder.tvMBTI.setText("여행 MBTI | " + post.getTeamMBTI());
        holder.tvCost.setText("1인당 총 경비 | " + post.getCostPerPerson());

        holder.btnHeart.setOnClickListener(v -> {
            Log.d("TripPostAdapter", "하트 버튼 클릭됨");
            if (heartClickListener != null) {
                heartClickListener.onHeartClick(post);
            }
        });
        holder.btnSaveMyTrip.setOnClickListener(v -> {
            showCopyDialog(context, post);
        });

        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlanDetailActivity.class);
            intent.putExtra("from", "home");  // 출처 정보 전달
            intent.putExtra("travelId", post.getTravelId());
            intent.putExtra("travelName", post.getTitle());
            intent.putExtra("startDate", post.getDate());
            intent.putExtra("ownerId", post.getOwnerId());
            context.startActivity(intent);
        });


        // 내부 RecyclerView 세팅
        PlaceListAdapter placeAdapter = new PlaceListAdapter(post.getPlaceList());
        holder.rvPlaceList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.rvPlaceList.setAdapter(placeAdapter);
    }

    @Override
    public int getItemCount() {
        return tripPostList.size();
    }

    // 클래스 내부에 추가
    private void showDatePickerDialog(boolean isStartDate, EditText target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    String formattedMonth = String.format("%02d", selectedMonth + 1);
                    String formattedDay = String.format("%02d", selectedDayOfMonth);

                    String selectedDate = selectedYear + "-" + formattedMonth + "-" + formattedDay;
                    target.setText(selectedDate);
                },
                year, month, dayOfMonth
        );

        datePickerDialog.show();
    }

    // showCopyDialog 수정
    private void showCopyDialog(Context context, TripPost tripPost) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_copy_trip, null);

        EditText etTravelName = dialogView.findViewById(R.id.et_travel_name);
        EditText etStartDate = dialogView.findViewById(R.id.et_start_date);

        etStartDate.setOnClickListener(v -> showDatePickerDialog(true, etStartDate));  // 변경된 호출

        new AlertDialog.Builder(context)
                .setTitle("내 일정에 담기")
                .setView(dialogView)
                .setPositiveButton("확인", (dialog, which) -> {
                    String name = etTravelName.getText().toString();
                    String start = etStartDate.getText().toString();

                    if (!name.isEmpty() && !start.isEmpty()) {
                        copyTripToMyPlan(context, tripPost, name, start);
                        Log.d("TripPostAdapter", "내 일정에 담기 완료");
                    } else {
                        Toast.makeText(context, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }


    private void copyTripToMyPlan(Context context, TripPost tripPost, String newTravelName, String newStartDate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        String ownerId = tripPost.getOwnerId();
        String originalTravelId = tripPost.getTravelId();
        String originalPath = "users/" + ownerId + "/travel/" + originalTravelId;
        String newTravelId = db.collection("users").document().getId();

        DocumentReference originalDocRef = db.document(originalPath);
        DocumentReference newDocRef = db.collection("users").document(userId)
                .collection("travel").document(newTravelId);

        originalDocRef.get().addOnSuccessListener(originalSnapshot -> {
            if (originalSnapshot.exists()) {
                Map<String, Object> originalData = originalSnapshot.getData();
                if (originalData != null) {
                    db.collection(originalPath + "/gpt_plan")
                            .get().addOnSuccessListener(planSnapshots -> {
                                List<DocumentSnapshot> dateDocs = planSnapshots.getDocuments();

                                // 새로운 startDate 파싱
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                Calendar calendar = Calendar.getInstance();
                                try {
                                    calendar.setTime(sdf.parse(newStartDate));
                                } catch (ParseException e) {
                                    Log.e("TripPostAdapter", "날짜 파싱 오류: " + e.getMessage());
                                    return;
                                }

                                // 자동 계산된 endDate 생성
                                Calendar endCalendar = (Calendar) calendar.clone();
                                endCalendar.add(Calendar.DATE, dateDocs.size() - 1);
                                String newEndDate = sdf.format(endCalendar.getTime());

                                originalData.put("travelId", newTravelId);
                                originalData.put("travelName", newTravelName);
                                originalData.put("startDate", newStartDate);
                                originalData.put("endDate", newEndDate);
                                originalData.put("creatorId", userId);
                                if (originalData.containsKey("total")) {
                                    originalData.put("total", 0);
                                }

                                String teamId = db.collection("users").document(userId).collection("teams").document().getId();
                                originalData.put("teamId", teamId);

                                Map<String, Object> teamData = new HashMap<>();
                                teamData.put("teamId", teamId);
                                teamData.put("members", Collections.singletonList(userId)); // 현재 userId만 추가

                                db.collection("users").document(userId)
                                        .collection("teams").document(teamId)
                                        .set(teamData)
                                        .addOnSuccessListener(aVoid2 -> Log.d("TripPostAdapter", "팀 생성 성공"))
                                        .addOnFailureListener(e -> Log.e("TripPostAdapter", "팀 생성 실패: " + e.getMessage()));


                                newDocRef.set(originalData).addOnSuccessListener(aVoid -> {
                                    for (int i = 0; i < dateDocs.size(); i++) {
                                        DocumentSnapshot planDoc = dateDocs.get(i);
                                        String newDate = sdf.format(calendar.getTime());

                                        DocumentReference newDateDocRef = db
                                                .collection("users").document(userId)
                                                .collection("travel").document(newTravelId)
                                                .collection("gpt_plan").document(newDate);

                                        Map<String, Object> dummy = new HashMap<>();
                                        dummy.put("exists", true); // 혹은 "index": i 등
                                        newDateDocRef.set(dummy);

                                        CollectionReference originalPlacesRef = db.collection(originalPath + "/gpt_plan/" + planDoc.getId() + "/places");
                                        CollectionReference newPlacesRef = db.collection("users/" + userId + "/travel/" + newTravelId + "/gpt_plan/" + newDate + "/places");

                                        originalPlacesRef.get().addOnSuccessListener(placeSnapshots -> {
                                            for (DocumentSnapshot placeDoc : placeSnapshots) {
                                                newPlacesRef.document(placeDoc.getId()).set(placeDoc.getData())
                                                        .addOnSuccessListener(a -> Log.d("TripPostAdapter", "장소 복사 성공: " + placeDoc.getId()))
                                                        .addOnFailureListener(e -> Log.e("TripPostAdapter", "장소 복사 실패: " + e.getMessage()));
                                            }
                                        }).addOnFailureListener(e -> Log.e("TripPostAdapter", "originalPlacesRef.get() 실패: " + e.getMessage()));

                                        calendar.add(Calendar.DATE, 1); // 다음 날짜로 이동
                                    }
                                }).addOnFailureListener(e -> Log.e("TripPostAdapter", "기본 정보 복사 실패: " + e.getMessage()));
                            }).addOnFailureListener(e -> Log.e("TripPostAdapter", "gpt_plan.get() 실패: " + e.getMessage()));
                }
            }
        });
    }

    public void updateList(List<TripPost> newList) {
        this.tripPostList = newList;
        notifyDataSetChanged();
    }


    static class TripPostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvLocation, tvPeople, tvCost, tvOwner, tvMBTI;
        RecyclerView rvPlaceList;
        ImageButton btnDetail, btnHeart, btnSaveMyTrip;

        public TripPostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvPeople = itemView.findViewById(R.id.tv_member_count);
            tvCost = itemView.findViewById(R.id.tv_total_cost_per_person);
            tvOwner = itemView.findViewById(R.id.tv_writerid);
            rvPlaceList = itemView.findViewById(R.id.rv_place_list);
            btnDetail = itemView.findViewById(R.id.btn_detail); //일정 보기
            tvMBTI = itemView.findViewById(R.id.tv_mbti);
            btnHeart = itemView.findViewById(R.id.just_keep); // 하트 찜 버튼
            btnSaveMyTrip = itemView.findViewById(R.id.goto_my_trip); // 여행 담기 버튼

        }
    }

    public interface OnHeartClickListener {
        void onHeartClick(TripPost tripPost);
    }

}
