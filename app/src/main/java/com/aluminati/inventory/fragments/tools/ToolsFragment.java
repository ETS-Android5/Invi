package com.aluminati.inventory.fragments.tools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.FloatingTitlebarFragment;

public class ToolsFragment extends FloatingTitlebarFragment {

    private ToolsViewModel toolsViewModel;

    public ToolsFragment(DrawerLayout drawer) {
        super(drawer);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_tools, container, false);
        setView(root);
        return root;
    }

    @Override
    public void onTextChanged(String searchText) {

    }

}