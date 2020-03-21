package com.aluminati.inventory.firestore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class DbHelper {
    private static DbHelper instance;
    private FirebaseFirestore db;

    private DbHelper() {
        db = FirebaseFirestore.getInstance();
    }

    /*
        We will be using this in tasks so good idea to make thread safe
     */
    public static DbHelper getInstance() {

        synchronized (DbHelper.class) {
            if(instance == null) instance = new DbHelper();
        }

        return instance;
    }


    public CollectionReference getCollection(String collectionName) {
        return db.collection(collectionName);
    }

    public Task<DocumentSnapshot> getItem(String collectionName, String docId) {
      return db.collection(collectionName)
                .document(docId)
                .get();
    }

    public Task<DocumentSnapshot> getItem(String docId) {
        return db.document(docId).get();
    }

    public Task<Void> setItem(String collectionName, String docId, Object item) {
        return db.collection(collectionName)
                .document(docId)
                .set(item);
    }

    public Task<DocumentReference> addItem(String collectionName, Object item) {
        return db.collection(collectionName).add(item);
    }


    public Task<Void> setItemWithMerge(String collectionName, String docId, Object item) {
        return db.collection(collectionName)
                .document(docId)
                .set(item, SetOptions.merge());
    }


    public Task<Void> deleteItem(String collectionName, String docId) {
        return db.collection(collectionName)
                .document(docId)
                .delete();
    }
    
}
