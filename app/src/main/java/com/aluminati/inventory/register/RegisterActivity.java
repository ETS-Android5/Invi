package com.aluminati.inventory.register;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.fragments.fragmentListeners.password.PassWordListenerReciever;
import com.aluminati.inventory.login.authentication.AuthenticationActivity;
import com.aluminati.inventory.login.authentication.ForgotPasswordActivity;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.login.authentication.password.Password;
import com.aluminati.inventory.users.User;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = RegisterActivity.class.getSimpleName();
    private PassWordListenerReciever passWordListenerReciever;
    private FirebaseAuth firebaseAuth;
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

            addTextChangeListenet(name, surName,findViewById(R.id.name_required_view));
            addTextChangeListenet(surName, email, findViewById(R.id.surname_required_view));
            addEmailTextListener(email, findViewById(R.id.email_required_view));

            loginMethod = getIntent().getIntExtra("login_method", 0);
            Log.d(TAG, "Login Method" + loginMethod);


        Password passwordFragment = (Password) getSupportFragmentManager().findFragmentById(R.id.password_fragment);
            if(passwordFragment != null) {
                bindFragmentToPassword(passwordFragment);
            }

        findViewById(R.id.register_button).setOnClickListener(this);
        findViewById(R.id.cancel_registration).setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();


        if(firebaseAuth.getCurrentUser() != null){
            User user = new User(firebaseAuth.getCurrentUser());
            String[] name_split = user.getDisplayName().split(" ");
            name.setText(name_split[0]);
            surName.setText(name_split[1]);
            email.setText(user.getEmail());
        }

    }


    public void askForPassWord(String passWord, String passWordConfirm, boolean meetsReqs) {
        Log.i(TAG, "Password Received");

        if(!(passWord.isEmpty() && passWordConfirm.isEmpty())) {
            if (passWord.isEmpty()) {
                Utils.makeSnackBarWithButtons(getResources().getString(R.string.reg_hint_password), name, this);
            } else if (passWordConfirm.isEmpty()) {
                Utils.makeSnackBarWithButtons(getResources().getString(R.string.confirm_password), name, this);
            } else if (passWord.equals(passWordConfirm)) {
                if (checkIfPassWordContains(passWord)) {
                    if (meetsReqs) {
                        createAccount(email.getText().toString().trim(), passWord);
                    } else {
                        Utils.makeSnackBarWithButtons(getResources().getString(R.string.password_dont_meet_requirements), name, this);
                    }
                }
            } else {
                Utils.makeSnackBarWithButtons(getResources().getString(R.string.password_dont_match), name, this);
            }
        }else {
            Utils.makeSnackBarWithButtons(getResources().getString(R.string.reg_hint_password), name, this);
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_button: {
                if(verifyFields()) {
                    if(emailValid(email.getText().toString())){
                        passWordListenerReciever.askForPassWord(3001);
                    }else{
                        Utils.makeSnackBarWithButtons(getResources().getString(R.string.invalid_email), name, this);
                    }
                }else{
                    Utils.makeSnackBarWithButtons(getResources().getString(R.string.fill_in_requried_fields), name, this);
                }
                break;
            }
            case R.id.cancel_registration: {
                if(firebaseAuth.getCurrentUser() != null){
                    FirebaseAuth.getInstance().signOut();
                    if(LoginManager.getInstance() != null){
                        LoginManager.getInstance().logOut();
                    }
                }
                finish();
                break;
            }
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(firebaseAuth.getCurrentUser() != null){
            FirebaseAuth.getInstance().signOut();
            if(LoginManager.getInstance() != null){
                LoginManager.getInstance().logOut();
            }
        }
        finish();
    }

    private boolean checkIfPassWordContains(String password){
        if(password.contains(name.getText().toString()) || password.contains(surName.getText().toString())){
            Utils.makeSnackBarWithButtons(getResources().getString(R.string.password_conatins_name), name, this);
            return false;
        }
        return true;
    }


    private boolean verifyFields(){
        return !email.getText().toString().isEmpty() && !name.getText().toString().isEmpty() && !surName.getText().toString().isEmpty();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        int request_code = 9002;
        if(requestCode == request_code){
            if(resultCode == Activity.RESULT_OK){
                //startActivity();
            }
        }
    }


    private void addTextChangeListenet(final EditText editTextViewOne, final EditText editTextViewTwo, final TextView view){


        editTextViewOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 3){
                    if(!editTextViewTwo.isEnabled()) {
                        editTextViewTwo.setEnabled(true);
                    }
                    view.setTextColor(getResources().getColor(R.color.password_verify));
                }else {
                    view.setTextColor(getResources().getColor(R.color.google_red));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void addEmailTextListener(final EditText emailField, final TextView view){
        emailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().isEmpty()){
                        if(emailValid(emailField.getText().toString())){
                            emailField.setTextColor(getResources().getColor(R.color.password_verify));
                            view.setTextColor(getResources().getColor(R.color.password_verify));
                        }

                }else{
                    view.setTextColor(getResources().getColor(R.color.google_red));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private boolean emailValid(String email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void createAccount(String email, String password) {

        Log.i(TAG, "Registering User");

        User user = addUser(email);

        if (firebaseAuth.getCurrentUser() != null) {

            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                firebaseAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(task -> {
                    Log.d(TAG, "linkWithCredential:success");
                    VerifyUser.verifyUser(user);
                    startActivity(new Intent(RegisterActivity.this, AuthenticationActivity.class));
                    finish();
                }).addOnFailureListener(result -> {
                    Log.w(TAG, "linkWithCredential:failure", result);
                    Utils.makeSnackBarWithButtons(getResources().getString(R.string.authentication_failed), name, this);
                });

        } else {

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(task -> {
                Log.i(TAG, "Successful");
                VerifyUser.verifyUser(user);
                if (!name.getText().toString().isEmpty() && !surName.getText().toString().isEmpty()) {
                    VerifyUser.updateFireBaseUser(firebaseAuth.getCurrentUser(), name.getText().toString() + " " + surName.getText().toString());
                }
                VerifyUser.isUserVerified(user, RegisterActivity.this, true);
            }).addOnFailureListener(result -> {
                Log.w(TAG, "Creating Account Failed", result);
                if (result instanceof FirebaseAuthUserCollisionException) {
                    showAlertDialog("Email is already registered, login " + " or recover password");
                }
                Utils.makeSnackBarWithButtons(getResources().getString(R.string.authentication_failed), name, this);
            });
        }
    }

    private void showAlertDialog(String message){
        new AlertDialog.Builder(this)
                .setTitle("Email Registerd")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(message)
                .setPositiveButton("Ok", (dialog, id) -> dialog.cancel())
                .setNegativeButton("Recover Password", (dialog, id) -> startActivity(new Intent(RegisterActivity.this, ForgotPasswordActivity.class)))
                .create()
                .show();
    }


    private User addUser(String email){
        User user = null;
        switch (loginMethod){
            case VerificationStatus.GOOGLE:{
                Log.d(TAG, "Google Profile Linking");
                user = new User(email, false, false, true, false, false);
                break;
            }
            case VerificationStatus.FACEBOOK:{
                Log.d(TAG, "Facebook Profile Linking");
                user = new User(email, false, false, true, true,false);
                break;
            }case VerificationStatus.EMAIL: {
                Log.d(TAG, "Email registration");
                user = new User(email, false, false, false, false,false);
                break;
            }case VerificationStatus.TWITTER:{
                Log.d(TAG, "Twitter Profile Linking");
                user = new User(email, false,false,false,false,true);
                break;
            }
        }
        return user;
    }


    public void bindFragmentToPassword(Password fragment){
        fragment.setPassWordListenerSender(this::askForPassWord);
    }


    public void setPassWordListenerReciever(PassWordListenerReciever passWordListenerReciever){
        this.passWordListenerReciever = passWordListenerReciever;
    }


}
