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

    public PhotoAdapter(ArrayList<Uri> photoList) {
        this.photoList = photoList;  // Uri 타입으로 설정
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

        // 사진 클릭 시 삭제 처리
        holder.deleteButton.setOnClickListener(v -> {
            removePhoto(position);  // 사진 삭제
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    // 사진을 삭제하는 메서드
    public void removePhoto(int position) {
        if (position >= 0 && position < photoList.size()) {
            photoList.remove(position);
            notifyItemRemoved(position);  // 해당 아이템을 RecyclerView에서 삭제
        }
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
