package com.aluminati.inventory.fragments.ui.currencyConverter.converterApi;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aluminati.inventory.fragments.ui.currencyConverter.CurrencyResult;
import com.aluminati.inventory.login.authentication.verification.VerificationStatus;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Currency;
import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class CurrencyConverter{

    private final static String TAG = CurrencyConverter.class.getName();
    private final String API_URL = "https://api.exchangeratesapi.io/";
    private Retrofit retrofit;
    private Activity activity;



    public CurrencyConverter(Activity activity, String base){
        this.activity = activity;
        this.base = base;
        this.retrofit = new Retrofit.Builder()
                .baseUrl(API_URL).addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public CurrencyConverter(Activity activity){
        this.activity = activity;
        this.retrofit = new Retrofit.Builder()
                .baseUrl(API_URL).addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public Single<CurrencyResult> getData() {
        CurrencyConverterApi apiService = retrofit.create(CurrencyConverterApi.class);
        return apiService.getLatestConversionRates(getBase());
    }

    public ArrayList<com.aluminati.inventory.fragments.ui.currencyConverter.Currency> toCurrencyArray(CurrencyResult currencyResultSingle, int filterType){
        LinkedTreeMap<String, Float> currencies = (LinkedTreeMap<String, Float>) currencyResultSingle.getCurrencyRates();
        return filter(locale(new ArrayList<>(currencies.keySet()), currencies), currencyResultSingle, filterType);
    }

    private HashSet<com.aluminati.inventory.fragments.ui.currencyConverter.Currency> locale(List<String> currenciesSymbols, LinkedTreeMap<String, Float> curren){
        HashSet<com.aluminati.inventory.fragments.ui.currencyConverter.Currency> currencies = new HashSet<>();
        Locale[] locales = Locale.getAvailableLocales();
        for(Locale locale : locales){
            try {
                       Currency currency = Currency.getInstance(locale);
                    if(currenciesSymbols.contains(currency.getCurrencyCode())){
                        Resources resources = getActivity().getResources();
                        String name = "";
                        if(locale.getDisplayCountry().equals("United States"))
                        {
                            name = "flag_united_states_of_america";
                        }else {
                            name = "flag_" + locale.getDisplayCountry().toLowerCase();
                        }
                        final int resourceId = resources.getIdentifier(name, "drawable", getActivity().getPackageName());
                        if(resourceId != 0){
                            Log.i(TAG, "Flag ==== > " + name + " Currency ====> " + currency.getDisplayName() + " Symbol =====> " + currency.getSymbol()
                            + " Currency code ====> " + currency.getCurrencyCode());

                            SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                            ed.putString(currency.getCurrencyCode(), Float.toString(curren.get(currency.getCurrencyCode())));
                            ed.apply();


                            currencies.add(new com.aluminati.inventory.fragments.ui.currencyConverter.Currency(resourceId, currency.getDisplayName(),
                                            Float.toString(curren.get(currency.getCurrencyCode())), currency.getSymbol(), currency.getCurrencyCode()));
                            //Log.i(TAG, "Got flag Resources " + getActivity().getResources().getResourceEntryName(resourceId));
                        }
                    }


            }catch (IllegalArgumentException | MissingResourceException e){
                Log.w(TAG, "Currency not found", e);
            }
        }



        return currencies;
    }

    private ArrayList<com.aluminati.inventory.fragments.ui.currencyConverter.Currency> filter(HashSet<com.aluminati.inventory.fragments.ui.currencyConverter.Currency> currencies
            , com.aluminati.inventory.fragments.ui.currencyConverter.CurrencyResult currencyResult, int filterType){

        LinkedHashMap map = new LinkedHashMap<>();

        switch (filterType){
            case VerificationStatus.CURRENCY_FLAGS_FILTER:{
                for (com.aluminati.inventory.fragments.ui.currencyConverter.Currency ays : currencies) {
                    if(!ays.getCurrencyCCode().equals(currencyResult.getBase())) {
                        map.put(ays.getImage(), ays);
                    }
                }
                break;
            }case VerificationStatus.CURRENCY_SIGN_FILTER:{
                for (com.aluminati.inventory.fragments.ui.currencyConverter.Currency ays : currencies) {
                    if(!ays.getCurrencySymbol().equals(currencyResult.getBase())) {
                        map.put(ays.getCurrencySymbol(), ays);
                    }
                }
                break;
            }
        }


        currencies.clear();
        currencies.addAll(map.values());

        ArrayList arrayList = new ArrayList<>(currencies);
        Collections.sort(arrayList, new Comparator<com.aluminati.inventory.fragments.ui.currencyConverter.Currency>() {
            @Override
            public int compare(com.aluminati.inventory.fragments.ui.currencyConverter.Currency currency, com.aluminati.inventory.fragments.ui.currencyConverter.Currency t1) {
                return currency.getCurrencySymbol().compareTo(t1.getCurrencySymbol());
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }
        });

        return arrayList;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    private String base;

    private Activity getActivity(){
        return this.activity;
    }

    public interface CurrencyConverterApi {

        @GET("latest")
        Single<CurrencyResult> getLatestConversionRates(@Query("base") String currency);
    }



}

