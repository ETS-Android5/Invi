package com.aluminati.inventory.fragments.fragmentListeners.socialAccounts;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

public interface LoginTypeReciever<T extends AppCompatActivity> extends Serializable {
    void loginResult(boolean result);
}
