package com.example.tripkey;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {

    private List<ChecklistItem> items; // 체크리스트 데이터
    private OnItemDeleteListener deleteListener; // 삭제 리스너

    public interface OnItemDeleteListener {
        void onItemDelete(int position);
    }

    public ChecklistAdapter(List<ChecklistItem> items, OnItemDeleteListener deleteListener) {
        this.items = items;
        this.deleteListener = deleteListener;
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

        // 체크박스 상태 변경 리스너 설정
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.setChecked(isChecked);
                // SharedPreferences에 저장하는 로직 추가 (ChecklistActivity의 saveChecklistItem() 호출)
                ((ChecklistActivity) buttonView.getContext()).saveChecklistItem(item);
            }
        });

        // 삭제 버튼 클릭 리스너 설정
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (deleteListener != null) {
                    deleteListener.onItemDelete(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;  // 체크박스
        TextView textView;  // 항목 이름
        ImageButton deleteButton; // 삭제 버튼


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            textView = itemView.findViewById(R.id.itemText);
            deleteButton = itemView.findViewById(R.id.deleteButton); // 삭제 버튼 초기화
        }
    }
}

