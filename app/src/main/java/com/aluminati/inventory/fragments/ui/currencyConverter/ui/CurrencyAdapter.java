package com.aluminati.inventory.fragments.ui.currencyConverter.ui;

import android.util.Log;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>
        implements View.OnClickListener
{


    private ArrayList<Currency> dataSet;
    private View view;
    private CurrencyViewHolder currencyViewHolder;
    private FragmentActivity activity;
    private String baseCurrency;
    private String currentTotal;



    public CurrencyAdapter(String baseCurrency, ArrayList<Currency> data, FragmentActivity activity, String currenTotal) {
        this.dataSet = data;
        this.activity = activity;
        this.baseCurrency = baseCurrency;
        this.currentTotal = currenTotal;
    }

    @Override
    public CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
             view = LayoutInflater.from(parent.getContext()).inflate(R.layout.currency_card_layout, parent, false);
        currencyViewHolder = new CurrencyViewHolder(view);
        return currencyViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        holder.textViewName.setText(dataSet.get(position).getCurrencySymbol());
        holder.textViewVersion.setText(String.format("%.2f", Float.parseFloat(dataSet.get(position).getCurrencyRate())));
        holder.baseCurrency.setText("1 ".concat(baseCurrency ).concat(" = "));
        holder.textViewSymbol.setText(dataSet.get(position).getCurrencyDisplayName());
        holder.imageViewIcon.setImageDrawable(view.getResources().getDrawable(dataSet.get(position).getImage()));
        view.setOnClickListener(click -> {
            ConversionPopUp conversionPopUp =
                    ConversionPopUp.newInstance(Float.parseFloat(dataSet.get(position).getCurrencyRate()), dataSet.get(position).getCurrencyDisplayName(), baseCurrency);
            conversionPopUp.show(activity.getSupportFragmentManager(), "Conversion Rate");
        });
        if(currentTotal.isEmpty()){
            holder.currentAmount.setText("Empty Cart");
            holder.convertedAmount.setText("Empty Cart");
        }else{
            Log.i("TAG", "basecurrency " + baseCurrency);
           ;

            holder.currentAmount.setText(baseCurrency.concat(" ").concat(currentTotal));
            Float crt = Float.parseFloat(currentTotal);
            Float crp = Float.parseFloat(dataSet.get(position).getCurrencyRate());
            holder.convertedAmount.setText(dataSet.get(position).getCurrencyDisplayName().concat(" ").concat(String.format("%.2f",crt*crp)));

        }


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
        TextView currentAmount;
        TextView convertedAmount;
        ImageView imageViewIcon;

        CurrencyViewHolder(View itemView) {
            super(itemView);
            this.textViewName =  itemView.findViewById(R.id.country_name);
            this.textViewVersion = itemView.findViewById(R.id.conversion_rate);
            this.imageViewIcon =  itemView.findViewById(R.id.country_flag);
            this.textViewSymbol = itemView.findViewById(R.id.currency_symbol);
            this.baseCurrency = itemView.findViewById(R.id.base_currency_one);
            this.convertedAmount = itemView.findViewById(R.id.converted_amount);
            this.currentAmount = itemView.findViewById(R.id.current_amount);
        }
    }

}