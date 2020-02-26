package com.aluminati.inventory.widgets;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aluminati.inventory.Constants;
import com.aluminati.inventory.HomeActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.FloatingTitlebarFragment;
import com.aluminati.inventory.fragments.scanner.ScannerFragment;
import com.aluminati.inventory.fragments.ui.currencyConverter.ui.CurrencyFrag;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


public class CustomFloatingActionButton extends Fragment implements View.OnClickListener
{
    private final static String TAG = CustomFloatingActionButton.class.getName();
    private FloatingActionButton fab, fab1, fab2, fab3;
    private Boolean isFABOpen = false;
    private ScannerFragment scannerFragment;
    private DrawerLayout drawerLayout;
    private ScannerFragContains scannerFragContains;


    public CustomFloatingActionButton(){

    }

    public void setDrawerLayout(DrawerLayout drawerLayout){
        this.drawerLayout = drawerLayout;
    }




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(getResources().getLayout(R.layout.customfloatingactionbutton),container,true);


        if(getActivity() instanceof HomeActivity){
            ((HomeActivity)getActivity()).setScannerFragContains(this::onContains);
        }


        fab = view.findViewById(R.id.fab);
        fab1 = view.findViewById(R.id.fab1);
        fab2 = view.findViewById(R.id.fab2);
        fab3 = view.findViewById(R.id.fab3);

        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fab3.setOnClickListener(this);



        return view;
    }


    private void showFABMenu(){
        isFABOpen=true;
        openFABAxis(fab1);
        openFABAxis(fab2);
        openFABAxis(fab3);
    }

    private void closeFABMenu(){
        isFABOpen=false;
        closeFABYaxis(fab1);
        closeFABYaxis(fab2);
        closeFABXaxis(fab3);
    }


    private void closeFABXaxis(FloatingActionButton fab){
        fab.animate().setDuration(500L);
        fab.animate().translationX(0);
        fab.setVisibility(View.INVISIBLE);
    }



    private void closeFABYaxis(FloatingActionButton fab){
        fab.animate().setDuration(500L);
        fab.animate().translationY(0);
        fab.setVisibility(View.INVISIBLE);
    }

    private void openFABAxis(FloatingActionButton fab){
        switch (fab.getId()){
            case R.id.fab1:{
                fab.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
                fab.animate().translationX(-getResources().getDimension(R.dimen.standard_55));
                fab.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.fab2:{
                fab.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
                fab.setVisibility(View.VISIBLE);
                break;
            }case R.id.fab3:{
                fab.animate().translationX(-getResources().getDimension(R.dimen.standard_105));
                fab.setVisibility(View.VISIBLE);
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


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab: {
                if(getActivity() instanceof HomeActivity) {
                    if (getActivity().getSupportFragmentManager().getFragments().contains(scannerFragment)) {
                        if (!isFABOpen) {
                            showFABMenu();
                        } else {
                            closeFABMenu();
                        }
                    } else {


                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestCameraPermission();
                        } else {
                            this.scannerFragment = new ScannerFragment();
                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.add(R.id.nav_host_fragment, scannerFragment, "scanner_frag").addToBackStack("scanner_frag").commit();
                        }
                    }
                }
                break;
            }case R.id.fab1:{

                break;
            }case R.id.fab2:{
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, new CurrencyFrag()).commit();
                closeFABMenu();
                break;
            }
        }
    }


    public void onContains(boolean conatins){
        if(!conatins){
            closeFABMenu();
        }
    }






}
