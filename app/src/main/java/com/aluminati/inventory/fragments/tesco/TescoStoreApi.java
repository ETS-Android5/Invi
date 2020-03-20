package com.aluminati.inventory.fragments.tesco;

import android.net.Uri;
import android.util.Log;

import com.aluminati.inventory.fragments.tesco.listeners.StoresReady;
import com.aluminati.inventory.fragments.tesco.objects.Product;
import com.aluminati.inventory.fragments.tesco.objects.StandardOpeningHours;
import com.aluminati.inventory.fragments.tesco.objects.Store;
import com.aluminati.inventory.fragments.tesco.objects.Totals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.type.LatLng;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;

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

public class TescoStoreApi {

    private static final String API_URL = "https://dev.tescolabs.com/locations/";
    private String locality;
    private CompositeDisposable compositeDisposable;
    private GetStoreListResultDeserializer getStoreListResultDeserializer;
    private StoresReady storesReady;


    public TescoStoreApi(String locality){
        this.locality = locality;
        this.compositeDisposable = new CompositeDisposable();
        this.getStoreListResultDeserializer = new GetStoreListResultDeserializer();
    }

    public String getLocality() {
        return "near:\"" + locality + "\"";
    }

    public void getStores(){
        getSearchResult()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Store>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(Store store) {
                        if(store != null){
                            storesReady.getStores(getStoreListResultDeserializer.getStores());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("TescoStoreApi", "Failed to get stores", e);

                        if(e instanceof HttpException) {
                            ResponseBody body = ((HttpException) e).response().errorBody();
                            try {
                                Log.i("TescoStoreApi", body.string());

                            } catch (IOException err) {
                                Log.i("TescoStoreApi", "Failed to get", err);
                            }
                        }
                    }
                });
    }

    public Single<Store> getSearchResult(){
        return TescoStoreApi
                .getRetrofitInstance(Store.class, getStoreListResultDeserializer)
                .create(TescoStores.class).getStoresList(getLocality());
    }

    private static GsonConverterFactory createGsonConverter(Type type, Object typeAdapter) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(type, typeAdapter);
        Gson gson = gsonBuilder.create();

        return GsonConverterFactory.create(gson);
    }

    private static Retrofit getRetrofitInstance(Type type, Object typeAdapter) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return new retrofit2.Retrofit.Builder()
                .baseUrl(API_URL)
                .client(builder.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(createGsonConverter(type, typeAdapter))
                .build();
    }

    public void setStoresReady(StoresReady storesReady) {
        this.storesReady = storesReady;
    }

    public interface TescoStores {
        @Headers({"Ocp-Apim-Subscription-Key: cbc1fdf45b5a454cae665a1d34a8a094", "Cache-Control: no-cache"})
        @GET("search")
        Single<Store> getStoresList(@Query("sort") String sort);

    }

}

class GetStoreListResultDeserializer implements JsonDeserializer<Store> {

    private ArrayList<Store> stores;
    private final String[] days = {"mo","tu","we","th","fr","sa","su"};


    public GetStoreListResultDeserializer(){
        this.stores = new ArrayList<>();
    }

    public ArrayList<Store> getStores() {
        return stores;
    }

    @Override
    public Store deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {


        final JsonArray jsonObject = json.getAsJsonObject().get("results").getAsJsonArray();

        for(JsonElement jsonElement : jsonObject){

            ArrayList<StandardOpeningHours> std = new ArrayList<>();

            JsonObject object = jsonElement.getAsJsonObject().getAsJsonObject("location");
            String name = object.get("name").getAsString();

            Log.i("TescoStoreApi", name);

            JsonElement contact = object.getAsJsonObject("contact").getAsJsonObject("address").get("lines").getAsJsonArray().get(0);
            String addressLine = contact.getAsJsonObject().get("text").getAsString();

            Log.i("TescoStoreApi", addressLine);

            JsonElement phoneNumber = object.getAsJsonObject("contact").getAsJsonArray("phoneNumbers").get(0);
            String phoneNbr = phoneNumber.getAsJsonObject().get("number").getAsString();

            Log.i("TescoStoreApi", phoneNbr);

            JsonObject geo = object.getAsJsonObject("geo").getAsJsonObject("coordinates");
            double lng = geo.get("longitude").getAsDouble();
            double lat = geo.get("latitude").getAsDouble();

            Log.i("TescoStoreApi", "Lat " + lat + " lng " + lng);

            JsonElement openingHrs = object.getAsJsonArray("openingHours").get(0).getAsJsonObject().get("standardOpeningHours");


            for(String day : days){
                StandardOpeningHours standardOpeningHours = new Gson().fromJson(openingHrs.getAsJsonObject().get(day).getAsJsonObject(), StandardOpeningHours.class);
                standardOpeningHours.setDay(day);
                std.add(standardOpeningHours);
            }

            stores.add(new Store("Tesco " + name, addressLine, phoneNbr, lng, lat, std));
        }

        return new Store();
    }

}

/*
"results": [{
    "location": {
      "name": "Coonagh Superstore",
      "contact": {
        "address": {
          "lines": [{
            "lineNumber": 1,
            "text": "Coonagh Cross"
          }],
          "town": "Coonagh"
        },
        "phoneNumbers": [{
          "alias": "Default",
          "number": "1890924821"
        }]
      },
      "geo": {
        "coordinates": {
          "longitude": -8.675014,
          "latitude": 52.675005
        }
      },
      "openingHours": [{
        "type": "Trading",
        "standardOpeningHours": {
          "mo": {
            "isOpen": "true",
            "open": "0700",
            "close": "2300"
          },
          "tu": {
            "isOpen": "true",
            "open": "0700",
            "close": "2300"
          },
          "we": {
            "isOpen": "true",
            "open": "0700",
            "close": "2300"
          },
          "th": {
            "isOpen": "true",
            "open": "0700",
            "close": "2300"
          },
          "fr": {
            "isOpen": "true",
            "open": "0700",
            "close": "2300"
          },
          "sa": {
            "isOpen": "true",
            "open": "0700",
            "close": "2300"
          },
          "su": {
            "isOpen": "true",
            "open": "0700",
            "close": "2300"
          }
        },
  }]
}
 */
