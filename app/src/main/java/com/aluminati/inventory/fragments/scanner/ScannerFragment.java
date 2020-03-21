package com.aluminati.inventory.fragments.scanner;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.Constants;
import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.fragments.tesco.TescoProductsApi;
import com.aluminati.inventory.fragments.tesco.objects.Product;
import com.aluminati.inventory.fragments.tesco.listeners.ProductReady;
import com.aluminati.inventory.firestore.DbHelper;
import com.aluminati.inventory.helpers.DialogHelper;
import com.aluminati.inventory.fragments.purchase.PurchaseItem;
import com.aluminati.inventory.fragments.rental.RentalItem;
import com.aluminati.inventory.utils.TextLoader;
import com.aluminati.inventory.utils.Toaster;
import com.aluminati.inventory.widgets.ToggleButton;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ScannerFragment extends Fragment implements ProductReady {
    private static final String TAG = ScannerFragment.class.getSimpleName();
    private CodeScanner mCodeScanner;
    private FirebaseFirestore db;
    private DbHelper dbHelper;
    private DialogHelper dialogHelper;
    private Toaster toaster;
    private ToggleButton btnSound;
    private ProductReady productReady;
    private TextView progressBar;

    //Update
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = DbHelper.getInstance();
        dialogHelper = DialogHelper.getInstance(getActivity());
        toaster = Toaster.getInstance(getActivity());
        btnSound = view.findViewById(R.id.toggleQRSound);
        progressBar = view.findViewById(R.id.scanner_progress_loader);

        TextLoader textLoader = new TextLoader();
        textLoader.setForeground(progressBar, getResources().getString(R.string.loading));

        try {

            CodeScannerView scannerView = view.findViewById(R.id.scanner_view);

            mCodeScanner = new CodeScanner(getContext(), scannerView);
            mCodeScanner.setFormats(CodeScanner.ALL_FORMATS);

            mCodeScanner.setDecodeCallback(result ->
                    getActivity().runOnUiThread(() -> {
                        if(!btnSound.isToggled()) {
                            MediaPlayer.create(getActivity(), R.raw.scan).start();
                        }


                        parseScanResult(result.getText());



                        Log.i(ScannerFragment.class.getName(), "Result --> " + result.getText());
                    }));

            scannerView.setOnClickListener(click -> mCodeScanner.startPreview());

        } catch (Exception ex) {
            toaster.toastLong("Error opening camera. Make sure permission is set");
            Log.e(ScannerFragment.class.getSimpleName(), ex.toString());

        }
    }

    private void parseScanResult(String result){
        if(result.contains("sid") && result.contains("iid")){ //basic check
            Gson gson = new GsonBuilder().create();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            boolean isVerified = user != null && user.isEmailVerified();
            if(isVerified) {
               try {
                   Map<String, Object> scanResult = gson.fromJson(result, Map.class);
                   boolean isPurchase = scanResult.containsKey("sid") && scanResult.containsKey("iid");
                   int size = scanResult.size();

                       switch (size) {

                           case 2:
                               if (isPurchase) {
                                   //valid purchase item
                                   addToCartDialog(scanResult);
                               }
                               break;
                           case 3:
                               if (isPurchase && scanResult.containsKey("idx")) {
                                   //valid rental item
                                   scanResult.put("uid", user.getUid());
                                   isRented(scanResult);
                               }
                               break;
                       }

               } catch (JsonSyntaxException ex) {
                   toaster.toastShort("Unknown barcode format");
                   Log.e(TAG, "Unknown barcode format: " +  ex.getMessage());
               }
           }

        } /*else if(Pattern.compile("[0-9]+").matcher(result).matches()){
            toaster.toastShort(result);
        }*/
        else{

                TescoProductsApi tescoApi = new TescoProductsApi(result);
                         tescoApi.getProduct();
                         tescoApi.setProductReady(this);
                         progressBar.setVisibility(View.VISIBLE);

        }
    }


    private void addToCartDialog(Map<String, Object> scanResult) {
        final String iid = scanResult.get("iid").toString();
        final String uid = FirebaseAuth.getInstance().getUid();

        //Check that his item is valid and in stock
        dbHelper.getItem(Constants.FirestoreCollections.STORE_ITEMS, iid)
                .addOnSuccessListener( task -> {
                    //TODO: Do some error checking here

                    PurchaseItem item = task.toObject(PurchaseItem.class);

                    final View pView = dialogHelper.buildPurchaseView(item.getTitle(),
                            "", item.getImgLink(), item.getQuantity(),
                            result -> item.setQuantity(result),
                            Color.GREEN);

                    AlertDialog.Builder dialog = dialogHelper.createDialog(pView);

                    dialog.setMessage(getResources().getString(R.string.add_to_cart));

                    dialog.setPositiveButton(getResources().getString(R.string.add_to_cart),
                            (dialogInterface, i) -> {
                        scanResult.put("uid", uid);
                        scanResult.put("addDate", Calendar.getInstance().getTime());
                        addToCart(item, uid);


                        dialogInterface.dismiss();})
                            .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

                    dialog.show();
                });
    }

    private void addToCart(PurchaseItem item, String uid) {

        dbHelper.addItem(String.format(Constants.FirestoreCollections.LIVE_USER_CART, uid), item)
                .addOnSuccessListener(setResult ->{
                    toaster.toastShort("Item added to cart");
                })
                .addOnFailureListener(setFail ->{
                    toaster.toastShort("Add to cart failed");
                });
    }

    private void isRented(Map<String, Object> scanResult) {
        String docId = String.format("%s_%s", scanResult.get("iid"), scanResult.get("idx"));

        dbHelper.getItem(String.format(Constants.FirestoreCollections.RENTALS,
                FirebaseAuth.getInstance().getUid()),docId)
                .addOnSuccessListener( res -> {

                    final String uid = res.getString("uid");

                    dbHelper.getItem(Constants.FirestoreCollections.STORE_ITEMS,
                            scanResult.get("iid").toString())
                            .addOnSuccessListener(task -> {

                                RentalItem item = task.toObject(RentalItem.class);

                                int color = res.exists() ? Color.RED : Color.GREEN;

                                AlertDialog.Builder dialog = dialogHelper
                                        .createDialog(dialogHelper.buildRentalView(item.getTitle(),
                                                "", item.getImgLink(), color));
                                dialog.setMessage("This item is all ready rented");

                                if(!res.exists()) { //if item isn't in rentals we can rent it
                                    dialog.setMessage("You can rent this item");
                                    dialog.setPositiveButton("Rent Item",
                                            (dialogInterface, i) -> {
                                                scanResult.put("checkedOutDate", Calendar.getInstance().getTime());
                                                scanResult.putAll(item.toMap());
                                                rentItem(scanResult);
                                                dialogInterface.dismiss();
                                            });
                                } else {
                                    //The item is in rentals - check that we own it
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if(uid != null && user != null) {
                                        if(uid.equals(user.getUid())) {
                                            color = Color.BLUE;
                                            dialog.setMessage("Do you want to check in this item");
                                        }

                                        dialog.setPositiveButton("Return Item",
                                                (dialogInterface, i) -> {
                                                    //move item we are checking in to archive

                                                    dbHelper.addItem(
                                                            String.format(Constants.FirestoreCollections.RENTALS_RETURNED, uid)
                                                            , res.toObject(RentalItem.class)).addOnSuccessListener( result -> {

                                                        final long ts = System.currentTimeMillis();
                                                        Map<String, Object> order = new HashMap<>();
                                                        order.put("timestamp", ""+ts);
                                                        order.put("quantity", 1);
                                                        order.put("itemref", String.format(Constants.FirestoreCollections.RENTALS_RETURNED, uid) + "/" + result.getId());
                                                        order.put("total", Utils.getRentalCharge(res.toObject(RentalItem.class)));
                                                        order.put("rental", true);

                                                        //create a receipt with new archived item
                                                        dbHelper.addItem(String.format(Constants.FirestoreCollections.RECEIPTS_TEST,
                                                                uid), order)
                                                                .addOnSuccessListener(returned -> {Log.d(TAG, "receipt created: " + returned.getId());});
                                                    });

                                                    //Delete original item from users rental list
                                                    dbHelper.deleteItem(
                                                            String.format(Constants.FirestoreCollections.RENTALS, uid)
                                                            , docId).addOnSuccessListener( deleted -> {
                                                                toaster.toastLong("You have now checked in " + item.getTitle());
                                                            });
                                                    dialogInterface.dismiss();
                                                });
                                    }
                                }

                                dialog.show();

                            });
                })
                .addOnFailureListener(ex -> {
                    Log.e(TAG, ex.getMessage());
                });

    }


    private void rentItem(Map<String, Object> scanResult) {
        String docId = String.format("%s_%s", scanResult.get("iid"), scanResult.get("idx"));

        dbHelper.setItem(String.format(Constants.FirestoreCollections.RENTALS,
                FirebaseAuth.getInstance().getUid()),docId, scanResult)
                .addOnSuccessListener(setResult ->{
                    toaster.toastShort("Item added");
                })
                .addOnFailureListener(setFail ->{toaster.toastShort("Failed to add item");});
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCodeScanner != null) {
            mCodeScanner.startPreview();
        }


    }

    @Override
    public void onPause() {
        if (mCodeScanner != null) {
            mCodeScanner.stopPreview();
            mCodeScanner.releaseResources();
        }

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mCodeScanner != null) {
            mCodeScanner.stopPreview();
            mCodeScanner.releaseResources();
        }
    }

    @Override
    public void getProduct(Product product) {
        if(product != null && product.getImage() != null && product.getPrice() != null){
            Log.d(TAG, product.toString());
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            float gbp = Float.parseFloat(preferences.getString("EUR", "0"));//Default is Euro

            String desc = product.getDescription();
            String alt = product.getExactMatch() ? "Alternative:" : "";

            PurchaseItem pItem = new PurchaseItem();
            pItem.setTitle( alt+ product.getName());
            pItem.setDescription(product.getDescription());
            pItem.setPrice(Double.parseDouble(product.getPrice()));
            pItem.setStoreID(Constants.FirestoreCollections.TESCO_STORE_ID);
            pItem.setImgLink(product.getImage());
            pItem.setDocID(product.getId());
            pItem.setDep(product.getDep());
            pItem.setTags(Arrays.asList(pItem.getTitle(), pItem.getDescription()));
            pItem.setRestricted(false);
            pItem.setStoreCity("Limerick");//This is only proof of concept
            pItem.setStoreCountry("Ireland");//This is only proof of concept
            pItem.setQuantity(1);//always need 1
            pItem.setTitle(product.getName());
            View pV = dialogHelper.buildPurchaseView(product.getName(),
                    String.format("%s\n\nProduct Price in GBP (%.2f)\nLocal Currency ( %.2f) ",
                            desc == null ? "" : desc,
                            Float.parseFloat(product.getPrice())
                            , (Float.parseFloat(product.getPrice()) + gbp)),
                    product.getImage(),
                    (int)(Math.random() * 25) + 1, //Proof of concept
                    result -> { //What quantity user selected
                        pItem.setQuantity(result);
                     },
                    Color.GREEN);

            dialogHelper.createDialog(pV).setPositiveButton(getResources().getString(R.string.add_to_cart),
                    (dialogInterface, i) -> {
                        //
                        addToCart(pItem, FirebaseAuth.getInstance().getUid());
                    }).show();

            progressBar.setVisibility(View.INVISIBLE);

        }else{
            dialogHelper.createDialog("Error",
                    "Sorry cannot find this product",
                    null, null).show();

            progressBar.setVisibility(View.INVISIBLE);

        }
    }



}