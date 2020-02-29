package com.aluminati.inventory.payments.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.aluminati.inventory.fragments.scanner.ScannerFragment;
import com.aluminati.inventory.login.authentication.encryption.PhoneAESEncryption;
import com.aluminati.inventory.payments.Payment;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.io.CharArrayReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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


    public interface cardDetails extends Serializable{
        public void cardDeatilsString(String card_dets);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.add_card, container, false);

        if(!allPermissionsGranted()){
            askForPermision();
        }


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

        view.findViewById(R.id.add_card_button).setOnClickListener(click -> {
            if(!verify(cardNumber) && !verify(cardExpiryDate)){

                if(getDec(getArguments().containsKey("dec_string"))){
                    Log.w(TAG, "Card empty +++++");
                    String dec = getArguments().getString("dec_string");
                        ArrayList<Payment> payments = Payment.stringToList(dec);

                        if (contains(payments)) {
                            Snackbar.make(cardNumber, "Card All ready Linked", BaseTransientBottomBar.LENGTH_INDEFINITE).show();
                        } else {
                            payments.add(new Payment(cardNumber.getText().toString()
                                    , cardName, cardExpiryDate.getText().toString()));
                            encryptPayments(payments, FirebaseAuth.getInstance().getCurrentUser());
                        }
                }else {
                    ArrayList<Payment> payments = new ArrayList<>();
                    payments.add(new Payment(cardNumber.getText().toString()
                            , cardName, cardExpiryDate.getText().toString()));
                    encryptPayments(payments, FirebaseAuth.getInstance().getCurrentUser());
                    Log.w(TAG, "Card empty ==== ");
                }


            }else {
                Log.w(TAG, "Card empty");
            }
        });




        return view;
    }

    private boolean getDec(boolean cotains){
        if(cotains){
            return getArguments().getString("dec_string") != null;
        }
        return false;
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
                Toast.makeText(getContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
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

    private String appendToDec(String toAppend){
        String number = cardNumber.getText().toString();
        String expiryDate = cardExpiryDate.getText().toString();
        String name = cardName;
        toAppend = toAppend + "Name=".concat(name).concat(";Number=").concat(number)
                .concat(";ExpiryDate=").concat(expiryDate);
        return toAppend;
    }

    private boolean verify(EditText editText){
        boolean isEmpty = false;
        if(editText.getText().toString().isEmpty()){
            isEmpty = true;
            editText.setHint("Please fill in cardNumber");
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
                if(charSequence.length()%4 == 0){
                    cardNumber.append(" ",charSequence.length(), charSequence.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void fillFiled(String resultFromAnalysis){
        String[] split = resultFromAnalysis.split("#");

        for(String splits : split){
            if(Pattern.compile("[0-9]{1,2}[/][0-9]{1,2}").matcher(splits).find()){
                cardExpiryDate.setText(splits);
                Log.i("Heloo", "Dedledplde");
            }

            if(Pattern.compile("[0-9]{16}").matcher(splits).find()){
                cardNumber.setText(splits);
                Log.i("Heloo", "Dedledplde");
            }

            if(splits.toLowerCase().contains("master")){
                cardName = "mastercard";
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.master_card));
            }else if(splits.toLowerCase().contains("visa")){
                cardName = "visa";
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.visa));
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
