package com.aluminati.inventory.fragments.googleMaps.customMarker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.tesco.objects.StandardOpeningHours;
import com.aluminati.inventory.fragments.tesco.objects.Store;
import com.aluminati.inventory.utils.TextLoader;
import com.aluminati.inventory.widgets.MagicTextView;
import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class CustomMarker extends DialogFragment implements View.OnClickListener, PlacesPhotoRefReady{

    private static final String TAG = CustomMarker.class.getName();
    private Store documentSnapshot;
    private PlacesClient placesClient;
    private static RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private static ArrayList<StandardOpeningHours> data;
    private static RecyclerView.Adapter adapter;
    private ImageView imageView;
    private MagicTextView loader;
    private TextLoader textLoader;

    public CustomMarker(Store documentSnapshot, PlacesClient placesClient){
        this.documentSnapshot = documentSnapshot;
        this.placesClient = placesClient;
    }

    public static CustomMarker newInstance(String title, Store documentSnapshot, PlacesClient placesClient) {
        CustomMarker customMarker = new CustomMarker(documentSnapshot, placesClient);
        Bundle args = new Bundle();
        args.putString("title", title);
        customMarker.setArguments(args);
        return customMarker;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.maps_info_windo, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView)view.findViewById(R.id.shop_name)).setText(documentSnapshot.getName());

        view.findViewById(R.id.website_button).setOnClickListener(this);
        view.findViewById(R.id.location_button).setOnClickListener(this);
        view.findViewById(R.id.phone_button).setOnClickListener(this);

        imageView = view.findViewById(R.id.marker_image);
        loader = view.findViewById(R.id.info_window_loader);

        textLoader = new TextLoader();
        textLoader.setForeground(loader, "Loading...");

        layoutManager = new LinearLayoutManager(getContext());


        recyclerView = view.findViewById(R.id.opening_times_recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        data = documentSnapshot.getStandardOpeningHours();
        adapter = new CustomMarkerAdapter(data);
        recyclerView.setAdapter(adapter);

        CustomMarkerPhoto customMarkerPhoto = new CustomMarkerPhoto(documentSnapshot.getName(), documentSnapshot.getLatLng());
                          customMarkerPhoto.setPlacesPhotoRefReady(this);
                          customMarkerPhoto.getPlacePhotoReference();

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
                    String escapedQuery = URLEncoder.encode(documentSnapshot.getName(), "UTF-8");
                    Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }catch (UnsupportedEncodingException e){
                    Log.w(TAG, "", e);
                }

                break;
            }
            case R.id.location_button:{
                //latitude,longitude
                    /*
                    String geo = documentSnapshot.get("storeName") + ",+"+
                                 documentSnapshot.get("city") + "+" +
                                 documentSnapshot.get("country");

                     */
                Log.i(TAG, documentSnapshot.getLatLng().toString());
                startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+documentSnapshot.getLatLngString())));

                break;
            }
            case R.id.phone_button:{
                call(documentSnapshot.getNumber());
                break;
            }
        }
    }

    @Override
    public void getPlacesPhotoRef(String ref) {
        if(!ref.isEmpty()) {
            if (!ref.equals("Photo Not Found")) {
                Log.i(TAG, ref);
                textLoader.stopLoader();
                loader.setVisibility(View.INVISIBLE);
                Glide.with(this).load("https://maps.googleapis.com/maps/api/place/photo?photoreference=" + ref + "&sensor=false&maxheight=300&maxwidth=300&key=AIzaSyAvRUUySDoRz7Jep4QQb8Zkw8w_ZzDje9o").into(imageView);
            }else{

                textLoader.stopLoader();
                loader.setText("Photo Not Found");
            }
        }
    }
}