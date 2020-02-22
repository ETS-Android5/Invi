package com.aluminati.inventory.currencyConverter;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;

import com.aluminati.inventory.R;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.Currency;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class CurrencyConverter{

    private final static String TAG = CurrencyConverter.class.getName();
    private final String API_URL = "https://api.exchangeratesapi.io/";
    private Retrofit retrofit;
    private Activity activity;


    public CurrencyConverter(Activity activity){
        this.activity = activity;
        this.retrofit = new Retrofit.Builder()
                .baseUrl(API_URL).addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public Single<CurrencyResult> getData() {
        CurrencyConverterApi apiService = retrofit.create(CurrencyConverterApi.class);
        return apiService.getLatestConversionRates();
    }

    public ArrayList<com.aluminati.inventory.currencyConverter.Currency> toCurrencyArray(CurrencyResult currencyResultSingle){
        LinkedTreeMap<String, Float> currencies = (LinkedTreeMap<String, Float>) currencyResultSingle.getCurrencyRates();
        return locale(new ArrayList<>(currencies.keySet()), currencies);
    }

    private ArrayList<com.aluminati.inventory.currencyConverter.Currency> locale(List<String> currenciesSymbols, LinkedTreeMap<String, Float> curren){
        HashSet<com.aluminati.inventory.currencyConverter.Currency> currencies = new HashSet<>();
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
                            Log.i(TAG, "Flag ==== > " + name);

                            currencies.add(new com.aluminati.inventory.currencyConverter.Currency(resourceId, currency.getDisplayName(),
                                            Float.toString(curren.get(currency.getCurrencyCode())), currency.getSymbol()));
                            //Log.i(TAG, "Got flag Resources " + getActivity().getResources().getResourceEntryName(resourceId));
                        }
                    }


            }catch (IllegalArgumentException | MissingResourceException e){
                //Log.w(TAG, "Currency not found", e);
            }
        }

        Log.i(TAG, "Size " + currencies.size());

        for(com.aluminati.inventory.currencyConverter.Currency currency : currencies){
            Log.i(TAG, "Currency " + currency.getCurrencySymbol());
        }
        Map<Integer, com.aluminati.inventory.currencyConverter.Currency> map = new LinkedHashMap<>();
        for (com.aluminati.inventory.currencyConverter.Currency ays : currencies
        ) {
            map.put(ays.getImage(), ays);
        }
        currencies.clear();
        currencies.addAll(map.values());

       return new ArrayList<>(currencies);
    }


    private Activity getActivity(){
        return this.activity;
    }

    public interface CurrencyConverterApi {

        @GET("latest")
        Single<CurrencyResult> getLatestConversionRates();
    }



}

