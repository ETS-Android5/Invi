package com.aluminati.inventory.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aluminati.inventory.R;
import com.bumptech.glide.Glide;

public class DialogHelper {
    private static DialogHelper instance;
    private Context context;

    public interface IClickAction {
        void onAction();
        String actionName();
    }

    public interface OnImageLoad {
        void load();
    }

    private DialogHelper(Context context) {
        this.context = context;
    }

    public static DialogHelper getInstance(Context context) {
        if(instance == null) instance = new DialogHelper(context);

        return instance;
    }

    public interface OnCartUpdate {
        void result(int result);
    }

    public View buildPurchaseView(String title, String message, String imgUrl,int quantity,
                                  OnCartUpdate onCartUpdate, int color) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View v = layoutInflater.inflate(R.layout.dialog_purchaseitem, null);
        ((TextView)v.findViewById(R.id.dialogTitle)).setText(title);
        ((TextView)v.findViewById(R.id.dialogTitle)).setBackgroundColor(color);
        ((TextView)v.findViewById(R.id.dialogMessage)).setText(message);

        ImageView imgV = (ImageView) v.findViewById(R.id.dialogImageHolder);

        TextView qq = v.findViewById(R.id.tvCartItemCount);
        qq.setText("1");

        v.findViewById(R.id.btnCartLess).setOnClickListener(view -> {
            //fdfd
            int q  =Integer.parseInt(((TextView)v.findViewById(R.id.tvCartItemCount)).getText().toString());
            if(q > 1) {
                q--;
                qq.setText("" + q);
                onCartUpdate.result(q);
            }
        });

        v.findViewById(R.id.btnCartMore).setOnClickListener(view -> {
            int q  =Integer.parseInt(((TextView)v.findViewById(R.id.tvCartItemCount)).getText().toString());
            if(q < quantity) {
                q++;
                qq.setText("" + q);
                onCartUpdate.result(q);
            }
        });

        if(imgUrl != null && imgUrl.trim().length() > 0) {
            Glide.with(context).load(imgUrl).into(imgV);
        }


        return v;
    }

    public View buildRentalView(String title, String message, String imgUrl, int color) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View v = layoutInflater.inflate(R.layout.dialog_rentitem, null);
        ((TextView)v.findViewById(R.id.dialogTitle)).setText(title);
        ((TextView)v.findViewById(R.id.dialogTitle)).setBackgroundColor(color);
        ((TextView)v.findViewById(R.id.dialogMessage)).setText(message);
        Glide.with(context).load(imgUrl).into((ImageView) v.findViewById(R.id.dialogImageHolder));

        return v;
    }

    public AlertDialog.Builder createDialog(View v) {
        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setView(v);
        return build;

    }

    public AlertDialog.Builder createDialog(String title, String message, IClickAction action1, IClickAction action2) {
        AlertDialog.Builder build =  new AlertDialog.Builder(context).setTitle(title).setMessage(message);

        if(action1 != null) {
            build.setPositiveButton(action1.actionName(), (di, i) -> {action1.onAction(); di.dismiss();});
        }
        if(action2 != null) {
            build.setNegativeButton(action2.actionName(), (di, i) -> {action2.onAction(); di.dismiss();});
        }

        if(action1 == null && action2 == null) build.setPositiveButton("Close", (di, i) -> { di.dismiss();});

        return build;
    }

}
