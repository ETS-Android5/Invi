package com.aluminati.inventory.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.FloatingTitlebarFragment;

public class HomeFragment extends FloatingTitlebarFragment {

    private HomeViewModel homeViewModel;

    public HomeFragment() {}

    public HomeFragment(DrawerLayout drawer) {
        super(drawer);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        setView(root);//setup floating titlebar
        return root;
    }

    @Override
    public void onTextChanged(String searchText) {

    }


}