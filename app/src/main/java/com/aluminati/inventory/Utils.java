package com.aluminati.inventory;


import android.app.Activity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;


public class Utils {

    public static void makeSnackBar(String message, View view, Activity activity){
        Snackbar snackbar = Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.setAction(activity.getResources().getString(R.string.ok), re -> {
            snackbar.dismiss();
        });
        snackbar.show();
    }

}
