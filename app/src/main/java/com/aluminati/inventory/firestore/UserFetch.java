package com.aluminati.inventory.firestore;

import android.util.Log;

import com.aluminati.inventory.login.authentication.encryption.PhoneAESEncryption;
import com.aluminati.inventory.users.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserFetch {

    private static final String TAG = UserFetch.class.getName();

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

    public static void createTransactionsDoc(String ref, String email, Map<String, List<Map<String, Object>>> data){
        FirebaseFirestore.getInstance().collection("/users/"+email+"/transactions")
                .document(ref).set(data)
                .addOnSuccessListener(success -> {
                    Log.i(TAG, "Transactions added successfully");
                })
                .addOnFailureListener(failure -> {
                    Log.i(TAG, "Failed to add transactions",failure);
                });


    }

    private static void update(String ref, String email, Map<String, Object> map){
        FirebaseFirestore.getInstance().collection("/users/"+email+"/transactions")
                .document(ref).set(map)
                .addOnSuccessListener(success -> {
                    Log.i(TAG, "Document Updated successfully");
                })
                .addOnFailureListener(failure -> {
                    Log.i(TAG, "Failed to update document", failure);
                });
    }

    public static Task<QuerySnapshot> getTransactions(String email){
        return FirebaseFirestore.getInstance().collection("/users/"+email+"/transactions").get();
    }

    public static Task<DocumentSnapshot> getTransactionsByRef(String ref, String email){
        return FirebaseFirestore.getInstance().collection("/users/"+email+"/transactions").document(ref).get();
    }

    public static void getTransactionsDoc(String ref,String email, Map<String, List<Map<String, Object>>> data){
        FirebaseFirestore.getInstance().collection("/users/"+email+"/transactions")
                .document(ref).get()
                .addOnSuccessListener(success -> {
                    Log.i(TAG, "Successfully got document");
                    if(success.contains("transactions")) {
                       List<Map<String, Object>> map =  ((List<Map<String, Object>>) success.get("transactions"));
                       map.add(data.get("transactions").get(0));

                       Map<String, Object> map1 = new HashMap<>();
                       map1.put("transactions", map);

                       update(ref, email, map1);

                    }else{
                        createTransactionsDoc(ref,email,data);
                    }
                })
                .addOnFailureListener(failure -> {
                    Log.i(TAG, "Document doesn't exist", failure);
                    createTransactionsDoc(ref,email,data);
                });
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
