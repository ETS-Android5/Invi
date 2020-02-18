package com.aluminati.inventory;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.login.authentication.ForgotPasswordActivity;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.aluminati.inventory.register.RegisterActivity;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = LogInActivity.class.getName();
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
        //connection = new ConnectivityCheck(registerButton);
        //registerReceiver(connection, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(connection);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unregisterReceiver(connection);
    }
}
