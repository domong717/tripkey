package com.example.tripkey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PlaceAdapter extends ArrayAdapter<GptPlan.Place> {
    private Context context;
    private List<GptPlan.Place> places;
    private String userId;
    private String travelId;
    private String date;
    private boolean shouldDeleteFromFirestore;



    public PlaceAdapter(Context context,
                        List<GptPlan.Place> places,
                        boolean shouldDeleteFromFirestore,
                        String userId, String travelId, String date) {
        super(context, 0, places);
        this.context = context;
        this.places = places;
        this.shouldDeleteFromFirestore = shouldDeleteFromFirestore;
        this.userId = userId;
        this.travelId = travelId;
        this.date = date;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        }

        GptPlan.Place place = places.get(position);

        TextView placeName = convertView.findViewById(R.id.place_name);
        TextView category = convertView.findViewById(R.id.place_category);
        TextView transport = convertView.findViewById(R.id.place_transport);
        ImageButton deleteButton = convertView.findViewById(R.id.delete_button);
//        ((TextView) convertView.findViewById(R.id.place_supply)).setText("Supply: " + place.supply);

        placeName.setText(place.getPlace());
        category.setText("카테고리: " + place.getCategory());
        transport.setText("이동 수단: " + place.getTransport());
        //time.setText("예상 소요 시간: " + place.getTime());
//        supply.setText("준비물 : "+place.getSupply());

        deleteButton.setOnClickListener(v -> {
            GptPlan.Place placeToDelete = places.get(position);

            if (shouldDeleteFromFirestore) {
                // Firestore에서 삭제
                String placeId = placeToDelete.getPlaceId();

                if (placeId == null || placeId.isEmpty()) {
                    Toast.makeText(context, "삭제할 수 없습니다 (placeId가 없음)", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .document(userId)
                        .collection("travel")
                        .document(travelId)
                        .collection("gpt_plan")
                        .document(date)
                        .collection("places")
                        .document(placeId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            places.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context, "삭제 완료!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } else {
                // Firestore 저장 전: 그냥 리스트에서만 제거
                places.remove(position);
                notifyDataSetChanged();
            }
        });


        return convertView;
    }
}
