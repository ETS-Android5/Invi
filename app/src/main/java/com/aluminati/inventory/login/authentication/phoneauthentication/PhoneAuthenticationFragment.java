package com.aluminati.inventory.login.authentication.phoneauthentication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationSender;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.hbb20.CountryCodePicker;

public class PhoneAuthenticationFragment extends Fragment {

    private static final String TAG = "PhoneAuthFrag";
    private EditText phoneNumber;
    private CountryCodePicker countryCodePicker;
    private PhoneVerificationSender phoneVerificationSender;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.phone_authentication_fragment, container, false);


        countryCodePicker = view.findViewById(R.id.verify_phone_number_country_picker);
        phoneNumber = view.findViewById(R.id.verify_phone_number);
        countryCodePicker.registerCarrierNumberEditText(phoneNumber);

        bindActivity((PhoneAuthentication)getActivity());

        return view;
    }

    private String getPhoneNumber(){
        String phoneNumber = this.phoneNumber.getText().toString();
        String countryPrefix = this.countryCodePicker.getSelectedCountryCode();
        return phoneNumber.startsWith("0") ? "+".concat(countryPrefix.concat("+" + phoneNumber.substring(1, phoneNumber.length()-1)))
                : "+".concat(countryPrefix.concat(phoneNumber).replace(" ", ""));
    }


    private void bindActivity(AppCompatActivity phoneAuthentication){
        if(phoneAuthentication instanceof PhoneAuthentication) {
            ((PhoneAuthentication)phoneAuthentication).setPhoneVerificationReciever(this::onCodeReceived);
        }
    }


    private void onCodeReceived(int code){
        if (code == 4001) {
            phoneVerificationSender.onPhoneNumberSend(getPhoneNumber());
        }
    }

    public <T extends AppCompatActivity> void setFragmentPhone(PhoneVerificationSender<T> phoneVerificationSender){
        this.phoneVerificationSender = phoneVerificationSender;
    }


}
