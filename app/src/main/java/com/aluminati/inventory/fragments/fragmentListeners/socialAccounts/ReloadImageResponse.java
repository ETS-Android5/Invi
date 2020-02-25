package com.aluminati.inventory.fragments.fragmentListeners.socialAccounts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.Serializable;

public interface ReloadImageResponse<T extends AppCompatActivity> extends Serializable {
    void reload(int type);
}
