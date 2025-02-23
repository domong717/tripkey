package com.example.tripkey;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import androidx.annotation.NonNull;
import android.widget.TextView;




public class TravelAdapter extends RecyclerView.Adapter<TravelAdapter.ViewHolder> {
    private List<TravelItem> travelList;

    public TravelAdapter(List<TravelItem> travelList) {
        this.travelList = travelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelItem travelItem = travelList.get(position);
        holder.travelName.setText(travelItem.getTravelName());
        holder.travelLocation.setText(travelItem.getLocation());
        holder.travelPeriod.setText(travelItem.getStartDate() + " ~ " + travelItem.getEndDate());
        holder.itemView.setBackgroundResource(R.drawable.yellow_box);
    }

    @Override
    public int getItemCount() {
        return travelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView travelName, travelLocation, travelPeriod;

        public ViewHolder(View itemView) {
            super(itemView);
            travelName = itemView.findViewById(R.id.travel_name);
            travelLocation = itemView.findViewById(R.id.travel_location);
            travelPeriod = itemView.findViewById(R.id.travel_period);
        }
    }
}
