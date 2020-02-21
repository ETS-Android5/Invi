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
import com.aluminati.inventory.MainActivity;
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
import java.util.concurrent.Callable;
public class NewProfileLayout extends AppCompatActivity  {

    private static final String TAG = UserProfile.class.getName();
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int PHONE_VERIFICATION = 3000;
    private static final int PASSWORD_RESET = 2999;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private TextView displayNameChange;
    private TextView phoneVerified;
    private TextView emailVerified;
    private ImageButton settingButton;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private ConnectivityCheck connection;
    private PopupMenu popup;


    private TextView tvProfileEmail;
    private EditText editProfileEmail;
    private TextView tvProfileName;
    private EditText editProfileName;
    private TextView tvProfileAddress;
    private EditText editProfileAddress;
    private TextView tvProfilePass;
    private EditText editProfilePass;
    private TextView tvProfilePhone;
    private EditText editProfilePhone;
    private boolean editMode;
    private ImageView imgProfileSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        settingButton = findViewById(R.id.profile_settings_button);
        phoneVerified = findViewById(R.id.phone_number_verified);
        emailVerified = findViewById(R.id.email_verified);

        registerForContextMenu(settingButton);

       // connection = new ConnectivityCheck(displayNameChange);


        //TODO: read from database
        setUserData(new NewUserProfile());

    }
    public void toggleEdit(View view) {

        imgProfileSave.setBackground(getResources().getDrawable(editMode
                ?
                R.mipmap.ic_launcher_round
                : R.drawable.profile_img));

        tvProfileEmail.setVisibility(editMode ? View.GONE: View.VISIBLE);
        editProfileEmail.setVisibility(editMode ? View.VISIBLE: View.GONE);
        tvProfileName.setVisibility(editMode ? View.GONE: View.VISIBLE);
        editProfileName.setVisibility(editMode ? View.VISIBLE: View.GONE);
        tvProfileAddress.setVisibility(editMode ? View.GONE: View.VISIBLE);
        editProfileAddress.setVisibility(editMode ? View.VISIBLE: View.GONE);
        tvProfilePhone.setVisibility(editMode ? View.GONE: View.VISIBLE);
        editProfilePhone.setVisibility(editMode ? View.VISIBLE: View.GONE);
        editMode = !editMode;

        if(editMode) {
            saveUserProfile(new NewUserProfile(
                    tvProfileEmail.getText().toString(),
                    tvProfileName.getText().toString(),
                    //  tvProfilePass.getText().toString(),
                    tvProfileAddress.getText().toString(),
                    tvProfilePhone.getText().toString()


            ));
        }
    }

    private void saveUserProfile(NewUserProfile userProfile) {

        //TODO: send to database
        setUserData(userProfile);
    }

    private void setUserData(NewUserProfile userProfile) {
        tvProfileEmail.setText(userProfile.getEmail());
        tvProfileName.setText(userProfile.getName());
        //tvProfilePass.setText(userProfile.getPassword());
        tvProfileAddress.setText(userProfile.getAddress());
        tvProfilePhone.setText(userProfile.getPhone());

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
                Utils.makeSnackBarWithButtons("Updated Phone Number", tvProfilePhone, this);
            } else if (resultCode == VerificationStatus.FAILED_UPDATE) {
                Utils.makeSnackBarWithButtons("Failed to Update Phone Number", tvProfilePhone, this);
            }
        } else if (requestCode == PASSWORD_RESET) {
            if (resultCode == Activity.RESULT_OK) {
                Utils.makeSnackBar("Password Updated", settingButton, this);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Utils.makeSnackBar("Cancel Password Reset", settingButton, this);
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

            tvProfileName.setText(name[0]);
            //surNameField.setText(name[1]);
            tvProfileName.setText(firebaseUser.getDisplayName());
            tvProfileEmail.setText(firebaseUser.getEmail());
            tvProfilePhone.setText(firebaseUser.getPhoneNumber());

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


//    @Override
//    public boolean onMenuItemClick(MenuItem menuItem) {
//        switch (menuItem.getItemId()) {
//            case R.id.reload: {
//                reloadUser();
//                break;
//            }
//            case R.id.password_reset: {
//                startActivityForResult(new Intent(NewProfileLayout.this, PassWordReset.class), PASSWORD_RESET);
//                break;
//            }
//            case R.id.logout: {
//                firebaseAuth.signOut();
//                if (LoginManager.getInstance() != null) {
//                    LoginManager.getInstance().logOut();
//                }
//                //TODO: not good --> look for clear flags when creating intent HomeActivity.homeActivity.finish();
//                Intent logout = new Intent(NewProfileLayout.this, LogInActivity.class);
//                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(logout);
//                finish();
//                break;
//            }
//        }
//        return false;
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
