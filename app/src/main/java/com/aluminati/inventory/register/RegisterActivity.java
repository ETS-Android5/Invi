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
import com.aluminati.inventory.users.User;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.Authentication;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

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

            addTextChangeListenet(name, surName,findViewById(R.id.name_required_view));
            addTextChangeListenet(surName, email, findViewById(R.id.surname_required_view));
            addEmailTextListener(email, findViewById(R.id.email_required_view));

            loginMethod = getIntent().getIntExtra("login_method", 0);
            Log.d(TAG, "Login Method" + loginMethod);


            if(firebaseAuth != null){
                User user = new User(firebaseAuth.getCurrentUser());
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


    public void askForPassWord(String passWord, String passWordConfirm, boolean meetsReqs){

        if(passWord.isEmpty()){
            makeSnackBar(getResources().getString(R.string.reg_hint_password));
        }else if(passWordConfirm.isEmpty()){
            makeSnackBar(getResources().getString(R.string.confirm_password));
        }else if(passWord.isEmpty() && passWordConfirm.isEmpty()){
            makeSnackBar(getResources().getString(R.string.reg_hint_password));
        }else if (passWord.equals(passWordConfirm)) {
                if (meetsReqs) {
                    createAccount(email.getText().toString().trim(), passWord);
                } else {
                    makeSnackBar(getResources().getString(R.string.password_dont_meet_requirements));
                }
            } else {
                makeSnackBar(getResources().getString(R.string.password_dont_match));
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
                        makeSnackBar(getResources().getString(R.string.invalid_email));
                    }
                }else{
                    makeSnackBar(getResources().getString(R.string.fill_in_requried_fields));
                }
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


    private boolean verifyFields(){
        return !email.getText().toString().isEmpty() && !name.getText().toString().isEmpty() && !surName.getText().toString().isEmpty();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
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
                if(charSequence.length() > 0){
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

    private void createAccount(String email, String password){

        User user = addUser(email);

        if(firebaseAuth.getCurrentUser() != null){

            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

            firebaseAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            VerifyUser.userExists(user);
                            startActivity(new Intent(RegisterActivity.this, Authentication.class));
                            finish();
                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            makeSnackBar(getResources().getString(R.string.authentication_failed));
                        }
                    });

        }else {

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Successful");
                            VerifyUser.userExists(user);
                            VerifyUser.isUserVerified(user, RegisterActivity.this, true);
                            if(!name.getText().toString().isEmpty() && !surName.getText().toString().isEmpty()){
                                VerifyUser.updateFireBaseUser(firebaseAuth.getCurrentUser(), name.getText().toString() + " " + surName.getText().toString());
                            }
                        } else {
                            if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                LinkAccounts.linkAccountsInfo(RegisterActivity.this, "Email is already registered, login " +
                                        " or recover password");
                            }
                            makeSnackBar(getResources().getString(R.string.authentication_failed));
                        }
                    });
        }
    }

    private void makeSnackBar(String message){
        Snackbar snackbar = Snackbar.make(name, message, BaseTransientBottomBar.LENGTH_INDEFINITE);
                 snackbar.setAction(getResources().getString(R.string.ok), view -> {
                     snackbar.dismiss();
                 });
                 snackbar.show();
    }

    private User addUser(String email){
        User user = null;
        switch (loginMethod){
            case VerificationStatus.GOOGLE:{
                Log.d(TAG, "Google Profile Linking");
                user = new User(email, false, false, true, false);
                break;
            }
            case VerificationStatus.FACEBOOK:{
                Log.d(TAG, "Facebook Profile Linking");
                user = new User(email, false, false, true, true);
                break;
            }case VerificationStatus.EMAIL: {
                Log.d(TAG, "Email registration");
                user = new User(email, false, false, false, false);
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
