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
import com.aluminati.inventory.helpers.DialogHelper;
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
    private RecyclerView recViewRental;
    private ItemAdapter itemAdapter;
    private ItemAdapter.OnItemClickListener itemClickListener;
    private Toaster toaster;
    private DbHelper dbHelper;
    private RentalBinder rentalBinder;
    private DialogHelper dialogHelper;
    public RentalFragment(DrawerLayout drawer) {
        super(drawer);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rental, container, false);

        super.setView(root);

        auth = FirebaseAuth.getInstance();

        floatingTitlebar.setLeftToggleOn(false);//dont change icon on toggle
        dbHelper = DbHelper.getInstance();
        dialogHelper = DialogHelper.getInstance(root.getContext());

        rentalBinder = new RentalBinder();
        //How to programmatically set icons on floating action bar
        floatingTitlebar.setRightToggleIcons(R.drawable.ic_refresh, R.drawable.ic_toggle_grid);
        floatingTitlebar.setToggleActive(false);

        firestore = FirebaseFirestore.getInstance();

        toaster = Toaster.getInstance(getActivity());

        recViewRental = root.findViewById(R.id.recViewRental);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);

        recViewRental.setLayoutManager(layoutManager);
        itemClickListener = (ItemAdapter.OnItemClickListener<RentalItem>) item -> {
            dialogHelper.createDialog(item.getTitle(),
                    String.format("Checkedout:%s\n\n%s",
                            item.getCheckedOutDate()
                    , item.getDescription()),
                    null, null).show();

        };

        reloadItems();

        setTrackerSwipe();
        setUpLiveListener();
        return root;
    }

    private void reloadItems() {

        dbHelper.getCollection(Constants.FirestoreCollections.RENTALS)
                .whereEqualTo("uid", auth.getCurrentUser().getUid())
                .get().addOnSuccessListener(snapshot -> {

            if (snapshot.isEmpty()) {
                Log.d(TAG, "onSuccess: no items");
                toaster.toastShort(getResources().getString(R.string.no_items_in_rental));
            } else {

                loadRentalItems(initRentalItems(snapshot.getDocuments()));
            }
        }).addOnFailureListener(fail -> {
            toaster.toastShort(getResources().getString(R.string.error_purchase_cart_items));
            Log.d(TAG, fail.getMessage());
        });
    }


    private void setUpLiveListener() {
        firestore.collection(Constants.FirestoreCollections.RENTALS)
                .whereEqualTo("uid", auth.getCurrentUser().getUid())
                .addSnapshotListener((snapshot, e) -> {
            if(snapshot != null && snapshot.size() > 0) {
                loadRentalItems(initRentalItems(snapshot.getDocuments()));
                Log.d(TAG, "addSnapshotListener: Items found " + snapshot.size());
            } else {
                recViewRental.setAdapter(null);
            }

        });

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
    private void loadRentalItems(List<RentalItem> rentalItems) {
        recViewRental.setAdapter(new ItemAdapter<>(rentalItems,
                itemClickListener,
                rentalBinder,
                R.layout.rental_item,
                getActivity()));
    }
    @Override
    public void onRightButtonToggle(boolean isActive) {
        super.onRightButtonToggle(isActive);
        toaster.toastShort(getResources().getString(R.string.loading_items));
        reloadItems();

    }

    private void setTrackerSwipe() {
        ItemSwipe.ItemHelperListener leftSwipe = (viewHolder, direction, position) -> {
            Log.d(TAG, "leftSwipe position: " + position);

            ItemAdapter<RentalItem> itemAdapter = (ItemAdapter<RentalItem>)recViewRental.getAdapter();
            if(itemAdapter != null) {
                toaster.toastShort(getResources().getString(R.string.must_return));
                itemAdapter.notifyItemChanged(position);
            }
        };


        ItemTouchHelper.SimpleCallback touchHelper = new ItemSwipe(0, ItemTouchHelper.LEFT,leftSwipe);

        new ItemTouchHelper(touchHelper).attachToRecyclerView(recViewRental);
    }

    @Override
    public void onTextChanged(String searchText) {

    }
}