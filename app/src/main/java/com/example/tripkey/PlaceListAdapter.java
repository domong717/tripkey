package com.example.tripkey;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tripkey.R;
import com.example.tripkey.Place;

import java.util.List;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {

    private List<Place> placeList;

    public PlaceListAdapter(List<Place> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_detail, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        String photoUrl = place.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(holder.ivPlacePhoto.getContext())
                    .load(photoUrl)
                    .into(holder.ivPlacePhoto);
        } else {
            // 사진 URL이 없을 때 기본 이미지 설정(선택)
            holder.ivPlacePhoto.setImageResource(R.drawable.profile);
        }

        holder.tvPlaceName.setText(place.getName());
        holder.tvComment.setText(place.getComment());
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlacePhoto;
        TextView tvPlaceName, tvComment;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlacePhoto = itemView.findViewById(R.id.iv_place_photo);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name);
            tvComment = itemView.findViewById(R.id.tv_comment);
        }
    }
}
