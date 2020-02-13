package com.aluminati.inventory.fragments.fragmentListeners.email;

import androidx.fragment.app.Fragment;

import java.io.Serializable;

public interface EmailVerificationReciever<T extends Fragment> extends Serializable {
    void onEmailRecieved(String message, int status);
}
