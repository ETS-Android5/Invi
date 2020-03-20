package com.aluminati.inventory.fragments.summary.recenttransaction.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.summary.recenttransaction.Transaction;
import com.aluminati.inventory.payments.model.Payment;
import com.aluminati.inventory.payments.ui.Payments;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.RecentTransactionsViewHolder> implements View.OnClickListener {


    private ArrayList<Transaction> dataSet;
    private Fragment activity;


    public TransactionAdapter(ArrayList<Transaction> data, Fragment activity) {
        this.dataSet = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecentTransactionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentTransactionsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_transactions_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentTransactionsViewHolder holder, int position) {
        holder.type.setText(dataSet.get(position).getType());
        holder.date.setText(dataSet.get(position).getDate());
        holder.amount.setText(NumberFormat.getCurrencyInstance(new Locale("en", "IE")).format(Double.parseDouble(dataSet.get(position).getAmount())));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public void onClick(View view) {

    }


    static class RecentTransactionsViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView type;
        TextView amount;

        RecentTransactionsViewHolder(View itemView) {
            super(itemView);
            this.date = itemView.findViewById(R.id.transaction_date);
            this.amount = itemView.findViewById(R.id.transaction_amount);
            this.type = itemView.findViewById(R.id.transaction_payment_type);
        }
    }

}