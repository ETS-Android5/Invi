package com.aluminati.inventory.userprofile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.login.authentication.LogInActivity;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

public class UserProfileButton extends Fragment implements View.OnClickListener{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.user_profile_buttons, container, false);

        view.findViewById(R.id.account_settings).setOnClickListener(this);
        view.findViewById(R.id.social_platforms).setOnClickListener(this);
        view.findViewById(R.id.account_info).setOnClickListener(this);
        view.findViewById(R.id.log_out).setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.account_info:{
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.user_profile,new UserDetails(), "user_details")
                        .addToBackStack("user_details")
                        .commit();
                break;
            }
            case R.id.account_settings:{
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.user_profile,new UserSettings(), "user_settings")
                        .addToBackStack("user_settings")
                        .commit();
                break;
            }
            case R.id.social_platforms:{
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.user_profile,new SocialPlatforms(), "social_platforms")
                        .addToBackStack("social_platforms")
                        .commit();
                break;
            }
            case R.id.log_out:{
                FirebaseAuth.getInstance().signOut();
                if (LoginManager.getInstance() != null) {
                    LoginManager.getInstance().logOut();
                }
                Intent logout = new Intent(getActivity(),  LogInActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logout);
                getActivity().finish();
                break;
            }
        }
    }
}
