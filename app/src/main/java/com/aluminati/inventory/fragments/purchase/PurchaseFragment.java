package com.aluminati.inventory.fragments.purchase;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.Constants;
import com.aluminati.inventory.binders.PurchaseBinder;
import com.aluminati.inventory.fragments.FloatingTitlebarFragment;
import com.aluminati.inventory.R;
import com.aluminati.inventory.adapters.ItemAdapter;
import com.aluminati.inventory.adapters.swipelisteners.ItemSwipe;
import com.aluminati.inventory.helpers.DbHelper;
import com.aluminati.inventory.utils.Toaster;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PurchaseFragment extends FloatingTitlebarFragment {

    private static final String TAG = PurchaseFragment.class.getSimpleName();
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecyclerView recViewPurchase;
    private ItemAdapter itemAdapter;
    private ItemAdapter.OnItemClickListener itemClickListener;
    private Toaster toaster;
    private DbHelper dbHelper;
    private PurchaseBinder purchaseBinder;

    public PurchaseFragment(DrawerLayout drawer) {
        super(drawer);
    }
    public PurchaseFragment(DrawerLayout drawer, Handler handler) {
        super(drawer, handler);
    }

    public void setNavDrawer(DrawerLayout drawer) {
        this.drawer = drawer;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_purchase, container, false);
        super.setView(root);

        floatingTitlebar.setLeftToggleOn(false);//dont change icon on toggle
        dbHelper = DbHelper.getInstance();

        purchaseBinder = new PurchaseBinder();
        //How to programmatically set icons on floating action bar
        floatingTitlebar.setRightToggleIcons(R.drawable.ic_toggle_list, R.drawable.ic_toggle_grid);
        floatingTitlebar.setToggleActive(true);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        toaster = Toaster.getInstance(getActivity());

        recViewPurchase = root.findViewById(R.id.recViewPurchase);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        recViewPurchase.setLayoutManager(layoutManager);
        itemClickListener = (ItemAdapter.OnItemClickListener<PurchaseItem>) item -> {
            toaster.toastShort("You clicked" + item.getTitle());
        };

        //Load items
        dbHelper.getCollection(String.format(Constants.FirestoreCollections.LIVE_USER_CART,
                FirebaseAuth.getInstance().getUid()))
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        Log.d(TAG, "onSuccess: no items");
                        toaster.toastShort(getResources().getString(R.string.no_items_in_cart));
                    } else {
                        loadPurchaseItems(initPurchaseItems(snapshot.getDocuments()));
                    }
                }).addOnFailureListener(fail -> {
            toaster.toastShort(getResources().getString(R.string.error_purchase_cart_items));
            Log.d(TAG, fail.getMessage());
        });

        setTrackerSwipe();
        setUpCartListener();
        return root;
    }

    private void setUpCartListener() {
        firestore.collection(String.format(Constants.FirestoreCollections.LIVE_USER_CART,
                FirebaseAuth.getInstance().getUid())).addSnapshotListener((snapshot, e) -> {
                    if(snapshot != null && snapshot.size() > 0) {
                        loadPurchaseItems(initPurchaseItems(snapshot.getDocuments()));
                        Log.d(TAG, "addSnapshotListener: Items found " + snapshot.size());
                    } else {
                        recViewPurchase.setAdapter(null);
                    }

                });

    }

    private List<PurchaseItem> initPurchaseItems(List<DocumentSnapshot> snapshots) {
        List<PurchaseItem> pItems = new ArrayList<>();
        for(DocumentSnapshot obj : snapshots) {
            PurchaseItem p = obj.toObject(PurchaseItem.class);
            p.setDocID(obj.getId());
            pItems.add(p);
        }

        return pItems;
    }
    private void loadPurchaseItems(List<PurchaseItem> purchaseItems) {
        recViewPurchase.setAdapter(new ItemAdapter<PurchaseItem>(purchaseItems,
                itemClickListener,
                purchaseBinder,
                R.layout.list_item,
                getActivity()));
    }
    @Override
    public void onRightButtonToggle(boolean isActive) {
        super.onRightButtonToggle(isActive);
        //TODO Do something here
    }

    private void setTrackerSwipe() {
        ItemSwipe.ItemHelperListener leftSwipe = (viewHolder, direction, position) -> {
            Log.d(TAG, "leftSwipe position: " + position);

            ItemAdapter<PurchaseItem> itemAdapter = (ItemAdapter<PurchaseItem>)recViewPurchase.getAdapter();
            if(itemAdapter != null) {
                PurchaseItem p = itemAdapter.getItem(position);

                recViewPurchase
                        .getAdapter()
                        .notifyItemChanged(position);
                    dbHelper.deleteItem(String.format(Constants.FirestoreCollections.LIVE_USER_CART,
                            FirebaseAuth.getInstance().getUid()), p.getDocID())
                            .addOnSuccessListener(task ->{
                                toaster.toastShort(getResources().getString(R.string.item_removed_from_cart));
                            });

                itemAdapter.notifyItemChanged(position);
            }
        };


        ItemTouchHelper.SimpleCallback touchHelper = new ItemSwipe(0, ItemTouchHelper.LEFT,leftSwipe);

        new ItemTouchHelper(touchHelper).attachToRecyclerView(recViewPurchase);
    }

    @Override
    public void onTextChanged(String searchText) {

    }
}