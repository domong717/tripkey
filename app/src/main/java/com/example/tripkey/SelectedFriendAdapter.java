package com.example.tripkey;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class SelectedFriendAdapter extends RecyclerView.Adapter<SelectedFriendAdapter.SelectedFriendViewHolder> {
    private List<FriendItem> selectedFriends;

    public SelectedFriendAdapter(List<FriendItem> selectedFriends) {
        this.selectedFriends = selectedFriends;
    }

    @NonNull
    @Override
    public SelectedFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_friend, parent, false);
        return new SelectedFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedFriendViewHolder holder, int position) {
        FriendItem friend = selectedFriends.get(position);
        holder.nameTextView.setText(friend.getName());

        Glide.with(holder.itemView.getContext())
                .load(friend.getProfileImageUrl())
                .circleCrop()
                .placeholder(R.drawable.profile)  // 🔹 기본 이미지 설정
                .error(R.drawable.profile)       // 🔹 오류 발생 시 기본 이미지
                .into(holder.profileImageView);
    }


    @Override
    public int getItemCount() {
        return selectedFriends.size();
    }

    static class SelectedFriendViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;

        public SelectedFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImage);
            nameTextView = itemView.findViewById(R.id.friendName);
        }
    }
}
