package com.aluminati.inventory.payments.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.utils.Utils;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.summary.recenttransaction.Transaction;
import com.aluminati.inventory.fragments.summary.recenttransaction.adapter.TransactionAdapter;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESDecryption;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESEncryption;
import com.aluminati.inventory.payments.model.Payment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Payments extends Fragment {

    private static final String TAG = Payment.class.getName();
    private static RecyclerView recyclerView, recyclerViewTransaction;
    private RecyclerView.LayoutManager layoutManager, layoutManagerTransactions;
    private static ArrayList<Payment> data;
    private static ArrayList<Transaction> transactions;
    private static RecyclerView.Adapter adapter, transactionsAdapter;
    private static String decCrypt;
    private TextView emptyView, transactionEmptyView;
    private Button deleteButton;
    private int pos;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(getResources().getLayout(R.layout.card_views), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        layoutManagerTransactions = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        emptyView = view.findViewById(R.id.empty_recyler_view);
        transactionEmptyView = view.findViewById(R.id.transaction_recycler_view_placeholder);


        view.findViewById(R.id.add_card).setOnClickListener(click -> {
            Bundle bundle = new Bundle();
            bundle.putString("dec_string", decCrypt);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.nav_host_fragment, Card.class ,bundle ,"card")
                    .addToBackStack("card")
                    .commit();
        });

        deleteButton = view.findViewById(R.id.delete_card);


        recyclerView = view.findViewById(R.id.card_view_recycler);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerViewTransaction = view.findViewById(R.id.transaction_recycler_view);
        recyclerViewTransaction.setHasFixedSize(true);
        recyclerViewTransaction.setLayoutManager(layoutManagerTransactions);
        recyclerViewTransaction.setItemAnimator(new DefaultItemAnimator());

        decryptPayments(FirebaseAuth.getInstance().getCurrentUser());

        deleteButton.setOnClickListener(click -> {
            String tmp = data.get(pos).getNumber();
            tmp = tmp.substring(tmp.length() - 4);
            data.remove(pos);
            recyclerView.removeViewAt(pos);



            Utils.makeSnackBar("Card ending " + tmp + " deleted successfully", emptyView, getActivity());

            deleteButton.setVisibility(View.INVISIBLE);
            int size = transactions.size();
            transactionsAdapter.notifyItemRangeRemoved(0,size);
            if(data.isEmpty()){
                UserFetch.update(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "cidi", "");
            }else {
                encryptPayments(data, FirebaseAuth.getInstance().getCurrentUser());
            }
        });

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
                            if(result.contains("cidi")) {
                                String cidi = (String) result.get("cidi");
                                if (cidi != null && !cidi.isEmpty()) {
                                    String pid = (String) result.get("pid");
                                    new PhoneAESDecryption(cidi, pid, (dec) -> {
                                        if (dec != null) {
                                            decCrypt = dec;
                                            data = Payment.stringToList(dec);

                                            if (!data.isEmpty()) {
                                                emptyView.setVisibility(View.INVISIBLE);
                                            }

                                            adapter = new PaymentAdapter(data, this);
                                            bind(adapter);
                                            recyclerView.setAdapter(adapter);
                                        }
                                    });
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


    private void onPaymentCardRecieved(int position){

           String cardRef = data.get(position).getCardRef();
           if(cardRef != null){

               UserFetch.getTransactionsByRef(cardRef,FirebaseAuth.getInstance().getCurrentUser().getEmail())
                       .addOnSuccessListener(success -> {
                           transactions = new ArrayList<>();
                           List<Map<String, Object>> trans = (List<Map<String, Object>>)success.get("transactions");
                           for(Map<String, Object> tr : trans){
                               transactions.add(new Transaction((String) tr.get("amount"), (String) tr.get("date"), "card"));
                           }

                           if(!transactions.isEmpty()){
                               transactionEmptyView.setVisibility(View.INVISIBLE);
                           }

                           transactionsAdapter = new TransactionAdapter(transactions, this);
                           recyclerViewTransaction.setAdapter(transactionsAdapter);

                       })
                       .addOnFailureListener(failure -> {
                          Log.i(TAG, "Failed to get transactions", failure);
                       });
           }

    }

    private void setDeleteButtonVisisbile(){
        deleteButton.setVisibility(View.VISIBLE);
    }

    private void bind(RecyclerView.Adapter adapter){
        if(adapter instanceof PaymentAdapter) {
            ((PaymentAdapter)adapter).setSelectedCard(this::onPaymentCardRecieved);
            ((PaymentAdapter)adapter).setSetButtonVisible(this::setDeleteButtonVisisbile);
        }
    }

    public interface SelectedCard extends Serializable{
        void selectedCard(int postion);
    }

    public interface SetButtonVisible extends Serializable{
        void setVisibile();
    }
}
