package com.aluminati.inventory;

import androidx.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.login.authentication.password.PassWordReset;
import com.google.firebase.auth.ActionCodeResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        firebaseAuth = FirebaseAuth.getInstance();
        listenForDynamicLink();
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
