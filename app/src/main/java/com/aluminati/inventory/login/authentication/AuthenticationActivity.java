package com.aluminati.inventory.login.authentication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aluminati.inventory.MainActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.aluminati.inventory.userprofile.UserProfile;
import com.aluminati.inventory.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "AuthenticationConfirm";
    private static final int REQUEST_CODE = 1;
    private TextView phoneVerified;
    private TextView emailVerified;
    private Button verifyPhone;
    private Button verifyEmail;
    private Button contineButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_authentication_confirmation);

        findViewById(R.id.cancel_verify_button).setOnClickListener(this);
        findViewById(R.id.info_icon_button).setOnClickListener(this);


        contineButton = findViewById(R.id.continue_verify_button);
        phoneVerified = findViewById(R.id.phone_verified);
        emailVerified = findViewById(R.id.email_verified);
        verifyPhone = findViewById(R.id.verify_phone);
        verifyEmail = findViewById(R.id.email_verify);

        verifyPhone.setOnClickListener(this);
        verifyEmail.setOnClickListener(this);
        contineButton.setOnClickListener(this);

        UserFetch.getUser(FirebaseAuth.getInstance().getCurrentUser().getEmail()).addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                User user = new User(task.getResult());
                phoneVerified.setText(user.isPhoneVerified() ? getResources().getString(R.string.veirified) : getResources().getString(R.string.not_verified));
                emailVerified.setText(user.isEmailVerified() ? getResources().getString(R.string.veirified) : getResources().getString(R.string.not_verified));

                if (user.isPhoneVerified()) {
                    verifyPhone.setVisibility(View.INVISIBLE);
                }

                if (user.isEmailVerified()) {
                    verifyEmail.setVisibility(View.INVISIBLE);
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if(resultCode == Activity.RESULT_OK){
                verifyPhone.setVisibility(View.INVISIBLE);
                phoneVerified.setText(getResources().getString(R.string.veirified));
                UserFetch.update(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "is_phone_verified", true);
                Utils.makeSnackBarWithButtons(getResources().getString(R.string.phone_successfully_linked), verifyPhone, this);
            }
    }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.reload().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(user.isEmailVerified()){
                    Log.i(TAG, "User successfully email verified");
                    UserFetch.update(user.getEmail(), "is_email_verified", true);
                    emailVerified.setText(getResources().getString(R.string.veirified));
                    verifyEmail.setVisibility(View.INVISIBLE);
                    contineButton.setEnabled(true);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.continue_verify_button:{
                UserFetch.getUser(FirebaseAuth.getInstance().getCurrentUser().getEmail()).addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        User user = new User(task.getResult());
                        if (user.isPhoneVerified() && user.isEmailVerified()) {
                            startActivity(new Intent(AuthenticationActivity.this, UserProfile.class));
                            finish();
                        }
                    }
                });

                break;
            }
            case R.id.cancel_verify_button:{
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
                finish();
                break;
            }
            case R.id.verify_phone: {
                startActivityForResult(new Intent(AuthenticationActivity.this, PhoneAuthentication.class), REQUEST_CODE);
                break;
            }
            case R.id.email_verify:{
                VerifyUser.verifyEmail().addOnCompleteListener(result -> {
                    if(result.isSuccessful()){
                        Utils.makeSnackBarWithButtons("Email Sent", emailVerified, this);
                        emailVerified.setText(getResources().getString(R.string.veirified));
                    }else{
                        Utils.makeSnackBarWithButtons("Failed to Send Email", emailVerified, this);
                    }
                });
                break;
            }
            case R.id.info_icon_button:{
                new AlertDialog.Builder(this)
                        .setTitle("Verification")
                        .setMessage("Email and Phone needs to be verified")
                        .setPositiveButton(getResources().getText(R.string.ok), (dialog,id) -> {
                            dialog.cancel();
                        }).show();

                break;
            }

        }
    }
}
