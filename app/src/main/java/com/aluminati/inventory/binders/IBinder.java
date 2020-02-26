package com.aluminati.inventory.binders;

import android.content.Context;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

public interface IBinder<T> {
    /**
     * Define your views to be used in ViewHolder
     * @param view
     * @return
     */
    View initViews(View view);

    /**
     * Set your data to views you defined in initViews
     * @param item
     * @param context
     * @return
     */
    T bind(T item, Context context);

    /*
     * Required For swipe left
     */
    TableLayout getForeground();
    TableLayout getBackground();

}
