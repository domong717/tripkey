package com.example.tripkey;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SupplyAdapter extends RecyclerView.Adapter<SupplyAdapter.ViewHolder> {
    private List<String> supplies;
    private OnAddClickListener listener;

    public interface OnAddClickListener {
        void onAddClick(String supply);
    }

    public SupplyAdapter(List<String> supplies, OnAddClickListener listener) {
        this.supplies = supplies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_supply, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String supply = supplies.get(position);
        holder.supplyText.setText(supply);
        holder.addButton.setOnClickListener(v -> listener.onAddClick(supply));
    }

    @Override
    public int getItemCount() {
        return supplies.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView supplyText;
        Button addButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            supplyText = itemView.findViewById(R.id.supplyText);
            addButton = itemView.findViewById(R.id.addButton);
        }
    }
}

