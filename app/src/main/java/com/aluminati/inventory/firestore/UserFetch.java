package com.aluminati.inventory.firestore;

import android.util.Log;

import androidx.annotation.NonNull;

import com.aluminati.inventory.users.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserFetch {

    private static final String TAG = "UserFetch";
    private static final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();



    public static void addNewUser(User user) {
        firebaseFirestore.collection("users").document(user.getEmail())
                .set(user.getUserHashMap()).addOnSuccessListener(
                aVoid -> Log.d(TAG, "Users successfully Added")
        ).addOnFailureListener(e -> Log.w(TAG, "Error Adding User", e));
    }

    public static void update(User user){
        firebaseFirestore.collection("users").document(user.getEmail()).set(user.getUserHashMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "User successfully updated");
            }
        }).addOnFailureListener(e -> Log.w(TAG, "Failed to update user", e));
    }

    public static void update(String email, String attr, Object toUpdate){
        firebaseFirestore.collection("users").document(email).update(attr, toUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "User successfully updated");
            }
        }).addOnFailureListener(e -> Log.w(TAG, "Failed to update user", e));
    }

    public static Task<DocumentSnapshot> getUser(String email){

        DocumentReference documentReference = firebaseFirestore.collection("users").document(email);
        return documentReference.get();

    }

    public static CollectionReference searchUser(){
        return firebaseFirestore.collection("users");
    }
}
