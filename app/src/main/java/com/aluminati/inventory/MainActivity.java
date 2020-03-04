package com.aluminati.inventory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aluminati.inventory.login.authentication.LogInActivity;
import com.aluminati.inventory.login.authentication.verification.VerificationStatus;
import com.aluminati.inventory.login.authentication.verification.VerifyUser;
import com.aluminati.inventory.login.authentication.password.PassWordReset;
import com.aluminati.inventory.widgets.MagicTextView;
import com.google.firebase.auth.ActionCodeResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getName();

    private FirebaseAuth firebaseAuth;
    private Handler handler;
    private long startTime, currentTime, finishedTime = 0L;
    private int endTime = 0;
    private boolean lock = false;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        firebaseAuth = FirebaseAuth.getInstance();
        setForeground();
        listenForDynamicLink();
    }



    private void setForeground(){


        textView = findViewById(R.id.invi_i);
        textView.setText(getResources().getString(R.string.app_name));
        handler = new Handler();
        startTime = System.currentTimeMillis();
        currentTime = startTime;


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                currentTime = System.currentTimeMillis();
                finishedTime = currentTime - startTime;

                if(lock){
                    endTime = (int) ((finishedTime / 250));// divide this by
                    if(endTime == textView.getText().toString().length()){
                        startTime = System.currentTimeMillis();
                        lock = false;
                    }
                    Spannable spannableString = new SpannableString(textView.getText());
                    spannableString.setSpan(new ForegroundColorSpan(Color.GRAY), 0, endTime, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(spannableString);
                    handler.postDelayed(this, 250);

                }else{
                    endTime = (int) ((finishedTime / 250));// divide this by
                    if(endTime == textView.getText().toString().length()){
                        startTime = System.currentTimeMillis();
                        lock = true;
                    }
                    Spannable spannableString = new SpannableString(textView.getText());
                    spannableString.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, endTime, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(spannableString);
                    handler.postDelayed(this, 250);

                }


            }
        }, 1000);
    }

    private void listenForDynamicLink(){


        if(FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).isComplete()){
            if(FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).getResult() != null) {

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
                                        Log.w(TAG, "Invalid code sent");
                                        finish();
                                    });
                                }
                            }
                        }).addOnFailureListener(result -> {
                            Log.w(TAG, "Failed to get dynamic link");
                            finish();
                });
            }
        }else{
            if(firebaseAuth.getCurrentUser() != null){
                Log.i(TAG, "User detected");
                VerifyUser.checkUser(firebaseAuth.getCurrentUser(), this, VerificationStatus.FIREBASE);
            }else{
                Log.i(TAG, "No User detected");
                startActivity(new Intent(MainActivity.this, LogInActivity.class));
                finish();
            }
        }

    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



}
