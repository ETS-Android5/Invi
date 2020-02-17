package com.aluminati.inventory.login.authentication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.aluminati.inventory.R;
import com.aluminati.inventory.firestore.UserFetch;
import com.google.firebase.auth.FirebaseAuth;


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

        findViewById(R.id.send_email).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.send_email){
            if(!emailField.getText().toString().isEmpty()){
                if(validateEmailInput(emailField.getText().toString())){
                    UserFetch.getUser(emailField.getText().toString()).addOnCompleteListener(result -> {
                       if(result.isSuccessful() && result.getResult() != null){
                               if(result.getResult().exists()){
                                   FirebaseAuth.getInstance().sendPasswordResetEmail(emailField.getText().toString(), VerifyUser.setActionCodeSettings()).addOnCompleteListener(task -> {
                                       if(task.isSuccessful()){
                                           Log.i(TAG, "Password reset send");
                                       } else {
                                           Log.i(TAG, "Failed to send email");
                                       }
                                   });
                               }
                       }
                    });
                    emailSent.setText(getResources().getString(R.string.email_sent_info));
                }else{
                    emailSent.setText(getResources().getString(R.string.invalid_email));
                }
            }else{
                emailSent.setText(getResources().getString(R.string.empty_email));
            }
        }
    }


    private boolean validateEmailInput(String email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
