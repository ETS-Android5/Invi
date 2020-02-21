package com.aluminati.inventory.login.authentication.phoneauthentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.PhoneAuthenticationFragment;
import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationReciever;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESEncryption;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PhoneAuthentication extends AppCompatActivity implements View.OnClickListener{

        private static final String TAG = "PhoneAuthentication";
        private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
        private PhoneAuthProvider.ForceResendingToken phoneAuthToken;
        private PhoneVerificationReciever phoneVerificationReciever;
        private PhoneAuthenticationFragment phoneAuthenticationFragment;
        private EditText verifyPhoneNumber;
        private CheckBox enablePhoneLogin;
        private String phoneAuthVerificationId;
        private Button verifyPhoneNumberButton;
        private TextView countDownLabel;
        private String phoneNumber;
        private LinearLayout linearLayout;
        private CountDownTimer countDownTimer;


        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.phone_authentication);

            try {
                phoneNumber = Objects.requireNonNull(getIntent().getExtras()).getString("phone_number");
            }catch (NullPointerException e){
                Log.w(TAG, "Null Phone number", e);
            }

            enablePhoneLogin = findViewById(R.id.enable_phone_login);
            verifyPhoneNumberButton = findViewById(R.id.verify_phone_number_button);
            countDownLabel = findViewById(R.id.verification_count_down);
            linearLayout = findViewById(R.id.frag_layout);
            verifyPhoneNumber = createVerifyPhoneNumber();
            verifyPhoneNumberButton.setOnClickListener(this);
            findViewById(R.id.phone_verification_cancel).setOnClickListener(this);

            phoneAuthenticationFragment = (PhoneAuthenticationFragment) getSupportFragmentManager().findFragmentById(R.id.phone_authentication);
            bindFragmentToPhone(phoneAuthenticationFragment);
            countDown();

            callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    Log.d(TAG, "onVerificationCompleted: Instant verification" + credential);
                    verifyPhoneNumber.setText(credential.getSmsCode());
                    new Thread(() -> {
                        SystemClock.sleep(3000);
                        runOnUiThread(() -> {
                            if (getCallingActivity() != null) {
                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                    if (enablePhoneLogin.isChecked()) {
                                        linkAccounts(credential);
                                    } else {
                                        verifyCodeSent(enablePhoneLogin.isChecked(), credential.getSmsCode(), credential);
                                    }
                                } else {
                                    verifyCodeSent(enablePhoneLogin.isChecked(), credential.getSmsCode(), credential);
                                }
                            }
                        });
                    }).start();
                }




                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Log.w(TAG, "onVerificationFailed", e);

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        Log.w(TAG, "Failed ", e);
                        setResult(VerificationStatus.INCCORECT_PHONE_NUMBER);
                        finish();
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        Log.w(TAG, "Failed ", e);
                        setResult(VerificationStatus.TOO_MANY_REQUESTS);
                        finish();
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


        @Override
        protected void onStart() {
            super.onStart();

        }


    private void checkNumberIsRegistered(String email, String phoneNumber) {
            UserFetch.getUser(email).addOnSuccessListener(task -> {
                Log.i(TAG, "Got User Successfully");
                new PhoneAESEncryption(phoneNumber, (result) -> {
                    Log.i(TAG, "Result Successfully " + result);
                    UserFetch.searchUser().whereEqualTo("phone_number", result).get().addOnSuccessListener(returnedResult -> {
                        if (!returnedResult.isEmpty()) {
                            Log.i(TAG, "Phone number registered ");
                            Utils.makeSnackBar(getResources().getString(R.string.phone_already_registered), verifyPhoneNumberButton, this);
                        } else {
                            Log.i(TAG, "Phone number not registered");
                            updateUserPhoneNumber(email, result);
                            replaceFragment(linearLayout, verifyPhoneNumber);
                            startPhoneVerifiation(phoneNumber);
                        }
                    }).addOnFailureListener(resultFaied -> Log.w(TAG, "Failed to get User from firestore", resultFaied));
                });
            }).addOnFailureListener(result -> Log.w(TAG, "Couldn't get user form firestore", result));
        }

        private void updateUserPhoneNumber(String email,String phoneNumber){
            UserFetch.update(email,"phone_number",phoneNumber);
        }


        private void replaceFragment(View view, View replacingView){
            ViewGroup viewGroup = (ViewGroup)view.getParent();
            final int index = viewGroup.indexOfChild(view);
            replacingView.setLayoutParams(view.getLayoutParams());
            viewGroup.removeView(view);
                      viewGroup.addView(replacingView, index);
                      this.verifyPhoneNumberButton.setText(getResources().getString(R.string.verify_button));
        }

        private EditText createVerifyPhoneNumber(){

            EditText editText = new EditText(this);
                     editText.setHint(getResources().getString(R.string.verify_phone_number));
                     editText.setTextColor(getResources().getColor(R.color.text_color));
                     editText.setTextSize(20);
                     editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                     editText.setGravity(Gravity.BOTTOM);
            return editText;
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
            this.countDownTimer.start();
            Log.i(TAG, "Verification started " + phoneNumber);
        }

        private void countDown(){
            countDownLabel.setVisibility(View.VISIBLE);
            this.countDownTimer = new CountDownTimer(60 * 1000, 1000){

                @Override
                public void onTick(long l) {
                    countDownLabel.setText(Long.toString(l/1000));
                }

                @Override
                public void onFinish() {
                    countDownLabel.setText("Resend");
                }
            };

        }


        private void verifyCodeSent(boolean linkPhoneNumber, String codeSent, PhoneAuthCredential cred) {


            String code = (codeSent == null ? verifyPhoneNumber.getText().toString() : codeSent);
            PhoneAuthCredential credential = (codeSent == null ? cred : PhoneAuthProvider.getCredential(phoneAuthVerificationId, code));

            Log.i(TAG, "code sent" + code);

            verifyPhoneNumber.setText(codeSent);

            if(!verifyPhoneNumber.getText().toString().isEmpty()){
                if(verifyPhoneNumber.getText().toString().equals(codeSent)){
                    checkCreditential(linkPhoneNumber,credential);
                }else{
                    Snackbar snackbar = Snackbar.make(verifyPhoneNumberButton, getResources().getString(R.string.failed_to_verify_code), BaseTransientBottomBar.LENGTH_INDEFINITE);
                    snackbar.setAction(getResources().getString(R.string.ok), re -> {
                        snackbar.dismiss();
                    });
                    snackbar.show();
                }
            }else{
                checkCreditential(linkPhoneNumber,credential);
            }



        }

        private void checkCreditential(boolean linkPhoneNumber, AuthCredential credential){
            if (getCallingActivity() != null) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    if (linkPhoneNumber) {
                        linkAccounts(credential);
                    }
                } else {
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnSuccessListener(result -> {
                                Log.d(TAG, "signInWithCredential:success");
                                setResult(Activity.RESULT_OK, new Intent());
                                finish();
                            })
                            .addOnFailureListener(result -> {
                                Log.w(TAG, "Failed to login", result);
                                setResult(Activity.RESULT_CANCELED, new Intent());
                                finish();
                            });
                }
            }
        }


        private void cancel(){
            if(findViewById(R.id.phone_authentication) == null){
                if(countDownTimer != null){
                    countDownTimer.cancel();
                }
                replaceFragment(verifyPhoneNumber,linearLayout);
            }

            if(getCallingActivity() != null){
                setResult(Activity.RESULT_CANCELED, new Intent());
                finish();
            }

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.verify_phone_number_button:{
                    if(verifyPhoneNumberButton.getText().toString().equals(getResources().getString(R.string.verify_button))){
                        verifyCodeSent(this.enablePhoneLogin.isChecked(), null, null);
                    }else if(verifyPhoneNumberButton.getText().toString().equals(getResources().getString(R.string.send_code))){
                        phoneVerificationReciever.onVerificationRecieved(4001);
                    }
                    break;
                }
                case R.id.verification_count_down: {
                    startPhoneVerifiation(phoneNumber);
                    break;
                }
                case R.id.phone_verification_cancel:{
                    cancel();
                    break;
                }
            }

        }

    public void linkAccounts(AuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnSuccessListener(result -> {
                            Log.d(TAG, "linkWithCrediential:success");
                            UserFetch.update(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "is_phone_verified", true);
                            setResult(Activity.RESULT_OK, new Intent());
                            finish();
                    })
                    .addOnFailureListener(result -> {
                        Log.w(TAG, "linkWithCreditential:failed", result);
                        setResult(Activity.RESULT_CANCELED, new Intent());
                        finish();
                    });
        }

    }


    private void onPhoneNumberRecieved(String phoneNumber){
            if(!phoneNumber.isEmpty()){
                this.phoneNumber = phoneNumber;
                    checkNumberIsRegistered(FirebaseAuth.getInstance().getCurrentUser().getEmail(), phoneNumber);
            }
        }

        public void bindFragmentToPhone(PhoneAuthenticationFragment fragment){
            fragment.setFragmentPhone(this::onPhoneNumberRecieved);
        }


        public <T extends Fragment> void setPhoneVerificationReciever(PhoneVerificationReciever<T> phoneVerificationReciever){
            this.phoneVerificationReciever = phoneVerificationReciever;
        }

}
