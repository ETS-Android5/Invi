package com.aluminati.inventory.currencyConverter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.ebanx.swipebtn.OnStateChangeListener;

import java.util.ArrayList;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>
        implements View.OnClickListener
{


    private ArrayList<Currency> dataSet;
    private View view;
    private CurrencyViewHolder currencyViewHolder;
    private FragmentActivity activity;



    public CurrencyAdapter(ArrayList<Currency> data, FragmentActivity activity) {
        this.dataSet = data;
        this.activity = activity;
    }

    @Override
    public CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
             view = LayoutInflater.from(parent.getContext()).inflate(R.layout.currency_card_layout, parent, false);
        currencyViewHolder = new CurrencyViewHolder(view);
        return currencyViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        TextView textViewName = holder.textViewName;
        TextView textViewVersion = holder.textViewVersion;
        TextView textViewSymbol = holder.textViewSymbol;
        ImageView imageView = holder.imageViewIcon;
        textViewName.setText(dataSet.get(position).getCurrencySymbol());
        textViewVersion.setText(dataSet.get(position).getCurrencyRate());
        textViewSymbol.setText(dataSet.get(position).getCurrencyDisplayName());
        imageView.setImageDrawable(view.getResources().getDrawable(dataSet.get(position).getImage()));
        view.setOnClickListener(click -> {
            ConversionPopUp conversionPopUp =
                    ConversionPopUp.newInstance(Float.parseFloat(dataSet.get(position).getCurrencyRate()), dataSet.get(position).getCurrencyDisplayName());
            conversionPopUp.show(activity.getSupportFragmentManager(), "Conversion Rate");
        });
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public void onClick(View view) {
    }


    static class CurrencyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewVersion;
        TextView textViewSymbol;
        ImageView imageViewIcon;

        CurrencyViewHolder(View itemView) {
            super(itemView);
            this.textViewName =  itemView.findViewById(R.id.country_name);
            this.textViewVersion = itemView.findViewById(R.id.conversion_rate);
            this.imageViewIcon =  itemView.findViewById(R.id.country_flag);
            this.textViewSymbol = itemView.findViewById(R.id.currency_symbol);
        }
    }

}