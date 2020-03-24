package com.aluminati.inventory.fragments.receipt;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.Constants;
import com.aluminati.inventory.R;
import com.aluminati.inventory.adapters.ItemAdapter;
import com.aluminati.inventory.binders.ReceiptsBinder;
import com.aluminati.inventory.fragments.FloatingTitlebarFragment;
import com.aluminati.inventory.firestore.DbHelper;
import com.aluminati.inventory.helpers.DialogHelper;
import com.aluminati.inventory.utils.Toaster;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceiptFragment extends FloatingTitlebarFragment {
    private static final String TAG  = ReceiptFragment.class.getName();
    private RecyclerView revViewReceipts;
    private ItemAdapter.OnItemClickListener<ReceiptItem> itemClickListener;
    private ReceiptsBinder receiptBinder;
    private DbHelper dbHelper;
    private DialogHelper dialogHelper;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private Toaster toaster;
    private double currentTotal;

    public ReceiptFragment(DrawerLayout drawer) {
        super(drawer);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_receipt, container, false);
        setView(root);

        revViewReceipts = root.findViewById(R.id.revViewReceipts);

        floatingTitlebar.setLeftToggleOn(false);//dont change icon on toggle
        dbHelper = DbHelper.getInstance();
        dialogHelper = DialogHelper.getInstance(getActivity());

        receiptBinder = new ReceiptsBinder();
        //How to programmatically set icons on floating action bar
        floatingTitlebar.setRightToggleIcons(R.drawable.ic_search, R.drawable.ic_dollar);
        floatingTitlebar.setLeftToggleIcons(R.drawable.ic_side_list_blue, R.drawable.ic_side_list_blue);
        floatingTitlebar.setToggleActive(true);
        floatingTitlebar.showTitleTextBar();

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        toaster = Toaster.getInstance(getActivity());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        revViewReceipts.setLayoutManager(layoutManager);

        itemClickListener = item -> {

            dbHelper.getItem(item.getItemref())
                    .addOnSuccessListener( snapshot -> {

                        if(snapshot != null) {
                            List<ReceiptListItem> items = new ArrayList<>();

                            List<String> results = (List<String>)snapshot.get("items");
                            Gson gson = new Gson();

                            if(results!= null) {
                                for(String json : results) {
                                    ReceiptListItem li = gson.fromJson(json, ReceiptListItem.class);
                                    if(li != null) {
                                        items.add(li);
                                    }
                                }
                            }

                            if(items.size() > 0) {
                                dialogHelper.createDialog(dialogHelper.buildReceiptView(items))
                                        .setPositiveButton("Ok", (dl , i) ->{
                                            dl.dismiss();
                                        }).show();
                            }

                        } else toaster.toastShort(getResources().getString(R.string.item_is_null));
                    })
                    .addOnFailureListener(ex -> {
                        toaster.toastShort(ex.getMessage());
                    });
        };

        reloadItems(null);
        setUpReceiptListener();
        return root;
    }

    private void setUpReceiptListener() {
        dbHelper.getCollection(String.format(Constants.FirestoreCollections.RECEIPTS_TEST,
                FirebaseAuth.getInstance().getUid()))
                .addSnapshotListener((snapshot, e) -> {
            if(snapshot != null && snapshot.size() > 0) {
                loadPurchaseItems(initReceiptItems(snapshot.getDocuments(), null));
                Log.d(TAG, "addSnapshotListener: Items found " + snapshot.size());
            } else {
                revViewReceipts.setAdapter(null);
                floatingTitlebar.setTitleText("");
            }

        });

    }


    private void loadPurchaseItems(List<ReceiptItem> receiptItems) {
        revViewReceipts.setAdapter(new ItemAdapter<ReceiptItem>(receiptItems,
                itemClickListener,
                receiptBinder,
                R.layout.receipts_item,
                getActivity()));
    }

    private List<ReceiptItem> initReceiptItems(List<DocumentSnapshot> snapshots, String filter) {
        List<ReceiptItem> pItems = new ArrayList<>();
        currentTotal = 0;
        for(DocumentSnapshot obj : snapshots) {
            ReceiptItem item  = obj.toObject(ReceiptItem.class);
            currentTotal += item.getTotal();
            if(filter == null || filter.isEmpty()) {
                pItems.add(item);
            } else {
                String f = filter.toLowerCase();
                if(f.contains("pur") && !item.isRental()) {
                    pItems.add(item);
                } else if(f.contains("ren") && item.isRental()) {
                    pItems.add(item);
                } else if(new Date(Long.parseLong(item.getTimestamp())).toString().contains(filter)
                || String.valueOf(item.getTotal()).contains(filter)){
                    pItems.add(item);
                }
            }

        }

        floatingTitlebar.setTitleText(String.format("Total: €%.2f", currentTotal > 0 ? currentTotal : 0));

        return pItems;
    }
    @Override
    public void onTextChanged(String searchText) {
        reloadItems(searchText);
    }

    private void reloadItems(String filter) {

        dbHelper.getCollection(String.format(Constants.FirestoreCollections.RECEIPTS_TEST,
                FirebaseAuth.getInstance().getUid()))
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        Log.d(TAG, "onSuccess: no items");
                        toaster.toastShort(getResources().getString(R.string.no_receipts));
                    } else {
                        loadPurchaseItems(initReceiptItems(snapshot.getDocuments(), filter));
                    }
                }).addOnFailureListener(fail -> {
            toaster.toastShort(getResources().getString(R.string.error_purchase_cart_items));
            Log.d(TAG, fail.getMessage());
        });

    }

    @Override
    public void onRightButtonToggle(boolean isActive) {
        super.onRightButtonToggle(isActive);
        if(isActive) {
            floatingTitlebar.showSearchBar();
        } else {
            floatingTitlebar.showTitleTextBar();
        }
    }
}