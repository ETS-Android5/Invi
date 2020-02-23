package com.aluminati.inventory.login.authentication;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.fragments.languageSelect.LanguageSelection;
import com.aluminati.inventory.login.authentication.forgotPassWord.ForgotPasswordActivity;
import com.aluminati.inventory.login.authentication.verification.VerificationStatus;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.aluminati.inventory.register.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = LogInActivity.class.getName();
    private static final int ACTION_SETTINGS = 0;
    public static LogInActivity logInActivity;
    private ConnectivityCheck connection;
    private TextView registerButton;
    private FirebaseAuth firebaseAuth;
    public static AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(this);
        findViewById(R.id.langauge_select).setOnClickListener(this);
        findViewById(R.id.forgot_password).setOnClickListener(this);
        findViewById(R.id.invi_info_login_page).setOnClickListener(this);
        ((TextView)findViewById(R.id.invi_info_login_page)).setText(getResources().getString(R.string.app_name).concat(" " + getYear()).concat(" ® | "));

         logInActivity = this;
            connetionInfo();

         connection = new ConnectivityCheck(registerButton);
         registerReceiver(connection, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

         firebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.register_button: {
                if(!ConnectivityCheck.isSnacBarVisible()) {
                    Intent intent = new Intent(this, RegisterActivity.class);
                    intent.putExtra("login_method", VerificationStatus.EMAIL);
                    startActivity(intent);
                }
                break;
            }
            case R.id.forgot_password: {
                if (!ConnectivityCheck.isSnacBarVisible()) {
                    startActivity(new Intent(this, ForgotPasswordActivity.class));
                }
                break;
            }
            case R.id.langauge_select:{
                LanguageSelection languageSelection = LanguageSelection.newInstance("Select Language");
                languageSelection.show(getSupportFragmentManager(), "language_select_frag");
                break;
            }
            case R.id.invi_info_login_page:{
                Utils.invInfo(this);
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
        if(requestCode == ACTION_SETTINGS){
            Log.i(TAG, "Result code ==> " + resultCode);
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
        registerReceiver(connection, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void connetionInfo(){
        this.alertDialog = new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("INVI Needs an Active Internet Connection\n\nCheck your Internet Settings")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .setPositiveButton("Ok", (dialog, i) -> {
                            dialog.dismiss();
                        }
                ).setNegativeButton("Settings", (dialog, i) -> {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), ACTION_SETTINGS);
                }).create();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connection);
    }

    private String getYear(){
        return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
    }

}
