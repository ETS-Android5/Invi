package com.aluminati.inventory.ui.gallery;

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
import com.aluminati.inventory.fragments.FloatingTitlebarFragment;
import com.aluminati.inventory.R;
import com.aluminati.inventory.adapters.ItemAdapter;
import com.aluminati.inventory.adapters.swipelisteners.ItemSwipe;
import com.aluminati.inventory.model.PurchaseItem;
import com.aluminati.inventory.utils.Toaster;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PurchaseFragment extends FloatingTitlebarFragment {

    private static final String TAG = PurchaseFragment.class.getSimpleName();
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecyclerView recViewPurchase;
    private ItemAdapter itemAdapter;
    private ItemAdapter.OnItemClickListener itemClickListener;
    private Toaster toaster;

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

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        super.setView(root);

        floatingTitlebar.setLeftToggleOff(true);//dont change icon on toggle

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        toaster = Toaster.getInstance(getActivity());

        recViewPurchase = root.findViewById(R.id.recViewPurchase);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        recViewPurchase.setLayoutManager(layoutManager);

        itemClickListener = item -> {
            toaster.toastShort("You clicked" + item.getTitle());
        };

        firestore.collection(Constants.FirestoreCollections.STORE_ITEMS)
                .whereGreaterThanOrEqualTo("quantity", 0)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "onSuccess: no items");
                    } else {
                        List<PurchaseItem> purchaseItems = queryDocumentSnapshots.toObjects(PurchaseItem.class);
                        recViewPurchase.setAdapter(new ItemAdapter(purchaseItems, itemClickListener, getActivity()));
                    }
                }).addOnFailureListener(fail -> {
            Log.d(TAG, fail.getMessage());
        });

        setTrackerSwipe();

        return root;
    }

    private void setTrackerSwipe() {
        ItemTouchHelper.SimpleCallback touchHelper = new ItemSwipe(0,
                ItemTouchHelper.LEFT, (viewHolder, direction, position) -> {
                    //TODO: Do something on swipe
                    recViewPurchase.getAdapter().notifyItemChanged(position);
                });

        new ItemTouchHelper(touchHelper).attachToRecyclerView(recViewPurchase);
    }

    @Override
    public void onTextChanged(String searchText) {

    }
}