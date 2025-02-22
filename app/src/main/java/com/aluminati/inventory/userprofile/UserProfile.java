package com.aluminati.inventory.userprofile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.aluminati.inventory.R;
import com.aluminati.inventory.utils.Utils;
import com.aluminati.inventory.fragments.fragmentListeners.socialAccounts.ReloadImageResponse;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfile extends AppCompatActivity{


    private static final String TAG = UserProfile.class.getName();


    private FirebaseUser firebaseUser;
    private ConnectivityCheck connection;
    private ReloadImageResponse reloadImageResponse;
    private AlertDialog alertDialog;
    private static final int ACTION_SETTINGS = 0;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        connetionInfo();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        LinearLayout baseLayOut = findViewById(R.id.base_layout);

        ((TextView)findViewById(R.id.name)).setText(firebaseUser.getDisplayName());
        ((TextView)findViewById(R.id.email)).setText(firebaseUser.getEmail());

        UserPhoto userPhoto = (UserPhoto) getSupportFragmentManager().findFragmentById(R.id.user_photo);
        bindFrag(userPhoto);


        connection = new ConnectivityCheck(baseLayOut, alertDialog);
        connection.setConnected(this::onConnected);


        getSupportFragmentManager().beginTransaction()
                .add(R.id.user_profile, new UserProfileButton())
                .commit();


    }


    private void onConnected(boolean connected){
    }


    private void connetionInfo(){
        this.alertDialog = new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("INVI Needs an Active Internet Connection\n\nCheck your Internet Settings")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .setPositiveButton("Ok", (dialog, i) -> {
                            dialog.dismiss();
                        }
                ).setNegativeButton("Settings", (dialog, i) -> {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), ACTION_SETTINGS);
                }).create();

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(!(getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size()-1) instanceof UserProfileButton)){
            getSupportFragmentManager().popBackStack();
        }
    }

}
