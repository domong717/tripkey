package com.example.tripkey;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    private List<FriendItem> requestList;
    private OnRequestClickListener listener;

    public interface OnRequestClickListener {
        void onAcceptClick(String targetUserId);
        void onRejectClick(String targetUserId);
    }

    public FriendRequestAdapter(List<FriendItem> requestList, OnRequestClickListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendItem request = requestList.get(position);
        holder.nameTextViewRequest.setText(request.getName());

        // 프로필 이미지 로드
        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(request.getProfileImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(holder.profileImageViewRequest);
        } else {
            holder.profileImageViewRequest.setImageResource(R.drawable.profile);
        }

        holder.acceptButton.setOnClickListener(v -> listener.onAcceptClick(request.getId()));
        holder.rejectButton.setOnClickListener(v -> listener.onRejectClick(request.getId()));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextViewRequest;
        public ImageView profileImageViewRequest;
        public Button acceptButton;
        public Button rejectButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextViewRequest = itemView.findViewById(R.id.nameTextViewRequest);
            profileImageViewRequest = itemView.findViewById(R.id.profileImageViewRequest);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }
}
