package com.example.tripkey;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EachMoneyAdapter extends RecyclerView.Adapter<EachMoneyAdapter.ViewHolder> {

    private List<UserExpense> userExpenses;

    public EachMoneyAdapter(List<UserExpense> userExpenses) {
        this.userExpenses = userExpenses;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textUserId, textAmount;

        public ViewHolder(View view) {
            super(view);
            textUserId = view.findViewById(R.id.textUserId);   // item_eachmoney.xml에서 정의 필요
            textAmount = view.findViewById(R.id.textAmount);
        }
    }

    @Override
    public EachMoneyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_eachmoney, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EachMoneyAdapter.ViewHolder holder, int position) {
        UserExpense ue = userExpenses.get(position);
        holder.textUserId.setText(ue.getUserId());
        holder.textAmount.setText(ue.getTotalAmount() + " 원");
    }

    @Override
    public int getItemCount() {
        return userExpenses.size();
    }
}
