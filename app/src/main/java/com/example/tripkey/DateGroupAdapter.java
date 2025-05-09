package com.example.tripkey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DateGroupAdapter extends RecyclerView.Adapter<DateGroupAdapter.DateGroupViewHolder> {
    private Context context;
    private List<DateGroup> dateGroupList;

    public DateGroupAdapter(Context context, List<DateGroup> dateGroupList) {
        this.context = context;
        this.dateGroupList = dateGroupList;
    }

    @Override
    public DateGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_date_group, parent, false);
        return new DateGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DateGroupViewHolder holder, int position) {
        DateGroup group = dateGroupList.get(position);
        holder.textDate.setText(group.getDate());

        holder.childRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        ExpenseAdapter adapter = new ExpenseAdapter(context, group.getExpenses());
        holder.childRecyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return dateGroupList.size();
    }

    class DateGroupViewHolder extends RecyclerView.ViewHolder {
        TextView textDate;
        RecyclerView childRecyclerView;

        DateGroupViewHolder(View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.text_date);
            childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
        }
    }
}
