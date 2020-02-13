package com.aluminati.inventory.fragments.fragmentListeners.setup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.Serializable;

public interface CompleteSetUp<T extends AppCompatActivity> extends Serializable {
    void onComplete(int status);
}
