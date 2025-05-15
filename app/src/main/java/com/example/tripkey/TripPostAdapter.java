package com.example.tripkey;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripkey.R;
import com.example.tripkey.Place;
import com.example.tripkey.TripPost;

import java.util.List;

public class TripPostAdapter extends RecyclerView.Adapter<TripPostAdapter.TripPostViewHolder> {

    private Context context;
    private List<TripPost> tripPostList;
    private OnHeartClickListener heartClickListener;

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
        holder.tvDate.setText(post.getDate());
        holder.tvLocation.setText("여행지 | " + post.getLocation());
        holder.tvPeople.setText("여행 인원 | " + post.getPeopleCount() + "인");
        holder.tvCost.setText("1인당 총 경비 | " + post.getCostPerPerson() +"원");

        holder.btnHeart.setOnClickListener(v -> {
            Log.d("TripPostAdapter", "하트 버튼 클릭됨");
            if (heartClickListener != null) {
                heartClickListener.onHeartClick(post);
            }
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

    public void updateList(List<TripPost> newList) {
        this.tripPostList = newList;
        notifyDataSetChanged();
    }


    static class TripPostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvLocation, tvPeople, tvCost;
        RecyclerView rvPlaceList;
        ImageButton btnDetail, btnHeart;

        public TripPostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvPeople = itemView.findViewById(R.id.tv_member_count);
            tvCost = itemView.findViewById(R.id.tv_total_cost_per_person);
            rvPlaceList = itemView.findViewById(R.id.rv_place_list);
            btnDetail = itemView.findViewById(R.id.btn_detail);
            btnHeart = itemView.findViewById(R.id.just_keep); // 하트 버튼

        }
    }

    public interface OnHeartClickListener {
        void onHeartClick(TripPost tripPost);
    }

}
