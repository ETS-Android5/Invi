package com.aluminati.inventory.helpers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    public Task<DocumentSnapshot> getItem(String collectionName, String docId) {
      return db.collection(collectionName)
                .document(docId)
                .get();
    }

    public Task<Void> setItem(String collectionName, String docId, Object item) {
        return db.collection(collectionName)
                .document(docId)
                .set(item);
    }

    public Task<Void> deleteItem(String collectionName, String docId) {
        return db.collection(collectionName)
                .document(docId)
                .delete();
    }



}
