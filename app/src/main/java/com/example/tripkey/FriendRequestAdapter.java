package com.example.tripkey;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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

        holder.acceptButton.setOnClickListener(v -> listener.onAcceptClick(request.getId()));
        holder.rejectButton.setOnClickListener(v -> listener.onRejectClick(request.getId()));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextViewRequest;
        public Button acceptButton;
        public Button rejectButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextViewRequest = itemView.findViewById(R.id.nameTextViewRequest);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }
}

