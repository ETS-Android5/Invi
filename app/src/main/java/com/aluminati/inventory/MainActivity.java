package com.aluminati.inventory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.aluminati.inventory.login.authentication.ForgotPasswordActivity;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.aluminati.inventory.register.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private static final String TAG = "MainActivity";
    private ConnectivityCheck connection;
    private TextView registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            VerifyUser.checkUser(FirebaseAuth.getInstance().getCurrentUser(), this, VerificationStatus.FIREBASE);
        }
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
        connection = new ConnectivityCheck(registerButton.getRootView());
        registerReceiver(connection, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connection);
    }







}
