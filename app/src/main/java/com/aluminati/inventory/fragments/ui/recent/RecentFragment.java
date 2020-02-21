package com.aluminati.inventory.fragments.ui.recent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.FloatingTitlebarFragment;
import com.aluminati.inventory.userprofile.UserProfile;

public class RecentFragment extends FloatingTitlebarFragment {
    public RecentFragment() {}

    public RecentFragment(DrawerLayout drawer) {
        super(drawer);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_recent, container, false);
        setView(root);//setup floating titlebar
        return root;
    }

    @Override
    public void onTextChanged(String searchText) {

    }

    @Override
    public void onRightButtonToggle(boolean isActive) {
        super.onRightButtonToggle(isActive);
        startActivity(new Intent(getActivity(), UserProfile.class));
    }
}