package com.aluminati.inventory.utils;


import android.app.Activity;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AlertDialog;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.rental.RentalItem;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import java.util.Calendar;
import java.util.Date;


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
                    .setPositiveButton(activity.getResources().getText(R.string.ok), ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .create()
                    .show();

    }

    public static double getRentalCharge(RentalItem item) {

        if(item.getCheckedOutDate() == null) return 0;

        Date now = Calendar.getInstance().getTime();
        Log.d("Utils--->", item.getPrice() + " " + item.getUnitType() );
        double charge = item.getPrice();
        long diff = now.getTime() - item.getCheckedOutDate().getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000);
        long diffHours = diff / (60 * 60 * 1000) % 24;
        switch (item.getUnitType()) {
            case "Hour":
                long dayHrs = diffDays * 24;
                return charge +(charge * (diffHours + dayHrs));

            case "Day":

                return charge + (charge * diffDays);
        }


        return charge;
    }

}
