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
import com.aluminati.inventory.HomeActivity;
import com.aluminati.inventory.LogInActivity;
import com.aluminati.inventory.MainActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
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

        userEmailVerified();
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
            }
        }
    }

    private void userEmailVerified(){
        if(firebaseAuth.getCurrentUser() != null) {
           firebaseAuth.addAuthStateListener(firebaseAuth -> {
                if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                    Log.i(TAG, "Verified");
                } else {
                    Log.i(TAG, "Still not verified");
                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.reload().addOnSuccessListener(task -> {
            Log.i(TAG, "Success to check");
                if(user.isEmailVerified()){
                    Log.i(TAG, "User successfully email verified");
                    UserFetch.update(user.getEmail(), "is_email_verified", true);
                    emailVerified.setText(getResources().getString(R.string.veirified));
                    verifyEmail.setVisibility(View.INVISIBLE);
                    contineButton.setEnabled(true);
                }else{
                    Log.i(TAG, "Not verifired");
                }
        }).addOnFailureListener(result -> Log.w(TAG, "Failed to check verifiy user", result));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.continue_verify_button:{
                UserFetch.getUser(FirebaseAuth.getInstance().getCurrentUser().getEmail()).addOnSuccessListener(task -> {
                    if (task.exists()) {
                        User user = new User(task);
                        if (user.isPhoneVerified() && user.isEmailVerified()) {
                            LogInActivity.logInActivity.finish();
                            startActivity(new Intent(AuthenticationActivity.this, HomeActivity.class));
                            finish();
                        }
                    }
                }).addOnFailureListener(task -> Log.w(TAG, "Failed to get user", task));

                break;
            }
            case R.id.cancel_verify_button:{
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
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
