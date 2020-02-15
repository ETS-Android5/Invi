package com.aluminati.inventory.fragments.fragmentListeners.socialAccounts;

import androidx.fragment.app.Fragment;

import java.io.Serializable;

public interface LoginTypeListener<T extends Fragment> extends Serializable {
    void actitvityType(int type);
}
