package com.aluminati.inventory.login.authentication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.aluminati.inventory.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ForgotPassWord";
    private EditText emailField;
    private TextView emailSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailField = findViewById(R.id.email_field);
        emailSent = findViewById(R.id.email_sent);

    }

    private boolean validateEmailInput(String email){
        return Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$").matcher(email).matches();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.send_email){
            if(!emailField.getText().toString().isEmpty()){
                if(validateEmailInput(emailField.getText().toString())){
                    FirebaseAuth.getInstance().sendPasswordResetEmail(emailField.getText().toString()).addOnCompleteListener(task -> {
                       if(task.isSuccessful()){
                           Log.i(TAG, "Password reset send");
                           emailSent.setText("Email Sent");
                       } else {
                           Log.i(TAG, "Failed to send email");
                       }
                    });
                }else{
                    emailSent.setText("Incorrect email format");
                }
            }
        }else{
            emailSent.setText("Fill in email");
        }
    }
}
