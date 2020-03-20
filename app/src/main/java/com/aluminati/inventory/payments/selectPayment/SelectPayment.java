package com.aluminati.inventory.payments.selectPayment;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.purchase.GetCardRefNumber;
import com.aluminati.inventory.fragments.tesco.objects.StandardOpeningHours;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESDecryption;
import com.aluminati.inventory.payments.model.Payment;
import com.aluminati.inventory.payments.ui.PaymentAdapter;
import com.aluminati.inventory.utils.Toaster;
import com.github.mrengineer13.snackbar.SnackBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.ArrayList;

public class SelectPayment extends DialogFragment {

    private static final String TAG = SelectPayment.class.getName();
    private static RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private static ArrayList<Payment> data;
    private static RecyclerView.Adapter adapter;
    private static String decCrypt;
    private int pos = 0;
    private GetCardRefNumber getCardRefNumber;


    public SelectPayment(){

    }

    public static SelectPayment getInstance(){
        return new SelectPayment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(getResources().getLayout(R.layout.select_payment), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView = view.findViewById(R.id.select_card_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        view.findViewById(R.id.pay_card_button).setOnClickListener(click -> {
            getCardRefNumber.getCardRef(data.get(pos).getCardRef());
            dismiss();
        });

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
                                String pid = (String) result.get("pid");
                                if((cidi != null && pid != null) && (!cidi.isEmpty() && !pid.isEmpty())) {
                                    new PhoneAESDecryption(cidi, pid, (dec) -> {
                                        if (dec != null) {
                                            decCrypt = dec;
                                            data = Payment.stringToList(dec);
                                            adapter = new PaymentAdapter(data, this);
                                            bind(adapter);
                                            recyclerView.setAdapter(adapter);
                                        }
                                    });
                                }else{
                                    dismiss();
                                    Toast.makeText(getContext(),"No Payments Cards Added", Toast.LENGTH_LONG ).show();
                                }
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

    private void onPaymentCardRecieved(int position){
            this.pos = position;
            Log.i(TAG, "Select Payment");
    }

    private void bind(RecyclerView.Adapter adapter){
        if(adapter instanceof PaymentAdapter) {
            ((PaymentAdapter)adapter).setSelectedCard(this::onPaymentCardRecieved);
        }
    }

    public void setGetCardRefNumber(GetCardRefNumber getCardRefNumber) {
        this.getCardRefNumber = getCardRefNumber;
    }
}
