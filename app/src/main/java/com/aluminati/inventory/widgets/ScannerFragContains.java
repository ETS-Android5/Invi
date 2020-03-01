package com.aluminati.inventory.widgets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.Serializable;

public interface ScannerFragContains<T extends AppCompatActivity> extends Serializable {
    public void contiansScannerFrag(Fragment fragment);
}
