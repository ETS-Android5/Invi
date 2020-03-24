package com.aluminati.inventory.fragments.ui.currencyConverter.ui.currencyChange;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.utils.Utils;
import com.aluminati.inventory.fragments.languageSelect.LanguageCustomAdapter;
import com.aluminati.inventory.fragments.ui.currencyConverter.Currency;
import com.aluminati.inventory.fragments.ui.currencyConverter.CurrencyResult;
import com.aluminati.inventory.fragments.ui.currencyConverter.converterApi.CurrencyConverter;
import com.aluminati.inventory.login.authentication.verification.VerificationStatus;

import java.util.ArrayList;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.facebook.FacebookSdk.getApplicationContext;

public class CurrencyChange extends DialogFragment implements AdapterView.OnItemSelectedListener{

    private String title;
    private CompositeDisposable compositeDisposable;
    private ArrayList<Currency> data;
    private Spinner spinner;
    private int[] flags;
    private String[] symbols;
    private Button button;

    public CurrencyChange(String title){
        this.title = title;
    }

    public static CurrencyChange newInstance(String title){
        return new CurrencyChange(title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.language_select, container, false);

        spinner = view.findViewById(R.id.language_selection_spinner);
        spinner.setOnItemSelectedListener(this);

        ((TextView)view.findViewById(R.id.dialog_title)).setText(title);
       button = view.findViewById(R.id.confirm_language_selection);
       button.setOnClickListener(click -> {
                changeCurrency(spinner.getSelectedItemPosition());

        });


        getFlags(new CurrencyConverter(getActivity()));


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
                        data = currencyConverter.toCurrencyArray(currencyResult, VerificationStatus.CURRENCY_SIGN_FILTER);

                        flags = extractFlag(data);
                        symbols = extractSymbol(data);

                        if(flags.length > 0 && symbols.length > 0) {
                            LanguageCustomAdapter languageCustomAdapter = new LanguageCustomAdapter(getApplicationContext(), flags, symbols);
                            spinner.setAdapter(languageCustomAdapter);
                        }


                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.w(CurrencyConverter.class.getName(), "Failed to get currency rates", e);
                    }
                });

    }


    private int[] extractFlag(ArrayList<Currency> data){
        ArrayList<Integer> arrayList = new ArrayList<>();
        for(Currency currency : data){
            arrayList.add(currency.getImage());
        }
        int[] ret = new int[arrayList.size()];
        Log.i("TAG", "Size " + arrayList.size());
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = arrayList.get(i);
        }
        return ret;
    }

    private String[] extractSymbol(ArrayList<Currency> data){
        ArrayList<String> arrayList = new ArrayList<>();
        for(Currency currency : data){
            arrayList.add(currency.getCurrencySymbol());
        }
        String[] ret = new String[arrayList.size()];
        Log.i("TAG", "Size symbols " + arrayList.size());
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = arrayList.get(i);
        }
        return ret;
    }

    private void changeCurrency(int index) {
            SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            ed.putString("currency", data.get(index).getCurrencySymbol());
            ed.apply();
            Utils.makeSnackBar("Currency Changed", button, getActivity());
            new Thread(() -> {
                SystemClock.sleep(1000);
                getActivity().runOnUiThread(this::dismiss);
            }).start();
    }

    @Override
    public void onDestroy() {
        if(compositeDisposable != null && !compositeDisposable.isDisposed()){
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
