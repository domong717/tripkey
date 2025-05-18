package com.example.tripkey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class PlaceAdapter extends ArrayAdapter<GptPlan.Place> {
    private Context context;
    private List<GptPlan.Place> places;

    public PlaceAdapter(Context context, List<GptPlan.Place> places) {
        super(context, 0, places);
        this.context = context;
        this.places = places;
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
        TextView time = convertView.findViewById(R.id.place_time);
//        ((TextView) convertView.findViewById(R.id.place_supply)).setText("Supply: " + place.supply);

        placeName.setText(place.getPlace());
        category.setText("카테고리: " + place.getCategory());
        transport.setText("이동 수단: " + place.getTransport());
        time.setText("예상 소요 시간: " + place.getTime());
//        supply.setText("준비물 : "+place.getSupply());

        return convertView;
    }
}
