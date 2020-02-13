package com.aluminati.inventory.fragments.fragmentListeners.email;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

public interface EmailVerificationSender<T extends AppCompatActivity> extends Serializable {
    void onEmailSend(String messege);
}
