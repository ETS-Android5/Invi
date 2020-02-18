package com.aluminati.inventory.login.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.firestore.UserFetch;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;

public class BaseFragment extends Fragment {

    protected FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }


    protected AlertDialog.Builder alertDialog(Context context, String title, String message){

       return new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false);
    }


}
