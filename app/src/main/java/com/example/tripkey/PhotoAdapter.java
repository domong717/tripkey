package com.example.tripkey;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private ArrayList<Uri> photoList;
    private OnPhotoDeleteListener onPhotoDeleteListener;
    private boolean isDeleteButtonVisible;


    public PhotoAdapter(ArrayList<Uri> photoList, boolean isDeleteButtonVisible, OnPhotoDeleteListener onPhotoDeleteListener) {
        this.photoList = photoList;
        this.isDeleteButtonVisible = isDeleteButtonVisible;
        this.onPhotoDeleteListener = onPhotoDeleteListener;

    }

    public interface OnPhotoDeleteListener {
        void onPhotoDelete(Uri photoUri);
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri photoUri = photoList.get(position);
        holder.bind(photoUri);

        if (isDeleteButtonVisible) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        // 삭제 버튼 클릭 이벤트
        holder.deleteButton.setOnClickListener(v -> {
            if (onPhotoDeleteListener != null) {
                onPhotoDeleteListener.onPhotoDelete(photoUri); // Corrected to use photoUri
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public void updatePhotoList(ArrayList<Uri> newPhotos) {
        photoList.clear();
        photoList.addAll(newPhotos);
        notifyDataSetChanged();
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;
        ImageButton deleteButton;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);  // 사진
            deleteButton = itemView.findViewById(R.id.deleteButton);      // 삭제 버튼
        }

        public void bind(Uri photoUri) {
            Glide.with(photoImageView.getContext())
                    .load(photoUri)
                    .into(photoImageView);
        }
    }
}
