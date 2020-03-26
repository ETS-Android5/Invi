package com.aluminati.inventory.fragments.summary.recenttransaction;

import android.graphics.EmbossMaskFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.fragments.summary.recenttransaction.adapter.TransactionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecentTransactions extends Fragment {

    private static final String TAG = RecentTransactions.class.getName();
    private FirebaseUser firebaseUser;
    private double amount;
    private static RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView.Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(getResources().getLayout(R.layout.recent_transactions), container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView = view.findViewById(R.id.recent_transaction_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        populateTransaction(view);

    }

    private void populateTransaction(View view){

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        if(firebaseUser != null) {
            UserFetch.getTransactions(firebaseUser.getEmail())
                    .addOnSuccessListener(doc -> {
                        ArrayList<Transaction> transactions = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : doc) {
                            Log.i(TAG, documentSnapshot.getId());
                            List<Map<String, Object>> trans = (List<Map<String, Object>>) documentSnapshot.get("transactions");
                            for (Map<String, Object> tr : trans) {
                                amount += Double.parseDouble((String) tr.get("amount"));
                                try {
                                    Date cmpDate = simpleDateFormat.parse((String) tr.get("date"));
                                    long dif = (date.getTime() - cmpDate.getTime()) / (1000 * 60 * 60 * 24);
                                    if (dif < 10) {
                                        transactions.add(new Transaction((String) tr.get("amount"), (String) tr.get("date"), documentSnapshot.getId().equals("cash") ? "cash" : "card"));
                                    }
                                } catch (ParseException e) {
                                    Log.i(TAG, "Failed to parse date", e);
                                }
                            }
                        }

                        ((TextView) view.findViewById(R.id.total_spent)).setText(NumberFormat.getCurrencyInstance(new Locale("en", "IE")).format(amount));

                        if (!transactions.isEmpty()) {
                            view.findViewById(R.id.recent_transaction_recycler_view_placeholder).setVisibility(View.INVISIBLE);
                        }

                        adapter = new TransactionAdapter(transactions, this);
                        recyclerView.setAdapter(adapter);

                    })
                    .addOnFailureListener(failure -> {
                        Log.i(TAG, "Failed to get transactions", failure);
                    });
        }
    }


}
