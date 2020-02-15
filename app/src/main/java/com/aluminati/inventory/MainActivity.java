package com.aluminati.inventory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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


public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private static final String TAG = "MainActivity";
    private ConnectivityCheck connection;
    private TextView registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_login);
            registerButton = findViewById(R.id.register_button);
            registerButton.setOnClickListener(this);
            findViewById(R.id.forgot_password).setOnClickListener(this);


    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.register_button: {
                Intent intent = new Intent(this, RegisterActivity.class);
                       intent.putExtra("login_method", VerificationStatus.EMAIL);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.forgot_password: {
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(connection, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
           VerifyUser.checkUser(FirebaseAuth.getInstance().getCurrentUser(), this, VerificationStatus.FIREBASE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



}
