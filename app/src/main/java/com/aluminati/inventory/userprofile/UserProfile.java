package com.aluminati.inventory.userprofile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.aluminati.inventory.LogInActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationReciever;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.login.authentication.facebook.FaceBookSignIn;
import com.aluminati.inventory.login.authentication.password.PassWordReset;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.aluminati.inventory.users.User;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.InputStream;
import java.util.concurrent.Callable;

public class UserProfile extends AppCompatActivity implements View.OnClickListener{


    private static final String TAG = UserProfile.class.getName();
    private static final int PHONE_VERIFICATION = 3000;
    private static final int PASSWORD_RESET = 2999;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private EditText userName;
    private EditText userEmail;
    private EditText phoneNumber;
    private ImageView userPhoto;
    private TextView displayNameChange;
    private TextView emailChange;
    private TextView phoneChange;
    private TextView nameField;
    private TextView surNameField;
    private TextView phoneVerified;
    private TextView emailVerified;
    private TextView userImageChange;
    private ImageButton settingButton;
    private FirebaseUser firebaseUser;
    private PhoneVerificationReciever phoneVerificationReciever;
    private LoginButton facebookLogin;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private String tmp;
    private ConnectivityCheck connection;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


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
        phoneVerified = findViewById(R.id.phone_number_verified);
        emailVerified = findViewById(R.id.email_verified);
        userImageChange = findViewById(R.id.user_image_change);

        displayNameChange.setOnClickListener(this);
        emailChange.setOnClickListener(this);
        phoneChange.setOnClickListener(this);
        userPhoto.setOnClickListener(this);

        registerForContextMenu(settingButton);

        connection = new ConnectivityCheck(displayNameChange);



        setVerificationLabels();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(getResources().getString(R.string.extras));
        menu.add(0, v.getId(), 0, getResources().getString(R.string.reload));
        menu.add(0, v.getId(), 0, getResources().getString(R.string.reset_password));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(getResources().getString(R.string.reload))) {
            reloadUser();
        }
        else if(item.getTitle().equals(getResources().getString(R.string.reset_password))){
            startActivityForResult(new Intent(UserProfile.this, PassWordReset.class), PASSWORD_RESET);
        }
        return true;
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

    private void countDown(){
        new CountDownTimer(5*1000,1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                userImageChange.setVisibility(View.INVISIBLE);
            }
        };
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

        if(requestCode == PHONE_VERIFICATION){
            if(resultCode == VerificationStatus.SUCCESSFULL_UPDATE){
                Utils.makeSnackBarWithButtons("Updated Phone Number", phoneNumber, this);
            }else if(resultCode == VerificationStatus.FAILED_UPDATE){
                Utils.makeSnackBarWithButtons("Failed to Update Phone Number", phoneNumber, this);
            }
        }else if(requestCode == PASSWORD_RESET){
            if(resultCode == Activity.RESULT_OK){
                Utils.makeSnackBar("Password Updated", settingButton, this);
            }else if(resultCode == Activity.RESULT_CANCELED){
                Utils.makeSnackBar("Failed to Update Password", settingButton, this);
            }
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

    private void emailCheck(){
       new AlertDialog.Builder(this)
                .setTitle("Change Email")
                .setMessage("Unlink Facebook And Google To Change Email")
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void changeAttribute(EditText editText, TextView textView, Callable<Void> change){
        if(textView.getText().equals(getResources().getString(R.string.change))){
            if(editText.getId() == R.id.email_field){
                UserFetch.getUser(firebaseUser.getEmail()).addOnCompleteListener(result -> {
                    if(result.isSuccessful() && result.getResult() != null){
                        User user = new User(result.getResult());
                        if(user.isGoogleLinked() || user.isFacebookLinked()){
                            emailCheck();
                        }else{
                            tmp = editText.getText().toString();
                            editText.setEnabled(true);
                            textView.setText(getResources().getString(R.string.save));
                        }
                    }
                });
            }else {
                tmp = editText.getText().toString();
                editText.setEnabled(true);
                textView.setText(getResources().getString(R.string.save));
            }
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
                    if(!userName.getText().toString().isEmpty() && !tmp.equals(userName.getText().toString())){
                        firebaseUser
                                .updateProfile(new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName.getText().toString()).build())
                                .addOnCompleteListener(result -> {
                                   if(result.isSuccessful()){
                                       Utils.makeSnackBarWithButtons("Name Updated", displayNameChange, this);
                                       reloadUser();
                                   } else {
                                       Utils.makeSnackBarWithButtons("Failed to Update Name", displayNameChange, this);
                                   }
                                });
                    }
                    return null;
                });
                break;
            }
            case R.id.phone_number_change:{
                changeAttribute(phoneNumber, phoneChange, () -> {
                    if(!phoneNumber.getText().toString().isEmpty() && !tmp.equals(phoneNumber.getText().toString())){
                        UserFetch.update(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "is_phone_verified", false);
                        startActivityForResult(new Intent(UserProfile.this, PhoneAuthentication.class), PHONE_VERIFICATION);
                    }
                    return null;
                });
                break;
            }
            case R.id.change_email:{
                changeAttribute(userEmail, emailChange, () -> {
                    if(!userEmail.getText().toString().isEmpty() && !tmp.equals(userEmail.getText().toString())){
                        firebaseUser.updateEmail(emailChange.toString()).addOnCompleteListener(result -> {
                            if(result.isSuccessful()){
                                Utils.makeSnackBarWithButtons("Email Updated", displayNameChange, this);
                                reloadUser();
                                VerifyUser.verifyEmail().addOnCompleteListener(verificationSent -> {
                                    if(verificationSent.isSuccessful()){
                                        Utils.makeSnackBarWithButtons("Verification Email Sent", userEmail, this);
                                        UserFetch.update(firebaseUser.getEmail(), "is_email_verified", false);
                                    }else{
                                        Utils.makeSnackBarWithButtons("Failed to Send Verification", userEmail, this);
                                    }
                                });
                            }else{
                                Utils.makeSnackBarWithButtons("Failed to Update Emailed", displayNameChange, this);
                            }
                        });
                    }
                    return null;
                });
                break;
            }
            case R.id.userImage:{
                userImageChange.setVisibility(View.VISIBLE);
                countDown();
                break;
            }
            case R.id.user_image_change:{

                break;
            }
        }
    }

    private void deleteUser(String... providers){
       new FaceBookSignIn.UnlinkFacebook(providers[0], (result) -> {
           if(result){
               Log.d(TAG, "Facebook unlinked successfully");
               firebaseUser.unlink(providers[1]).addOnCompleteListener(unlinkGoogle -> {
                   if(unlinkGoogle.isSuccessful()){
                       Log.d(TAG, "Google unlinked Successfully");
                       firebaseUser.delete().addOnCompleteListener(deleteUser -> {
                           if(deleteUser.isSuccessful()){
                               Log.d(TAG, "User Deleted Successfully");
                               startActivity(new Intent(UserProfile.this, LogInActivity.class));
                               finish();
                           }else{
                               Log.d(TAG, "Failed to Delete User");
                           }
                       });
                   }else {
                       Log.d(TAG, "Failed to Unlink Google");
                   }
               });
           }else {
               Log.d(TAG, "Failed to unlink Facebook");
           }
       });
    }

    private void setVerificationLabels(){

        User user = new User(firebaseUser);

        if(user.isEmailVerified()){
            emailVerified.setText(getResources().getString(R.string.veirified));
            emailVerified.setTextColor(getResources().getColor(R.color.password_verify));
        }

        if(user.isPhoneVerified()){
            phoneVerified.setText(getResources().getString(R.string.veirified));
            phoneVerified.setTextColor(getResources().getColor(R.color.password_verify));
        }
    }


    private void reloadUser(){
        firebaseUser.reload().addOnCompleteListener(result -> {
            if(result.isSuccessful()){
                String[] name = firebaseUser.getDisplayName().split(" ");

                nameField.setText(name[0]);
                surNameField.setText(name[1]);
                userName.setText(firebaseUser.getDisplayName());
                userEmail.setText(firebaseUser.getEmail());
                phoneNumber.setText(firebaseUser.getPhoneNumber());
                new RemoteImage(userPhoto).execute(firebaseUser.getPhotoUrl().toString());

                setVerificationLabels();
            }else{
                Utils.makeSnackBarWithButtons("Failed to reload user", displayNameChange, this);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        reloadUser();
        registerReceiver(connection, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        Utils.makeSnackBar("Welcome back " + firebaseUser.getDisplayName(), displayNameChange, this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connection);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onStateNotSaved() {
        super.onStateNotSaved();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private static class RemoteImage extends AsyncTask<String, Void, Bitmap> {

        @SuppressLint("StaticFieldLeak")
        ImageView profileImageViwe;

        RemoteImage(ImageView profileImageViwe){
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
