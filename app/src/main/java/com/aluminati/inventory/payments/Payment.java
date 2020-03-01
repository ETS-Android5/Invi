package com.aluminati.inventory.payments;

import android.util.Log;

import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESDecryption;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESEncryption;
import com.aluminati.inventory.users.User;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Payment implements Serializable {

    private final static String TAG = Payment.class.getName();
    private String number;
    private String name;
    private String expiryDate;


    public Payment(String number, String name, String expiryDate){
        this.number = number;
        this.name = name;
        this.expiryDate = expiryDate;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }


    @Override
    public String toString(){
        return "Name=".concat(getName()).concat(";Number=").concat(getNumber())
                .concat(";ExpiryDate=").concat(getExpiryDate());
    }


    public static ArrayList<Payment> stringToList(String concat){
        ArrayList<Payment> payments = new ArrayList<>();
        String[] concatSplit = concat.split("#");
        for(String ct : concatSplit){
            String[] split = ct.split(";");
            String name = split[0].split("Name=")[1];
            String number = split[1].split("Number=")[1];
            String expiryDate = split[2].split("ExpiryDate=")[1];
            payments.add(new Payment(number,name,expiryDate));
        }
        return payments;
    }

}
