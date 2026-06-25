package com.apk.catatkeuanganku.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apk.catatkeuanganku.R;
import com.apk.catatkeuanganku.database.TransactionEntity;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<TransactionEntity> transactions;
    private Context context;
    // 1. Tambahkan interface listener untuk hapus
    private OnTransactionLongClickListener longClickListener;

    public interface OnTransactionLongClickListener {
        void onLongClick(TransactionEntity transaction, int position);
    }

    public void setOnTransactionLongClickListener(OnTransactionLongClickListener listener) {
        this.longClickListener = listener;
    }

    public TransactionAdapter(Context context) {
        this.context = context;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionEntity currentItem = transactions.get(position);
        holder.bind(currentItem);

        // 2. Set Long Click Listener pada itemView
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(currentItem, position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDate, tvAmount, tvCategory;
        private ImageView ivIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_transaction_name);
            tvDate = itemView.findViewById(R.id.tv_transaction_date);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
            tvCategory = itemView.findViewById(R.id.tv_transaction_category);
            ivIcon = itemView.findViewById(R.id.iv_type_icon);
        }

        public void bind(TransactionEntity transaction) {
            tvName.setText(transaction.getNote()); // Menggunakan getNote() sesuai entity lu
            tvDate.setText(transaction.getDate());

            String category = transaction.getCategory();

            if (category != null && !category.isEmpty()) {
                tvCategory.setText(category);
                tvCategory.setVisibility(View.VISIBLE);
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            boolean isIncome = "pemasukan".equals(transaction.getType());

            if (isIncome) {
                tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Hijau
                tvAmount.setText("+ " + formatRupiah(transaction.getAmount()));
                ivIcon.setBackgroundResource(R.drawable.rounded_bg_pemasukan);

                // Logic Icon Pemasukan
                if (category != null && category.equals(context.getString(R.string.cat_interest))) {
                    ivIcon.setImageResource(R.drawable.bunga);
                } else {
                    ivIcon.setImageResource(R.drawable.gaji);
                }

            } else {
                tvAmount.setTextColor(Color.parseColor("#F44336")); // Merah
                tvAmount.setText("- " + formatRupiah(transaction.getAmount()));
                ivIcon.setBackgroundResource(R.drawable.rounded_bg_pengeluaran);

                // Logic Icon Pengeluaran
                if (category != null) {
                    if (category.equals(context.getString(R.string.cat_food))) {
                        ivIcon.setImageResource(R.drawable.makan);
                    } else if (category.equals(context.getString(R.string.cat_transport))) {
                        ivIcon.setImageResource(R.drawable.transport);
                    } else if (category.equals(context.getString(R.string.cat_shopping))) {
                        ivIcon.setImageResource(R.drawable.belanja);
                    } else {
                        ivIcon.setImageResource(R.drawable.hiburan);
                    }
                } else {
                    ivIcon.setImageResource(R.drawable.hiburan);
                }
            }
        }

        private String formatRupiah(long number) {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            return format.format(Math.abs(number)).replace("Rp", "Rp ").replaceAll(",00", "");
        }
    }
}