package com.aluminati.inventory.firestore;

import android.util.Log;

import com.aluminati.inventory.login.authentication.encryption.PhoneAESEncryption;
import com.aluminati.inventory.users.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;


public class UserFetch {

    private static final String TAG = "UserFetch";

    public static void addNewUser(User user) {
        FirebaseFirestore.getInstance().collection("users").document(user.getEmail())
                .set(user.getUserHashMap()).addOnSuccessListener(
                aVoid -> Log.d(TAG, "Users successfully Added")
        ).addOnFailureListener(e -> Log.w(TAG, "Error Adding User", e));
    }

    public static boolean userExists(String email){
        return FirebaseFirestore.getInstance().collection("users").document(email).get().isSuccessful();
    }

    public static void update(User user){
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getEmail()).set(user.getUserHashMap())
                .addOnSuccessListener(aVoid -> Log.i(TAG, "User successfully updated"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to update user", e));
    }

    public static void update(String email, String attr, Object toUpdate){
        FirebaseFirestore.getInstance().collection("users")
                .document(email).update(attr, toUpdate)
                .addOnSuccessListener(aVoid -> Log.i(TAG, "User successfully updated"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to update user", e));
    }

    public static Task<DocumentSnapshot> getUser(String email){
        return FirebaseFirestore.getInstance().collection("users").document(email).get();

    }

    public static CollectionReference searchUser(){
        return FirebaseFirestore.getInstance().collection("users");
    }

    public static void addReason(String email, String reason){
        HashMap<String, String> reasons = new HashMap<>();
                                reasons.put("reason_deleted", reason);
                                reasons.put("deleted_user_email",email);
        FirebaseFirestore.getInstance().collection("reasons").add(reasons)
                .addOnSuccessListener(result -> Log.i(TAG, "Logged reasons successfully"))
                .addOnFailureListener(result -> Log.w(TAG, "Failed to log reason", result));
    }

    public static void deleteUser(String email){
        FirebaseFirestore.getInstance().collection("users")
                .document(email)
                .delete()
                .addOnSuccessListener(result -> Log.i(TAG, "User deleted from firestore"))
                .addOnFailureListener(result -> Log.w(TAG, "Failed to delete user from firestore", result));
    }
}
