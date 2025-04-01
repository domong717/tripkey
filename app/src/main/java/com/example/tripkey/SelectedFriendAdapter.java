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
import java.util.List;

public class SelectedFriendAdapter extends RecyclerView.Adapter<SelectedFriendAdapter.SelectedFriendViewHolder> {
    private List<FriendItem> selectedFriends;
    private String currentUserId; // 🔹 현재 사용자 ID 추가

    public SelectedFriendAdapter(List<FriendItem> selectedFriends, String currentUserId) {
        this.selectedFriends = selectedFriends;
        this.currentUserId = currentUserId; // 🔹 생성자로 받아서 저장
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
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(holder.profileImageView);

        // 🔹 내 계정이면 테두리 추가
        if (friend.getId().equals(currentUserId)) {
            holder.profileBorder.setVisibility(View.VISIBLE);
            holder.profileBorder.post(() -> holder.profileBorder.setVisibility(View.VISIBLE)); // 💡 추가
            Log.d("SelectedFriendAdapter", "MY account YES");
        } else {
            holder.profileBorder.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return selectedFriends.size();
    }

    static class SelectedFriendViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;
        ImageView profileBorder;

        public SelectedFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImage);
            profileBorder = itemView.findViewById(R.id.profileBorder);
            nameTextView = itemView.findViewById(R.id.friendName);
        }
    }
}
