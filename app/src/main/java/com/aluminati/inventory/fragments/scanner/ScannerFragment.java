package com.aluminati.inventory.fragments.scanner;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.Constants;
import com.aluminati.inventory.R;
import com.aluminati.inventory.helpers.DbHelper;
import com.aluminati.inventory.helpers.DialogHelper;
import com.aluminati.inventory.model.RentalItem;
import com.aluminati.inventory.ui.home.HomeFragment;
import com.aluminati.inventory.utils.Toaster;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.Map;

public class ScannerFragment extends Fragment {

    private CodeScanner mCodeScanner;
    private FirebaseFirestore db;
    private DbHelper dbHelper;
    private Toaster toaster;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.scanner_layout, container, false);
        dbHelper = DbHelper.getInstance();
        toaster = Toaster.getInstance(getActivity());


        try {
            CodeScannerView scannerView = root.findViewById(R.id.scanner_view);

            mCodeScanner = new CodeScanner(getContext(), scannerView);

            mCodeScanner.setDecodeCallback(result -> getActivity().runOnUiThread(() -> {
                //TODO: remove after debug
                toaster.toastShort(result.getText());
            /*
            //TODO: remove after debug
             Test barcode QR code
             {"sid":"Wcxb1fSbI0uz2RRaIaTl","iid":"tg4AccyV3uulNiVAa8jT","idx":"3"}
             https://barcode.tec-it.com/en/QRCode?data=%7B%22sid%22%3A%22Wcxb1fSbI0uz2RRaIaTl%22%2C%22iid%22%3A%22tg4AccyV3uulNiVAa8jT%22%2C%22idx%22%3A%223%22%7D
             */
                Gson gson = new GsonBuilder().create();
                //I could cast to ScanItem here but map is better because we can add more fields dynamically
                Map<String, Object> scanResult = gson.fromJson(result.getText(), Map.class);

                //TODO: getCurrentUser().getUid() - wont work till auth is fixed
                scanResult.put("uid", "ng3v4taCYkR2TZ67uwThOCLJDUO2");

                isRented(scanResult);
            }));

            scannerView.setOnClickListener(view -> mCodeScanner.startPreview());

        } catch (Exception ex) {
            toaster.toastLong("Error opening camera. Make sure permission is set");
            Log.e(HomeFragment.class.getSimpleName(), ex.toString());

        }

        return root;
    }

    private void isRented(Map<String, Object> scanResult) {
        String docId = String.format("%s_%s", scanResult.get("iid"), scanResult.get("idx"));
        dbHelper.getItem(Constants.FirestoreCollections.RENTALS,docId)
                .addOnSuccessListener( res -> {
                    String msg = res.exists() ? "Sorry this item is already rented" :"You can rent this item";

                    dbHelper.getItem(Constants.FirestoreCollections.STORE_ITEMS,
                            scanResult.get("iid").toString())
                            .addOnSuccessListener(task -> {
                                RentalItem item = task.toObject(RentalItem.class);
                                toaster.toastShort(item.getTitle());
                                int color = res.exists() ? Color.RED : Color.GREEN;
                                AlertDialog.Builder dialog = DialogHelper.getInstance(getActivity())
                                        .createDialog(item.getTitle(), msg, item.getImgLink(), color);

                                if(!res.exists()) { //if item isn't in rentals we can rent it
                                    dialog.setPositiveButton("Rent Item",
                                            (dialogInterface, i) -> {
                                                scanResult.put("checkedOutDate", Calendar.getInstance().getTime());
                                                rentItem(scanResult);
                                                dialogInterface.dismiss();
                                            });
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


    /* TODO: Remove in production

    example of function call on server side

    exports.rentItem = functions.https.onCall((data, context) => {

        var result = new Object();
        result.isRented = false;
        result.message = "this is a message";

        const db = admin.firestore();

        var docName = data.iid + '_' + data.idx;

        return db.collection('rentals').doc(docName).get()
        .then((docSnapshot) => {
          if (docSnapshot.exists) { //if the doc exits return it

            result.isRented = true;
            result.message = "Sorry this item is already out";
            return result;
          } else { //doc does not exist so return message
            result.isRented = false;
            result.message = "You can rent to item";
            return result;
          }
        });
     */
//    private Task<Map<String, Object>> rentOrReturnItem(Map<String, Object> data) {
//
//        return firebaseFunctions
//                .getHttpsCallable("rentItem")
//                .call(data)
//                .continueWith(task -> (Map<String, Object>) task.getResult().getData());
//    }

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
            mCodeScanner.releaseResources();
        }

        super.onPause();
    }
}