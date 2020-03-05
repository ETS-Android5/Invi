package com.aluminati.inventory.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aluminati.inventory.R;
import com.aluminati.inventory.helpers.DbHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MapsActivity extends Fragment implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getName();

    private GoogleMap mMap;
    private String storeId;
    private DocumentSnapshot documentSnapshot;

    public View onCreateView(@NonNull LayoutInflater inflater,
                         ViewGroup container, Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.activity_maps, container, false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(getArguments() != null && getArguments().containsKey("store_id")){
            storeId = getArguments().getString("store_id");
            DbHelper.getInstance().getItem("stores", storeId)
                    .addOnSuccessListener(success-> {
                        if(success != null){
                            documentSnapshot = success;
                        }
                    })
                    .addOnFailureListener(failure -> {
                        Log.w(TAG, "Failed to get store", failure);
                    });


        }
        return root;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(52.6638, 8.6267);//Limerick :)
        MarkerOptions markerOptions = new MarkerOptions().position(sydney).title("Marker");

        mMap.addMarker(markerOptions);
        mMap.setOnMarkerClickListener(latLng -> {
            CustomMarkerView customMarkerView = CustomMarkerView.newInstance("Map Marker", documentSnapshot);
            customMarkerView.show(getChildFragmentManager(), "map_marker");
            return true;
        });
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


    }






    public static class CustomMarkerView extends DialogFragment implements View.OnClickListener{

        private DocumentSnapshot documentSnapshot;
        private int REQUEST_CODE_PERMISSIONS = 101;
        private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CALL_PHONE};

        public CustomMarkerView(DocumentSnapshot documentSnapshot){
            this.documentSnapshot = documentSnapshot;
        }

        public static CustomMarkerView newInstance(String title, DocumentSnapshot documentSnapshot) {
            CustomMarkerView customMarkerView = new CustomMarkerView(documentSnapshot);
            Bundle args = new Bundle();
            args.putString("title", title);
            customMarkerView.setArguments(args);
            return customMarkerView;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            return inflater.inflate(R.layout.maps_marker, container);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            ((TextView)view.findViewById(R.id.shop_name)).setText((String)documentSnapshot.get("storeName"));
            ((TextView)view.findViewById(R.id.shop_opening_times)).setText((String)documentSnapshot.get("openTimes"));

            view.findViewById(R.id.website_button).setOnClickListener(this);
            view.findViewById(R.id.location_button).setOnClickListener(this);
            view.findViewById(R.id.phone_button).setOnClickListener(this);

        }

        private void askForPermision(){
            ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        public boolean allPermissionsGranted() {
            return ContextCompat.checkSelfPermission(getActivity(), REQUIRED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == REQUEST_CODE_PERMISSIONS) {
                if (!allPermissionsGranted()) {
                    Toast.makeText(getContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                }else{
                    call((String)documentSnapshot.get("phone"));
                }
            }
        }

        private void call(String phone){
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:"+phone));
            startActivity(intent);
        }


        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.website_button:{
                    try {
                        String escapedQuery = URLEncoder.encode((String) documentSnapshot.get("storeName"), "UTF-8");
                        Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }catch (UnsupportedEncodingException e){
                        Log.w(TAG, "", e);
                    }

                    break;
                }
                case R.id.location_button:{
                    String geo = documentSnapshot.get("storeName") + ",+"+
                                 documentSnapshot.get("city") + "+" +
                                 documentSnapshot.get("country");
                    startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + geo)));
                    break;
                }
                case R.id.phone_button:{
                    if(!allPermissionsGranted()){
                        askForPermision();
                    }else{
                        call((String)documentSnapshot.get("phone"));
                    }
                    break;
                }
            }
        }
    }
}
