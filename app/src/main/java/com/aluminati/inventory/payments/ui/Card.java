package com.aluminati.inventory.payments.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aluminati.inventory.HomeActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.firestore.UserFetch;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESEncryption;
import com.aluminati.inventory.payments.model.Payment;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class Card extends Fragment {

    private static final String TAG = Card.class.getName();
    private EditText cardNumber;
    private EditText cardExpiryDate;
    private ImageView cardImage;
    private String cardName;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.NFC, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int REQUEST_CODE_PERMISSIONS = 101;
    private HomeActivity.nfcCardScan nfcCardScan;
    private FirebaseUser firebaseUser;
    private TextView errorText;



    public interface cardDetails extends Serializable{
        void cardDeatilsString(String card_dets);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.add_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(!allPermissionsGranted()){
            askForPermision();
        }

        errorText = view.findViewById(R.id.error_view);
        cardNumber = view.findViewById(R.id.card_number);
        cardExpiryDate = view.findViewById(R.id.card_expiry_date);
        cardImage = view.findViewById(R.id.card_logo);
        view.findViewById(R.id.camera_icon).setOnClickListener(result -> {

            Bundle bundle = new Bundle();
            bundle.putString("card_details", "");
            getActivity().getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_host_fragment, PaymentsFrag.class, bundle,"payments_frag")
                    .addToBackStack("payments_frag")
                    .commit();
        });

        if(getArguments() != null){
            String cardDetails = getArguments().getString("card_details");
            if(cardDetails != null)fillFiled(cardDetails);
        }

        addTextWatcher();

        if(getActivity() instanceof HomeActivity){
            bindActivity((HomeActivity)getActivity());
        }


        view.findViewById(R.id.clear_fields).setOnClickListener(click -> {
            if(!cardExpiryDate.isEnabled()){
                cardExpiryDate.setEnabled(true);
                cardExpiryDate.setText("");
            }

            if(!cardNumber.isEnabled()){
                cardNumber.setEnabled(true);
                cardNumber.setText("");
            }

            if(errorText.getVisibility() == View.VISIBLE){
                errorText.setVisibility(View.INVISIBLE);
            }
        });

        view.findViewById(R.id.add_card_button).setOnClickListener(click -> {
            if(!verify(cardNumber) && !verify(cardExpiryDate)){
                if(Pattern.compile("[0-9]{1,2}[/][0-9]{1,2}").matcher(cardExpiryDate.getText()).find()) {


                    String uniqeCardRef = unqiueCardRef();
                    ArrayList<Payment> payments;

                    if (getDec(getArguments().containsKey("dec_string"))) {
                        String dec = getArguments().getString("dec_string");
                        payments = Payment.stringToList(dec);

                        if (contains(payments)) {
                            Snackbar.make(cardNumber, getResources().getString(R.string.card_linked), BaseTransientBottomBar.LENGTH_INDEFINITE).show();
                        } else {
                            payments.add(new Payment(cardNumber.getText().toString()
                                    , cardName, cardExpiryDate.getText().toString(), uniqeCardRef));
                            UserFetch.createTransactionsDoc(uniqeCardRef, firebaseUser.getEmail(), initTransactionsMap());
                            encryptPayments(payments, FirebaseAuth.getInstance().getCurrentUser());
                        }


                    } else {
                        payments = new ArrayList<>();
                        payments.add(new Payment(cardNumber.getText().toString()
                                , cardName, cardExpiryDate.getText().toString(), uniqeCardRef));
                        UserFetch.createTransactionsDoc(uniqeCardRef, firebaseUser.getEmail(), initTransactionsMap());

                        encryptPayments(payments, FirebaseAuth.getInstance().getCurrentUser());
                    }
                }else{
                    errorText.setText(getResources().getString(R.string.incorrect_expiry_date));
                }

            }else {
                Log.w(TAG, "Card empty");
            }
        });




    }

    private Map<String, List<Map<String, Object>>> initTransactionsMap(){
        Map<String, List<Map<String, Object>>> transactionMap = new HashMap<>();
        transactionMap.put("transactions", new ArrayList<Map<String, Object>>());
        return transactionMap;
    }

    private boolean getDec(boolean cotains){
        if(cotains){
            return getArguments().getString("dec_string") != null;
        }
        return false;
    }

    public String unqiueCardRef() {
            return UUID.randomUUID().toString();
    }


    private void askForPermision(){
        ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    public boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(getActivity(), REQUIRED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), REQUIRED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), REQUIRED_PERMISSIONS[2]) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(getContext(), getResources().getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show();
            }
        }
    }



    private boolean contains(ArrayList<Payment> payments){
        boolean contains = false;
        for(Payment payment: payments){
            if (payment.getNumber().equals(cardNumber.getText().toString())){
                contains = true;
            }
        }
        return contains;
    }

    public void encryptPayments(List<Payment> payments, FirebaseUser firebaseUser){
        String payment = "";
        for(Payment payment1 : payments){
            payment = payment.concat(payment1.toString().concat("#"));
        }

        new PhoneAESEncryption(payment, (result) -> {
            if(firebaseUser != null){
                UserFetch.update(firebaseUser.getEmail(), "cidi", result);
            }
        });

        new Thread(() -> {
            SystemClock.sleep(1000);
            getActivity().runOnUiThread(() -> {
                getActivity().getSupportFragmentManager().popBackStack("card", FragmentManager.POP_BACK_STACK_INCLUSIVE);

                Intent intent = getActivity().getIntent();
                       intent.putExtra("add_card", "restart");
                getActivity().overridePendingTransition(0, 0);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
                startActivity(intent);


            });
        }).start();


    }

    private boolean verify(EditText editText){
        boolean isEmpty = false;
        if(editText.getText().toString().isEmpty()){
            isEmpty = true;
            errorText.setText(getResources().getString(R.string.fill_in_fields));
            errorText.setVisibility(View.VISIBLE);
        }

        return isEmpty;
    }

    private void addTextWatcher(){
        cardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 2){
                    fillCardFilter(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 16){
                    cardNumber.setEnabled(false);
                    formatString(editable.toString());
                }
            }
        });

        cardExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 5){
                    cardExpiryDate.setEnabled(false);
                }
            }
        });
    }

    private void fillCardFilter(String text){
        if(text.startsWith("4")){
            cardName = "visa";
            cardImage.setImageDrawable(getResources().getDrawable(R.drawable.visa));
        }else if(text.startsWith("5")){
            cardName = "mastercard";
            cardImage.setImageDrawable(getResources().getDrawable(R.drawable.master_card));
        }else{
            cardName = "default";
            cardImage.setImageDrawable(getResources().getDrawable(R.drawable.logo_invi));
        }
    }

    private void fillFiled(String resultFromAnalysis){
        String[] split = resultFromAnalysis.split("#");

        for(String splits : split){
            if(Pattern.compile("[0-9]{1,2}[/][0-9]{1,2}").matcher(splits).find()){
                cardExpiryDate.setText(splits);
            }

            if(Pattern.compile("[0-9]{16}").matcher(splits).find()){
                cardNumber.setText(splits);
            }

            if(splits.toLowerCase().contains("master")){
                cardName = "mastercard";
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.master_card));
            }else if(splits.toLowerCase().contains("visa")){
                cardName = "visa";
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.visa));
            }else{
                cardName = "default";
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.logo_invi));
            }
        }

        if(cardNumber.getText().toString().isEmpty()){
            cardNumber.setText("Failed to get card number Enter manually");
        }

        if(cardExpiryDate.getText().toString().isEmpty()){
            cardNumber.setText("Failed to get expiry date Enter manually");
        }
    }


    private void formatString(String string){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < string.length(); i++){
            if(i%4 == 0){
                stringBuilder.append(" ".concat(Character.toString(string.charAt(i))));
            }else stringBuilder.append(string.charAt(i));
        }
        cardNumber.setText(stringBuilder.toString());
    }

    private void scannedNfc(boolean scanned){
        if(scanned){
            nfcCardScan.cardScanned(cardNumber.getText().toString().isEmpty());
        }
    }

    private void bindActivity(AppCompatActivity appCompatActivity){
        if(appCompatActivity instanceof HomeActivity){
            ((HomeActivity)appCompatActivity).setCardDetails(this::fillFiled);
            ((HomeActivity)appCompatActivity).setScanNfc(this::scannedNfc);
        }
    }

    public void setNfcCardScan(HomeActivity.nfcCardScan nfcCardScan){
        this.nfcCardScan = nfcCardScan;
    }

    public interface scanNfc<T extends Fragment> extends Serializable{
        void nfcScan(boolean result);
    }


}
