package com.aluminati.inventory.fragments.fragmentListeners.phone;

import androidx.appcompat.app.AppCompatActivity;
import java.io.Serializable;

public interface PhoneVerificationSender<T extends AppCompatActivity> extends Serializable {
    void onPhoneNumberSend(String message);
}
