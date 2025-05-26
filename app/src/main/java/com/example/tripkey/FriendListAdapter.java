package com.example.tripkey;

import android.content.Intent;
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

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private List<FriendItem> friendList;

    public FriendListAdapter(List<FriendItem> friendList) {
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendItem friend = friendList.get(position);
        holder.nameTextView.setText(friend.getName());

        // 프로필 이미지 로드
        if (friend.getProfileImageUrl() != null && !friend.getProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(friend.getProfileImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.profile) // 기본 이미지
                    .error(R.drawable.profile) // 오류 발생 시 기본 이미지
                    .circleCrop()
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.profile);
        }

        holder.mbtiButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MBTIDescriptionActivity.class);
            intent.putExtra("from", "profileCard");
            intent.putExtra("friendName", friend.getName());
            v.getContext().startActivity(intent);
        });

        holder.recordButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), FriendRecordAllActivity.class);
            intent.putExtra("userId", friend.getName());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageView profileImageView;
        public Button mbtiButton;
        public Button recordButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            mbtiButton = itemView.findViewById(R.id.mbtiButton);// mbti 확인하기 버튼
            recordButton = itemView.findViewById(R.id.recordButton);// 기록 확인하기 버튼
        }
    }
}
