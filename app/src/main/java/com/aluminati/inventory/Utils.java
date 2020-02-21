package com.aluminati.inventory;


import android.app.Activity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;


public class Utils {

    public static void makeSnackBarWithButtons(String message, View view, Activity activity){
        Snackbar snackbar = Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.setAction(activity.getResources().getString(R.string.ok), re -> {
            snackbar.dismiss();
        });
        snackbar.show();
    }

    public static void makeSnackBar(String message, View view, Activity activity){
        Snackbar snackbar = Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_LONG);
        snackbar.show();
    }

    public static void invInfo(Activity activity){
            new AlertDialog
                    .Builder(activity)
                    .setView(R.layout.invinfo)
                    .setPositiveButton(activity.getResources().getText(R.string.ok), ((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }))
                    .create()
                    .show();

    }

}
