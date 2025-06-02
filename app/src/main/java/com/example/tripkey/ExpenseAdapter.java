package com.example.tripkey;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private OnExpenseDeletedListener listener;

    public void setOnExpenseDeletedListener(OnExpenseDeletedListener listener) {
        this.listener = listener;
    }

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
        holder.buttonDelete.setOnClickListener(v -> {
            String travelId = item.getTravelId();
            String date = item.getDate();
            int amount = item.getAmount();
            String description = item.getDescription();
            String writerUserId = item.getUserId();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(writerUserId)
                    .collection("travel")
                    .document(travelId)
                    .get()
                    .addOnSuccessListener(travelDoc -> {
                        if (travelDoc.exists()) {
                            String teamId = travelDoc.getString("teamId");
                            if (teamId == null) {
                                Log.e("DeleteExpense", "teamId is null");
                                return;
                            }

                            // 팀 멤버 목록 불러오기
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(writerUserId)
                                    .collection("teams")
                                    .document(teamId)
                                    .get()
                                    .addOnSuccessListener(teamDoc -> {
                                        List<String> members = (List<String>) teamDoc.get("members");
                                        if (members == null) return;

                                        for (String memberId : members) {
                                            FirebaseFirestore.getInstance()
                                                    .collection("users")
                                                    .document(memberId)
                                                    .collection("travel")
                                                    .document(travelId)
                                                    .collection("expenses")
                                                    .document(date)
                                                    .collection("items")
                                                    .whereEqualTo("description", description)
                                                    .whereEqualTo("amount", amount)
                                                    .get()
                                                    .addOnSuccessListener(querySnapshot -> {
                                                        for (var doc : querySnapshot.getDocuments()) {
                                                            doc.getReference().delete();
                                                        }
                                                    });

                                            // total 차감
                                            FirebaseFirestore.getInstance()
                                                    .collection("users")
                                                    .document(memberId)
                                                    .collection("travel")
                                                    .document(travelId)
                                                    .update("total", com.google.firebase.firestore.FieldValue.increment(-amount));
                                        }

                                        // 어댑터에서 제거
                                        expenses.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, expenses.size());

                                        if (listener != null) {
                                            listener.onExpenseDeleted(amount);
                                        }

                                    });
                        }
                    });
        });


    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView textDesc, textAmount;
        ImageView profileImage;
        ImageButton buttonDelete;

        ExpenseViewHolder(View itemView) {
            super(itemView);
            textDesc = itemView.findViewById(R.id.text_desc);
            textAmount = itemView.findViewById(R.id.text_amount);
            profileImage = itemView.findViewById(R.id.profileImage);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }
    }

    // ExpenseAdapter 내부에 interface 추가
    public interface OnExpenseDeletedListener {
        void onExpenseDeleted(int amountChanged); // 삭제된 금액을 알려줌
    }

}
