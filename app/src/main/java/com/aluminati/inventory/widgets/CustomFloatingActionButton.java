package com.aluminati.inventory.widgets;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.aluminati.inventory.Constants;
import com.aluminati.inventory.HomeActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.scanner.ScannerFragment;
import com.aluminati.inventory.fragments.ui.currencyConverter.ui.CurrencyFrag;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


public class CustomFloatingActionButton extends Fragment implements View.OnClickListener
{
    private final static String TAG = CustomFloatingActionButton.class.getName();
    private FloatingActionButton fab, fab1, fab2;
    private Boolean isFABOpen = false;
    private ScannerFragment scannerFragment;


    public CustomFloatingActionButton(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(getResources().getLayout(R.layout.customfloatingactionbutton),container,true);


        fab = view.findViewById(R.id.fab);
        fab1 = view.findViewById(R.id.fab1);
        fab2 = view.findViewById(R.id.fab2);

        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);

        return view;
    }



    private void showFABMenu(){
        isFABOpen=true;
        openFAB(fab1, R.dimen.standard_55);
        openFAB(fab2, R.dimen.standard_105);
    }

    private void closeFABMenu(){
        isFABOpen=false;
        closeFAB(fab1);
        closeFAB(fab2);
    }


    private void closeFAB(FloatingActionButton fab){
        fab.animate().translationY(0);
        fab.setVisibility(View.INVISIBLE);
    }

    private void openFAB(FloatingActionButton fab, int id){
        fab.animate().translationY(-getResources().getDimension(id));
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
                            scannerFragment = new ScannerFragment();
                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.nav_host_fragment, scannerFragment).commit();
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
}
