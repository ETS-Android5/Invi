package com.aluminati.inventory.fragments.tesco;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.widget.TextView;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Headers;



public class TescoApi{

    private String result;
    private CompositeDisposable compositeDisposable;

    public TescoApi(String reseult){
        Log.i("Tesco", "Tesco called");
        this.result = reseult;
    }

    public void getProduct(){
        this.compositeDisposable = new CompositeDisposable();
        new Tesco(getResult()).getData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<TescoProduct>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(TescoProduct tescoProducts) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

    }

    public String getResult() {
        return result;
    }

}


class Tesco {


    private final static String TAG = TescoApi.class.getName();
    private final String API_URL = "https://dev.tescolabs.com/";
    private Retrofit retrofit;
    private String barcode;



    public Tesco(String barcode){
        this.barcode = barcode;
        this.retrofit = new Retrofit.Builder()
                .baseUrl(API_URL).addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }


    public Single<TescoProduct> getData() {
        TescoProductQuery apiService = retrofit.create(TescoProductQuery.class);
        return apiService.getProducts(this.barcode);
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

