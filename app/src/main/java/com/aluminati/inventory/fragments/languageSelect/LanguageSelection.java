package com.aluminati.inventory.fragments.languageSelect;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aluminati.inventory.R;

import java.util.Locale;

import static com.facebook.FacebookSdk.getApplicationContext;

public class LanguageSelection extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = LanguageSelection.class.getName();
    private String[] languages={"English","Polish","French"};
    int flags[] = {R.drawable.flag_united_kingdom,R.drawable.flag_poland,  R.drawable.flag_france};


    public LanguageSelection(){

    }

    public static LanguageSelection newInstance(String title) {
        LanguageSelection languageSelection = new LanguageSelection();
        Bundle args = new Bundle();
        args.putString("title", title);
        languageSelection.setArguments(args);
        return languageSelection;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(getResources().getLayout(R.layout.language_select), container ,false);


        Spinner spinner = view.findViewById(R.id.language_selection_spinner);
                spinner.setOnItemSelectedListener(this);

        LanguageCustomAdapter languageCustomAdapter = new LanguageCustomAdapter(getApplicationContext(), flags, languages);
        spinner.setAdapter(languageCustomAdapter);
        return view;
    }


    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        changeLang(languages[position]);
        Toast.makeText(getApplicationContext(), languages[position], Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }



    public void changeLang(String lang) {
        Configuration config = getActivity().getBaseContext().getResources().getConfiguration();
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {

            SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            ed.putString("local_lang", lang);
            ed.commit();

            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration conf = new Configuration(config);
            conf.locale = locale;
            getActivity().getBaseContext().getResources().updateConfiguration(conf, getActivity().getBaseContext().getResources().getDisplayMetrics());
        }
    }



}
