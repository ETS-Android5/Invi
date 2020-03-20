package com.aluminati.inventory.fragments.googleMaps.customMarker;

import android.util.Log;

import com.aluminati.inventory.fragments.tesco.TescoStoreApi;
import com.aluminati.inventory.fragments.tesco.listeners.StoresReady;
import com.aluminati.inventory.fragments.tesco.objects.StandardOpeningHours;
import com.aluminati.inventory.fragments.tesco.objects.Store;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public class CustomMarkerPhoto {

    private static final String TAG = CustomMarkerPhoto.class.getName();
    private static final String API_PLACE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/";
    private static final String API_PLACE_PHOTO_URL = "https://maps.googleapis.com/maps/api/place/";
    private static final String API_KEY = "AIzaSyAvRUUySDoRz7Jep4QQb8Zkw8w_ZzDje9o";
    private String locality;
    private LatLng location;
    private CompositeDisposable compositeDisposable;
    private PlacesPhotoRefReady placesPhotoRefReady;


    public CustomMarkerPhoto(String locality, LatLng location){
        this.locality = locality;
        this.location = location;
        this.compositeDisposable = new CompositeDisposable();
    }

    public String getLocality() {
        return locality;
    }

    public String getLocation() {
        return location.latitude + "," + location.longitude;
    }

    public void getPlacePhotoReference(){
        getPlaceSearchResult(getLocality(), getLocation())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(String s) {
                        if(!s.isEmpty()){
                            placesPhotoRefReady.getPlacesPhotoRef(s);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "Failed to get photo", e);
                        placesPhotoRefReady.getPlacesPhotoRef("Photo Not Found");
                    }
                });
    }

    public Single<String> getPlaceSearchResult(String sort, String location){
        return getRetrofitInstance(API_PLACE_URL).create(Places.class).getPlacesId(sort,location,API_KEY);
    }

    private static GsonConverterFactory createGsonConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(String.class, new GetStoreListResultDeserializer());
        Gson gson = gsonBuilder.create();

        return GsonConverterFactory.create(gson);
    }


    private static Retrofit getRetrofitInstance(String url) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return new retrofit2.Retrofit.Builder()
                .baseUrl(url)
                .client(builder.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(createGsonConverter())
                .build();
    }

    public void setPlacesPhotoRefReady(PlacesPhotoRefReady placesPhotoRefReady) {
        this.placesPhotoRefReady = placesPhotoRefReady;
    }

    public interface Places {
        ///json?query=Tesco+Coonagh+Superstore&location=52.675005,-8.675014&key=AIzaSyAvRUUySDoRz7Jep4QQb8Zkw8w_ZzDje9o
        ///https://maps.googleapis.com/maps/api/place/photo?photoreference=PHOTO_REFERENCE&sensor=false&maxheight=MAX_HEIGHT&maxwidth=MAX_WIDTH&key=YOUR_API_KEY
        @GET("json")
        Single<String> getPlacesId(@Query("query") String sort, @Query("location") String location, @Query("key") String key);
    }
}

class GetStoreListResultDeserializer implements JsonDeserializer<String> {

    public GetStoreListResultDeserializer(){

    }


    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        String placeRef = json.getAsJsonObject()
                .get("results").getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("photos").getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("photo_reference").getAsString();

        Log.i("Places", placeRef);
        return placeRef;

    }

}


/*
"html_attributions" : [],
   "results" : [
      {
         "formatted_address" : "Coonagh Cross, Coonagh, Limerick, Ireland",
         "geometry" : {
            "location" : {
               "lat" : 52.6767084,
               "lng" : -8.6747459
            },
            "viewport" : {
               "northeast" : {
                  "lat" : 52.67790502989271,
                  "lng" : -8.673418820107276
               },
               "southwest" : {
                  "lat" : 52.67520537010727,
                  "lng" : -8.67611847989272
               }
            }
         },
         "icon" : "https://maps.gstatic.com/mapfiles/place_api/icons/shopping-71.png",
         "id" : "8addc3d5166c57362904244befd7bba7c4f74bed",
         "name" : "Tesco Superstore",
         "opening_hours" : {
            "open_now" : false
         },
         "photos" : [
            {
               "height" : 3120,
               "html_attributions" : [
                  "\u003ca href=\"https://maps.google.com/maps/contrib/104204130402635882362\"\u003eMark Noonan\u003c/a\u003e"
               ],
               "photo_reference" : "CmRaAAAAFjWMAFzyegQgyf8PN6W2yrNrTe2LufADfcUqg1t8mFz7CuBuod9L61diQkwatvAW6yBiFiEIhLJ3i9fPdH7b_PePPvYVvXEfRVei7779arz7naV4HunHa3kDdE_IeK1EEhAup9KIk29oku1XsW2IiEgAGhS4ttBkQ2_Xu29uTqdOkHSp9HNxHQ",
               "width" : 4160
            }
         ],
         "place_id" : "ChIJScWaistcW0gRmRcf_qMMgEo",
         "plus_code" : {
            "compound_code" : "M8GG+M4 Coonagh, County Limerick",
            "global_code" : "9C4HM8GG+M4"
         },
         "price_level" : 1,
         "rating" : 4.1,
         "reference" : "ChIJScWaistcW0gRmRcf_qMMgEo",
         "types" : [
            "supermarket",
            "convenience_store",
            "grocery_or_supermarket",
            "food",
            "point_of_interest",
            "store",
            "establishment"
         ],
         "user_ratings_total" : 756
      }
   ],
   "status" : "OK"
 */