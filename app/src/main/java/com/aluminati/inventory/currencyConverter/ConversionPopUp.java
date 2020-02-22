package com.aluminati.inventory.currencyConverter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aluminati.inventory.R;

import java.math.BigDecimal;

public class ConversionPopUp extends DialogFragment
{


    private float conversionRate;
    private String conversionSymbol;
    private EditText baseCurrencyEditText;
    private EditText targetCurrencyEditText;

    public ConversionPopUp(float conversionRate, String conversionSymbol){
        this.conversionRate = conversionRate;
        this.conversionSymbol = conversionSymbol;
    }

    public static ConversionPopUp newInstance(float conversionRate, String conversionSymbol) {
        return new ConversionPopUp(conversionRate, conversionSymbol);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(getResources().getLayout(R.layout.convert), container, true);

        ((TextView)view.findViewById(R.id.base_currency)).setText("Euro");
        ((TextView)view.findViewById(R.id.target_result_currency)).setText(conversionSymbol);

        baseCurrencyEditText = view.findViewById(R.id.base_currency_edit_text);
        targetCurrencyEditText = view.findViewById(R.id.target_resulut_currency_edit_text);



        view.findViewById(R.id.convert_button).setOnClickListener(click -> {
             if(!baseCurrencyEditText.getText().toString().isEmpty()){
                 targetCurrencyEditText.setText(Float.toString(convertToTarget(baseCurrencyEditText)));
             }else{
                 baseCurrencyEditText.setText(Float.toString(convertToBase(targetCurrencyEditText)));
             }

        });
        return view;
    }


    private float convertToBase(EditText editText){
       return !editText.toString().isEmpty() ? (Float.parseFloat(editText.getText().toString()) / conversionRate) : 0;
    }

    private float convertToTarget(EditText editText){
        return !editText.toString().isEmpty() ? (Float.parseFloat(editText.getText().toString()) * conversionRate) : 0;
    }


}
