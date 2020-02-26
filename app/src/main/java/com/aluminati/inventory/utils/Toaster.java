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

    public void toastShort(Object... message) {
        toastShortPadding("", message);
    }

    public void toastShortPadding(String padding, Object... message) {
        Toast.makeText(context, buildString(padding, message), Toast.LENGTH_SHORT).show();
    }

    public void toastLong(Object... message) {
         toastLongPadding("", message);
    }

    public void toastLongPadding(String padding, Object... message) {
        Toast.makeText(context, buildString(padding, message), Toast.LENGTH_LONG).show();
    }

    private String buildString(String padding, Object... message) {
        StringBuilder sb = new StringBuilder();
        for(Object o : message) {
            sb.append(o + padding);
        }

        return sb.toString().trim();
    }
}
