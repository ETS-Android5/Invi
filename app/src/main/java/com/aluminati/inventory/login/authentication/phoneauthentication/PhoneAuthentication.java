package com.aluminati.inventory.login.authentication.phoneauthentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.aluminati.inventory.InfoPageActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.BaseFragment;
import com.aluminati.inventory.fragments.PhoneAuthenticationFragment;
import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationReciever;
import com.aluminati.inventory.login.authentication.LinkAccounts;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESEncryption;
import com.aluminati.inventory.users.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.TimeUnit;

public class PhoneAuthentication extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "PhoneAuthentication";
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private PhoneAuthProvider.ForceResendingToken phoneAuthToken;
    private PhoneVerificationReciever phoneVerificationReciever;
    private EditText verifyPhoneNumber;
    private CheckBox enablePhoneLogin;
    private String phoneAuthVerificationId;
    private Button verifyPhoneNumberButton;
    private TextView countDownLabel;
    private String phoneNumber;
    private LinearLayout linearLayout;
    private PhoneAuthenticationFragment phoneAuthenticationFragment;
    private boolean registerActivity = false;
    private String email;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_authentication);

        String phoneNumber = getIntent().getStringExtra("phone_number");




        enablePhoneLogin = findViewById(R.id.enable_phone_login);
        verifyPhoneNumberButton = findViewById(R.id.verify_phone_number_button);
        countDownLabel = findViewById(R.id.verification_count_down);
        linearLayout = findViewById(R.id.frag_layout);
        verifyPhoneNumber = createVerifyPhoneNumber();
        verifyPhoneNumberButton.setOnClickListener(this);

        phoneAuthenticationFragment = (PhoneAuthenticationFragment) getSupportFragmentManager().findFragmentById(R.id.phone_authentication);

        bindFragmentToPhone(phoneAuthenticationFragment);

        registerActivity = getIntent().getBooleanExtra("user_reg", false);

        //email = FirebaseAuth.getInstance().getCurrentUser().getEmail();



        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted: Instant verification" + credential);

                if(phoneNumber != null) {
                    startActivity(new Intent(PhoneAuthentication.this, InfoPageActivity.class));
                    finish();
                }else{
                    if (enablePhoneLogin.isChecked()) {
                        try {

                            FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "linkWithCrediential:success");
                                    UserFetch.update(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "is_phone_verified", true);
                                } else {
                                    Log.w(TAG, "linkWithCreditential:failed");
                                }
                            });
                        }catch (NullPointerException e){
                            Log.w("LinkAccounts", "Failed to link accounts", e);
                        }
                    }

                    if (!registerActivity) {
                        startActivity(new Intent(PhoneAuthentication.this, InfoPageActivity.class));
                        finish();
                    } else {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.w(TAG, "Failed ", e);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Log.w(TAG, "Failed ", e);
                }


            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Log.d(TAG, "onCodeSent:" + verificationId);

                phoneAuthToken = token;
                phoneAuthVerificationId = verificationId;

            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }
        };


        if(phoneNumber != null){
            this.verifyPhoneNumber = createVerifyPhoneNumber();
            this.verifyPhoneNumberButton.setText(getResources().getString(R.string.verify_button));
            replaceFragment(linearLayout,verifyPhoneNumber);
            startPhoneVerifiation(phoneNumber);
        }else {
            this.enablePhoneLogin.setVisibility(View.VISIBLE);
        }

    }

    private void checkNumberIsRegistered(String email, String phoneNumber){
        UserFetch.getUser(email).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.i(TAG, "Got User Successfully");
                new PhoneAESEncryption(phoneNumber, (result) -> {
                    Log.i(TAG, "Result Successfully " + result);
                    UserFetch.searchUser().whereEqualTo("phone_number", result).get().addOnCompleteListener(returnedResult -> {
                        if(returnedResult.isSuccessful()) {
                            if (returnedResult.getResult().size() == 1) {
                                Log.i(TAG, "Phone number registered " + returnedResult.getResult().size());
                                Toast.makeText(this, "Phone Number Already registerd", Toast.LENGTH_LONG).show();
                            } else {
                                Log.i(TAG, "Phone number not registered");
                                updateUserPhoneNumber(email, result);
                                replaceFragment(linearLayout, verifyPhoneNumber);
                                startPhoneVerifiation(phoneNumber);
                            }
                        }
                    });
                });
            }
        });
    }

    private void updateUserPhoneNumber(String email,String phoneNumber){
        new PhoneAESEncryption(phoneNumber, (result) -> {
            if(!result.isEmpty()){
                UserFetch.update(email,"phone_number", result);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        registerActivity = (requestCode == 1);
        Log.i(TAG, "Code recieved " + requestCode);
    }

    private void replaceFragment(View view, View replacingView){
        ViewGroup viewGroup = (ViewGroup)view.getParent();
        final int index = viewGroup.indexOfChild(view);
                  viewGroup.removeView(view);
                  viewGroup.addView(replacingView, index);
                  this.verifyPhoneNumberButton.setText(getResources().getString(R.string.verify_button));
    }

    private EditText createVerifyPhoneNumber(){
        return new EditText(this);
    }

    private void startPhoneVerifiation(String phoneNumber){
        Log.i(TAG, "Verification started " + phoneNumber);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                callbacks
        );
        countDown();
        Log.i(TAG, "Verification started " + phoneNumber);
    }

    private void countDown(){
        new CountDownTimer(60 * 1000, 1000){

            @Override
            public void onTick(long l) {
                countDownLabel.setText(Long.toString(l/1000));
            }

            @Override
            public void onFinish() {
                countDownLabel.setText("Resend");
            }
        }.start();
    }

    private void verifyCodeSent(boolean linkPhoneNumber){
        String codeSent = this.verifyPhoneNumber.getText().toString().trim();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(this.phoneAuthVerificationId, codeSent);

        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "signInWithCredential:success");
                if(phoneNumber != null){
                    startActivity(new Intent(PhoneAuthentication.this, InfoPageActivity.class));
                    finish();
                }else {
                    if (linkPhoneNumber) {
                        LinkAccounts.linkAccounts(credential, PhoneAuthentication.this, TAG);
                        UserFetch.update(email, "is_phone_verified", true);
                    }

                    if (!registerActivity) {
                        startActivity(new Intent(PhoneAuthentication.this, InfoPageActivity.class));
                        finish();
                    } else {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                }
            }else{
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.i(TAG, "Verification code entered is invalid");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.verify_phone_number_button){
            if(verifyPhoneNumberButton.getText().toString().equals(getResources().getString(R.string.verify_button))){
                verifyCodeSent(this.enablePhoneLogin.isChecked());
            }else if(verifyPhoneNumberButton.getText().toString().equals(getResources().getString(R.string.send_code))){
                phoneVerificationReciever.onVerificationRecieved(4001);
            }
        }else if(view.getId() == R.id.verification_count_down){
            startPhoneVerifiation(phoneNumber);
        }
    }

    private void onPhoneNumberRecieved(String phoneNumber){
        if(!phoneNumber.isEmpty()){
            this.phoneNumber = phoneNumber;
                checkNumberIsRegistered(FirebaseAuth.getInstance().getCurrentUser().getEmail(), phoneNumber);
        }
    }

    public void bindFragmentToPhone(PhoneAuthenticationFragment fragment){
        fragment.setFragmentPhone((message) -> onPhoneNumberRecieved(message));
    }


    public <T extends Fragment> void setPhoneVerificationReciever(PhoneVerificationReciever<T> phoneVerificationReciever){
        this.phoneVerificationReciever = phoneVerificationReciever;
    }

}
