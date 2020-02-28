package com.aluminati.inventory.userprofile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.fragments.fragmentListeners.socialAccounts.ReloadImageResponse;
import com.aluminati.inventory.login.authentication.LogInActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.DeleteUser;
import com.aluminati.inventory.login.authentication.verification.VerificationStatus;
import com.aluminati.inventory.login.authentication.verification.VerifyUser;
import com.aluminati.inventory.login.authentication.password.PassWordReset;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.aluminati.inventory.users.User;
import com.facebook.login.LoginManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Calendar;
import java.util.concurrent.Callable;

public class UserProfile extends AppCompatActivity{


    private static final String TAG = UserProfile.class.getName();


    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private ConnectivityCheck connection;
    private ReloadImageResponse reloadImageResponse;
    private LinearLayout baseLayOut;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        baseLayOut = findViewById(R.id.base_layout);

        ((TextView)findViewById(R.id.name)).setText(firebaseUser.getDisplayName());
        ((TextView)findViewById(R.id.email)).setText(firebaseUser.getEmail());

        UserPhoto userPhoto = (UserPhoto) getSupportFragmentManager().findFragmentById(R.id.user_photo);
        bindFrag(userPhoto);

        connection = new ConnectivityCheck(baseLayOut);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.user_profile, new UserProfileButton())
                .commit();


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

    private void reloadUser() {
        firebaseUser.reload().addOnSuccessListener(result -> {
            Log.i(TAG, "User reloaded");
            reloadImageResponse.reload(3001);
        }).addOnFailureListener(result -> {
            Log.w(TAG, "Failed to reload user", result);
        });
    }

    private void onReloaded(boolean reloaded){
        if(reloaded){
            Log.i(TAG, "Reloaded user photo successfully");
        } else Log.w(TAG, "Failed to reload user");
    }

    private void bindFrag(Fragment fragment){
        if(fragment instanceof UserPhoto){
            ((UserPhoto)fragment).setReloadImage(this::onReloaded);
        }
    }

    public  <T extends Fragment> void setReloadImageResponse(ReloadImageResponse reloadImageResponse) {
        this.reloadImageResponse = reloadImageResponse;
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

    private String getYear(){
        return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
    }



    /*

            case R.id.logout: {
                firebaseAuth.signOut();
                if (LoginManager.getInstance() != null) {
                    LoginManager.getInstance().logOut();
                }
                Intent logout = new Intent(UserProfile.this, LogInActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logout);
                finish();
                break;
            }
        }
        return false;
    }

     */

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(!(getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size()-1) instanceof UserProfileButton)){
            getSupportFragmentManager().popBackStack();
        }
    }

}
