package com.aluminati.inventory.login.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;

public class LinkAccounts {

    public static void linkAccounts(AuthCredential credential,Activity activity, String tag){
        try {
            FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential).addOnCompleteListener(activity, task -> {
                if (task.isSuccessful()) {
                    Log.d(tag, "linkWithCrediential:success");
                } else {
                    Log.w(tag, "linkWithCreditential:failed");
                }
            });
        }catch (NullPointerException e){
            Log.w("LinkAccounts", "Failed to link accounts", e);
        }
    }

    public static void linkAccountsInfo(Context context,String message){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            alertDialogBuilder.setTitle("Login Failed");

            alertDialogBuilder
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, id) -> {
                        dialog.cancel();
                    })
                    .setNegativeButton("Recover Password", (dialog, id) -> {
                        context.startActivity(new Intent(context, ForgotPasswordActivity.class));
                    });

        alertDialogBuilder.create().show();

    }
}
