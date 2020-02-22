package com.aluminati.inventory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.aluminati.inventory.currencyConverter.CurrencyFrag;
import com.aluminati.inventory.fragments.ui.purchase.PurchaseFragment;
import com.aluminati.inventory.fragments.ui.receipt.ReceiptFragment;
import com.aluminati.inventory.fragments.ui.recent.RecentFragment;
import com.aluminati.inventory.fragments.ui.rental.RentalFragment;
import com.aluminati.inventory.fragments.ui.scanner.ScannerFragment;
import com.aluminati.inventory.fragments.ui.search.SearchFragment;
import com.aluminati.inventory.fragments.ui.tools.ToolsFragment;

import com.aluminati.inventory.utils.Toaster;
import com.facebook.login.LoginManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.RowId;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private FloatingActionButton fab;
    private DrawerLayout mDrawerLayout;
    private Map<Integer, Fragment> fragMap;
    private Fragment lastOpenFrag;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();

        ((TextView)findViewById(R.id.invi_rights_reserved))
                .setText("| ".concat(getResources()
                        .getString(R.string.app_name))
                        .concat(" " + getYear()).concat(" ®"));


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {

            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                requestCameraPermission();
            } else {
                loadFrag(fragMap.get(R.id.nav_scanner));
            }

        });

        /*
         * If you want to send data from a fragment use pass this handler
         *
         * In your Fragment send objects like this
         * handler.obtainMessage(Constants.SCANNER_FINISHED, new ExampleObject()).sendToTarget();
         *
         * Pass any Object you want then cast
         *
         * or if you want to send a message only
         * handler.obtainMessage(Constants.SCANNER_FINISHED).sendToTarget();
         */
        Handler homeHandler = new Handler(msg -> {
            //do something here
            switch (msg.what) {
                case Constants.SCANNER_STARTED:
                    //ExampleObject obj = (ExampleObject)what.obj;
                    break;
                case Constants.SCANNER_FINISHED:
                    break;

            }
            return false;
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);
        fragMap = new HashMap<Integer, Fragment>();

        /* Why go to all this trouble to have custom titlebar?
         * Gives a lot more options for functionality than the
         * standard titlebar and easier to code listeners
         *
         * We need to pass the drawer into the frag so we can close it
         * if needed. Another benefit of a custom titlebar is we have
         * a lot more options with styling
         */
        /* --If you want a titlebar in your frag extend the FloatingTitlebarFragment
         * --If you want access to the nav drawer for closing and opening pass to constructor
         * --If you want to send data to the call Activity pass a handler
         */
        fragMap.put(R.id.nav_gallery, new PurchaseFragment(mDrawerLayout, homeHandler));
        fragMap.put(R.id.nav_home, new RecentFragment(mDrawerLayout));
        fragMap.put(R.id.nav_send, new ReceiptFragment(mDrawerLayout));
        fragMap.put(R.id.nav_share, new RentalFragment(mDrawerLayout));
        fragMap.put(R.id.nav_slideshow, new SearchFragment(mDrawerLayout));
        fragMap.put(R.id.nav_tools, new ToolsFragment(mDrawerLayout));
        fragMap.put(R.id.maps, new MapsActivity());
        fragMap.put(R.id.nav_scanner, new ScannerFragment());
        fragMap.put(R.id.currency_converions, new CurrencyFrag());

        NavigationView navigationView = findViewById(R.id.nav_view);

                      navigationView.findViewById(R.id.invi_nfo).setOnClickListener(click -> {
                          Utils.invInfo(this);
                      });

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
        //Hide fab if scanner
        fab.setVisibility(frag instanceof ScannerFragment ? View.GONE : View.VISIBLE);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.nav_host_fragment, frag).commit();
        lastOpenFrag = frag;//catch back press when camera is open
    }
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
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



}
