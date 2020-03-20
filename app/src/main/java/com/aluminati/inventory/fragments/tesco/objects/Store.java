package com.aluminati.inventory.fragments.tesco.objects;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Store {

    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("text")
    private String text;
    @Expose
    @SerializedName("number")
    private String number;
    private ArrayList<StandardOpeningHours> standardOpeningHours;
    private LatLng latLng;
    private String day;

    public Store(String name, String text, String number, double longitude, double latitude, ArrayList<StandardOpeningHours> standardOpeningHours) {
        this.name = name;
        this.text = text;
        this.number = number;
        this.standardOpeningHours = standardOpeningHours;
        this.latLng = new LatLng(latitude, longitude);
    }

    public Store(DocumentSnapshot documentSnapshot){
        this.name = documentSnapshot.getString("storeName");
        this.number = documentSnapshot.getString("phone");
        this.latLng = toLatLng(documentSnapshot.getGeoPoint("geoPoint"));
        this.standardOpeningHours = toArrayList((List<Map<String,String>>) documentSnapshot.get("openTimes"));
    }

    private ArrayList<StandardOpeningHours> toArrayList(List<Map<String,String>> openingTimes){
        ArrayList<StandardOpeningHours> standardOpeningHours = new ArrayList<>();
        for(int i = 0; i < openingTimes.size(); i++){
            standardOpeningHours.add(new StandardOpeningHours(toDays(i), openingTimes.get(i).get("open"), openingTimes.get(i).get("close")));
        }
        return standardOpeningHours;
    }

    private String toDays(int i){
        switch (i){
            case 0: return "mo";
            case 1: return "tu";
            case 2: return "we";
            case 3: return "th";
            case 4: return "fr";
            case 5: return "sa";
            case 6: return "su";
        }

        return "";
    }

    private LatLng toLatLng(GeoPoint geoPoint){
        return new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    public Store(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public ArrayList<StandardOpeningHours> getStandardOpeningHours() {
        return standardOpeningHours;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getDay() {
        return day;
    }

    public String getLatLngString(){
        return latLng.latitude + "," + latLng.longitude;
    }
}

