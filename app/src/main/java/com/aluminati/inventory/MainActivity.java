package com.aluminati.inventory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationReciever;
import com.aluminati.inventory.login.authentication.ForgotPasswordActivity;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.login.authentication.facebook.FaceBookSignIn;
import com.aluminati.inventory.login.authentication.google.GoogleSignIn;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.aluminati.inventory.register.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Log.i(TAG, "User detected");
            VerifyUser.checkUser(FirebaseAuth.getInstance().getCurrentUser(), this, VerificationStatus.FIREBASE);
        }else{
            Log.i(TAG, "No User detected");
            startActivity(new Intent(MainActivity.this, LogInActivity.class));
            finish();
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
