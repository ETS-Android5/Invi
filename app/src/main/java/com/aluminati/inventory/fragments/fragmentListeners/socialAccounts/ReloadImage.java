package com.aluminati.inventory.fragments.fragmentListeners.socialAccounts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.Serializable;

public interface ReloadImage<T extends Fragment> extends Serializable {
    void reloaded(boolean result);
}
