package com.aluminati.inventory.fragments.languageSelect;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aluminati.inventory.R;

public class LanguageCustomAdapter extends BaseAdapter {

    private int[] flags;
    private String[] countryNames;
    private LayoutInflater inflter;

    public LanguageCustomAdapter(Context applicationContext, int[] flags, String[] countryNames) {
        this.flags = flags;
        this.countryNames = countryNames;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return flags.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.language_select_spinner_item, null);
        ImageView icon =  view.findViewById(R.id.flagView);
        TextView names = view.findViewById(R.id.language);
        icon.setImageResource(flags[i]);
        names.setText(countryNames[i]);
        return view;
    }
}