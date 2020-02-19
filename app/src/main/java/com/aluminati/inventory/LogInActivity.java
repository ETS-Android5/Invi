package com.aluminati.inventory;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.login.authentication.ForgotPasswordActivity;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.aluminati.inventory.register.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = LogInActivity.class.getName();
    public static LogInActivity logInActivity;
    private ConnectivityCheck connection;
    private TextView registerButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(this);
        findViewById(R.id.forgot_password).setOnClickListener(this);

        logInActivity = this;

       // connection = new ConnectivityCheck(registerButton);
       // this.registerReceiver(connection, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        firebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.register_button: {
                Intent intent = new Intent(this, RegisterActivity.class);
                intent.putExtra("login_method", VerificationStatus.EMAIL);
                startActivity(intent);
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
        if(firebaseAuth.getCurrentUser() != null){
            firebaseAuth.getCurrentUser().reload().addOnSuccessListener(result -> {
                Log.i(TAG, "User reloaded");
            }).addOnFailureListener(result -> Log.w(TAG, "Failed to reload user", result));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
  //      unregisterReceiver(connection);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        unregisterReceiver(connection);
    }
}
