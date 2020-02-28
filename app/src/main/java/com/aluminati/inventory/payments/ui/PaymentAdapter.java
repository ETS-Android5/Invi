package com.aluminati.inventory.payments.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.ui.currencyConverter.Currency;
import com.aluminati.inventory.fragments.ui.currencyConverter.ui.converterDialog.ConversionPopUp;
import com.aluminati.inventory.payments.Payment;

import java.util.ArrayList;



public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentsViewHolder>
        implements View.OnClickListener
{


    private ArrayList<Payment> dataSet;
    private View view;
    private PaymentsViewHolder currencyViewHolder;
    private FragmentActivity activity;



    public PaymentAdapter(ArrayList<Payment> data, FragmentActivity activity) {
        this.dataSet = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PaymentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_card, parent, false);
        currencyViewHolder = new PaymentsViewHolder(view);
        return currencyViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentsViewHolder holder, int position) {
        TextView cardNumber = holder.cardNumber;
        TextView cardExpiry = holder.cardExpiry;
        ImageView cardIcon = holder.cardLogo;
        cardNumber.setText(formatString(dataSet.get(position).getNumber()));
        cardExpiry.setText(dataSet.get(position).getExpiryDate());
        cardIcon.setImageDrawable(dataSet.get(position).getName().equals("visa")
                ? view.getResources().getDrawable(R.drawable.visa)
                : view.getResources().getDrawable(R.drawable.master_card) );
        view.setOnClickListener(click -> {

        });
    }

    private String formatString(String string){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < string.length(); i++){
            if(i%4 == 0){
                stringBuilder.append(" ".concat(Character.toString(string.charAt(i))));
            }else stringBuilder.append(string.charAt(i));
        }
        return stringBuilder.toString();
    }
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public void onClick(View view) {

    }


    static class PaymentsViewHolder extends RecyclerView.ViewHolder {

        TextView cardNumber;
        TextView cardExpiry;
        ImageView cardLogo;

        PaymentsViewHolder(View itemView) {
            super(itemView);
            this.cardNumber =  itemView.findViewById(R.id.card_number);
            this.cardExpiry = itemView.findViewById(R.id.card_expiry_date);
            this.cardLogo =  itemView.findViewById(R.id.card_logo);
        }
    }

}