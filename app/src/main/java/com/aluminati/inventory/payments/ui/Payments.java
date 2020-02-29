package com.aluminati.inventory.payments.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.ui.currencyConverter.Currency;
import com.aluminati.inventory.fragments.ui.currencyConverter.ui.CurrencyAdapter;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESDecryption;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESEncryption;
import com.aluminati.inventory.payments.Payment;
import com.aluminati.inventory.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Payments extends Fragment {

    private static final String TAG = Payment.class.getName();
    private static RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private static ArrayList<Payment> data;
    private static RecyclerView.Adapter adapter;
    private static String decCrypt;
    private TextView emptyView;
    private LinearLayout layout;
    private EditText nameField, expiryDate, cardNumber;
    private int pos;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(getResources().getLayout(R.layout.card_views), container, false);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        emptyView = view.findViewById(R.id.empty_recyler_view);

        view.findViewById(R.id.add_card).setOnClickListener(click -> {
            Bundle bundle = new Bundle();
            bundle.putString("dec_string", decCrypt);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.nav_host_fragment, Card.class ,bundle ,"card")
                    .addToBackStack("card")
                    .commit();
        });

        view.findViewById(R.id.delete_card).setOnClickListener(click -> {

            String tmp = cardNumber.getText().toString();
                   tmp = tmp.substring(tmp.length() - 4);
            data.remove(pos);
            recyclerView.removeViewAt(pos);
            clearFields();

            Utils.makeSnackBar("Card ending " + tmp + " deleted successfully", nameField, getActivity());
            if(data.isEmpty()){
                UserFetch.update(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "cidi", "");
            }else {
                encryptPayments(data, FirebaseAuth.getInstance().getCurrentUser());
            }
        });


        nameField = view.findViewById(R.id.card_name);
        cardNumber = view.findViewById(R.id.card_number);
        expiryDate = view.findViewById(R.id.card_expiry);

        layout = view.findViewById(R.id.card_details);

        recyclerView = view.findViewById(R.id.card_view_recycler);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView = view.findViewById(R.id.card_view_recycler);



        decryptPayments(FirebaseAuth.getInstance().getCurrentUser());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        decryptPayments(FirebaseAuth.getInstance().getCurrentUser());
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

                                        if(!data.isEmpty()){
                                            emptyView.setVisibility(View.INVISIBLE);
                                        }

                                        adapter = new PaymentAdapter(data, getActivity());
                                        bind(adapter);
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

    public void encryptPayments(List<Payment> payments, FirebaseUser firebaseUser){
        String payment = "";
        for(Payment payment1 : payments){
            payment = payment.concat(payment1.toString().concat("#"));
        }

        new PhoneAESEncryption(payment, (result) -> {
            if(firebaseUser != null){
                UserFetch.update(firebaseUser.getEmail(), "cidi", result);
            }
        });

    }

    private void clearFields(){
        cardNumber.setText("");
        expiryDate.setText("");
        nameField.setText("");
        layout.setVisibility(View.INVISIBLE);
    }

    private void onPaymentCardRecieved(Payment payment, int position){
        if(payment != null){
            layout.setVisibility(View.VISIBLE);
            cardNumber.setText(payment.getNumber());
            expiryDate.setText(payment.getExpiryDate());
            nameField.setText(payment.getName());
            pos = position;
        }
    }

    private void bind(RecyclerView.Adapter adapter){
        if(adapter instanceof PaymentAdapter) {
            ((PaymentAdapter)adapter).setSelectedCard(this::onPaymentCardRecieved);
        }
    }

    public interface SelectedCard extends Serializable{
        void selectedCard(Payment payment, int postion);
    }
}
