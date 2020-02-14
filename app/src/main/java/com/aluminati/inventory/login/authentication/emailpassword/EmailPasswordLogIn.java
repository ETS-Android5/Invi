package com.aluminati.inventory.login.authentication.emailpassword;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class EmailPasswordLogIn extends Fragment {


    private static final String TAG = "EmailPasswordLogIn";
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


        addTextWatcher(userNameField);

        verifyInput = view.findViewById(R.id.verify_input);

        firebaseAuth = FirebaseAuth.getInstance();

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

    private void buttonLogIn(){
        loginButton.setOnClickListener(view -> {
            if(loginButton.getText().toString().equals(getString(R.string.login_button))){
                checkInput();
            }else if(loginButton.getText().toString().equals(getString(R.string.confirm_password))){
                signWithEmailAndPassword(email, passWordField.getText().toString().trim());
            }
        });
    }

    private void checkInput(){
        String input = userNameField.getText().toString();
        if(verifyInput(input)){
            if(input.startsWith("+")){
                Intent intent = new Intent(getActivity(), PhoneAuthentication.class);
                       intent.putExtra("phone_number", input);
                startActivity(intent);
                getActivity().finish();
            }else if(input.contains("@")){
                email = input;
                getPassWordField();
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
        ViewGroup viewGroup = (ViewGroup)view.getParent();
        final int index = viewGroup.indexOfChild(view);
                 viewGroup.removeView(view);
                 viewGroup.addView(replacingView,index);

    }


    private void signWithEmailAndPassword(String name, String password){
        if(!name.isEmpty() && !password.isEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(name, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        VerifyUser.checkUser(firebaseAuth.getCurrentUser(), getActivity(), VerificationStatus.FIREBASE);
                    } else {
                        replace(passWordField, userNameField);
                        loginButton.setText(getResources().getString(R.string.login_button));
                        Snackbar.make(loginButton, "Login Failed", BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }
            });
        }

    }


}
