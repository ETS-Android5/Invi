package com.aluminati.inventory.fragments.recent;

import android.content.Intent;
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
import com.aluminati.inventory.binders.BaseBinder;
import com.aluminati.inventory.binders.PurchaseBinder;
import com.aluminati.inventory.fragments.FloatingTitlebarFragment;
import com.aluminati.inventory.fragments.MapsActivity;
import com.aluminati.inventory.fragments.purchase.PurchaseFragment;
import com.aluminati.inventory.helpers.DbHelper;
import com.aluminati.inventory.model.BaseItem;
import com.aluminati.inventory.model.PurchaseItem;
import com.aluminati.inventory.userprofile.UserProfile;
import com.aluminati.inventory.utils.Toaster;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RecentFragment extends FloatingTitlebarFragment {
    private static final String TAG = RecentFragment.class.getSimpleName();
    private FirebaseFirestore firestore;
    private Toaster toaster;
    private DbHelper dbHelper;
    private ItemAdapter.OnItemClickListener itemClickListener;
    private BaseBinder baseBinder;
    private RecyclerView recViewRecent;


    public RecentFragment() {}

    public RecentFragment(DrawerLayout drawer) {
        super(drawer);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_recent, container, false);
        setView(root);//setup floating titlebar

        dbHelper = DbHelper.getInstance();
        baseBinder = new BaseBinder();
        //How to programmatically set icons on floating action bar
        floatingTitlebar.setRightToggleIcons(R.drawable.ic_toggle_list, R.drawable.ic_toggle_grid);
        floatingTitlebar.setToggleActive(true);

        firestore = FirebaseFirestore.getInstance();
        toaster = Toaster.getInstance(getActivity());

        recViewRecent = root.findViewById(R.id.recViewRecent);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);

        recViewRecent.setLayoutManager(layoutManager);
        itemClickListener = (ItemAdapter.OnItemClickListener<BaseItem>) item -> {
            toaster.toastShort("You clicked" + item.getTitle());
        };


        setLeftTrackerSwipe();
        setRightTrackerSwipe();
        return root;
    }

    @Override
    public void onTextChanged(String searchText) {
        dbHelper.getCollection(Constants.FirestoreCollections.STORE_ITEMS)
                .whereArrayContains("tags", searchText)
                .orderBy("title")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        Log.d(TAG, "onSuccess -> onTextChanged: no items");
                        recViewRecent.setAdapter(null);
                    } else {
                        loadPurchaseItems(initItems(snapshot.getDocuments()));
                    }
                }).addOnFailureListener(fail -> {
                toaster.toastShort(getResources().getString(R.string.error_getting_searchitems));
            Log.d(TAG, fail.getMessage());
        });
    }

    private void setLeftTrackerSwipe() {
        ItemSwipe.ItemHelperListener swipe = (viewHolder, direction, position) -> {
            Log.d(TAG, "Swipe position: " + position);
            ItemAdapter<BaseItem> itemAdapter = (ItemAdapter<BaseItem>) recViewRecent.getAdapter();
                if (itemAdapter != null) {
                    itemAdapter.notifyItemChanged(position);
                }

        };


        ItemTouchHelper.SimpleCallback touchHelper = new ItemSwipe(0, ItemTouchHelper.LEFT,swipe);

        new ItemTouchHelper(touchHelper).attachToRecyclerView(recViewRecent);
    }

    private void setRightTrackerSwipe(){
        ItemSwipe.ItemHelperListener swipe = (viewHolder, direction, position) -> {
            Log.d(TAG, "Swipe position: " + position);
            ItemAdapter<BaseItem> itemAdapter = (ItemAdapter<BaseItem>) recViewRecent.getAdapter();
                if (itemAdapter != null) {
                    Bundle bundle = new Bundle();
                           bundle.putString("store_id", itemAdapter.getItem(position).getStoreID());
                           getActivity().getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.nav_host_fragment, MapsActivity.class, bundle,"maps_frag")
                                   .commit();
                }
        };


        ItemTouchHelper.SimpleCallback touchHelper = new ItemSwipe(0, ItemTouchHelper.RIGHT,swipe);


        new ItemTouchHelper(touchHelper).attachToRecyclerView(recViewRecent);
    }

    private List<BaseItem> initItems(List<DocumentSnapshot> snapshots) {
        List<BaseItem> pItems = new ArrayList<>();
        for(DocumentSnapshot obj : snapshots) {
            PurchaseItem p = obj.toObject(PurchaseItem.class);
            p.setDocID(obj.getId());
            pItems.add(p);
        }

        return pItems;
    }

    private void loadPurchaseItems(List<BaseItem> purchaseItems) {
        recViewRecent.setAdapter(new ItemAdapter<>(purchaseItems,
                itemClickListener,
                baseBinder,
                getActivity()));
    }

    @Override
    public void onRightButtonToggle(boolean isActive) {
        super.onRightButtonToggle(isActive);
        startActivity(new Intent(getActivity(), UserProfile.class));
    }
}