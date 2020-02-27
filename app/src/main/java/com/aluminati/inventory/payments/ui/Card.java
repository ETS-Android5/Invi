package com.aluminati.inventory.payments.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

public class Card extends Fragment {

    private TextView cardName;
    private TextView cardNumber;
    private TextView cardExpiryDate;
    private ImageView cardImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.add_card, container, false);

        Log.i("Heloo", "Dedledplde");

        cardName = view.findViewById(R.id.card_name);
        cardNumber = view.findViewById(R.id.card_number);
        cardExpiryDate = view.findViewById(R.id.card_expiry_date);
        cardImage = view.findViewById(R.id.card_logo);

        String spilt = getArguments().getString("card_result");
        fillFiled(spilt);

        return view;
    }

    private void fillFiled(String resultFromAnalysis){
        String[] split = resultFromAnalysis.split("#");

        for(String splits : split){
            if(Pattern.compile("[0-9]{1,2}[/][0-9]{1,2}").matcher(splits).find()){
                cardExpiryDate.setText(splits);
                Log.i("Heloo", "Dedledplde");
            }

            if(Pattern.compile("[0-9]{16}").matcher(splits).find()){
                cardNumber.setText(splits);
                Log.i("Heloo", "Dedledplde");
            }

            if(splits.toLowerCase().matches("mastercard")){
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.master_card));
            }else if(splits.toLowerCase().matches("visa")){
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.visa));
            }
        }
    }

}
