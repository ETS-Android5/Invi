package com.aluminati.inventory.payments.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

    public interface cardDetails extends Serializable{
        public void cardDeatilsString(String card_dets);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.add_card, container, false);

        Log.i("Heloo", "Dedledplde");

        cardNumber = view.findViewById(R.id.card_number);
        cardExpiryDate = view.findViewById(R.id.card_expiry_date);
        cardImage = view.findViewById(R.id.card_logo);
        view.findViewById(R.id.camera_icon).setOnClickListener(result -> {

            Bundle bundle = new Bundle();
                   bundle.putString("card_details", "");
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, PaymentsFrag.class, bundle,"payments_frag")
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

                if(getArguments() != null){
                    String dec = getArguments().getString("dec_string");
                    ArrayList<Payment> payments = Payment.stringToList(dec);

                    if(contains(payments)){
                        Snackbar.make(cardNumber, "Card All ready Linked", BaseTransientBottomBar.LENGTH_INDEFINITE).show();
                    }else {
                        payments.add(new Payment(cardNumber.getText().toString()
                                , cardName, cardExpiryDate.getText().toString()));
                        encryptPayments(payments, FirebaseAuth.getInstance().getCurrentUser());
                    }
                }else {
                    ArrayList<Payment> payments = new ArrayList<>();
                    payments.add(new Payment(cardNumber.getText().toString()
                            , cardName, cardExpiryDate.getText().toString()));
                    encryptPayments(payments, FirebaseAuth.getInstance().getCurrentUser());
                }


            }
        });




        return view;
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

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new Payments(),"payments")
                .commit();
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
            editText.setText("Please fill in cardNumber");
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

    private void bindActivity(AppCompatActivity appCompatActivity){
        if(appCompatActivity instanceof HomeActivity){
            ((HomeActivity)appCompatActivity).setCardDetails(this::fillFiled);
        }

    }
}
