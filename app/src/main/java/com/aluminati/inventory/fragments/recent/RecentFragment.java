package com.aluminati.inventory.fragments.recent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.Constants;
import com.aluminati.inventory.R;
import com.aluminati.inventory.adapters.ItemAdapter;
import com.aluminati.inventory.adapters.swipelisteners.ItemSwipe;
import com.aluminati.inventory.binders.BaseBinder;
import com.aluminati.inventory.fragments.FloatingTitlebarFragment;
import com.aluminati.inventory.fragments.googleMaps.MapsActivity;
import com.aluminati.inventory.fragments.summary.Summary;
import com.aluminati.inventory.fragments.tesco.TescoProductsApi;
import com.aluminati.inventory.fragments.tesco.objects.Product;
import com.aluminati.inventory.fragments.tesco.listeners.ProductsReady;
import com.aluminati.inventory.fragments.tesco.TescoProductsApi;
import com.aluminati.inventory.helpers.DbHelper;
import com.aluminati.inventory.model.BaseItem;
import com.aluminati.inventory.fragments.purchase.PurchaseItem;
import com.aluminati.inventory.userprofile.UserProfile;
import com.aluminati.inventory.utils.Toaster;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecentFragment extends FloatingTitlebarFragment implements ProductsReady {
    private static final String TAG = RecentFragment.class.getSimpleName();
    private FirebaseFirestore firestore;
    private Toaster toaster;
    private DbHelper dbHelper;
    private ItemAdapter.OnItemClickListener itemClickListener;
    private BaseBinder baseBinder;
    private RecyclerView recViewRecent;
    private TescoProductsApi tescoApi;
    private Summary summary;

//Update
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


        setSwipeListeners();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.tescoApi = new TescoProductsApi();
        this.tescoApi.setProductsReady(this);

        summary = new Summary();

        getParentFragmentManager().beginTransaction()
                .add(R.id.nav_host_fragment, summary)
                .commit();

    }

    @Override
    public void onTextChanged(String searchText) {
        /*
            Firebase doesn't have search by case sensitivity. Another flaw in this
            framework. Downloading every item to search isn't practical so we have
            to make tags lower case.
         */

        if(searchText.isEmpty()){
            if(!getParentFragmentManager().getFragments().contains(summary)){
                getParentFragmentManager().beginTransaction()
                        .add(R.id.nav_host_fragment, summary)
                        .commit();
            }

            recViewRecent.setAdapter(null);
        }else {
            getParentFragmentManager().beginTransaction().remove(summary).commit();
        }


        dbHelper.getCollection(Constants.FirestoreCollections.STORE_ITEMS)
                .whereArrayContains("tags", searchText.toLowerCase().trim())
                .orderBy("title")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        Log.d(TAG, "onSuccess -> onTextChanged: no items");
                        //recViewRecent.setAdapter(null);
                        if(!searchText.isEmpty()) {
                            tescoApi.getProducts(searchText);
                        } else {
                            recViewRecent.setAdapter(null);
                        }

                    } else {
                        loadPurchaseItems(initItems(snapshot.getDocuments()));
                    }
                }).addOnFailureListener(fail -> {
                toaster.toastShort(getResources().getString(R.string.error_getting_searchitems));
            Log.d(TAG, fail.getMessage());
        });
    }

    private void setSwipeListeners() {
        ItemSwipe.ItemHelperListener swipe = (viewHolder, direction, position) -> {
            Log.d(TAG, "Swipe position: " + position);
            ItemAdapter<BaseItem> itemAdapter = (ItemAdapter<BaseItem>) recViewRecent.getAdapter();
                if (itemAdapter != null) {
                    switch (direction) {
                        case ItemTouchHelper.LEFT:
                            itemAdapter.notifyItemChanged(position);
                            break;
                            case ItemTouchHelper.RIGHT:
                                Bundle bundle = new Bundle();

                                String storeId = itemAdapter.getItem(position).getStoreID();
                                if(!storeId.equals(Constants.FirestoreCollections.TESCO_STORE_ID)) {
                                    bundle.putString("store_id", itemAdapter.getItem(position).getStoreID());
                                }

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.nav_host_fragment, MapsActivity.class, bundle, "maps_frag")
                                        .commit();
                                break;
                    }

                }

        };

        //ItemTouchHelper.LEFT ---> Only add right swipe on search for map
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
                R.layout.base_item,
                getActivity()));
    }

    @Override
    public void onRightButtonToggle(boolean isActive) {
        super.onRightButtonToggle(isActive);
        startActivity(new Intent(getActivity(), UserProfile.class));
    }

    @Override
    public void getProducts(ArrayList<Product> products) {
        recViewRecent.setAdapter(null);
        if(products.size() > 0){
            loadPurchaseItems(toPurchaseItmes(products));
        }
    }

    private List<BaseItem> toPurchaseItmes(ArrayList<Product> products){
        ArrayList<BaseItem> purchaseItems = new ArrayList<>();

        for(Product product : products){

            String desc = product.getDescription();
            String alt = product.getExactMatch() ? "Alternative:" : "";

            PurchaseItem pItem = new PurchaseItem();
            pItem.setTitle(alt+ product.getName());
            pItem.setDescription(product.getDescription());
            pItem.setPrice(Double.parseDouble(product.getPrice()));
            pItem.setStoreID(Constants.FirestoreCollections.TESCO_STORE_ID);
            pItem.setImgLink(product.getImage());
            pItem.setDocID(product.getId());
            pItem.setTags(Arrays.asList(pItem.getTitle(), pItem.getDescription()));
            pItem.setRestricted(false);
            pItem.setStoreCity("Limerick");//This is only proof of concept
            pItem.setStoreCountry("Ireland");//This is only proof of concept
            pItem.setQuantity(1);//always need 1

            purchaseItems.add(pItem);
        }

        return purchaseItems;
    }
}