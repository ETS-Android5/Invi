package com.aluminati.inventory.fragments.scanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.Constants;
import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.PriceCheck;
import com.aluminati.inventory.helpers.DbHelper;
import com.aluminati.inventory.helpers.DialogHelper;
import com.aluminati.inventory.model.PurchaseItem;
import com.aluminati.inventory.model.RentalItem;
import com.aluminati.inventory.fragments.recent.RecentFragment;
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
import com.google.gson.stream.MalformedJsonException;

import java.util.Calendar;
import java.util.Map;
import java.util.regex.Pattern;

public class ScannerFragment extends Fragment {
    private static final String TAG = ScannerFragment.class.getSimpleName();
    private CodeScanner mCodeScanner;
    private FirebaseFirestore db;
    private DbHelper dbHelper;
    private Toaster toaster;
    private Switch switchPriceCheck;
    private ToggleButton btnSound;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_scanner, container, false);
        dbHelper = DbHelper.getInstance();
        toaster = Toaster.getInstance(getActivity());
        switchPriceCheck = root.findViewById(R.id.price_check_switch);
        btnSound = root.findViewById(R.id.toggleQRSound);

        try {

            CodeScannerView scannerView = root.findViewById(R.id.scanner_view);

                            mCodeScanner = new CodeScanner(getContext(), scannerView);
                            mCodeScanner.setDecodeCallback(result ->
                                    getActivity().runOnUiThread(() -> {
                                        if(!btnSound.isToggled()) {
                                            MediaPlayer.create(getActivity(), R.raw.scan).start();
                                        }


                                        if(switchPriceCheck.isChecked()){
                                            PriceCheck priceCheck = PriceCheck.newInstance("PriceCheck");
                                            priceCheck.show(getActivity().getSupportFragmentManager(), "price_check_frag");
                                        }else{
                                            parseScanResult(result.getText());
                                        }

                //TODO: remove after debug -

            /*
            //TODO: remove after debug
             Test barcode QR code
             {"sid":"Wcxb1fSbI0uz2RRaIaTl","iid":"tg4AccyV3uulNiVAa8jT","idx":"3"} //rental item
             {"sid":"Wcxb1fSbI0uz2RRaIaTl","iid":"yMjwSXjkyPLhOIjay8DI"}//purchase item item
             https://barcode.tec-it.com/en/QRCode?data=%7B%22sid%22%3A%22Wcxb1fSbI0uz2RRaIaTl%22%2C%22iid%22%3A%22tg4AccyV3uulNiVAa8jT%22%2C%22idx%22%3A%223%22%7D

                */

                Log.i(ScannerFragment.class.getName(), "Result --> " + result.getText());
            }));

            scannerView.setOnClickListener(view -> mCodeScanner.startPreview());

        } catch (Exception ex) {
            toaster.toastLong("Error opening camera. Make sure permission is set");
            Log.e(RecentFragment.class.getSimpleName(), ex.toString());

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
                   //scanResult.put("uid", user.getUid());

                   switch(size) {
                       case 2:
                           if(isPurchase) {
                               //valid purchase item
                               addToCart(scanResult);
                           }
                           break;
                       case 3:
                           if(isPurchase && scanResult.containsKey("idx")) {
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
            toaster.toastShort("Un know barcode format");
        }
    }

    private void addToCart(Map<String, Object> scanResult) {
        final String iid = scanResult.get("iid").toString();
        final String uid = FirebaseAuth.getInstance().getUid();

        dbHelper.getItem(Constants.FirestoreCollections.STORE_ITEMS, iid)
                .addOnSuccessListener( task -> {
                    //TODO: Do some error checking here
                    PurchaseItem item = task.toObject(PurchaseItem.class);
                    AlertDialog.Builder dialog = DialogHelper.getInstance(getActivity())
                            .createDialog(item.getTitle(), "", item.getImgLink(), Color.GREEN);
                    dialog.setMessage("Do you want to add to cart");
                    dialog.setPositiveButton("Yes", (dialogInterface, i) -> {
                        scanResult.put("uid", uid);
                        scanResult.put("addDate", Calendar.getInstance().getTime());
                        dbHelper.setItem(Constants.FirestoreCollections.LIVE_USER_CART, iid, scanResult)
                                .addOnSuccessListener(setResult ->{
                                    //TODO: Item is added
                                    toaster.toastShort("Item added to cart");
                                })
                                .addOnFailureListener(setFail ->{});

                        dialogInterface.dismiss();})
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                    dialog.show();
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

                                AlertDialog.Builder dialog = DialogHelper.getInstance(getActivity())
                                        .createDialog(item.getTitle(), "", item.getImgLink(), color);
                                dialog.setMessage("This item is all ready rented");

                                if(!res.exists()) { //if item isn't in rentals we can rent it
                                    dialog.setMessage("You can rent this item");
                                    dialog.setPositiveButton("Rent Item",
                                            (dialogInterface, i) -> {
                                                scanResult.put("checkedOutDate", Calendar.getInstance().getTime());
                                                rentItem(scanResult);
                                                dialogInterface.dismiss();
                                            });
                                } else {
                                    //The item is in rentals so is it ours
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
}