package com.aluminati.inventory.payments.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.ui.currencyConverter.Currency;
import com.aluminati.inventory.fragments.ui.currencyConverter.ui.CurrencyAdapter;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESDecryption;
import com.aluminati.inventory.payments.Payment;
import com.aluminati.inventory.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Payments extends Fragment {

    private static final String TAG = Payment.class.getName();
    private static RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private static ArrayList<Payment> data;
    private static RecyclerView.Adapter adapter;
    private static String decCrypt;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(getResources().getLayout(R.layout.card_views), container, false);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);


        view.findViewById(R.id.add_card).setOnClickListener(click -> {
            Bundle bundle = new Bundle();
            bundle.putString("dec_string", decCrypt);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, Card.class ,bundle ,"card")
                    .addToBackStack("card")
                    .commit();
        });

        recyclerView = view.findViewById(R.id.card_view_recycler);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView = view.findViewById(R.id.card_view_recycler);

        decryptPayments(FirebaseAuth.getInstance().getCurrentUser());

        return view;
    }

    private void decryptPayments(FirebaseUser firebaseUser){
        if(firebaseUser != null){
            UserFetch.getUser(firebaseUser.getEmail())
                    .addOnSuccessListener(result -> {
                        Log.i(TAG, "Got Payment Successfully");
                        try {
                            if(result.contains("cidi")){
                                String cidi = (String)result.get("cidi");
                                String pid = (String)result.get("pid");
                                new PhoneAESDecryption(cidi, pid, (dec) -> {
                                    if(dec != null){
                                        decCrypt = dec;
                                        data = Payment.stringToList(dec);
                                        adapter = new PaymentAdapter(data, getActivity());
                                        recyclerView.setAdapter(adapter);
                                    }
                                });
                            }

                        }catch (Exception e){
                            Log.w(TAG, "Failed to decrypt phone number", e);
                        }
                    })
                    .addOnFailureListener(result -> {
                        Log.w(TAG, "Failed to retrieve payment", result);
                    });
        }
    }
}
