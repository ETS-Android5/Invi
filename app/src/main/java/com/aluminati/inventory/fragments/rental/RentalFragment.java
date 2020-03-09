package com.aluminati.inventory.fragments.rental;

import android.os.Bundle;
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
import com.aluminati.inventory.R;
import com.aluminati.inventory.adapters.ItemAdapter;
import com.aluminati.inventory.adapters.swipelisteners.ItemSwipe;
import com.aluminati.inventory.binders.RentalBinder;
import com.aluminati.inventory.fragments.FloatingTitlebarFragment;
import com.aluminati.inventory.helpers.DbHelper;
import com.aluminati.inventory.model.PurchaseItem;
import com.aluminati.inventory.model.RentalItem;
import com.aluminati.inventory.utils.Toaster;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RentalFragment extends FloatingTitlebarFragment {
    private static final String TAG = RentalFragment.class.getSimpleName();
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecyclerView recViewPurchase;
    private ItemAdapter itemAdapter;
    private ItemAdapter.OnItemClickListener itemClickListener;
    private Toaster toaster;
    private DbHelper dbHelper;
    private RentalBinder rentalBinder;
    public RentalFragment(DrawerLayout drawer) {
        super(drawer);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_purchase, container, false);
        super.setView(root);

        floatingTitlebar.setLeftToggleOn(false);//dont change icon on toggle
        dbHelper = DbHelper.getInstance();

        rentalBinder = new RentalBinder();
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
        itemClickListener = (ItemAdapter.OnItemClickListener<RentalItem>) item -> {
            toaster.toastShort("You clicked" + item.getTitle());
        };

        dbHelper.getCollection(String.format(Constants.FirestoreCollections.LIVE_USER_CART,
                FirebaseAuth.getInstance().getUid()))
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        Log.d(TAG, "onSuccess: no items");
                        toaster.toastShort(getResources().getString(R.string.no_items_in_cart));
                    } else {
                        loadRentalItems(initRentalItems(snapshot.getDocuments()));
                    }
                }).addOnFailureListener(fail -> {
            toaster.toastShort(getResources().getString(R.string.error_purchase_cart_items));
            Log.d(TAG, fail.getMessage());
        });

        setTrackerSwipe();
        return root;
    }


    private List<RentalItem> initRentalItems(List<DocumentSnapshot> snapshots) {
        List<RentalItem> pItems = new ArrayList<>();
        for(DocumentSnapshot obj : snapshots) {
            RentalItem p = obj.toObject(RentalItem.class);
            p.setDocID(obj.getId());
            pItems.add(p);
        }

        return pItems;
    }
    private void loadRentalItems(List<RentalItem> purchaseItems) {
        recViewPurchase.setAdapter(new ItemAdapter<RentalItem>(purchaseItems,
                itemClickListener,
                rentalBinder,
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

            ItemAdapter<RentalItem> itemAdapter = (ItemAdapter<RentalItem>)recViewPurchase.getAdapter();
            if(itemAdapter != null) {
                RentalItem p = itemAdapter.getItem(position);
                //TODO: Do something here
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