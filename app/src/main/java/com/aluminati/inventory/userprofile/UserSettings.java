package com.aluminati.inventory.userprofile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.utils.Utils;
import com.aluminati.inventory.fragments.DeleteUser;
import com.aluminati.inventory.fragments.languageSelect.LanguageSelection;
import com.aluminati.inventory.fragments.ui.currencyConverter.ui.currencyChange.CurrencyChange;
import com.aluminati.inventory.login.authentication.password.PassWordReset;
import com.aluminati.inventory.login.authentication.verification.VerificationStatus;

public class UserSettings extends Fragment implements View.OnClickListener {

    private Button deleteButton;
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.user_settings, container, false);
        deleteButton = view.findViewById(R.id.delete_user);
        deleteButton.setOnClickListener(this);
        view.findViewById(R.id.change_password).setOnClickListener(this);
        view.findViewById(R.id.change_language).setOnClickListener(this);
        view.findViewById(R.id.change_currency).setOnClickListener(this);
        view.findViewById(R.id.verify_identity).setOnClickListener(this);

        textView = view.findViewById(R.id.current_currency);

        setCurrentCurrency();
        return view;
    }

    private void deleteUser() {
        DeleteUser deleteUser = DeleteUser.newInstance("Delete User");
        deleteUser.show(getParentFragmentManager(), "delete_user_frag");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VerificationStatus.PASSWORD_RESET) {
            if (resultCode == Activity.RESULT_OK) {
                Utils.makeSnackBar("Password Updated", deleteButton, getActivity());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Utils.makeSnackBar("Cancel Password Reset", deleteButton, getActivity());
            }
        }
    }

    private void setCurrentCurrency(){
        SharedPreferences ed = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ed.registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> {
            Log.i("UserSettings",s);
            if(s.equals("currency")){
                textView.setText(sharedPreferences.getString(s, null));
            }
        });

        if(ed.contains("currency")){
            textView.setText(ed.getString("currency", null));
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.delete_user:{
                deleteUser();
                break;
            }case R.id.change_password:{
                    startActivityForResult(new Intent(getContext(), PassWordReset.class), VerificationStatus.PASSWORD_RESET);
                    break;
            }case R.id.change_language:{
                LanguageSelection languageSelection = LanguageSelection.newInstance("Select Language");
                languageSelection.show(getParentFragmentManager(), "language_select_frag");
                break;
            }
            case R.id.change_currency:{
                CurrencyChange currencyChange = CurrencyChange.newInstance("Select Currency");
                currencyChange.show(getParentFragmentManager(), "currency_select_frag");
                break;
            }
            case R.id.verify_identity:{
                Toast.makeText(getContext(), "To be implemented in final version", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }
}
