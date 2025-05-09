package com.example.tripkey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private Context context;
    private List<Expense> expenses;

    public ExpenseAdapter(Context context, List<Expense> expenses) {
        this.context = context;
        this.expenses = expenses;
    }

    @Override
    public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense_row, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExpenseViewHolder holder, int position) {
        Expense item = expenses.get(position);
        holder.textDesc.setText(item.getDescription());
        holder.textAmount.setText(item.getAmount() + "원");
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView textDesc, textAmount;

        ExpenseViewHolder(View itemView) {
            super(itemView);
            textDesc = itemView.findViewById(R.id.text_desc);
            textAmount = itemView.findViewById(R.id.text_amount);
        }
    }
}
