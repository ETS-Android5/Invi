package com.aluminati.inventory.login.authentication.emailpassword;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailPasswordLogIn extends Fragment {


    private static final String TAG = "EmailPasswordLogIn";
    private EditText userNameField;
    private EditText passWordField;
    private Button loginButton;
    private String email;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.email_password_login, container, false);

        userNameField = view.findViewById(R.id.user_name_field);

        loginButton = view.findViewById(R.id.login_button);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonLogIn();

        return view;
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
        if(!input.isEmpty()){
            if(input.startsWith("+")){
                Intent intent = new Intent(getActivity(), PhoneAuthentication.class);
                       intent.putExtra("phone_number", input);
                startActivity(intent);
                getActivity().finish();
            }else if(input.contains("@")){
                email = input;
                replace(userNameField);
            }
        }
    }

    private void replace(View view){
        ViewGroup viewGroup = (ViewGroup)view.getParent();
        final int index = viewGroup.indexOfChild(view);

        passWordField = new EditText(getContext());
        passWordField.setText(R.string.reg_hint_password);
        passWordField.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passWordField.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        passWordField.setTextColor(getResources().getColor(R.color.white_text));

        loginButton.setText(getString(R.string.confirm_password));
                 viewGroup.removeView(view);
                 viewGroup.addView(passWordField,index);

    }


    private void signWithEmailAndPassword(String name, String password){
        if(!name.isEmpty() && !password.isEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(name, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        VerifyUser.checkUser(firebaseAuth.getCurrentUser(), getActivity(), VerificationStatus.FIREBASE);
                    } else {
                        Toast.makeText(getActivity(), "Failed to LogiIn", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }


}
