package com.aluminati.inventory.fragments.ui.currencyConverter.ui;

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

import java.util.ArrayList;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>
        implements View.OnClickListener
{


    private ArrayList<Currency> dataSet;
    private View view;
    private CurrencyViewHolder currencyViewHolder;
    private FragmentActivity activity;
    private String baseCurrency;



    public CurrencyAdapter(String baseCurrency, ArrayList<Currency> data, FragmentActivity activity) {
        this.dataSet = data;
        this.activity = activity;
        this.baseCurrency = baseCurrency;
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
        TextView baseone = holder.baseCurrency;
        ImageView imageView = holder.imageViewIcon;
        textViewName.setText(dataSet.get(position).getCurrencySymbol());
        textViewVersion.setText(dataSet.get(position).getCurrencyRate());
        baseone.setText("1 ".concat(baseCurrency ).concat(" = "));
        textViewSymbol.setText(dataSet.get(position).getCurrencyDisplayName());
        imageView.setImageDrawable(view.getResources().getDrawable(dataSet.get(position).getImage()));
        view.setOnClickListener(click -> {
            ConversionPopUp conversionPopUp =
                    ConversionPopUp.newInstance(Float.parseFloat(dataSet.get(position).getCurrencyRate()), dataSet.get(position).getCurrencyDisplayName(), baseCurrency);
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
        TextView baseCurrency;
        ImageView imageViewIcon;

        CurrencyViewHolder(View itemView) {
            super(itemView);
            this.textViewName =  itemView.findViewById(R.id.country_name);
            this.textViewVersion = itemView.findViewById(R.id.conversion_rate);
            this.imageViewIcon =  itemView.findViewById(R.id.country_flag);
            this.textViewSymbol = itemView.findViewById(R.id.currency_symbol);
            this.baseCurrency = itemView.findViewById(R.id.base_currency_one);
        }
    }

}