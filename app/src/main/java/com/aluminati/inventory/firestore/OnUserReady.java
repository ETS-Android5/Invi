package com.aluminati.inventory.firestore;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnUserReady {
    void userReady(DocumentSnapshot documentSnapshot);
}
