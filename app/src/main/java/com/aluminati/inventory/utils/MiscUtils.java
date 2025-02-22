package com.aluminati.inventory.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.aluminati.inventory.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiscUtils {

    private MiscUtils() {}


    public static void setViewsState(List<View> views, int state) {
        for(View v : views) v.setVisibility(state);
    }

    public static void makeSnackBarWithButtons(String message, View view, Context context){
        Snackbar snackbar = Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.setAction(context.getResources().getString(R.string.ok), re -> {
            snackbar.dismiss();
        });
        snackbar.show();
    }

    public static void makeSnackBar(String message, View view, Context context){
        Snackbar snackbar = Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_LONG);
        snackbar.show();
    }




}
