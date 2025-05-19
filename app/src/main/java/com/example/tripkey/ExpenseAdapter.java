package com.example.tripkey;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private Context context;
    private List<Expense> expenses;

    private FirebaseFirestore db;

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
        Log.d("profileimage","holder.textDesc");
        holder.textDesc.setText(item.getDescription());
        holder.textAmount.setText(item.getAmount() + "원");
        db = FirebaseFirestore.getInstance();

        String userId = item.getUserId();
        Log.d("profileimage", "Success");
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("profileImage");
                        Log.d("profileimage", "imageUrl:"+imageUrl);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(holder.itemView.getContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.profile)
                                    .circleCrop()
                                    .into(holder.profileImage);
                        } else {
                            holder.profileImage.setImageResource(R.drawable.profile);
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView textDesc, textAmount;
        ImageView profileImage;

        ExpenseViewHolder(View itemView) {
            super(itemView);
            textDesc = itemView.findViewById(R.id.text_desc);
            textAmount = itemView.findViewById(R.id.text_amount);
            profileImage = itemView.findViewById(R.id.profileImage);

        }
    }
}
