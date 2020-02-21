package com.aluminati.inventory.login.authentication.emailpassword;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.aluminati.inventory.HomeActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.LogInActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class EmailPasswordLogIn extends Fragment {


    private static final String TAG = EmailPasswordLogIn.class.getName();
    private int REQUEST_CODE = 3001;
    private EditText userNameField;
    private EditText passWordField;
    private TextView verifyInput;
    private Button loginButton;
    private String email;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.email_password_login, container, false);

        userNameField = view.findViewById(R.id.user_name_field);
        loginButton = view.findViewById(R.id.login_button);
        verifyInput = view.findViewById(R.id.verify_input);

        firebaseAuth = FirebaseAuth.getInstance();

        addTextWatcher(userNameField);
        buttonLogIn();

        return view;
    }


    private void addTextWatcher(EditText editText){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                verifyInput.setText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                startActivity(new Intent(getActivity(), HomeActivity.class));
                getActivity().finish();
            }else if(resultCode == Activity.RESULT_CANCELED){
                verifyInput.setText(getResources().getString(R.string.failed_log_in));
            }else if(resultCode == VerificationStatus.INCCORECT_PHONE_NUMBER){
                verifyInput.setText(getResources().getString(R.string.inccorect_phone_number));
            }else if(resultCode == VerificationStatus.TOO_MANY_REQUESTS){
                verifyInput.setText(getResources().getString(R.string.to_many_requests));
            }
        }
    }

    private void buttonLogIn(){
        loginButton.setOnClickListener(view -> {
            if(!ConnectivityCheck.isSnacBarVisible()) {
                if (loginButton.getText().toString().equals(getString(R.string.login_button))) {
                    checkInput();
                } else if (loginButton.getText().toString().equals(getString(R.string.confirm_password))) {
                    signWithEmailAndPassword(email, passWordField.getText().toString().trim());
                }
            }else{
                LogInActivity.alertDialog.show();
            }
        });
    }

    private void checkInput(){
        String input = userNameField.getText().toString();
        if(verifyInput(input)){
            if(input.startsWith("+")){
                Intent intent = new Intent(getActivity(), PhoneAuthentication.class);
                       intent.putExtra("phone_number", input);
                getActivity().startActivityForResult(intent, REQUEST_CODE);
            }else if(input.contains("@")){
                email = input;
                if(passWordField == null) getPassWordField();
                replace(userNameField, passWordField);
            }
        }else{
            verifyInput.setText(getResources().getString(R.string.verify_login_input));
        }
    }

    private boolean verifyInput(String input){
        return Patterns.EMAIL_ADDRESS.matcher(input).matches() || Pattern.compile("\\+[0-9]{4,15}").matcher(input).matches();
    }

    private void getPassWordField(){

        passWordField = new EditText(getContext());
        passWordField.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passWordField.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        passWordField.setHint(getResources().getString(R.string.reg_hint_password));
        passWordField.setHintTextColor(getResources().getColor(R.color.text_color));
        passWordField.setTextColor(getResources().getColor(R.color.text_color));
        userNameField.setText("");
        loginButton.setText(getString(R.string.confirm_password));

    }

    private void replace(View view, View replacingView){
        if(view != null && replacingView != null) {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            if(viewGroup != null) {
                final int index = viewGroup.indexOfChild(view);
                replacingView.setLayoutParams(view.getLayoutParams());
                viewGroup.removeView(view);
                viewGroup.addView(replacingView, index);
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(passWordField != null) {
            replace(passWordField,userNameField);
        }
    }

    private void signWithEmailAndPassword(String name, String password){
        if(!name.isEmpty() && !password.isEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(name, password).addOnSuccessListener(result -> {
                Log.d(TAG, "Logied In Successfull");
                VerifyUser.checkUser(FirebaseAuth.getInstance().getCurrentUser(), getActivity(), VerificationStatus.FIREBASE);
            }).addOnFailureListener(result -> {
                Log.w(TAG, "Failed to LogIn", result);
                replace(passWordField, userNameField);
                loginButton.setText(getResources().getString(R.string.login_button));
                Utils.makeSnackBar(getResources().getString(R.string.login_failed), loginButton, getActivity());
            });
        }
    }


}
