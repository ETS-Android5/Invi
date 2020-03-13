package com.aluminati.inventory.fragments.scanner;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.Constants;
import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.PriceCheck;
import com.aluminati.inventory.fragments.tesco.Product;
import com.aluminati.inventory.fragments.tesco.ProductReady;
import com.aluminati.inventory.fragments.tesco.TescoApi;
import com.aluminati.inventory.helpers.DbHelper;
import com.aluminati.inventory.helpers.DialogHelper;
import com.aluminati.inventory.fragments.purchase.PurchaseItem;
import com.aluminati.inventory.fragments.rental.RentalItem;
import com.aluminati.inventory.utils.Toaster;
import com.aluminati.inventory.widgets.ToggleButton;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

public class ScannerFragment extends Fragment implements ProductReady {
    private static final String TAG = ScannerFragment.class.getSimpleName();
    private CodeScanner mCodeScanner;
    private FirebaseFirestore db;
    private DbHelper dbHelper;
    private DialogHelper dialogHelper;

    private Toaster toaster;
    private Switch switchPriceCheck;
    private ToggleButton btnSound;
    private ProductReady productReady;

    //Update
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_scanner, container, false);
        dbHelper = DbHelper.getInstance();
        dialogHelper = DialogHelper.getInstance(getActivity());
        toaster = Toaster.getInstance(getActivity());
        switchPriceCheck = root.findViewById(R.id.price_check_switch);
        btnSound = root.findViewById(R.id.toggleQRSound);




        try {

            CodeScannerView scannerView = root.findViewById(R.id.scanner_view);

                            mCodeScanner = new CodeScanner(getContext(), scannerView);
                            mCodeScanner.setFormats(CodeScanner.ALL_FORMATS);

                            mCodeScanner.setDecodeCallback(result ->
                                    getActivity().runOnUiThread(() -> {
                                        if(!btnSound.isToggled()) {
                                            MediaPlayer.create(getActivity(), R.raw.scan).start();
                                        }

                                        if(switchPriceCheck.isChecked()){
                                            PriceCheck priceCheck = PriceCheck.newInstance("PriceCheck");
                                            priceCheck.show(getActivity().getSupportFragmentManager(),
                                                    "price_check_frag");
                                        }else{
                                            parseScanResult(result.getText());
                                        }

                //TODO: remove after debug -

            /*
            //TODO: remove after debug
             Test barcode QR code
             https://idev.ie/demo/

             {"sid":"Wcxb1fSbI0uz2RRaIaTl","iid":"tg4AccyV3uulNiVAa8jT","idx":"3"} //rental item
             {"sid":"Wcxb1fSbI0uz2RRaIaTl","iid":"yMjwSXjkyPLhOIjay8DI"}//purchase item item

                */

                Log.i(ScannerFragment.class.getName(), "Result --> " + result.getText());
            }));

            scannerView.setOnClickListener(view -> mCodeScanner.startPreview());

        } catch (Exception ex) {
            toaster.toastLong("Error opening camera. Make sure permission is set");
            Log.e(ScannerFragment.class.getSimpleName(), ex.toString());

        }

        return root;
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
                   toaster.toastShort("Un know barcode format");
                   Log.e(TAG, "Un know barcode format: " +  ex.getMessage());
               }
           }

        } /*else if(Pattern.compile("[0-9]+").matcher(result).matches()){
            toaster.toastShort(result);
        }*/
        else{
                TescoApi tescoApi = new TescoApi(result);
                         tescoApi.getProduct();
                         tescoApi.setProductReady(this::getProduct);

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

                    dialog.setPositiveButton("Add", (dialogInterface, i) -> {
                        scanResult.put("uid", uid);
                        scanResult.put("addDate", Calendar.getInstance().getTime());
                        //addToCart(scanResult, uid);
                        addToCart(item, uid);


                        dialogInterface.dismiss();})
                            .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

                    dialog.show();
                });
    }

    private void addToCart(PurchaseItem item, String uid) {
        dbHelper.addItem(String.format(Constants.FirestoreCollections.LIVE_USER_CART, uid), item)
                .addOnSuccessListener(setResult ->{
                    //TODO: Item is added
                    toaster.toastShort("Item added to cart");
                })
                .addOnFailureListener(setFail ->{
                    toaster.toastShort("Add to cart failed");
                });
    }

    private void isRented(Map<String, Object> scanResult) {
        String docId = String.format("%s_%s", scanResult.get("iid"), scanResult.get("idx"));

        dbHelper.getItem(Constants.FirestoreCollections.RENTALS,docId)
                .addOnSuccessListener( res -> {
                   // String msg = res.exists() ? "Sorry this item is already rented" :"You can rent this item";

                    final String uid = res.getString("uid");

                    dbHelper.getItem(Constants.FirestoreCollections.STORE_ITEMS,
                            scanResult.get("iid").toString())
                            .addOnSuccessListener(task -> {

                                RentalItem item = task.toObject(RentalItem.class);

                                toaster.toastShort(item.getTitle());

                                int color = res.exists() ? Color.RED : Color.GREEN;

                                AlertDialog.Builder dialog = dialogHelper
                                        .createDialog(dialogHelper.buildRentalView(item.getTitle(), "", item.getImgLink(), color));
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
                                                    //Print receipt here
                                                    dbHelper.deleteItem(Constants.FirestoreCollections.RENTALS, docId)
                                                            .addOnSuccessListener( deleted -> {
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
                    //TODO: something happened here
                });

    }



    private void rentItem(Map<String, Object> scanResult) {
        String docId = String.format("%s_%s", scanResult.get("iid"), scanResult.get("idx"));

        dbHelper.setItem(Constants.FirestoreCollections.RENTALS,docId, scanResult)
                .addOnSuccessListener(setResult ->{
                    //TODO: Item is added
                    toaster.toastShort("Item added");
                })
                .addOnFailureListener(setFail ->{});
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

            View pV = dialogHelper.buildPurchaseView(product.getName(),
                    String.format("Product Price in GBP (%.2f)\nLocal Currency ( %.2f) ",
                            Float.parseFloat(product.getPrice())
                            , (Float.parseFloat(product.getPrice()) + gbp)),
                    product.getImage(),
                    (int)(Math.random() * 25) + 1,
                    result -> { //What quantity user selected
                     },
                    Color.GREEN );

            dialogHelper.createDialog(pV).show();
// TODO: remove this once we know standard image loading works
//                Glide.with(this)
//                        .asBitmap()
//                        .load(new GlideUrl(product.getImage(), new LazyHeaders.Builder()
//                                .addHeader("Ocp-Apim-Subscription-Key", "cbc1fdf45b5a454cae665a1d34a8a094")
//                                .build()))
//                        .into((ImageView)pV.findViewById(R.id.dialogImageHolder));


        }else{
            dialogHelper.createDialog("Error",
                    "Sorry cannot find this product",
                    null, null).show();
        }
    }



}