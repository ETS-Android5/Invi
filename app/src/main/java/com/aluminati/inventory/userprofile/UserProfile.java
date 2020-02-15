package com.aluminati.inventory.userprofile;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.InfoPageActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationReciever;
import com.aluminati.inventory.login.authentication.LinkAccounts;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.aluminati.inventory.users.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.InputStream;
import java.util.concurrent.Callable;

public class UserProfile extends AppCompatActivity implements View.OnClickListener{


    private static final String TAG = UserProfile.class.getName();
    private EditText userName;
    private EditText userEmail;
    private EditText phoneNumber;
    private ImageView userPhoto;
    private TextView displayNameChange;
    private TextView emailChange;
    private TextView phoneChange;
    private TextView nameField;
    private TextView surNameField;
    private ImageButton settingButton;
    private FirebaseUser firebaseUser;
    private PhoneVerificationReciever phoneVerificationReciever;
    private LoginButton facebookLogin;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private ConnectivityCheck connection;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        displayNameChange = findViewById(R.id.display_name_change);
        emailChange = findViewById(R.id.change_email);
        userEmail = findViewById(R.id.email_field);
        userName = findViewById(R.id.display_name_field);
        phoneNumber = findViewById(R.id.phone_number_field);
        userPhoto = findViewById(R.id.userImage);
        surNameField = findViewById(R.id.surname_field);
        nameField = findViewById(R.id.name_field);
        phoneChange = findViewById(R.id.phone_number_change);
        settingButton = findViewById(R.id.profile_settings_button);
        settingButton.setOnClickListener(this);
        displayNameChange.setOnClickListener(this);
        emailChange.setOnClickListener(this);
        phoneChange.setOnClickListener(this);


        connection = new ConnectivityCheck(displayNameChange);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{

                break;
            }
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                fragment.onActivityResult(requestCode, resultCode, data);
                Log.d("Activity", "ON RESULT CALLED");
            }
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }
    }



    private void changeView(EditText editText, TextView textView){
        if(editText.isEnabled()){
            editText.setEnabled(false);
            textView.setText(getResources().getString(R.string.change));
        }else{
            editText.setEnabled(true);
            textView.setText(getResources().getString(R.string.save));
        }
    }

    private void changeAttribute(EditText editText, TextView textView, Callable<Void> change){
        if(textView.getText().equals(getResources().getString(R.string.change))){
            editText.setEnabled(true);
            textView.setText(getResources().getString(R.string.save));
        }else if(textView.getText().equals(getResources().getString(R.string.save))){
            editText.setEnabled(false);
            textView.setText(getResources().getString(R.string.change));
            try {
                change.call();
            }catch (Exception e){
                Log.w(TAG, "Exception changing attributes => ", e);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.display_name_change:{
                changeAttribute(userName, displayNameChange, () -> {
                    if(!userName.getText().toString().isEmpty()){
                        firebaseUser
                                .updateProfile(new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName.getText().toString()).build())
                                .addOnCompleteListener(result -> {
                                   if(result.isSuccessful()){
                                       Utils.makeSnackBar("Name Updated", displayNameChange, this);
                                       reloadUser();
                                   } else {
                                       Utils.makeSnackBar("Failed to Update Name", displayNameChange, this);
                                   }
                                });
                    }
                    return null;
                });
                break;
            }
            case R.id.phone_number_change:{
                changeAttribute(phoneNumber, phoneChange, () -> {
                    if(!phoneNumber.getText().toString().isEmpty()){
                    }
                    return null;
                });
                break;
            }
            case R.id.change_email:{
                changeAttribute(userEmail, emailChange, () -> {
                    if(!emailChange.getText().toString().isEmpty()){
                        firebaseUser.updateEmail(emailChange.toString()).addOnCompleteListener(result -> {
                            if(result.isSuccessful()){
                                Utils.makeSnackBar("Email Updated", displayNameChange, this);
                                reloadUser();
                            }else{
                                Utils.makeSnackBar("Failed to Update Emailed", displayNameChange, this);
                            }
                        });
                    }
                    return null;
                });
                break;
            }


        }
    }

    private void reloadUser(){
        firebaseAuth.getCurrentUser().reload().addOnCompleteListener(result -> {
            if(result.isSuccessful()){
                String name[] = firebaseUser.getDisplayName().split(" ");

                nameField.setText(name[0]);
                surNameField.setText(name[1]);
                userName.setText(firebaseUser.getDisplayName());
                userEmail.setText(firebaseUser.getEmail());
                phoneNumber.setText(firebaseUser.getPhoneNumber());
                new RemoteImage(userPhoto).execute(firebaseUser.getPhotoUrl().toString());
            }else{
                Utils.makeSnackBar("Failed to reload user", displayNameChange, this);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        reloadUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onStateNotSaved() {
        super.onStateNotSaved();
    }



    private class RemoteImage extends AsyncTask<String, Void, Bitmap> {

        ImageView profileImageViwe;

        public RemoteImage(ImageView profileImageViwe){
            this.profileImageViwe = profileImageViwe;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap userIcon = null;
            try{
                InputStream inputStream = new java.net.URL(urlDisplay).openStream();
                userIcon = BitmapFactory.decodeStream(inputStream);
            }catch (Exception e){
                Log.w("Error Converting", e);
            }
            return userIcon;
        }

        protected void onPostExecute(Bitmap result){
            profileImageViwe.setImageBitmap(result);
        }


    }
}
