package com.aluminati.inventory;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aluminati.inventory.utils.MiscUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;


public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private CardView floatingTitlebarCard;
    private TableRow floatingTitlebarSearchTable;
    private FloatingActionButton fab;
    private ImageView imgViewMenuBack, imgViewMenuMain,imgViewMainProfile;

    private DrawerLayout drawer;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);

        //Floating titlebar
        floatingTitlebarCard = findViewById(R.id.floatingTitlebarCard);
        floatingTitlebarSearchTable = findViewById(R.id.floatingTitlebarSearchTable);

        imgViewMenuBack = findViewById(R.id.imgViewMenuBack);
        imgViewMenuMain = findViewById(R.id.imgViewMenuMain);
        imgViewMainProfile = findViewById(R.id.imgViewMainProfile);

        fab.setOnClickListener(v -> {

            if(ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                requestCameraPermission();
            } else {
                navController.navigate(R.id.nav_scanner);
            }

        });

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_gallery,
                R.id.nav_home,
                R.id.nav_slideshow,
                R.id.nav_tools,
                R.id.nav_share,
                R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();

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


}
