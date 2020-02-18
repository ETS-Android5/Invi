package com.aluminati.inventory.utils;

import android.content.Context;
import android.widget.Toast;

public class Toaster {
    private final Context context;
    private static Toaster instance;

    private Toaster(Context context) {
        this.context = context;
    }

    public static Toaster getInstance(Context context) {

        if(instance == null) instance = new Toaster(context);

        return instance;
    }

    public void toastShort(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void toastLong(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
