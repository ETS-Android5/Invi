package com.aluminati.inventory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.aluminati.inventory.fragments.scanner.ScannerFragment;
import com.aluminati.inventory.ui.gallery.PurchaseFragment;
import com.aluminati.inventory.ui.home.HomeFragment;
import com.aluminati.inventory.ui.send.SendFragment;
import com.aluminati.inventory.ui.share.ShareFragment;
import com.aluminati.inventory.ui.slideshow.SlideshowFragment;
import com.aluminati.inventory.ui.tools.ToolsFragment;
import com.aluminati.inventory.utils.Toaster;
import com.facebook.login.LoginManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private FloatingActionButton fab;
    private DrawerLayout mDrawerLayout;
    private Map<Integer, Fragment> fragMap;
    private Fragment lastOpenFrag;
    private FirebaseAuth firebaseAuth;
    public static AtomicBoolean scannerTurendOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        scannerTurendOn = new AtomicBoolean(false);

        firebaseAuth = FirebaseAuth.getInstance();

        ((TextView)findViewById(R.id.invi_rights_reserved))
                .setText("| ".concat(getResources()
                        .getString(R.string.app_name))
                        .concat(" " + getYear()).concat(" ®"));


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {

            if(ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                requestCameraPermission();
            } else {
                loadFrag(fragMap.get(R.id.nav_scanner));
            }

        });

        mDrawerLayout = findViewById(R.id.drawer_layout);
        fragMap = new HashMap<>();

        /* Why go to all this trouble to have custom titlebar?
         * Gives a lot more options for functionality than the
         * standard titlebar and easier to code listeners amoung
         * other things
         *
         * We need to pass the drawer into the nav so we can close it
         * if needed. Another benefit of a custom titlebar is we have
         * a lot more options with styling
         */
        fragMap.put(R.id.nav_gallery, new PurchaseFragment(mDrawerLayout));
        fragMap.put(R.id.nav_home, new HomeFragment(mDrawerLayout));
        fragMap.put(R.id.nav_send, new SendFragment(mDrawerLayout));
        fragMap.put(R.id.nav_share, new ShareFragment(mDrawerLayout));
        fragMap.put(R.id.nav_slideshow, new SlideshowFragment(mDrawerLayout));
        fragMap.put(R.id.nav_tools, new ToolsFragment(mDrawerLayout));
        fragMap.put(R.id.nav_scanner, new ScannerFragment());

        NavigationView navigationView = findViewById(R.id.nav_view);


        navigationView.setNavigationItemSelectedListener(item -> {
            Toaster.getInstance(getApplicationContext()).toastShort("" + item.getTitle());


            switch (item.getItemId()) {
                case R.id.nav_log_out:{
                    FirebaseAuth.getInstance().signOut();
                    if(LoginManager.getInstance() != null){
                        LoginManager.getInstance().logOut();
                    }
                    Intent logout = new Intent(getApplicationContext(), LogInActivity.class);
                    logout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(logout);
                    finish();
                    break;
                }
                default:
                    loadFrag(fragMap.get(item.getItemId()));
            }

            closeDrawer();
            return false;
        });

        loadFrag(fragMap.get(R.id.nav_home));//default nav
    }

    private void loadFrag(Fragment frag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.nav_host_fragment, frag).commit();
        lastOpenFrag = frag;//catch back press when camera is open
    }
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST);
        }
    }

    private String getYear(){
        return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == Constants.CAMERA_REQUEST) {

            Log.i(TAG, "Received response for Camera permission request.");

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.");
                loadFrag(fragMap.get(R.id.nav_scanner));
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
                Snackbar.make(getWindow().getDecorView().getRootView(),
                        R.string.camera_permission_failed,
                        Snackbar.LENGTH_LONG).setAction(R.string.try_again, e ->{
                    requestCameraPermission();
                }).show();
            }
        }
    }


    @Override
    public void onBackPressed() {
        if(lastOpenFrag != null && lastOpenFrag instanceof ScannerFragment) {
            loadFrag(fragMap.get(R.id.nav_home));
        } else {
            super.onBackPressed();
        }

    }

    private void inviInfo(){
        new AlertDialog
                .Builder(this)
                .setView(R.layout.invinfo)
                .setPositiveButton(getResources().getText(R.string.ok), ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }))
                .create()
                .show();

    }

    /*

        @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgViewMainProfile:{
                startActivity(new Intent(HomeActivity.this, UserProfile.class));
                break;
            }
            case R.id.fab:{
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestCameraPermission();
                } else {
                    navController.navigate(R.id.nav_scanner);
                }
                break;
            }
            case R.id.invi_info:{
                inviInfo();
                break;
            }
        }

    }
        @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_log_out:{
                firebaseAuth.signOut();
                if(LoginManager.getInstance() != null){
                    LoginManager.getInstance().logOut();
                }
                startActivity(new Intent(this, LogInActivity.class));
                finish();
                break;
            }
            case R.id.nav_gallery:{

                break;
            }

        }
        return true;

     */

}
