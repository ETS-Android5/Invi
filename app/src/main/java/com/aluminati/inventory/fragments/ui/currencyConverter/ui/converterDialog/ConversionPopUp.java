package com.aluminati.inventory.fragments.ui.currencyConverter.ui.converterDialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aluminati.inventory.R;

public class ConversionPopUp extends DialogFragment
{


    private float conversionRate;
    private String conversionSymbol;
    private String baseSymbol;
    private EditText baseCurrencyEditText;
    private EditText targetCurrencyEditText;

    public ConversionPopUp(float conversionRate, String conversionSymbol, String baseSymbol){
        this.conversionRate = conversionRate;
        this.conversionSymbol = conversionSymbol;
        this.baseSymbol = baseSymbol;
    }

    public static ConversionPopUp newInstance(float conversionRate, String conversionSymbol, String baseSymbol) {
        return new ConversionPopUp(conversionRate, conversionSymbol, baseSymbol);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(getResources().getLayout(R.layout.convert), container, true);

        ((TextView)view.findViewById(R.id.base_currency)).setText(baseSymbol);
        ((TextView)view.findViewById(R.id.target_result_currency)).setText(conversionSymbol);

        baseCurrencyEditText = view.findViewById(R.id.base_currency_edit_text);
        targetCurrencyEditText = view.findViewById(R.id.target_resulut_currency_edit_text);



        view.findViewById(R.id.convert_button).setOnClickListener(click -> {
            try {
                if (!baseCurrencyEditText.getText().toString().isEmpty()) {
                    targetCurrencyEditText.setText(Float.toString(convertToTarget(baseCurrencyEditText)));
                } else {
                    baseCurrencyEditText.setText(Float.toString(convertToBase(targetCurrencyEditText)));
                }
            }catch (NumberFormatException e){
                Log.w(ConversionPopUp.class.getName(), "Failed to convert number", e);
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
