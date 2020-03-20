package com.aluminati.inventory.fragments.googleMaps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.googleMaps.customMarker.CustomMarker;
import com.aluminati.inventory.fragments.tesco.TescoStoreApi;
import com.aluminati.inventory.fragments.tesco.listeners.StoresReady;
import com.aluminati.inventory.fragments.tesco.objects.Store;
import com.aluminati.inventory.helpers.DbHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import static android.content.Context.LOCATION_SERVICE;

public class MapsActivity extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnInfoWindowClickListener, StoresReady {

    private static final String TAG = MapsActivity.class.getName();

    private GoogleMap mMap;
    private String storeId;
    private ArrayList<DocumentSnapshot> documentSnapshot = new ArrayList<>();
    private ArrayList<Store> stores;
    private LocationManager locationManager;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final long LOCATION_REFRESH_TIME = (60 * 60);
    private final float LOCATION_REFRESH_DISTANCE = 1000;
    private Location location;
    private PlacesClient placesClient;
    private boolean contains = true;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.activity_maps, container, false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Places.initialize(getContext(), "AIzaSyAvRUUySDoRz7Jep4QQb8Zkw8w_ZzDje9o");

        placesClient = Places.createClient(getContext());

        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);



        if (getArguments() != null && getArguments().containsKey("store_id")) {
            contains = false;
            storeId = getArguments().getString("store_id");
            DbHelper.getInstance().getItem("stores", storeId)
                    .addOnSuccessListener(success -> {
                        if (success != null) {
                            Log.i(TAG, "Success " + success.get("storeName"));

                            documentSnapshot.add(success);
                            GeoPoint geoPoint = documentSnapshot.get(0).getGeoPoint("geoPoint");
                            if(geoPoint != null) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()), 8.0f));
                                mMap.addMarker(new MarkerOptions().position(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude())).title(documentSnapshot.get(0).getString("storeName"))).setTag(Integer.toString(0));
                            }
                        }
                    })
                    .addOnFailureListener(failure -> {
                        Log.w(TAG, "Failed to get store", failure);
                    });


        } else {
            DbHelper.getInstance().getCollection("stores")
                    .get()
                    .addOnSuccessListener(success -> {
                        for (int i = 0; i < success.getDocuments().size(); i++) {
                            documentSnapshot.add(success.getDocuments().get(i));
                            double lat = success.getDocuments().get(i).getGeoPoint("geoPoint").getLatitude();
                            double lng = success.getDocuments().get(i).getGeoPoint("geoPoint").getLongitude();
                            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(success.getDocuments().get(i).getString("storeName")));
                            marker.setTag(Integer.toString(i));
                        }
                    })
                    .addOnFailureListener(failure -> {
                        Log.w(TAG, "Failed to get store collection", failure);
                    });

            if (locationManager != null) {
                if (!allPermissionsGranted()) {
                    askForPermision();
                } else {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        askForPermision();
                    }else {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, this);
                        location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                        if(location != null) {
                            String locality = getLocality(getContext(), location.getLatitude(), location.getLongitude());
                            if(locality != null){

                                TescoStoreApi tescoStoreApi = new TescoStoreApi(locality);
                                tescoStoreApi.getStores();
                                tescoStoreApi.setStoresReady(this);
                            }
                        }
                    }
                }
            }
        }

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
            }
        }
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

        if(location != null && contains){
                getmMap().animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 7.0f));
        }

        mMap.setOnInfoWindowClickListener(this);
    }

    public GoogleMap getmMap() {
        return mMap;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location " + location.getAltitude() + " " + location.getLatitude() + " " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if(marker.getTag() != null) {
            int index = Integer.parseInt((String) marker.getTag());

            if(index < documentSnapshot.size()){
                CustomMarker customMarkerView = CustomMarker.newInstance("Map Marker", new Store(documentSnapshot.get(index)), placesClient);
                                 customMarkerView.show(getChildFragmentManager(), "map_marker");
            }else{

                index = (index - documentSnapshot.size());
                CustomMarker customMarkerView = CustomMarker.newInstance("Map Marker", stores.get(index), placesClient);
                customMarkerView.show(getChildFragmentManager(), "map_marker");
            }
        }
    }

    public String getLocality(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            return geocoder.getFromLocation(lat,lng,1).get(0).getLocality();
        } catch (IOException e) {
            Log.i(TAG, "Failed to get address",e);
            return null;
        }
    }

    @Override
    public void getStores(ArrayList<Store> stores) {
        Log.i(TAG, "From maps " + stores.size());
        this.stores = stores;
        int index = documentSnapshot.size();
        for(int i = 0; i < stores.size(); i++){
            getmMap().addMarker(new MarkerOptions().position(stores.get(i).getLatLng()).title(stores.get(i).getName())).setTag(Integer.toString(index+i));
        }
    }



}
