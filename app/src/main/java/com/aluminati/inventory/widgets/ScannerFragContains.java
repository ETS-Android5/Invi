package com.aluminati.inventory.widgets;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

public interface ScannerFragContains<T extends AppCompatActivity> extends Serializable {
    public void contiansScannerFrag(boolean contains);
}
