package com.aluminati.inventory.fragments.fragmentListeners.password;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

public interface PassWordListenerSender<T extends AppCompatActivity> extends Serializable  {
    void onPassWordMatchSend(String password, String confirmpassword);

}
