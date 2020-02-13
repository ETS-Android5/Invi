package com.aluminati.inventory.fragments.fragmentListeners.password;

import androidx.fragment.app.Fragment;

import java.io.Serializable;

public interface PassWordListenerReciever<T extends Fragment> extends Serializable {
    void askForPassWord(int status);

}
