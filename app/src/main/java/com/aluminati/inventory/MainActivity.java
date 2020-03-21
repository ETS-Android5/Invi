package com.aluminati.inventory;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.login.authentication.LogInActivity;
import com.aluminati.inventory.login.authentication.password.PassWordReset;
import com.aluminati.inventory.login.authentication.verification.VerificationStatus;
import com.aluminati.inventory.login.authentication.verification.VerifyUser;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.aluminati.inventory.utils.TextLoader;
import com.google.firebase.auth.ActionCodeResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;


public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getName();

    private FirebaseAuth firebaseAuth;
    private ConnectivityCheck connectivityCheck;
    public static AlertDialog alertDialog;
    private static final int ACTION_SETTINGS = 0;
    private TextView infoOffiline;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        firebaseAuth = FirebaseAuth.getInstance();
        infoOffiline = findViewById(R.id.offile_text);
        TextView textView = findViewById(R.id.invi_i);

        connetionInfo();
        connectivityCheck = new ConnectivityCheck(textView, alertDialog);
        connectivityCheck.setConnected(this::onConnected);
        TextLoader textLoader = new TextLoader();
        textLoader.setForeground(textView, getResources().getString(R.string.app_name));


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        if(requestCode == ACTION_SETTINGS){
            Log.i(TAG, "Result code ==> " + resultCode);
        }
    }

    private void onConnected(boolean connected){
        if(connected){
            infoOffiline.setVisibility(View.INVISIBLE);
            listenForDynamicLink();
        }else{
            infoOffiline.setVisibility(View.VISIBLE);
        }
    }

    private void connetionInfo(){
        alertDialog = new AlertDialog.Builder(this)
                .setView(R.layout.offline_dialog)
                .setCancelable(true)
                .setPositiveButton("Ok", (dialog, i) -> dialog.dismiss()
                ).setNegativeButton("Settings", (dialog, i) -> startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), ACTION_SETTINGS)).create();

    }



    private void listenForDynamicLink(){


            if (FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).isComplete()) {
                if (FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).getResult() != null) {

                    FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                            .addOnSuccessListener(this, pendingDynamicLinkData -> {
                                Log.i(TAG, "Dynamic link detected");
                                if (pendingDynamicLinkData != null && pendingDynamicLinkData.getLink() != null) {
                                    String oobCode = pendingDynamicLinkData.getLink().getQueryParameter("oobCode");
                                    if (oobCode != null) {
                                        firebaseAuth.checkActionCode(oobCode).addOnSuccessListener(result -> {
                                            switch (result.getOperation()) {
                                                case ActionCodeResult.VERIFY_EMAIL: {
                                                    firebaseAuth.applyActionCode(oobCode).addOnSuccessListener(resultCode -> {
                                                        Log.i(TAG, "Verified email");
                                                        finish();
                                                    }).addOnFailureListener(resultCode -> Log.w(TAG, "Failed to Verified Email", resultCode));
                                                    break;
                                                }
                                                case ActionCodeResult.PASSWORD_RESET: {
                                                    Intent passWordResetInetemnt = new Intent(MainActivity.this, PassWordReset.class);
                                                    passWordResetInetemnt.putExtra("oobCode", oobCode);
                                                    startActivity(passWordResetInetemnt);
                                                    finish();
                                                    break;
                                                }
                                            }
                                        }).addOnFailureListener(result -> {
                                            Log.w(TAG, "Invalid code sent", result);
                                            if(result instanceof FirebaseAuthActionCodeException){
                                                Toast.makeText(this, "Expired PassWord Reset Link", Toast.LENGTH_LONG).show();
                                            }
                                            finish();
                                        });
                                    }
                                }
                            }).addOnFailureListener(result -> {
                                Log.w(TAG, "Failed to get dynamic link");
                            finish();
                    });
                }
            } else {
                if (firebaseAuth.getCurrentUser() != null) {
                    Log.i(TAG, "User detected");
                    VerifyUser.checkUser(firebaseAuth.getCurrentUser(), this, VerificationStatus.FIREBASE);
                } else {
                    Log.i(TAG, "No User detected");
                    startActivity(new Intent(MainActivity.this, LogInActivity.class));
                    finish();
                }
            }

    }



    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(connectivityCheck, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connectivityCheck);

    }



}
