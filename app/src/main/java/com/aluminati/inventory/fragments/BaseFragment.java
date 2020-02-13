package com.aluminati.inventory.fragments;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.fragments.fragmentListeners.email.EmailVerificationSender;
import com.aluminati.inventory.fragments.fragmentListeners.phone.PhoneVerificationSender;
import com.aluminati.inventory.fragments.fragmentListeners.setup.CompleteSetUp;


public class BaseFragment extends Fragment {

    protected EmailVerificationSender emailVerificationSender;
    protected CompleteSetUp completeSetUp;


    public <T extends AppCompatActivity> void setFragmentEmail(EmailVerificationSender<T> emailVerificationSender){
        this.emailVerificationSender = emailVerificationSender;
    }

    public <T extends AppCompatActivity> void setOnCompleteSetUp(CompleteSetUp<T> completeSetUp){
        this.completeSetUp = completeSetUp;
    }
}
