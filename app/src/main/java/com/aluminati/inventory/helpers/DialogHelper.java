package com.aluminati.inventory.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aluminati.inventory.R;
import com.bumptech.glide.Glide;

public class DialogHelper {
    private static DialogHelper instance;
    private Context context;

    private DialogHelper(Context context) {
        this.context = context;
    }

    public static DialogHelper getInstance(Context context) {
        if(instance == null) instance = new DialogHelper(context);

        return instance;
    }

    public AlertDialog.Builder createDialog(String title, String message, String imgUrl, int color) {
        AlertDialog.Builder build = new AlertDialog.Builder(context);

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View v = layoutInflater.inflate(R.layout.dialog_storeitem, null);
        ((TextView)v.findViewById(R.id.dialogTitle)).setText(title);
        ((TextView)v.findViewById(R.id.dialogTitle)).setBackgroundColor(color);
        ((TextView)v.findViewById(R.id.dialogMessage)).setText(message);
        Glide.with(context).load(imgUrl).into((ImageView) v.findViewById(R.id.dialogImageHolder));
        build.setView(v);

        return build;

    }

}
