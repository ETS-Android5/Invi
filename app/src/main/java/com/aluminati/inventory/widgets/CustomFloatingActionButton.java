package com.aluminati.inventory.widgets;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.aluminati.inventory.Constants;
import com.aluminati.inventory.HomeActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.purchase.PurchaseFragment;
import com.aluminati.inventory.fragments.purchase.PurchaseItem;
import com.aluminati.inventory.fragments.scanner.ScannerFragment;
import com.aluminati.inventory.fragments.ui.currencyConverter.ui.CurrencyFrag;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class CustomFloatingActionButton extends Fragment implements View.OnClickListener
{
    private final static String TAG = CustomFloatingActionButton.class.getName();
    private FloatingActionButton fab;
    private Button cartFab, cart_count, fab2, fab3, fab1;
    private Boolean isFABOpen = false;
    private ScannerFragment scannerFragment;
    private RelativeLayout relativeLayout;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(getResources().getLayout(R.layout.customfloatingactionbutton),container,true);

        if(getActivity() instanceof HomeActivity){
            ((HomeActivity)getActivity()).setScannerFragContains(this::onContains);
        }


        fab = view.findViewById(R.id.fab);
        fab2 = view.findViewById(R.id.fab2);
        fab3 = view.findViewById(R.id.fab3);
        relativeLayout = view.findViewById(R.id.fab_copy_1);
        cartFab = view.findViewById(R.id.fab1);
        cart_count = view.findViewById(R.id.cart_count);

        view.findViewById(R.id.fab_copy_1).setOnClickListener(this);

        fab.setOnClickListener(this);
        cartFab.setOnClickListener(this);
        relativeLayout.setOnClickListener(this);
        cart_count.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fab3.setOnClickListener(this);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection(String.format(Constants.FirestoreCollections.LIVE_USER_CART,
                FirebaseAuth.getInstance().getUid()))
                .addSnapshotListener((snapshot, e) -> {
            if(snapshot != null && snapshot.size() > 0) {
                int total = 0;
                for(DocumentSnapshot obj : snapshot) {
                    PurchaseItem p = obj.toObject(PurchaseItem.class);
                    total += p.getQuantity();
                }
                cart_count.setVisibility(View.VISIBLE);
                cart_count.setText("" + total);
            } else {
                cart_count.setVisibility(View.INVISIBLE);
                cart_count.setText("");
            }

        });

        return view;
    }


    private void showFABMenu(){
        isFABOpen = true;
        openFABAxis(R.id.fab_copy_1);
        openFABAxis(R.id.fab2);
        openFABAxis(R.id.fab3);
    }

    private void closeFABMenu(){
        isFABOpen = false;
        closeFABYaxis(relativeLayout);
        closeFABYaxis(fab2);
        closeFABXaxis(fab3);
    }


    private void closeFABXaxis(View view){
        view.animate().setDuration(500L);
        view.animate().translationX(0);
        view.setVisibility(View.INVISIBLE);
    }



    private void closeFABYaxis(View view){
        view.animate().setDuration(500L);
        view.animate().translationY(0);
        view.setVisibility(View.INVISIBLE);
    }

    private void openFABAxis(int id){
        switch (id){
            case R.id.fab_copy_1:{
                relativeLayout.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
                relativeLayout.animate().translationX(-getResources().getDimension(R.dimen.standard_55));
                relativeLayout.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.fab2:{
                fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
                fab2.setVisibility(View.VISIBLE);
                break;
            }case R.id.fab3:{
                fab3.animate().translationX(-getResources().getDimension(R.dimen.standard_105));
                fab3.setVisibility(View.VISIBLE);
                break;
            }
        }

        fab.setVisibility(View.VISIBLE);
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == Constants.CAMERA_REQUEST) {

            Log.i(TAG, "Received response for Camera permission request.");

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.");
                setScannerFragment();
                //loadFrag(fragMap.get(R.id.nav_scanner));
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
                Snackbar.make(fab,
                        R.string.camera_permission_failed,
                        Snackbar.LENGTH_LONG).setAction(R.string.try_again, e ->{
                    requestCameraPermission();
                }).show();
            }
        }
    }

    private void setScannerFragment(){
        this.scannerFragment = new ScannerFragment();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.nav_host_fragment, scannerFragment, "scanner_frag").addToBackStack("scanner_frag").commit();
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
           case R.id.fab1:
               replaceFarg(R.id.nav_host_fragment, new PurchaseFragment());
               break;
            case R.id.fab2:{
                replaceFarg(R.id.nav_host_fragment, new CurrencyFrag());
                closeFABMenu();
                break;
            }
            case R.id.fab3:{
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission();
                } else {
                    setScannerFragment();
                }
                closeFABMenu();
                break;
            }
            case R.id.fab: {
                if (!isFABOpen) {
                    showFABMenu();
                } else closeFABMenu();
            }


        }

    }



    private void replaceFarg(int id, Fragment fragment){
        getParentFragmentManager().beginTransaction().replace(id, fragment).commit();
        closeFABMenu();
    }


    public void onContains(Fragment fragment){
        if(!(fragment instanceof ScannerFragment)){
            closeFABMenu();
        }
    }
}
