package com.example.tripkey;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ViewRecordAdapter extends RecyclerView.Adapter<ViewRecordAdapter.RecordViewHolder> {

    public interface OnEditClickListener {
        void onEditClick(String recordId, String place, String record, ArrayList<String> photoUris);
    }

    private Context context;
    private List<RecordItem> recordList;
    private OnEditClickListener editClickListener;

    public ViewRecordAdapter(Context context, List<RecordItem> recordList, OnEditClickListener listener) {
        this.context = context;
        this.recordList = recordList;
        this.editClickListener = listener;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_viewrecord, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        RecordItem item = recordList.get(position);
        holder.placeTextView.setText(" 📍 " + item.getPlace());
        holder.recordTextView.setText(item.getRecord());

        PhotoAdapter photoAdapter = new PhotoAdapter(new ArrayList<>(), false, uri -> {});
        ArrayList<Uri> uriList = new ArrayList<>();

        List<String> photoUris = item.getPhotoUris();
        if (photoUris != null) {
            for (String uriStr : photoUris) {
                uriList.add(Uri.parse(uriStr));
            }
        }

        photoAdapter.updatePhotoList(uriList);
        holder.photoRecyclerView.setAdapter(photoAdapter);
        holder.photoRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        holder.editButton.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(item.getRecordId(), item.getPlace(), item.getRecord(), item.getPhotoUris());
            }
        });
    }


    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView placeTextView, recordTextView;
        RecyclerView photoRecyclerView;
        ImageButton editButton;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            placeTextView = itemView.findViewById(R.id.placeTextView);
            recordTextView = itemView.findViewById(R.id.recordTextView);
            photoRecyclerView = itemView.findViewById(R.id.photoRecyclerView);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
}
