package com.aluminati.inventory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aluminati.inventory.userprofile.UserProfile;
import com.aluminati.inventory.utils.MiscUtils;
import com.facebook.login.LoginManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.Calendar;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    public static HomeActivity homeActivity;

    private CardView floatingTitlebarCard;
    private TableRow floatingTitlebarSearchTable;
    private FloatingActionButton fab;
    private ImageView imgViewMenuBack, imgViewMenuMain, imgViewMainProfile;
    private DrawerLayout drawer;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        homeActivity = this;
        firebaseAuth = FirebaseAuth.getInstance();

        ((TextView)findViewById(R.id.invi_rights_reserved))
                .setText("| ".concat(getResources()
                .getString(R.string.app_name))
                .concat(" " + getYear()).concat(" ®"));



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        findViewById(R.id.invi_info).setOnClickListener(this);



        //Floating titlebar
        floatingTitlebarCard = findViewById(R.id.floatingTitlebarCard);
        floatingTitlebarSearchTable = findViewById(R.id.floatingTitlebarSearchTable);

        imgViewMenuBack = findViewById(R.id.imgViewMenuBack);
        imgViewMenuMain = findViewById(R.id.imgViewMenuMain);
        imgViewMainProfile = findViewById(R.id.imgViewMainProfile);
        imgViewMainProfile.setOnClickListener(this);


        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);


        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_gallery,
                R.id.nav_home,
                R.id.nav_slideshow,
                R.id.nav_tools,
                R.id.nav_share,
                R.id.nav_log_out)
                .setDrawerLayout(drawer)
                .build();

        navigationView.setNavigationItemSelectedListener(this);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

            CharSequence lab = destination.getLabel();
            int searchMode = View.VISIBLE; //default
            int titlebarMode = View.VISIBLE; //default

            if(lab != null) {
                switch (lab.toString()) {
                    case Constants.SCAN_FRAG:
                        searchMode = titlebarMode = View.GONE;
                        break;
                    case Constants.MY_ITEMS_FRAG:
                        searchMode = View.VISIBLE;
                        imgViewMenuBack.setVisibility(View.GONE);
                        break;
                    case Constants.PROFILE_FRAG:
                        searchMode = View.INVISIBLE;
                        imgViewMenuBack.setVisibility(View.VISIBLE);
                        break;
                }

                MiscUtils.setViewsState(Arrays.asList(imgViewMenuMain, floatingTitlebarSearchTable, fab),
                        searchMode);
                floatingTitlebarCard.setVisibility(titlebarMode);
            }

        });

        navigationView.setNavigationItemSelectedListener(this);

    }


    private String getYear(){
        return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == Constants.CAMERA_REQUEST) {

            Log.i(TAG, "Received response for Camera permission request.");

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.");
                navController.navigate(R.id.nav_scanner);
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
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void openSideMenu(View view) {
        if(drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.closeDrawer(Gravity.LEFT);
        } else {
            drawer.openDrawer(Gravity.LEFT);
        }
    }


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
    }
}
