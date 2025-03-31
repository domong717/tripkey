package com.example.tripkey;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;

public class FriendTeamAdapter extends RecyclerView.Adapter<FriendTeamAdapter.FriendViewHolder> {
    private static final String TAG = "FriendTeamAdapter";
    private final List<FriendItem> friendList;
    private final OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(FriendItem friend);
    }

    public FriendTeamAdapter(List<FriendItem> friendList, OnFriendClickListener listener) {
        this.friendList = friendList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_team, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendItem friend = friendList.get(position);
        holder.nameTextView.setText(friend.getName());

        if (friend.getProfileImageUrl() != null && !friend.getProfileImageUrl().isEmpty()) {
            Log.d(TAG, "Glide 로드 URL: " + friend.getProfileImageUrl());
            Glide.with(holder.itemView.getContext())
                    .load(friend.getProfileImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(holder.profileImageView);

        } else {
            holder.profileImageView.setImageResource(R.drawable.profile);
        }


        Log.d(TAG, "onBindViewHolder: 친구 이름 = " + friend.getName() + ", 프로필 URL = " + friend.getProfileImageUrl());

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "onFriendClick: " + friend.getName() + " 클릭됨");
            listener.onFriendClick(friend);
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImage);
            nameTextView = itemView.findViewById(R.id.friendName);
        }
    }
}