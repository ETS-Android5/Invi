package com.aluminati.inventory.payments;

import android.util.Log;

import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESDecryption;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESEncryption;
import com.aluminati.inventory.users.User;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Payment {

    private final static String TAG = Payment.class.getName();
    private String number;
    private String name;
    private String cvs;
    private String expiryDate;


    public Payment(String number, String name, String cvs, String expiryDate){

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

    public String getCvs() {
        return cvs;
    }

    public void setCvs(String cvs) {
        this.cvs = cvs;
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
                .concat(";ExpiryDate=").concat(getExpiryDate()).concat(";Cvs=").concat(getCvs());
    }

    public static void encryptPayments(List<Payment> payments, FirebaseUser firebaseUser){
        String payment = "";
        for(Payment payment1 : payments){
            payment = payment.concat(payment1.toString().concat("#"));
        }

        new PhoneAESEncryption(payment, (result) -> {
            if(firebaseUser != null){
                UserFetch.update(firebaseUser.getEmail(), "pid", result);
            }
        });
    }

    public static void decryptPayments(FirebaseUser firebaseUser){
        if(firebaseUser != null){
            UserFetch.getUser(firebaseUser.getEmail())
                    .addOnSuccessListener(result -> {
                        Log.i(TAG, "Got Payment Successfully");
                        try {
                            User user = new User(result);
                            new PhoneAESDecryption(user.getPhoneNumber(), (dec) -> {

                            });
                        }catch (Exception e){
                            Log.w(TAG, "Failed to decrypt phone number", e);
                        }
                    })
                    .addOnFailureListener(result -> {
                        Log.w(TAG, "Failed to retrieve payment", result);
                    });
        }
    }

    public static ArrayList<Payment> stringToList(String concat){
        ArrayList<Payment> payments = new ArrayList<>();
        String[] concatSplit = concat.split("#");
        for(String ct : concatSplit){
            String[] split = ct.split(";");
            String name = split[0].split("Name=")[1];
            String number = split[1].split("Number=")[1];
            String cvs = split[2].split("ExpiryDate=")[1];
            String expiryDate = split[3].split("Cvs=")[1];
            payments.add(new Payment(number,name,cvs,expiryDate));
        }
        return payments;
    }

}
