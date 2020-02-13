package com.aluminati.inventory.fragments.fragmentListeners.phone;

import androidx.fragment.app.Fragment;

import java.io.Serializable;

public interface PhoneVerificationReciever<T extends Fragment> extends Serializable {
    void onVerificationRecieved(int status);
}
