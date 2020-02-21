package com.aluminati.inventory.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aluminati.inventory.R;

public class PriceCheck extends DialogFragment {

    public static final String TAG = PriceCheck.class.getName();


    public PriceCheck(){

    }

    public static PriceCheck newInstance(String title) {
        PriceCheck priceCheck = new PriceCheck();
        Bundle args = new Bundle();
        args.putString("title", title);
        priceCheck.setArguments(args);
        return priceCheck;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(getResources().getLayout(R.layout.pricecheck), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
