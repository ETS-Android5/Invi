package com.aluminati.inventory.payments.ui;

import android.graphics.EmbossMaskFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.payments.model.Payment;
import com.aluminati.inventory.payments.selectPayment.SelectPayment;

import java.util.ArrayList;



public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentsViewHolder>
        implements View.OnClickListener
{


    private ArrayList<Payment> dataSet;
    private View view;
    private PaymentsViewHolder currencyViewHolder;
    private Fragment activity;
    private Payments.SelectedCard selectedCard;
    private Payments.SetButtonVisible setButtonVisible;



    public PaymentAdapter(ArrayList<Payment> data, Fragment activity) {
        this.dataSet = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PaymentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(activity instanceof SelectPayment) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_card_small, parent, false);
        }else if(activity instanceof Payments){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_card, parent, false);
        }
        currencyViewHolder = new PaymentsViewHolder(view);

        selectedCard.selectedCard(0);

        return currencyViewHolder;
    }



    @Override
    public void onBindViewHolder(@NonNull PaymentsViewHolder holder, int position) {
        EditText cardNumber = holder.cardNumber;
        EditText cardExpiry = holder.cardExpiry;
        ImageView cardIcon = holder.cardLogo;
        Button editCardButton = holder.editButton;
        if(activity instanceof Payments){
            editCardButton.setOnClickListener(click -> editCard(cardNumber, cardExpiry, editCardButton));
        }
        cardNumber.setText(formatString(dataSet.get(position).getNumber()));
        cardExpiry.setText(dataSet.get(position).getExpiryDate());

        setEmbos(cardNumber);
        setEmbos(cardExpiry);
        cardIcon.setImageDrawable(dataSet.get(position).getName().equals("visa")
                ? view.getResources().getDrawable(R.drawable.visa)
                : view.getResources().getDrawable(R.drawable.master_card) );


        selectedCard.selectedCard(position);

        view.setOnClickListener(click -> {
            cardNumber.setSelection(0);
            setButtonVisible.setVisibile();
        });
    }

    private void editCard(EditText number, EditText expiryDate, Button editCardButton){
        if(!number.isEnabled() && !expiryDate.isEnabled()){
            number.setEnabled(true);
            expiryDate.setEnabled(true);
            editCardButton.setText(view.getResources().getString(R.string.save_card));
        }else {
            number.setEnabled(false);
            number.setEnabled(false);
            editCardButton.setText(view.getResources().getString(R.string.edit_card));
        }
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

    private void setEmbos(TextView view){
        EmbossMaskFilter filter = new EmbossMaskFilter(
                new float[]{ 0f, 1f, -0.5f }, // direction of the light source
                0.8f, // ambient light between 0 to 1
                15, // specular highlights
                7.0f // blur before applying lighting
        );

        // Set the TextView layer type to software
        view.setLayerType(View.LAYER_TYPE_SOFTWARE,null);

        // Finally, make the TextView text effect deboss.
        view.getPaint().setMaskFilter(filter);
    }

    public void setSelectedCard(Payments.SelectedCard selectedCard){
        this.selectedCard = selectedCard;
    }

    public void setSetButtonVisible(Payments.SetButtonVisible setButtonVisible) {
        this.setButtonVisible = setButtonVisible;
    }

    static class PaymentsViewHolder extends RecyclerView.ViewHolder {

        EditText cardNumber;
        EditText cardExpiry;
        Button editButton;
        ImageView cardLogo;

        PaymentsViewHolder(View itemView) {
            super(itemView);
            this.cardNumber =  itemView.findViewById(R.id.card_number);
            this.cardExpiry = itemView.findViewById(R.id.card_expiry_date);
            this.cardLogo =  itemView.findViewById(R.id.card_logo);
            this.editButton = itemView.findViewById(R.id.edit_card);
        }
    }

}