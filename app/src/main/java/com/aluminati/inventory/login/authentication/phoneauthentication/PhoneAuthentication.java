package com.aluminati.inventory.login.authentication.phoneauthentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.utils.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationReciever;
import com.aluminati.inventory.login.authentication.verification.VerificationStatus;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESEncryption;
import com.aluminati.inventory.users.User;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
        private TextView errorLabel;
        private String phoneNumber;
        private RelativeLayout linearLayout;


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
            errorLabel = findViewById(R.id.error_label);
            findViewById(R.id.phone_verification_cancel).setOnClickListener(this);

            phoneAuthenticationFragment = (PhoneAuthenticationFragment) getSupportFragmentManager().findFragmentById(R.id.phone_authentication);
            bindFragmentToPhone(phoneAuthenticationFragment);

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
                                    Toast.makeText(getApplicationContext(), "Phone recognised : Instant Log In", Toast.LENGTH_LONG).show();
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
                        //replaceFragment(linearLayout, verifyPhoneNumber);
                        //verifyPhoneNumberButton.setText(getResources().getString(R.string.verify_button));
                        Snackbar.make(verifyPhoneNumberButton,"Incorrect Phone Number", BaseTransientBottomBar.LENGTH_LONG).show();
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        Log.w(TAG, "Failed ", e);
                        setResult(VerificationStatus.TOO_MANY_REQUESTS);
                        finish();
                    };


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
                this.verifyPhoneNumberButton.setText(getResources().getString(R.string.send_code));
                startPhoneVerifiation(phoneNumber);
            }else {
                this.enablePhoneLogin.setVisibility(View.VISIBLE);
            }



        }

    private void checkNumberIsRegistered(String email, String phoneNumber) {
            UserFetch.getUser(email).addOnSuccessListener(task -> {
                Log.i(TAG, "Got User Successfully");
                new PhoneAESEncryption(phoneNumber, (result) -> {
                    Log.i(TAG, "Result Successfully " + result);
                    UserFetch.searchUser().whereEqualTo("phone_number", result).get().addOnSuccessListener(returnedResult -> {
                        if (!returnedResult.isEmpty()) {
                            returnedResult.getDocuments();
                            if(checkIfNumberIsVerified(returnedResult, email, result)){
                                Log.i(TAG, "Phone number not verified");
                                updateUserPhoneNumber(email, result);
                                replaceFragment(linearLayout, verifyPhoneNumber);
                                startPhoneVerifiation(phoneNumber);
                            }else {
                                Utils.makeSnackBar(getResources().getString(R.string.phone_already_registered), verifyPhoneNumberButton, this);
                            }
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

        private boolean checkIfNumberIsVerified(QuerySnapshot queryDocumentSnapshots, String email, String result){
            boolean verified = false;
            for(DocumentSnapshot documentSnapshot :queryDocumentSnapshots.getDocuments()){
                if(documentSnapshot.get("user_email").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                    User user = new User(documentSnapshot);
                    if(!user.isPhoneVerified()){
                        verified = true;
                    }
                }
            }
            return verified;
        }


        private void replaceFragment(View view, View replacingView){
            ViewGroup viewGroup = (ViewGroup)view.getParent();
            final int index = viewGroup.indexOfChild(view);
            replacingView.setLayoutParams(view.getLayoutParams());
            viewGroup.removeView(view);
                      viewGroup.addView(replacingView, index);

                      findViewById(R.id.no_code_label).setVisibility(View.VISIBLE);
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


            verifyPhoneNumberButton.setText(getResources().getString(R.string.verify_button));

            if(!errorLabel.getText().toString().isEmpty()){
                errorLabel.setText("");
            }

            countDown();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    this,
                    callbacks
            );
            Utils.makeSnackBarWithButtons("Code Sent", verifyPhoneNumberButton, this);
        }

        private void countDown(){
            countDownLabel.setVisibility(View.VISIBLE);

            countDownLabel.setOnClickListener(null);
            new CountDownTimer(60000, 1000) {

                @Override
                public void onTick(long l) {
                    Log.i(TAG, "time " + l / 1000);
                    countDownLabel.setText(Long.toString(l / 1000));
                }

                @Override
                public void onFinish() {
                    countDownLabel.setText(getResources().getString(R.string.resend));
                    countDownLabel.setOnClickListener(click -> startPhoneVerifiation(phoneNumber));
                }
            }.start();



        }


        private void verifyCodeSent(boolean linkPhoneNumber, String codeSent, PhoneAuthCredential cred) {


            String code = (codeSent == null ? verifyPhoneNumber.getText().toString() : codeSent);
            PhoneAuthCredential credential = (codeSent == null ? cred : PhoneAuthProvider.getCredential(phoneAuthVerificationId, code));

            Log.i(TAG, "code sent" + code);


            if(!code.isEmpty()) {
                verifyPhoneNumber.setText(codeSent);
            }

            Log.i(TAG, "Code sent " + codeSent );

                if(verifyPhoneNumber.getText().toString().equals(codeSent)){
                    checkCreditential(linkPhoneNumber,credential);
                }else{
                    Snackbar snackbar = Snackbar.make(verifyPhoneNumberButton, getResources().getString(R.string.failed_to_verify_code), BaseTransientBottomBar.LENGTH_INDEFINITE);
                    snackbar.setAction(getResources().getString(R.string.ok), re -> {
                        snackbar.dismiss();
                    });
                    snackbar.show();
                }
        }

        private void checkCreditential(boolean linkPhoneNumber, AuthCredential credential){
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


        private void cancel(){
                setResult(Activity.RESULT_CANCELED, new Intent());
                finish();
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.verify_phone_number_button:{
                    if(verifyPhoneNumberButton.getText().toString().equals(getResources().getString(R.string.verify_button))){
                        Log.i(TAG, "Phone number " + verifyPhoneNumberButton.getText().toString());
                        if(!verifyPhoneNumber.getText().toString().isEmpty()) {
                            if(codeMatch(verifyPhoneNumber.getText().toString())) {
                                verifyCodeSent(this.enablePhoneLogin.isChecked(), null, null);
                            }else{
                                errorLabel.setText(R.string.inccorect_code);
                            }
                        }else{
                            errorLabel.setText(R.string.enter_ver_code);
                        }
                    }else if(verifyPhoneNumberButton.getText().toString().equals(getResources().getString(R.string.send_code))){
                        phoneVerificationReciever.onVerificationRecieved(4001);
                    }
                    break;
                }
                case R.id.phone_verification_cancel:{
                    cancel();
                    break;
                }
            }

        }

    public void linkAccounts(AuthCredential credential) {
            FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential)
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

    private boolean codeMatch(String match){
        return Pattern.compile("[0-9]{6}").matcher(match).find();
    }




    private void onPhoneNumberRecieved(String phoneNumber){
            Log.i(TAG, " number " + phoneNumber + " " + phoneNumber.isEmpty());
            if(!phoneNumber.isEmpty()){
                if(phoneNumber.equals("wrong_number")){
                    errorLabel.setText(R.string.enter_correct_phone_number);
                }else {
                    this.phoneNumber = phoneNumber;
                    checkNumberIsRegistered(FirebaseAuth.getInstance().getCurrentUser().getEmail(), phoneNumber);
                }
            }else{
                errorLabel.setText(R.string.enter_phone_number);
            }
        }

        public void bindFragmentToPhone(Fragment fragment){
            if(fragment instanceof PhoneAuthenticationFragment) {
                ((PhoneAuthenticationFragment)fragment).setFragmentPhone(this::onPhoneNumberRecieved);
            }
        }


        public <T extends Fragment> void setPhoneVerificationReciever(PhoneVerificationReciever<T> phoneVerificationReciever){
            this.phoneVerificationReciever = phoneVerificationReciever;
        }

}
