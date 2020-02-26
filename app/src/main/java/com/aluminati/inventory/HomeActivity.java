package com.aluminati.inventory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aluminati.inventory.fragments.MapsActivity;
import com.aluminati.inventory.fragments.ui.currencyConverter.ui.CurrencyFrag;
import com.aluminati.inventory.fragments.purchase.PurchaseFragment;
import com.aluminati.inventory.fragments.receipt.ReceiptFragment;
import com.aluminati.inventory.fragments.recent.RecentFragment;
import com.aluminati.inventory.fragments.rental.RentalFragment;
import com.aluminati.inventory.fragments.scanner.ScannerFragment;
import com.aluminati.inventory.fragments.search.SearchFragment;
import com.aluminati.inventory.fragments.tools.ToolsFragment;

import com.aluminati.inventory.login.authentication.LogInActivity;
import com.aluminati.inventory.payments.ui.PaymentsFrag;
import com.aluminati.inventory.utils.Toaster;
import com.aluminati.inventory.widgets.CustomFloatingActionButton;
import com.aluminati.inventory.widgets.ScannerFragContains;
import com.facebook.login.LoginManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

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
    private ScannerFragContains scannerFragContains;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();

        ((TextView)findViewById(R.id.invi_rights_reserved))
                .setText("| ".concat(getResources()
                        .getString(R.string.app_name))
                        .concat(" " + getYear()).concat(" ®"));


        CustomFloatingActionButton customFloatingActionButton = (CustomFloatingActionButton) getSupportFragmentManager()
                                                                .findFragmentById(R.id.floating_action_button);


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
        /* --If you want a titlebar in your frag extend the FloatingTitlebarFragment or
         *   implement the widget like another android widget
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
        fragMap.put(R.id.payments, new PaymentsFrag());

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



    public void loadFrag(Fragment frag) {
        //Hide fab if scanner
//        fab.setVisibility(frag instanceof ScannerFragment ? View.GONE : View.VISIBLE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.nav_host_fragment, frag).commit();
        if(!(frag instanceof ScannerFragment)){
            scannerFragContains.contiansScannerFrag(false);
        }
        lastOpenFrag = frag;//catch back press when camera is open
    }
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }


    private String getYear(){
        return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
    }


    public void setScannerFragContains(ScannerFragContains scannerFragContains){
        this.scannerFragContains = scannerFragContains;
    }

    @Override
    public void onBackPressed() {

            getSupportFragmentManager().popBackStack("scanner_frag",  FragmentManager.POP_BACK_STACK_INCLUSIVE);
        scannerFragContains.contiansScannerFrag(false);


    }


}
