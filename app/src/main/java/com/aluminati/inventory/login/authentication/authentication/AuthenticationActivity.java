package com.aluminati.inventory.login.authentication.authentication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aluminati.inventory.Constants;
import com.aluminati.inventory.HomeActivity;
import com.aluminati.inventory.login.authentication.LogInActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.utils.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.aluminati.inventory.login.authentication.verification.VerifyUser;
import com.aluminati.inventory.users.User;
import com.github.mrengineer13.snackbar.SnackBar;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.regex.Pattern;


public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "AuthenticationConfirm";
    private static final int REQUEST_CODE = 1;
    private TextView phoneVerified;
    private TextView emailVerified;
    private Button verifyPhone;
    private Button verifyEmail;
    private Button contineButton;
    private FirebaseAuth firebaseAuth;



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

        firebaseAuth = FirebaseAuth.getInstance();

        UserFetch.getUser(firebaseAuth.getCurrentUser().getEmail()).addOnCompleteListener(task -> {
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
        if(requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Info ===== > Ok ");
                verifyPhone.setVisibility(View.INVISIBLE);
                phoneVerified.setText(getResources().getString(R.string.veirified));
                UserFetch.update(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "is_phone_verified", true);
                Utils.makeSnackBarWithButtons(getResources().getString(R.string.phone_successfully_linked), verifyPhone, this);
            }else if(resultCode == Activity.RESULT_CANCELED){
                Utils.makeSnackBarWithButtons(getResources().getString(R.string.phone_verification_canceled), verifyPhone, this);
            }else if(resultCode == Constants.PHONE_NUMBER_LINKED){
                Utils.makeSnackBarWithButtons(getResources().getString(R.string.phone_already_linked), verifyPhone, this);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseAuth.getCurrentUser() != null) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            user.reload().addOnSuccessListener(task -> {
                Log.i(TAG, "Success to check");
                if (user.isEmailVerified()) {
                    Log.i(TAG, "User successfully email verified");
                    UserFetch.update(user.getEmail(), "is_email_verified", true);
                    emailVerified.setText(getResources().getString(R.string.veirified));
                    verifyEmail.setVisibility(View.INVISIBLE);
                    contineButton.setEnabled(true);
                    if(contains(user)){
                        googleInfo();
                    }
                } else {
                    Log.i(TAG, "Not verified");
                }
            }).addOnFailureListener(result -> Log.w(TAG, "Failed to check verify user", result));
        }
    }

    private boolean contains(FirebaseUser firebaseUser){
        for(UserInfo userInfo : firebaseUser.getProviderData()){
            if(userInfo.getProviderId().equals(Constants.GoogleProviderId)){
                return true;
            }
        }
        return false;
    }

    private void alerDialog(String title, String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setIcon(R.drawable.infoicon)
                .setMessage(message)
                .setPositiveButton("Ok", ((dialogInterface, i) -> dialogInterface.dismiss()))
                .show();
    }

    private void googleInfo(){
        Snackbar.make(verifyEmail, "Email Was Instantly Verified", BaseTransientBottomBar.LENGTH_LONG)
                .setAction(getResources().getString(R.string.info), click -> {
                    alerDialog("Google Instant Verification","Invi uses Googles Firebase, when signing up using Google your email will be instantly verified");
                }).show();
    }

    private void authenticationInfo(){
        Snackbar.make(verifyEmail, "Email And Phone Verification", BaseTransientBottomBar.LENGTH_INDEFINITE)
                .setAction(getResources().getString(R.string.info), click -> {
                    alerDialog("Email And Phone Verification","Phone and Email needs to be verified to use Invi");
                }).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.continue_verify_button:{
                UserFetch.getUser(FirebaseAuth.getInstance().getCurrentUser().getEmail()).addOnSuccessListener(task -> {
                    if (task.exists()) {
                        User user = new User(task);
                        if (user.isPhoneVerified() && user.isEmailVerified()) {
                            Intent intent = new Intent(AuthenticationActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }else{
                            authenticationInfo();
                        }
                    }
                }).addOnFailureListener(task -> Log.w(TAG, "Failed to get user", task));

                break;
            }
            case R.id.cancel_verify_button:{
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AuthenticationActivity.this, LogInActivity.class));
                finish();
                break;
            }
            case R.id.verify_phone: {
                startActivityForResult(new Intent(AuthenticationActivity.this, PhoneAuthentication.class),REQUEST_CODE);
                break;
            }
            case R.id.email_verify:{
                VerifyUser.verifyEmail().addOnCompleteListener(result -> {
                    if(result.isSuccessful()){
                        Utils.makeSnackBarWithButtons("Email Sent", emailVerified, this);
                    }else{
                        Utils.makeSnackBarWithButtons("Failed to Send Email", emailVerified, this);
                    }
                });
                break;
            }
            case R.id.info_icon_button:{
                alerDialog("Email And Phone Verification","Phone and Email needs to be verified to use Invi");
                break;
            }

        }
    }


}
