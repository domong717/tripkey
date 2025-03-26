package com.example.tripkey;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;

import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {

    private List<ChecklistItem> items;
    private CollectionReference checklistRef;

    public ChecklistAdapter(List<ChecklistItem> items, CollectionReference checklistRef) {
        this.items = items;
        this.checklistRef = checklistRef;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checklist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChecklistItem item = items.get(position);
        holder.checkBox.setChecked(item.isChecked());
        holder.textView.setText(item.getText());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);

            // Firestore 업데이트
            checklistRef.document(item.getId()).update("isChecked", isChecked);
        });

        holder.deleteButton.setOnClickListener(v -> {
            String id = item.getId();

            // Firestore에서 삭제
            checklistRef.document(id).delete().addOnSuccessListener(aVoid -> {
                items.remove(position);
                notifyItemRemoved(position);
            });
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textView;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            textView = itemView.findViewById(R.id.itemText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
