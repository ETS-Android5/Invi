package com.aluminati.inventory;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aluminati.inventory.fragments.googleMaps.MapsActivity;
import com.aluminati.inventory.fragments.ui.currencyConverter.ui.CurrencyFrag;
import com.aluminati.inventory.fragments.purchase.PurchaseFragment;
import com.aluminati.inventory.fragments.receipt.ReceiptFragment;
import com.aluminati.inventory.fragments.recent.RecentFragment;
import com.aluminati.inventory.fragments.rental.RentalFragment;
import com.aluminati.inventory.fragments.scanner.ScannerFragment;

import com.aluminati.inventory.login.authentication.LogInActivity;
import com.aluminati.inventory.offline.ConnectivityCheck;
import com.aluminati.inventory.payments.ui.Card;
import com.aluminati.inventory.payments.ui.Payments;
import com.aluminati.inventory.payments.ui.PaymentsFrag;
import com.aluminati.inventory.utils.Toaster;
import com.aluminati.inventory.utils.Utils;
import com.aluminati.inventory.widgets.ScannerFragContains;
import com.facebook.login.LoginManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.pro100svitlo.creditCardNfcReader.CardNfcAsyncTask;
import com.pro100svitlo.creditCardNfcReader.utils.CardNfcUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class HomeActivity extends AppCompatActivity implements CardNfcAsyncTask.CardNfcInterface {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private FloatingActionButton fab;
    private DrawerLayout mDrawerLayout;
    private Map<Integer, Fragment> fragMap;
    private Fragment lastOpenFrag;
    private FirebaseAuth firebaseAuth;
    private ScannerFragContains scannerFragContains;
    private Card.cardDetails cardDetails;
    private NfcAdapter mAdapter;
    private CardNfcUtils cardNfcUtils;
    private PendingIntent mPendingIntent;
    private boolean mIntentFromCreate;
    private CardNfcAsyncTask mCardNfcAsyncTask;
    private Card.scanNfc scanNfc;
    private ConnectivityCheck connectivityCheck;
    public static AlertDialog alertDialog;
    private static final int ACTION_SETTINGS = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();
        connetionInfo();
        connectivityCheck = new ConnectivityCheck(findViewById(R.id.invi_nfo), alertDialog);
        connectivityCheck.setConnected(this::onConnected);

        ((TextView)findViewById(R.id.invi_rights_reserved))
                .setText("| ".concat(getResources()
                        .getString(R.string.app_name))
                        .concat(" " + getYear()).concat(" ®"));




        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null){
            //do something if there are no nfc module on device
        } else {
            //do something if there are nfc module on device
            mIntentFromCreate = true;
            cardNfcUtils = new CardNfcUtils(this);
            //next few lines here needed in case you will scan credit card when app is closed
            onNewIntent(getIntent());
        }
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
        fragMap.put(R.id.nav_gallery, new PurchaseFragment());
        fragMap.put(R.id.nav_home, new RecentFragment(mDrawerLayout));
        fragMap.put(R.id.nav_receipts, new ReceiptFragment(mDrawerLayout));
        fragMap.put(R.id.nav_rental, new RentalFragment(mDrawerLayout));
        fragMap.put(R.id.maps, new MapsActivity());
        fragMap.put(R.id.nav_scanner, new ScannerFragment());
        fragMap.put(R.id.currency_converions, new CurrencyFrag());
        fragMap.put(R.id.payments, new Payments());

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
                    finish();
                    Intent logout = new Intent(getApplicationContext(), LogInActivity.class);
                    logout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(logout);
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

    private void connetionInfo(){
        alertDialog = new AlertDialog.Builder(this)
                .setView(R.layout.offline_dialog)
                .setCancelable(false)
                .setPositiveButton("Settings", (dialog, i) -> startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), ACTION_SETTINGS)).create();

        alertDialog.setCanceledOnTouchOutside(false);

    }

    private void onConnected(boolean connected){
        if(connected) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            alertDialog.dismiss();
        }else{
            alertDialog.show();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null && !mAdapter.isEnabled()){
            mIntentFromCreate = false;
            //show some turn on nfc dialog here. take a look in the samle ;-)
        } else if (mAdapter != null){
            cardNfcUtils.enableDispatch();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) cardNfcUtils.disableDispatch();
    }

    public void loadFrag(Fragment frag) {
        //Hide fab if scanner
//        fab.setVisibility(frag instanceof ScannerFragment ? View.GONE : View.VISIBLE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.nav_host_fragment, frag).commit();
        if(!(frag instanceof ScannerFragment)){
            //scannerFragContains.contiansScannerFrag(frag);
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

    public void setCardDetails(Card.cardDetails cardDetails){
        this.cardDetails = cardDetails;
    }

    @Override
    public void onBackPressed() {

        if(getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size()-1) instanceof PaymentsFrag){
            getSupportFragmentManager().popBackStack("payments_frag", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }else if(getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size()-1) instanceof Card) {
            getSupportFragmentManager().popBackStack("card", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

       // getSupportFragmentManager().popBackStack("scanner_frag",  FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG,"Card numbe detected");

        for(Fragment fragment : getSupportFragmentManager().getFragments()){
            if(fragment instanceof Card){
                bindFrag(fragment);
            }
        }

        if(intent != null && (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))){
            if (mAdapter != null && mAdapter.isEnabled()) {
                Log.i(TAG,"Card numbe detected");
                //this - interface for callbacks
                //intent = intent :)
                //mIntentFromCreate - boolean flag, for understanding if onNewIntent() was called from onCreate or not
                mCardNfcAsyncTask = new CardNfcAsyncTask.Builder(this, intent, mIntentFromCreate).build();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connectivityCheck);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(connectivityCheck, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if(getIntent().getExtras() != null){
            if(getIntent().getExtras().containsKey("add_card")){
                loadFrag(fragMap.get(R.id.payments));
                Utils.makeSnackBarWithButtons("Card added successfully", mDrawerLayout, this);
            }
        }
    }

    @Override
    public void startNfcReadCard() {
        Toast.makeText(this, "Keep holding card", Toast.LENGTH_LONG).show();
        scanNfc.nfcScan(true);
        Log.i(TAG, "Card detected");
    }

    @Override
    public void cardIsReadyToRead() {
        String card = mCardNfcAsyncTask.getCardNumber();
        String expiredDate = mCardNfcAsyncTask.getCardExpireDate();
        String cardType = mCardNfcAsyncTask.getCardType();

        if(card != null){
            cardDetails.cardDeatilsString(card+"#"+expiredDate+"#"+cardType);
        }
        Log.i(TAG,"Card number " + card + " Card Expiry " + expiredDate + " card type " + cardType);
    }

    @Override
    public void doNotMoveCardSoFast() {
        Toast.makeText(this, "Card moved to fast", Toast.LENGTH_LONG).show();
    }

    @Override
    public void unknownEmvCard() {
        Toast.makeText(this, "Unknown card protocol", Toast.LENGTH_LONG).show();
    }

    @Override
    public void cardWithLockedNfc() {
        Toast.makeText(this, "Card locked", Toast.LENGTH_LONG).show();
    }

    @Override
    public void finishNfcReadCard() {
        Log.i(TAG, "Card read successfully");
    }

    private void cardResult(boolean result){
        if(result){
            Toast.makeText(this, "Card read successfully", Toast.LENGTH_LONG).show();
        }else Toast.makeText(this, "Failed to read card successfully", Toast.LENGTH_LONG).show();

    }

    private void bindFrag(Fragment fragment){
        if(fragment instanceof Card){
            ((Card)fragment).setNfcCardScan(this::cardResult);
        }
    }

    public void setScanNfc(Card.scanNfc scanNfc){
        this.scanNfc = scanNfc;
    }

    public interface nfcCardScan extends Serializable{
        void cardScanned(boolean scanned);
    }
}
