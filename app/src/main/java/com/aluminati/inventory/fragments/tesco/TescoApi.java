package com.aluminati.inventory.fragments.tesco;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Arrays;
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
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;



public class TescoApi{

    private String result;
    private CompositeDisposable compositeDisposable;
    private ProductReady productReady;
    private final int LIMIT = 1000;


    public TescoApi(String reseult){
        Log.i("TescoProductData", "TescoProductData called");
        this.result = reseult;

    }

    public TescoApi(){

    }

    public void getProduct(){
        this.compositeDisposable = new CompositeDisposable();
        new TescoProductData(getResult()).getData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<TescoProduct>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(TescoProduct tescoProducts) {
                        if(tescoProducts != null){
                            new TescoProductQuery(tescoProducts.getTpnc(), tescoProducts.getDescription())
                                    .getDataTotals(10, new GetItemsCounts())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Totals>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            compositeDisposable.add(d);

                                        }

                                        @Override
                                        public void onSuccess(Totals totals) {
                                            if(totals != null){

                                                int total = Integer.parseInt(totals.getAll()) > 100 ? LIMIT : Integer.parseInt(totals.getAll()) ;

                                                Log.i("Tesco", "Totals " + totals.getAll());
                                                new TescoProductQuery(tescoProducts.getTpnc(), tescoProducts.getDescription())
                                                        .getData(total, new GetItemListResultDeserializer(tescoProducts.getTpnc(), tescoProducts.getDescription()))
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new SingleObserver<Product>() {
                                                            @Override
                                                            public void onSubscribe(Disposable d) {
                                                                compositeDisposable.add(d);
                                                            }

                                                            @Override
                                                            public void onSuccess(Product product) {
                                                                productReady.getProduct(product);
                                                            }

                                                            @Override
                                                            public void onError(Throwable e) {
                                                                Log.i("Tesco", "Prodcut is null", e);


                                                                if(e instanceof HttpException) {
                                                                    ResponseBody body = ((HttpException) e).response().errorBody();


                                                                    try {
                                                                        Log.i("Tesco", body.string());

                                                                    } catch (IOException err) {
                                                                        Log.i("Tesco", "Failed to get", err);
                                                                    }

                                                                }



                                                                productReady.getProduct(null);
                                                            }
                                                        });
                                            }else{
                                                Log.i("Tesco", "totals null");
                                                productReady.getProduct(null);

                                            }
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.i("Tesco", "totals null", e);
                                            productReady.getProduct(null);



                                        }
                                    });

                        }else{
                            Log.i("Tesco", "Product is null");
                            productReady.getProduct(null);

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("Tesco", "Product is null", e);


                        }
                });

    }

    public String getResult() {
        return result;
    }


    public void setProductReady(ProductReady productReady) {
        this.productReady = productReady;
    }

}

class GetItemListDeserializer implements JsonDeserializer<TescoProduct> {

    @Override
    public TescoProduct deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        TescoProduct tescoProduct = new TescoProduct();

        final JsonObject jsonObject = json.getAsJsonObject();
        final JsonArray itemsJsonArray = jsonObject.get("products").getAsJsonArray();

        for (JsonElement itemsJsonElement : itemsJsonArray) {
            final JsonObject itemJsonObject = itemsJsonElement.getAsJsonObject();
            final String des = itemJsonObject.get("description").getAsString();
            final String tpnc = itemJsonObject.get("tpnc").getAsString();
            if(!des.isEmpty() && !tpnc.isEmpty()){
                Log.i("Tesco", des);
                tescoProduct.setDescription(des);
                tescoProduct.setTpnc(tpnc);
            }
        }

        return tescoProduct;
    }
}

class GetItemsCounts implements JsonDeserializer<Totals> {

    @Override
    public Totals deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final JsonObject jsonObject = json.getAsJsonObject().get("uk")
                .getAsJsonObject().get("ghs")
                .getAsJsonObject()
                .get("products")
                .getAsJsonObject()
                .get("totals").getAsJsonObject();

        final String id = jsonObject.get("all").getAsString();
        final String name = jsonObject.get("new").getAsString();
        final String amount = jsonObject.get("offer").getAsString();
        return new Totals(id,name,amount);
    }
}

class GetItemListResultDeserializer implements JsonDeserializer<Product> {

    private String tpnb;
    private String des;


    public GetItemListResultDeserializer(String tpnb, String des){
        Log.i("Tesco", "Call from des + " + des);
        this.des = des;
        this.tpnb = tpnb;
    }

    public String getTpnb() {
        return tpnb;
    }

    public String getDes() {
        return des;
    }

    @Override
    public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {


        Product tescoProduct = new Product();
        boolean exactMatch = false;
        boolean weakMatch = false;
        Product alternative = null;

        final JsonObject jsonObject = json.getAsJsonObject();

        Set<String> en = jsonObject.keySet();
        for(String string : en){
            Log.i("Tesco", "jsobObject " + jsonObject.get("uk")
                    .getAsJsonObject().get("ghs")
                    .getAsJsonObject().get("products"));

        }

        final JsonArray itemsJsonArray = jsonObject.get("uk")
                .getAsJsonObject().get("ghs")
                .getAsJsonObject()
                .get("products")
                .getAsJsonObject()
                .get("results").getAsJsonArray();


        Log.i("Tesco", "Size" + itemsJsonArray.size());




        if(itemsJsonArray.size() == 1){
            final JsonObject itemJsonObject = itemsJsonArray.get(0).getAsJsonObject();
            exactMatch = true;
            final String img = itemJsonObject.get("image").getAsString();
            final String name = itemJsonObject.get("name").getAsString();
            final String price = itemJsonObject.get("price").getAsString();
            JsonElement tmp = itemJsonObject.get("description");
            String description = tmp != null ? tmp.getAsJsonArray().get(0).getAsString() : name;
            final String id = itemJsonObject.get("id").getAsString();

            Log.i("Tesco", name + " " + getDes());

                Log.i("Tesco", "Tesco name matches " + name);
                tescoProduct = loadProduct(id, img, name, price, description);

        }else {

            for (JsonElement itemsJsonElement : itemsJsonArray) {
                final JsonObject itemJsonObject = itemsJsonElement.getAsJsonObject();

                final String img = itemJsonObject.get("image").getAsString();
                final String name = itemJsonObject.get("name").getAsString();
                final String price = itemJsonObject.get("price").getAsString();
                final String id = itemJsonObject.get("id").getAsString();
                JsonElement tmp = itemJsonObject.get("description");
                String description = tmp != null ? tmp.getAsJsonArray().get(0).getAsString() : name;


                //TODO: to much data for logs remove in production
                Log.i("Tesco", "id:" + id + " name:" +
                        name + " desc:" + getDes() + " desc2:"
                        + tmp.getAsJsonArray().toString());

                String des = getDes();
                String desArray = tmp != null ? tmp.getAsJsonArray().toString() : null;

                weakMatch = (des != null && desArray != null && desArray.toLowerCase().contains(des.toLowerCase()));
                alternative = null;
                if(weakMatch && alternative == null) {
                    alternative = loadProduct(id, img, name, price, description);
                }

                exactMatch = !id.isEmpty() && id.equals((getTpnb()));
                if (exactMatch) {
                    Log.i("Tesco", "Tesco name matches " + name);
                    tescoProduct = loadProduct(id, img, name, price, description);
                    tescoProduct.setExactMatch(true);
                    break;
                }

                if (!name.isEmpty() && name.contains(getDes())) {
                    Log.i("Tesco", "Tesco name matches " + name);
                    tescoProduct = loadProduct(id, img, name, price, description);
                    tescoProduct.setExactMatch(true);
                    break;
                }
            }


        }

        return exactMatch ? tescoProduct : (weakMatch ? alternative : new Product()) ;
    }

    private Product loadProduct(String id, String img, String name, String price, String description) {
        Product tescoProduct = new Product();
        tescoProduct.setId(id);
        tescoProduct.setImage(img);
        tescoProduct.setName(name);
        tescoProduct.setPrice(price);
        tescoProduct.setDescription(description);

        return tescoProduct;
    }
}

/*
    "image": "http://img.tesco.com/Groceries/pi/164/4060800128164/IDShot_90x90.jpg",
            "superDepartment": "Drinks",
            "tpnb": 76206044,
            "ContentsMeasureType": "ML",
            "name": "Pepsi Max 12 X 330Ml",
            "UnitOfSale": 1,
            "AverageSellingUnitWeight": 4.4,
            "description": ["Low Calorie Cola Flavoured Soft Drink with Sweeteners"],
            "UnitQuantity": "100ML",
            "id": 282775492,
            "ContentsQuantity": 3960,
            "department": "Fizzy Drinks & Cola",
            "price": 4.75,
            "unitprice": 0.12
 */


class TescoProductQuery{


    //curl -v -X GET "https://dev.tescolabs.com/grocery/products/?query={query}&offset={offset}&limit={limit}"
    private final static String TAG = TescoApi.class.getName();
    private static final String API_URL = "https://dev.tescolabs.com/grocery/";
    private String tpnc;
    private String des;

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



    public TescoProductQuery(String tpnc, String des){
        Log.i("Tesco", des+  " tpnp  " + tpnc);
        this.tpnc = tpnc;
        this.des = des;
    }

    public Single<Product> getData(int limit, Object typeAdapter) {
        return TescoProductQuery
                .getRetrofitInstance(Product.class, typeAdapter)
                .create(TescoProductInfo.class).getProducts(getDes(), Integer.toString(0), Integer.toString(limit));

    }

    public Single<Totals> getDataTotals(int limit, Object typeAdapter) {
        return TescoProductQuery
                .getRetrofitInstance(Totals.class, typeAdapter)
                .create(TescoProductInfo.class).getProductsTotasl(Uri.encode(getDes()), 0, limit);

    }

    public String getTpnc() {
        return tpnc;
    }

    public String getDes() {
        return des;
    }

    public interface TescoProductInfo {
        @Headers({"Ocp-Apim-Subscription-Key: cbc1fdf45b5a454cae665a1d34a8a094", "Cache-Control: no-cache"})
        @GET("products")
        Single<Product> getProducts(@Query("query") String query, @Query("offset") String offset, @Query("limit") String limit);

        @Headers({"Ocp-Apim-Subscription-Key: cbc1fdf45b5a454cae665a1d34a8a094", "Cache-Control: no-cache"})
        @GET("products")
        Single<Totals> getProductsTotasl(@Query("query") String query, @Query("offset") int offset, @Query("limit") int limit);

    }

}


class TescoProductData {


    private final static String TAG = TescoApi.class.getName();
    private static final String API_URL = "https://dev.tescolabs.com/";
    private String barcode;

    private static GsonConverterFactory createGsonConverter(Type type, Object typeAdapter) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(type, typeAdapter);
        Gson gson = gsonBuilder.create();

        return GsonConverterFactory.create(gson);
    }

    private static Retrofit getRetrofitInstance(Type type, Object typeAdapter) {
        return new retrofit2.Retrofit.Builder()
                .baseUrl(API_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(createGsonConverter(type, typeAdapter))
                .build();
    }



    public TescoProductData(String barcode){
        this.barcode = barcode;
    }


    public Single<TescoProduct> getData() {
        return TescoProductData
                .getRetrofitInstance(TescoProduct.class, new GetItemListDeserializer())
                .create(TescoProductQuery.class).getProducts(getBarcode());
    }

    public String getBarcode() {
        return barcode;
    }

    public interface TescoProductQuery {
        @Headers("Ocp-Apim-Subscription-Key: cbc1fdf45b5a454cae665a1d34a8a094")
        @GET("product")
        Single<TescoProduct> getProducts(@Query("gtin") String gtin);
    }


}


