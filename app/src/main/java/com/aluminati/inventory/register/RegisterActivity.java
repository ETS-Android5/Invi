package com.aluminati.inventory.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.aluminati.inventory.InfoPageActivity;
import com.aluminati.inventory.MainActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.fragmentListeners.password.PassWordListenerReciever;
import com.aluminati.inventory.login.authentication.LinkAccounts;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.login.authentication.password.Password;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.aluminati.inventory.users.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = RegisterActivity.class.getSimpleName();
    private PassWordListenerReciever passWordListenerReciever;
    private FirebaseAuth firebaseAuth;
    private final int request_code = 9002;
    private EditText email;
    private EditText name;
    private EditText surName;
    private TextView emailVerified;
    private int loginMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        name = findViewById(R.id.name_view);
        surName = findViewById(R.id.surname_view);
        email = findViewById(R.id.email_view);
        emailVerified = findViewById(R.id.email_verified);

            addTextChangeListenet(name, surName);
            addTextChangeListenet(surName, email);
            addEmailTextListener(email);

            User user = (User)getIntent().getSerializableExtra("user_info");
            loginMethod = getIntent().getIntExtra("login_method", 0);
            Log.i(TAG, "Login Method" + loginMethod);


            if(user != null){
                String[] name_split = user.getDisplayName().split(" ");
                name.setText(name_split[0]);
                surName.setText(name_split[1]);
                email.setText(user.getEmail());
            }

        Password passwordFragment = (Password) getSupportFragmentManager().findFragmentById(R.id.password_fragment);
            if(passwordFragment != null) {
                bindFragmentToPassword(passwordFragment);
            }

        findViewById(R.id.register_button).setOnClickListener(this);
        findViewById(R.id.cancel_registration).setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();

    }


    public void askForPassWord(String passWord, String passWordConfirm){
        if(!passWord.isEmpty() && !passWordConfirm.isEmpty()){
            if(passWord.equals(passWordConfirm)) {
               if(!email.getText().toString().isEmpty()){
                   createAccount(email.getText().toString().trim(), passWord);
               }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_button: {
                passWordListenerReciever.askForPassWord(3001);
                break;
            }
            case R.id.cancel_registration: {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            }
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == request_code){
            if(resultCode == Activity.RESULT_OK){
                updateLayout(firebaseAuth.getCurrentUser());
            }
        }
    }


    private void addTextChangeListenet(final EditText editTextViewOne, final EditText editTextViewTwo){
        editTextViewOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    if(!editTextViewTwo.isEnabled()) {
                        editTextViewTwo.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void addEmailTextListener(final EditText emailField){
        emailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                    if(emailField.getInputType() == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS){
                        if(Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(charSequence.toString()).matches()){
                            emailVerified.setText("Correct format");
                            emailField.setTextColor(getResources().getColor(R.color.password_verify));
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    private void createAccount(String email, String password){

        User user = addUser(email);

        if(firebaseAuth.getCurrentUser() != null){
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

            firebaseAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            VerifyUser.userExists(user);
                            startActivity(new Intent(RegisterActivity.this, PhoneAuthentication.class));
                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });

        }else {

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Successful");
                            VerifyUser.userExists(user);
                            VerifyUser.isUserVerified(user, RegisterActivity.this, true);
                            updateFireBaseUser(task.getResult().getUser());
                        } else {
                            if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                LinkAccounts.linkAccountsInfo(RegisterActivity.this, "Email is already registered, login " +
                                        " or recover password");
                            }
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private User addUser(String email){
        User user = null;
        switch (loginMethod){
            case VerificationStatus.GOOGLE:{
                Log.i(TAG, "Google Profile Linking");
                user = new User(email, false, false, true, false);
                break;
            }
            case VerificationStatus.FACEBOOK:{
                Log.i(TAG, "Facebook Profile Linking");
                user = new User(email, false, false, true, true);
                break;
            }case VerificationStatus.EMAIL: {
                Log.i(TAG, "Email registration");
                user = new User(email, false, false, false, false);
                break;
            }
        }
        return user;
    }

    private void updateFireBaseUser(FirebaseUser firebaseUser){
        if(!name.getText().toString().isEmpty() && !surName.getText().toString().isEmpty()){
            firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                    .setDisplayName(name.getText().toString() + " " + surName.getText().toString()).build());
        }
    }


    private void updateLayout(FirebaseUser firebaseUser){
        Intent infoPageIntent = new Intent(this, InfoPageActivity.class);

        if(firebaseUser != null){
            infoPageIntent.putExtra("user_info", new User(firebaseUser));
        }

        startActivity(infoPageIntent);
    }


    public void bindFragmentToPassword(Password fragment){
        fragment.setPassWordListenerSender(this::askForPassWord);
    }


    public <T extends Fragment> void setPassWordListenerReciever(PassWordListenerReciever passWordListenerReciever){
        this.passWordListenerReciever = passWordListenerReciever;
    }





}
