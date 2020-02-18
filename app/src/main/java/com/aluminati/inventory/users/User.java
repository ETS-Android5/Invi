package com.aluminati.inventory.users;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable {

    private static final String TAG = "User";

    private String email;
    private String displayName;
    private String phoneNumber;
    private String photo;
    private boolean isPhoneVerified;
    private boolean isEmailVerified;
    private boolean isGoogleLinked;
    private boolean isFacebookLinked;

    public User(){

    }

    public User(String email, boolean isPhoneVerified, boolean isEmailVerified, boolean isGoogleLinked, boolean isFacebookLinked){
        this.email = email;
        this.isPhoneVerified = isPhoneVerified;
        this.isEmailVerified = isEmailVerified;
        this.isGoogleLinked = isGoogleLinked;
        this.isFacebookLinked = isFacebookLinked;
        this.phoneNumber = "";
    }

    public User(String displayName, String phoneNumber, boolean isPhoneVerified, boolean isEmailVerified){
        this.email = displayName;
        this.phoneNumber = phoneNumber;
        this.isPhoneVerified = isPhoneVerified;
        this.isEmailVerified = isEmailVerified;
    }

    public User(String email, String displayName, String phoneNumber, String photo){
        this.email = email;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.photo = photo;
        this.isPhoneVerified = false;
        this.isEmailVerified = false;
    }

    public User(FirebaseUser firebaseUser){
        this.email = firebaseUser.getEmail();
        this.displayName = firebaseUser.getDisplayName();
        this.phoneNumber = firebaseUser.getPhoneNumber();
        this.photo = uriToString(firebaseUser.getPhotoUrl());
        this.isPhoneVerified = false;
        this.isEmailVerified = false;
        this.isGoogleLinked = false;
        this.isFacebookLinked = false;
    }


    public User(FirebaseUser firebaseUser, boolean isGoogleLinked, boolean isFacebookLinked){
        this.email = firebaseUser.getEmail();
        this.displayName = firebaseUser.getDisplayName();
        this.phoneNumber = firebaseUser.getPhoneNumber();
        this.photo = uriToString(firebaseUser.getPhotoUrl());
        this.isPhoneVerified = false;
        this.isEmailVerified = false;
        this.isGoogleLinked = isGoogleLinked;
        this.isFacebookLinked = isFacebookLinked;
    }

    public User(DocumentSnapshot documentSnapshot){
        try {
            this.displayName = (String) documentSnapshot.get("user_email");
            this.isPhoneVerified = (Boolean) documentSnapshot.get("is_phone_verified");
            this.isEmailVerified = (Boolean) documentSnapshot.get("is_email_verified");
            this.isGoogleLinked = (Boolean) documentSnapshot.get("is_google_linked");
            this.isFacebookLinked = (Boolean) documentSnapshot.get("is_facebook_linked");

            Log.w(TAG, "Facebook " + isFacebookLinked);
        }catch (NullPointerException e){
            Log.w(TAG, "Failed To Read Fields From Document Snapshot", e);
        }
    }

    private String uriToString(Uri photoUri){
        String photoUriString = "";
        if(photoUri != null){
            photoUriString = photoUri.toString();
        }
        return photoUriString;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPhoto(String photo){
        this.photo = photo;
    }

    public String getPhoto(){
        return this.photo;
    }

    public boolean isPhoneVerified() {
        return isPhoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        isPhoneVerified = phoneVerified;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public boolean isGoogleLinked() {
        return isGoogleLinked;
    }

    public void setGoogleLinked(boolean googleLinked) {
        isGoogleLinked = googleLinked;
    }

    public boolean isFacebookLinked() {
        return isFacebookLinked;
    }

    public void setFacebookLinked(boolean facebookLinked) {
        isFacebookLinked = facebookLinked;
    }


    public HashMap<String, Object> getUserHashMap(){
        HashMap<String, Object> user = new HashMap<>();
                                user.put("user_email", getEmail());
                                user.put("phone_number", getPhoneNumber());
                                user.put("is_phone_verified", isPhoneVerified());
                                user.put("is_email_verified", isEmailVerified());
                                user.put("is_facebook_linked", isFacebookLinked());
                                user.put("is_google_linked", isGoogleLinked());
                                return user;
    }

}
