package com.aluminati.inventory.fragments.ui.currencyConverter.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.ui.currencyConverter.Currency;
import com.aluminati.inventory.fragments.ui.currencyConverter.converterApi.CurrencyConverter;
import com.aluminati.inventory.fragments.ui.currencyConverter.CurrencyResult;

import java.util.ArrayList;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CurrencyFrag extends Fragment{


    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<Currency> data;
    private static ArrayList<Integer> removedItems;
    private CompositeDisposable compositeDisposable;
    private TextView dateOfConversion;
    private Spinner baseCurrency;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(getResources().getLayout(R.layout.currencies), container, false);
        layoutManager = new LinearLayoutManager(getContext());


        recyclerView = view.findViewById(R.id.currencies_recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        dateOfConversion = view.findViewById(R.id.date_of_conversion_rates);
        baseCurrency = view.findViewById(R.id.current_base_currency);
        baseCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CurrencyConverter currencyConverter = new CurrencyConverter(getActivity(), baseCurrency.getSelectedItem().toString());
                getFlags(currencyConverter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




        removedItems = new ArrayList<>();


        return view;
    }

    private void getFlags(CurrencyConverter currencyConverter){
        this.compositeDisposable = new CompositeDisposable();
                currencyConverter.getData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CurrencyResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(CurrencyResult currencyResult) {
                        data = currencyConverter.toCurrencyArray(currencyResult);
                        adapter = new CurrencyAdapter(currencyResult.getBase(),data, getActivity());
                        recyclerView.setAdapter(adapter);
                        dateOfConversion.setText("Conversion on " + currencyResult.getDate());
                        //baseCurrency.setText("Base Currency " + currencyResult.getBase());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.w(CurrencyConverter.class.getName(), "Failed to get currency rates", e);
                    }
                });

    }

    @Override
    public void onDestroy() {
        if(compositeDisposable != null && !compositeDisposable.isDisposed()){
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_currency, menu);
    }







}