package com.aluminati.inventory.userprofile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.HomeActivity;
import com.aluminati.inventory.LogInActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.DeleteUser;
import com.aluminati.inventory.login.authentication.VerificationStatus;
import com.aluminati.inventory.login.authentication.VerifyUser;
import com.aluminati.inventory.login.authentication.password.PassWordReset;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.aluminati.inventory.users.User;
import com.facebook.login.LoginManager;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserProfile extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {


    private static final String TAG = UserProfile.class.getName();
    private static final int GET_PHOTO = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;
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
    private FirebaseAuth firebaseAuth;
    private String tmp;
    private ConnectivityCheck connection;
    private PopupMenu popup;


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
        settingButton.setOnClickListener(this);
        userImageChange.setOnClickListener(this);
        findViewById(R.id.delete_user).setOnClickListener(this);
        registerForContextMenu(settingButton);

        connection = new ConnectivityCheck(displayNameChange);



    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (popup != null) {
                popup.dismiss();
            }
        }
        return true;
    }

    public void showMenu(View anchor) {

        Context wrapper = new ContextThemeWrapper(this, R.style.MyPopupMenu);
        popup = new PopupMenu(wrapper, anchor, Gravity.END);
        popup.setOnMenuItemClickListener(this);
        popup.getMenuInflater().inflate(R.menu.user_profile_menu, popup.getMenu());
        popup.show();
    }


    private void countDown() {
        new CountDownTimer(5 * 1000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                userImageChange.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

    private boolean hasImage(@NonNull ImageView view) {

                Drawable drawable = view.getDrawable();
                boolean hasImage = (drawable != null);

                if (hasImage && (drawable instanceof BitmapDrawable)) {
                    hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
                }

                return hasImage;

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

        if (requestCode == PHONE_VERIFICATION) {
            if (resultCode == VerificationStatus.SUCCESSFULL_UPDATE) {
                Utils.makeSnackBarWithButtons("Updated Phone Number", phoneNumber, this);
            } else if (resultCode == VerificationStatus.FAILED_UPDATE) {
                Utils.makeSnackBarWithButtons("Failed to Update Phone Number", phoneNumber, this);
            }
        } else if (requestCode == PASSWORD_RESET) {
            if (resultCode == Activity.RESULT_OK) {
                Utils.makeSnackBar("Password Updated", settingButton, this);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Utils.makeSnackBar("Cancel Password Reset", settingButton, this);
            }
        }

        if (requestCode == GET_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.i(TAG, "Picture not found");
                return;
            }
            try {
                Log.i(TAG, "Picture found");

                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                if(inputStream != null){
                    Log.i(TAG, "Stream not null" + inputStream.toString());
                    Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
                    userPhoto.setImageBitmap(bitmap);
                    if(hasImage(userPhoto)) {
                        userImageChange.setVisibility(View.INVISIBLE);
                        UserFetch.update(firebaseUser.getEmail(), "user_photo", encodeTobase64(bitmap));
                        Snackbar.make(settingButton, getResources().getString(R.string.photo_changed), BaseTransientBottomBar.LENGTH_LONG);
                    }
                }else Log.i(TAG, "Stream  null");
            }catch (FileNotFoundException e){
                Log.w(TAG, "File not found", e);
            }
        }
    }

    public String encodeTobase64(Bitmap image)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b,Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }

    public Bitmap decodeBase64(String input)
    {
        try{
            byte [] encodeByte = Base64.decode(input,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    private void changeView(EditText editText, TextView textView) {
        if (editText.isEnabled()) {
            editText.setEnabled(false);
            textView.setText(getResources().getString(R.string.change));
        } else {
            editText.setEnabled(true);
            textView.setText(getResources().getString(R.string.save));
        }
    }

    private void emailCheck() {
        new AlertDialog.Builder(this)
                .setTitle("Change Email")
                .setMessage("Unlink Facebook And Google To Change Email")
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void changeAttribute(EditText editText, TextView textView, Callable<Void> change) {
        if (textView.getText().equals(getResources().getString(R.string.change))) {
            if (editText.getId() == R.id.email_field) {
                UserFetch.getUser(firebaseUser.getEmail()).addOnCompleteListener(result -> {
                    if (result.isSuccessful() && result.getResult() != null) {
                        User user = new User(result.getResult());
                        if (user.isGoogleLinked() || user.isFacebookLinked()) {
                            emailCheck();
                        } else {
                            tmp = editText.getText().toString();
                            editText.setEnabled(true);
                            textView.setText(getResources().getString(R.string.save));
                        }
                    }
                });
            } else {
                tmp = editText.getText().toString();
                editText.setEnabled(true);
                textView.setText(getResources().getString(R.string.save));
            }
        } else if (textView.getText().equals(getResources().getString(R.string.save))) {
            editText.setEnabled(false);
            textView.setText(getResources().getString(R.string.change));
            try {
                change.call();
            } catch (Exception e) {
                Log.w(TAG, "Exception changing attributes => ", e);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.display_name_change: {
                changeAttribute(userName, displayNameChange, () -> {
                    if (!userName.getText().toString().isEmpty() && !tmp.equals(userName.getText().toString())) {
                        firebaseUser
                                .updateProfile(new UserProfileChangeRequest.Builder()
                                        .setDisplayName(userName.getText().toString()).build())
                                .addOnCompleteListener(result -> {
                                    if (result.isSuccessful()) {
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
            case R.id.phone_number_change: {
                changeAttribute(phoneNumber, phoneChange, () -> {
                    if (!phoneNumber.getText().toString().isEmpty() && !tmp.equals(phoneNumber.getText().toString())) {
                        UserFetch.update(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "is_phone_verified", false);
                        startActivityForResult(new Intent(UserProfile.this, PhoneAuthentication.class), PHONE_VERIFICATION);
                    }
                    return null;
                });
                break;
            }
            case R.id.change_email: {
                changeAttribute(userEmail, emailChange, () -> {
                    if (!userEmail.getText().toString().isEmpty() && !tmp.equals(userEmail.getText().toString())) {
                        firebaseUser.updateEmail(emailChange.toString()).addOnCompleteListener(result -> {
                            if (result.isSuccessful()) {
                                Utils.makeSnackBarWithButtons("Email Updated", displayNameChange, this);
                                reloadUser();
                                VerifyUser.verifyEmail().addOnCompleteListener(verificationSent -> {
                                    if (verificationSent.isSuccessful()) {
                                        Utils.makeSnackBarWithButtons("Verification Email Sent", userEmail, this);
                                        UserFetch.update(firebaseUser.getEmail(), "is_email_verified", false);
                                    } else {
                                        Utils.makeSnackBarWithButtons("Failed to Send Verification", userEmail, this);
                                    }
                                });
                            } else {
                                Utils.makeSnackBarWithButtons("Failed to Update Emailed", displayNameChange, this);
                            }
                        });
                    }
                    return null;
                });
                break;
            }
            case R.id.userImage: {
                userImageChange.setVisibility(View.VISIBLE);
                countDown();
                break;
            }
            case R.id.user_image_change: {
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                break;
            }
            case R.id.delete_user: {
                deleteUser();
                break;
            }
            case R.id.profile_settings_button: {
                showMenu(settingButton);
            }
        }
    }

    private void deleteUser() {
        DeleteUser deleteUser = DeleteUser.newInstance("Hello");
        deleteUser.show(getSupportFragmentManager(), "delete_user_frag");
    }

    private void setVerificationLabels() {

        UserFetch.getUser(firebaseUser.getEmail()).addOnSuccessListener(result -> {
            if (result.exists()) {
                User user = new User(result);
                if (user.isEmailVerified()) {
                    emailVerified.setText(getResources().getString(R.string.veirified));
                    emailVerified.setTextColor(getResources().getColor(R.color.password_verify));
                }

                if (user.isPhoneVerified()) {
                    phoneVerified.setText(getResources().getString(R.string.veirified));
                    phoneVerified.setTextColor(getResources().getColor(R.color.password_verify));
                }
            }
        }).addOnFailureListener(result -> {
            Log.w(TAG, "Failed to fetch user", result);
        });

    }


    private void reloadUser() {
        firebaseUser.reload().addOnSuccessListener(result -> {
            Log.i(TAG, "User reloaded");
            String[] name = firebaseUser.getDisplayName().split(" ");

            nameField.setText(name[0]);
            surNameField.setText(name[1]);
            userName.setText(firebaseUser.getDisplayName());
            userEmail.setText(firebaseUser.getEmail());
            phoneNumber.setText(firebaseUser.getPhoneNumber());
            try {
                new RemoteImage(userPhoto).execute(firebaseUser.getPhotoUrl().toString());
            } catch (NullPointerException e) {
                Log.w(TAG, "User photo null", e);
                UserFetch.getUser(firebaseUser.getEmail()).addOnSuccessListener(resultImage -> {
                    Log.i(TAG, "Got user successfully");
                    User user = new User(resultImage);
                    if(user.getPhoto() != null){
                        userPhoto.setImageBitmap(decodeBase64(user.getPhoto()));
                        userImageChange.setVisibility(View.INVISIBLE);
                    }
                }).addOnFailureListener(resultImage -> {
                    Log.w(TAG, "Failed to get user successfully", resultImage);
                });
            }
            setVerificationLabels();

            //Utils.makeSnackBarWithButtons("User reloaded successfully", displayNameChange, this);

        }).addOnFailureListener(result -> {
            Log.w(TAG, "Failed to reload user", result);
            Utils.makeSnackBarWithButtons("Failed to reload user", displayNameChange, this);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        reloadUser();
        registerReceiver(connection, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (!hasImage(userPhoto)) {
            userImageChange.setVisibility(View.VISIBLE);
            userImageChange.setText(getResources().getString(R.string.upload_user_image));
        }
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

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.reload: {
                reloadUser();
                break;
            }
            case R.id.password_reset: {
                startActivityForResult(new Intent(UserProfile.this, PassWordReset.class), PASSWORD_RESET);
                break;
            }
            case R.id.logout: {
                firebaseAuth.signOut();
                if (LoginManager.getInstance() != null) {
                    LoginManager.getInstance().logOut();
                }
                //TODO: not good --> look for clear flags when creating intent HomeActivity.homeActivity.finish();
                Intent logout = new Intent(UserProfile.this, LogInActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logout);
                finish();
                break;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        } else {
            Snackbar.make(settingButton, " Permission already granted", BaseTransientBottomBar.LENGTH_LONG).show();
            pickImage();
        }
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GET_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(settingButton, "Storage Permission Granted", BaseTransientBottomBar.LENGTH_LONG).show();
                pickImage();
            } else {
                Snackbar.make(settingButton, "Storage Permission Denied", BaseTransientBottomBar.LENGTH_LONG).show();
            }
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
