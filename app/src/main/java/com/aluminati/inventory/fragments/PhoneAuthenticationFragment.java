package com.aluminati.inventory.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationSender;
import com.aluminati.inventory.login.authentication.phoneauthentication.PhoneAuthentication;
import com.hbb20.CountryCodePicker;

public class PhoneAuthenticationFragment extends BaseFragment {

    private static final String TAG = "PhoneAuthFrag";
    private EditText phoneNumber;
    private CountryCodePicker countryCodePicker;
    protected PhoneVerificationSender phoneVerificationSender;

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


    public void bindActivity(PhoneAuthentication phoneAuthentication){
        phoneAuthentication.setPhoneVerificationReciever((code) -> onCodeReceived(code));
    }


    private void onCodeReceived(int code){
        switch (code){
            case 4001:{
                phoneVerificationSender.onPhoneNumberSend(getPhoneNumber());
                break;
            }
        }
    }

    public <T extends AppCompatActivity> void setFragmentPhone(PhoneVerificationSender<T> phoneVerificationSender){
        this.phoneVerificationSender = phoneVerificationSender;
    }


}
