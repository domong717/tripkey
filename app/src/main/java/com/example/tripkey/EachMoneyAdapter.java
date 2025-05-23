package com.example.tripkey;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EachMoneyAdapter extends RecyclerView.Adapter<EachMoneyAdapter.ViewHolder> {

    private List<UserExpense> userExpenses;
    private Context context;

    public EachMoneyAdapter(Context context, List<UserExpense> userExpenses) {
        this.userExpenses = userExpenses;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textUserId, textAmount, accountReg, detail;
        ImageView profileImage;

        public ViewHolder(View view) {
            super(view);
            textUserId = view.findViewById(R.id.textUserId);   // item_eachmoney.xml에서 정의 필요
            textAmount = view.findViewById(R.id.textAmount);
            profileImage = view.findViewById(R.id.profileImage);
            accountReg = view.findViewById(R.id.account_reg);
            detail = view.findViewById(R.id.detail);
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

        holder.accountReg.setOnClickListener(v -> {
            String userId = ue.getUserId();
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String account = doc.getString("account");
                        showAccountDialog(userId, account);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "계좌를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show());
        });

        holder.detail.setOnClickListener(v -> {
            Intent intent = new Intent(context, MoneyDetailActivity.class);
            intent.putExtra("userId", ue.getUserId());
            intent.putExtra("travelId", ue.getTravelId());
            context.startActivity(intent);
        });

    }

    private void showAccountDialog(String userId, String account) {
        String accountText = (account != null && !account.isEmpty()) ? account : "등록된 계좌가 없습니다.";
        new AlertDialog.Builder(context)
                .setTitle(userId + "의 계좌번호")
                .setMessage(accountText)
                .setPositiveButton("확인", null)
                .show();
    }


    @Override
    public int getItemCount() {
        return userExpenses.size();
    }
}
