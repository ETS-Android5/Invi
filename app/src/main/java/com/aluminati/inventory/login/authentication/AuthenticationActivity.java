package com.aluminati.inventory.login.authentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aluminati.inventory.InfoPageActivity;
import com.aluminati.inventory.MainActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.aluminati.inventory.users.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;


public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "AuthenticationConfirm";
    private static final int REQUEST_CODE = 1;
    private ActionCodeSettings actionCodeSettings;
    private TextView phoneVerified;
    private TextView emailVerified;
    private Button verifyPhone;
    private Button verifyEmail;
    private Button contineButton;
    private boolean verified = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_authentication_confirmation);

        findViewById(R.id.cancel_verify_button).setOnClickListener(this);


        contineButton = findViewById(R.id.continue_verify_button);
        phoneVerified = findViewById(R.id.phone_verified);
        emailVerified = findViewById(R.id.email_verified);
        verifyPhone = findViewById(R.id.verify_phone);
        verifyEmail = findViewById(R.id.email_verify);

        verifyPhone.setOnClickListener(this);
        verifyEmail.setOnClickListener(this);
        contineButton.setOnClickListener(this);

        UserFetch.getUser(FirebaseAuth.getInstance().getCurrentUser().getEmail()).addOnCompleteListener(task -> {
            if(task.getResult() != null){
                User user = new User(task.getResult());
                     phoneVerified.setText(user.isPhoneVerified() ? getResources().getString(R.string.veirified) : getResources().getString(R.string.not_verified));
                     emailVerified.setText(user.isEmailVerified() ? getResources().getString(R.string.veirified) : getResources().getString(R.string.not_verified));

                     if(user.isPhoneVerified()){
                         verifyPhone.setVisibility(View.INVISIBLE);
                     }

                     if(user.isEmailVerified()){
                         verifyEmail.setVisibility(View.INVISIBLE);
                     }
            }
        });



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if(resultCode == Activity.RESULT_OK){
                this.verified = true;
                verifyPhone.setVisibility(View.INVISIBLE);
                verifyPhone.setText(getResources().getString(R.string.veirified));
            }else if(resultCode == Activity.RESULT_CANCELED){

            }

    }

    private void verifyEmail(){
        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    emailVerified.setText("Email Sent");
                    infoPopUp("Verify Email");
                }
        });
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

    public void infoPopUp(String message){

        new AlertDialog.Builder(this).
                setTitle("Email Sent")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> {

                    dialog.cancel();
                })
                .create()
                .show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.continue_verify_button:{
                UserFetch.getUser(FirebaseAuth.getInstance().getCurrentUser().getEmail()).addOnCompleteListener(task -> {
                    User user = new User(task.getResult());
                    if(user.isPhoneVerified() || verified){
                        startActivity(new Intent(AuthenticationActivity.this, InfoPageActivity.class));
                        finish();
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
                verifyEmail();
                break;
            }

        }
    }
}
