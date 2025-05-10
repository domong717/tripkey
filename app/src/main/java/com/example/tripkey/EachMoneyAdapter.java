package com.example.tripkey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class EachMoneyAdapter extends RecyclerView.Adapter<EachMoneyAdapter.ViewHolder> {

    private List<UserExpense> userExpenses;
    private Context context;

    public EachMoneyAdapter(Context context, List<UserExpense> userExpenses) {
        this.userExpenses = userExpenses;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textUserId, textAmount;
        ImageView profileImage;

        public ViewHolder(View view) {
            super(view);
            textUserId = view.findViewById(R.id.textUserId);   // item_eachmoney.xml에서 정의 필요
            textAmount = view.findViewById(R.id.textAmount);
            profileImage = view.findViewById(R.id.profileImage);
        }
    }

    @Override
    public EachMoneyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_eachmoney, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserExpense ue = userExpenses.get(position);
        holder.textUserId.setText(ue.getUserId() + " 님");
        holder.textAmount.setText(ue.getTotalAmount() + " 원");

        if (ue.getProfileImageUrl() != null && !ue.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(ue.getProfileImageUrl())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.profile);
        }
    }

    @Override
    public int getItemCount() {
        return userExpenses.size();
    }
}
