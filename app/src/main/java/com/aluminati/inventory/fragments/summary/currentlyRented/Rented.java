package com.aluminati.inventory.fragments.summary.currentlyRented;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.adapters.ItemAdapter;
import com.aluminati.inventory.binders.BaseBinder;
import com.aluminati.inventory.binders.RentalBinder;
import com.aluminati.inventory.fragments.purchase.PurchaseItem;
import com.aluminati.inventory.fragments.rental.RentalItem;
import com.aluminati.inventory.helpers.DbHelper;
import com.aluminati.inventory.model.BaseItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class Rented extends Fragment {


    private static final String TAG = Rented.class.getName();
    private static RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView.Adapter adapter;
    private FirebaseUser firebaseUser;
    private TextView placholder, totalRents;
    private RentalBinder rentalBinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(getResources().getLayout(R.layout.currently_rented), container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.currently_rented_recycler_view);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        rentalBinder = new RentalBinder();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        placholder = view.findViewById(R.id.items_on_loan_placeholder);
        totalRents = view.findViewById(R.id.total_rents);

        populateAdapter();
    }

    private void populateAdapter(){
        DbHelper.getInstance().getCollection("rentals")
                .document(firebaseUser.getUid())
                .collection("returned")
                .get()
                .addOnSuccessListener(success -> {
                    Log.i(TAG, "Successfully got collection");
                    ArrayList<RentalItem> arrayList = new ArrayList<>();
                    for(DocumentSnapshot documentSnapshot : success.getDocuments()){
                        arrayList.add(documentSnapshot.toObject(RentalItem.class));
                    }

                    if(!arrayList.isEmpty()) {
                        placholder.setVisibility(View.INVISIBLE);
                        loadPurchaseItems(arrayList);
                    }

                    totalRents.setText(Integer.toString(arrayList.size()));

                })
                .addOnFailureListener(failure -> {
                    Log.i(TAG, "Failed to get collection",failure);
                });
    }

    private void loadPurchaseItems(List<RentalItem> purchaseItems) {
        recyclerView.setAdapter(new ItemAdapter<>(purchaseItems,
                rentalBinder,
                R.layout.rental_item,
                getActivity()));
    }

}
